package com.axondragonscale.devinator.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import androidx.annotation.RequiresApi
import com.axondragonscale.commons.log
import com.axondragonscale.devinator.model.AdbEndpoint
import com.axondragonscale.devinator.model.EndpointKind
import java.net.Inet4Address
import java.net.NetworkInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Created by Ronak on 11/06/26 */

private const val SERVICE_TYPE = "_adb-tls-connect._tcp"

/** One local IPv4 address, as the platform reports it. */
private data class InterfaceAddress(val name: String, val host: String)

/**
 * Labels each address and ranks it Wi-Fi, then hotspot, then VPN, then everything else.
 *
 * The order is the reachability guess, not a preference: a computer on the same router is the common
 * case, a computer on the phone's own hotspot is the next, and a tunnel address rarely routes from
 * another machine. The screen draws only the first candidate, like Android's own wireless debugging
 * screen.
 *
 * No permission-free API says which interface is tethered, so the Wi-Fi/hotspot split falls back to
 * interface names; [vpnInterfaces] from `ConnectivityManager` stays authoritative for VPN.
 */
private fun classifyEndpoints(
    addresses: List<InterfaceAddress>,
    vpnInterfaces: Set<String>,
    port: Int?,
): List<AdbEndpoint> = addresses
    .map { AdbEndpoint(kindOf(it.name, vpnInterfaces), it.host, port) }
    .distinct()
    .sortedBy { it.kind.ordinal }

private fun kindOf(interfaceName: String, vpnInterfaces: Set<String>): EndpointKind = when {
    interfaceName in vpnInterfaces -> EndpointKind.VPN
    interfaceName.startsWith("tun") || interfaceName.startsWith("ppp") -> EndpointKind.VPN
    interfaceName == "wlan1" || interfaceName.startsWith("ap") ||
        interfaceName.startsWith("swlan") || interfaceName.startsWith("softap") -> EndpointKind.HOTSPOT
    interfaceName.startsWith("wlan") -> EndpointKind.WIFI
    else -> EndpointKind.OTHER
}

/**
 * Local `adb connect` candidates: every non-loopback IPv4 address plus the port mDNS reports for
 * `_adb-tls-connect._tcp`. Best effort throughout — an unknown port degrades to the address alone,
 * and nothing here is allowed to block or fail a toggle.
 */
class WirelessEndpointRepository(context: Context) {

    private val executor = context.mainExecutor
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _endpoints = MutableStateFlow<List<AdbEndpoint>>(emptyList())
    val endpoints: StateFlow<List<AdbEndpoint>> = _endpoints.asStateFlow()

    private var port: Int? = null
    private var running = false
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private val serviceInfoCallbacks = mutableListOf<NsdManager.ServiceInfoCallback>()

    /** Re-reads the addresses and starts discovery. Idempotent; returns without waiting for mDNS. */
    fun start() {
        if (running) return
        running = true
        publish(port)
        val listener = DiscoveryListener()
        try {
            nsdManager?.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener)
            discoveryListener = listener
        } catch (e: Exception) {
            this.log { e }
        }
    }

    fun stop() {
        running = false
        stopDiscovery()
        port = null
        _endpoints.value = emptyList()
    }

    /** A port change means a new session, so the addresses are re-read with it. */
    private fun publish(newPort: Int?) {
        // A callback can arrive after [stop]; only a running resolver publishes.
        if (!running) return
        port = newPort
        _endpoints.value = classifyEndpoints(readAddresses(), vpnInterfaces(), newPort)
    }

    private inner class DiscoveryListener : NsdManager.DiscoveryListener {

        override fun onServiceFound(serviceInfo: NsdServiceInfo) = resolve(serviceInfo)

        override fun onServiceLost(serviceInfo: NsdServiceInfo) = publish(null)

        override fun onDiscoveryStarted(serviceType: String) = Unit

        override fun onDiscoveryStopped(serviceType: String) = Unit

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) = Unit

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit
    }

    // `resolveService` is deprecated on API 34+, where registerServiceInfoCallback replaces it, and
    // is still the only resolver below that.
    @Suppress("DEPRECATION")
    private fun resolve(serviceInfo: NsdServiceInfo) {
        val nsd = nsdManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                registerServiceInfo(nsd, serviceInfo)
            } else {
                nsd.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) = Unit

                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        publish(serviceInfo.port)
                    }
                })
            }
        } catch (e: Exception) {
            this.log { e }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun registerServiceInfo(nsd: NsdManager, serviceInfo: NsdServiceInfo) {
        val callback = object : NsdManager.ServiceInfoCallback {
            override fun onServiceUpdated(serviceInfo: NsdServiceInfo) = publish(serviceInfo.port)

            override fun onServiceLost() = publish(null)

            override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) = Unit

            override fun onServiceInfoCallbackUnregistered() = Unit
        }
        serviceInfoCallbacks += callback
        nsd.registerServiceInfoCallback(serviceInfo, executor, callback)
    }

    private fun stopDiscovery() {
        discoveryListener?.let { listener ->
            discoveryListener = null
            try {
                nsdManager?.stopServiceDiscovery(listener)
            } catch (e: Exception) {
                this.log { e }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) unregisterCallbacks()
        serviceInfoCallbacks.clear()
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun unregisterCallbacks() {
        val nsd = nsdManager ?: return
        serviceInfoCallbacks.forEach { callback ->
            try {
                nsd.unregisterServiceInfoCallback(callback)
            } catch (e: Exception) {
                this.log { e }
            }
        }
    }

    private fun readAddresses(): List<InterfaceAddress> = try {
        NetworkInterface.getNetworkInterfaces().toList()
            .filter { it.isUp && !it.isLoopback }
            .flatMap { networkInterface ->
                networkInterface.inetAddresses.toList()
                    .filterIsInstance<Inet4Address>()
                    .filterNot { it.isLoopbackAddress || it.isLinkLocalAddress }
                    .map { InterfaceAddress(networkInterface.name.orEmpty(), it.hostAddress.orEmpty()) }
            }
    } catch (e: Exception) {
        this.log { e }
        emptyList()
    }

    private fun vpnInterfaces(): Set<String> {
        val manager = connectivityManager ?: return emptySet()
        return try {
            val network = manager.activeNetwork ?: return emptySet()
            val isVpn = manager.getNetworkCapabilities(network)
                ?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
            if (isVpn) setOfNotNull(manager.getLinkProperties(network)?.interfaceName) else emptySet()
        } catch (e: Exception) {
            this.log { e }
            emptySet()
        }
    }
}
