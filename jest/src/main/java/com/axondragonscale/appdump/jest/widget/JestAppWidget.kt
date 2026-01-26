package com.axondragonscale.appdump.jest.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import com.axondragonscale.appdump.jest.repository.JokeRepository
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Ronak Harkhani on 20/04/24
 */
class JestAppWidget : GlanceAppWidget(), KoinComponent {

    companion object {
        val updateCountKey = longPreferencesKey("updateCount")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository: JokeRepository by inject()

        provideContent {
            GlanceTheme {
                currentState<Preferences>() // Widget is not updated unless currentState is called
                val joke = runBlocking { repository.getCurrentJoke() }
                JestWidget(joke)
            }
        }
    }

}
