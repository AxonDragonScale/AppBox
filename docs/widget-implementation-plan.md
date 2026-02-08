# Topty Widget Implementation Plan

## Overview
Two independent widget types: Single Token and Multi Token. Codes hidden by default, revealed on click until expiry, then auto-hidden. Uses Glance's self-update mechanism with in-memory state - no WorkManager needed.

**Note:** App is pre-release, so no database migration needed. Widget State uses existing Token and Code classes to avoid duplication.

---

## 📋 Phase 1: Foundation & Dependencies

### 1.1 Add Dependencies
**File:** `apps/topty/build.gradle.kts`

```kotlin
dependencies {
    // Glance (Compose for Widgets)
    implementation("androidx.glance:glance-appwidget:1.0.0")
    implementation("androidx.glance:glance-material3:1.0.0")
}
```

**Note:** No WorkManager needed!

### 1.2 Database Schema - Widget Table
**File:** `database/entity/Widget.kt`

```kotlin
@Entity(tableName = "widget")
data class Widget(
    @PrimaryKey
    val widgetId: Int,
    val widgetType: WidgetType,      // SINGLE or MULTI
    val tokenIds: String,             // JSON array: "[1,2,3]" for multi, "[1]" for single
    val createdAt: Long = System.currentTimeMillis(),
)

enum class WidgetType {
    SINGLE,
    MULTI
}
```

**Note:** No `codeExpiryTimestamp` field - stored in memory only!

**File:** `database/dao/WidgetDao.kt`

```kotlin
@Dao
interface WidgetDao {
    @Query("SELECT * FROM widget WHERE widgetId = :widgetId")
    suspend fun getWidget(widgetId: Int): Widget?
    
    @Query("SELECT * FROM widget WHERE widgetType = :type")
    suspend fun getWidgetsByType(type: WidgetType): List<Widget>
    
    @Query("SELECT * FROM widget")
    suspend fun getAllWidgets(): List<Widget>
    
    @Upsert
    suspend fun upsert(widget: Widget)
    
    @Query("DELETE FROM widget WHERE widgetId = :widgetId")
    suspend fun delete(widgetId: Int)
    
    @Query("DELETE FROM widget")
    suspend fun deleteAll()
}
```

### 1.3 Database Updates
**File:** `database/ToptyDatabase.kt`

- Add Widget entity to database entities list
- Increment database version to 2
- Add widgetDao() abstract method

**Note:** No migration needed - app is pre-release, uninstall/reinstall is acceptable.

### 1.4 Create Widget Package Structure
```
widget/
├── single/
│   ├── SingleTokenWidget.kt          // Glance widget composable
│   ├── SingleTokenWidgetReceiver.kt  // AppWidgetProvider
│   ├── SingleTokenConfigScreen.kt    // Configuration UI
│   ├── SingleTokenConfigVM.kt        // Configuration ViewModel
│   └── SingleTokenConfigActivity.kt  // Activity wrapper
├── multi/
│   ├── MultiTokenWidget.kt           // Glance widget composable
│   ├── MultiTokenWidgetReceiver.kt   // AppWidgetProvider
│   ├── MultiTokenConfigScreen.kt     // Configuration UI
│   ├── MultiTokenConfigVM.kt         // Configuration ViewModel
│   └── MultiTokenConfigActivity.kt   // Activity wrapper
├── common/
│   ├── WidgetState.kt                // Data classes for widget state
│   ├── WidgetStateProvider.kt        // Generate widget states
│   ├── WidgetMemoryState.kt          // In-memory expiry state
│   ├── WidgetActions.kt              // Click actions
│   ├── WidgetTheme.kt                // Widget Material3 theme
│   └── WidgetUtils.kt                // Utilities (time calc, formatting)
└── di/
    └── WidgetModule.kt               // Koin dependency injection
```

---

## 📋 Phase 2: Data Layer

### 2.1 Widget State Models
**File:** `widget/common/WidgetState.kt`

```kotlin
/**
 * State for single token widget
 * Uses existing Token and Code classes to avoid duplication
 */
data class SingleTokenWidgetState(
    val widgetId: Int,
    val token: Token?,    // Contains all token info (issuer, name, group, etc.)
    val code: Code?,      // Contains code string + timing (TOTPCode has periodStart/periodEnd)
    val error: String? = null,  // Widget-specific error messages
)

/**
 * State for multi token widget
 */
data class MultiTokenWidgetState(
    val widgetId: Int,
    val tokens: List<TokenWidgetItem>,
    val isEmpty: Boolean,
)

/**
 * Individual token item in multi token widget
 */
data class TokenWidgetItem(
    val token: Token,  // Contains all token info
    val code: Code?,   // Contains code + timing
)
```

### 2.2 Widget Memory State
**File:** `widget/common/WidgetMemoryState.kt`

```kotlin
/**
 * In-memory storage for widget code visibility state.
 * This is intentionally not persisted - if process dies, codes are hidden for security.
 */
object WidgetMemoryState {
    private val expiryTimestamps = mutableMapOf<Int, Long>()
    
    /**
     * Set expiry timestamp for a widget (code will be visible until this time)
     */
    fun setExpiry(widgetId: Int, timestamp: Long) {
        expiryTimestamps[widgetId] = timestamp
    }
    
    /**
     * Get expiry timestamp for a widget
     * Returns 0 if not set or if code should be hidden
     */
    fun getExpiry(widgetId: Int): Long {
        val expiry = expiryTimestamps[widgetId] ?: 0
        val now = System.currentTimeMillis()
        
        // Auto-cleanup expired entries
        if (expiry > 0 && now >= expiry) {
            expiryTimestamps.remove(widgetId)
            return 0
        }
        
        return expiry
    }
    
    /**
     * Check if code should be visible for this widget
     */
    fun isCodeVisible(widgetId: Int): Boolean {
        return getExpiry(widgetId) > System.currentTimeMillis()
    }
    
    /**
     * Clear expiry for a widget (hide code)
     */
    fun clearExpiry(widgetId: Int) {
        expiryTimestamps.remove(widgetId)
    }
    
    /**
     * Clear all expiry timestamps (useful for cleanup)
     */
    fun clearAll() {
        expiryTimestamps.clear()
    }
}
```

### 2.3 Widget Repository
**File:** `repository/WidgetRepository.kt` (in main repository package)

```kotlin
class WidgetRepository(
    private val widgetDao: WidgetDao,
    private val tokenRepository: TokenRepository,
) {
    // Widget Configuration
    suspend fun saveWidget(widgetId: Int, type: WidgetType, tokenIds: List<Long>) {
        val widget = Widget(
            widgetId = widgetId,
            widgetType = type,
            tokenIds = Json.encodeToString(tokenIds)
        )
        widgetDao.upsert(widget)
    }
    
    suspend fun getWidget(widgetId: Int): Widget? {
        return widgetDao.getWidget(widgetId)
    }
    
    suspend fun deleteWidget(widgetId: Int) {
        widgetDao.delete(widgetId)
        // Also clear from memory
        WidgetMemoryState.clearExpiry(widgetId)
    }
    
    suspend fun getWidgetTokenIds(widgetId: Int): List<Long> {
        val widget = widgetDao.getWidget(widgetId) ?: return emptyList()
        return Json.decodeFromString(widget.tokenIds)
    }
    
    // Code generation - returns Code object (TOTPCode or HOTPCode)
    suspend fun generateCode(token: Token): Code {
        return when (token.protocol) {
            Protocol.TOTP -> TOTPGenerator.generateCode(token)
            Protocol.HOTP -> HOTPGenerator.generateCode(token)
        }
    }
    
    // Calculate next expiry based on Code object
    // For TOTP: use periodEnd from TOTPCode
    // For HOTP: fixed 30 seconds from now (or never expire)
    fun calculateNextExpiry(code: Code): Long {
        return when (code) {
            is TOTPCode -> code.periodEnd
            else -> System.currentTimeMillis() + 30_000 // HOTP: 30 seconds
        }
    }
}
```

### 2.4 Widget State Provider
**File:** `widget/common/WidgetStateProvider.kt`

```kotlin
class WidgetStateProvider(
    private val widgetRepository: WidgetRepository,
    private val tokenRepository: TokenRepository,
) {
    suspend fun provideSingleWidgetState(widgetId: Int): SingleTokenWidgetState {
        val widget = widgetRepository.getWidget(widgetId)
            ?: return SingleTokenWidgetState(
                widgetId = widgetId,
                tokenId = null,
                issuer = "",
                name = "",
                group = "",
                code = null,
                progress = 0f,
                remainingSeconds = 0,
                error = "Widget not configured"
            )
        
        val tokenIds = Json.decodeFromString<List<Long>>(widget.tokenIds)
        val tokenId = tokenIds.firstOrNull()
            ?: return SingleTokenWidgetState(
                widgetId = widgetId,
                tokenId = null,
                issuer = "",
                name = "",
                group = "",
                code = null,
                progress = 0f,
                remainingSeconds = 0,
                error = "No token configured"
            )
        
        val token = tokenRepository.getToken(tokenId)
            ?: return SingleTokenWidgetState(
                widgetId = widgetId,
                tokenId = tokenId,
                issuer = "",
                name = "",
                group = "",
                code = null,
                progress = 0f,
                remainingSeconds = 0,
                error = "Token not found"
            )
        val token = tokenRepository.getToken(tokenId)
            ?: return SingleTokenWidgetState(
                widgetId = widgetId,
                token = null,
                code = null,
                error = "Token not found"
            )
        
        // Check if code should be visible (from memory)
        val isCodeVisible = WidgetMemoryState.isCodeVisible(widgetId)
        val code = if (isCodeVisible) {
            widgetRepository.generateCode(token)
        } else {
            null
        }
        
        return SingleTokenWidgetState(
            widgetId = widgetId,
            token = token,
            code = code,
            error = null
        )
    }
    
    suspend fun provideMultiWidgetState(widgetId: Int): MultiTokenWidgetState {
        val widget = widgetRepository.getWidget(widgetId)
            ?: return MultiTokenWidgetState(
                widgetId = widgetId,
                tokens = emptyList(),
                isEmpty = true
            )
        
        val tokenIds = Json.decodeFromString<List<Long>>(widget.tokenIds)
        if (tokenIds.isEmpty()) {
            return MultiTokenWidgetState(
                widgetId = widgetId,
                tokens = emptyList(),
                isEmpty = true
            )
        }
        
        // Check if code should be visible (from memory)
        val isCodeVisible = WidgetMemoryState.isCodeVisible(widgetId)
        
        val tokens = tokenIds.mapNotNull { tokenId ->
            val token = tokenRepository.getToken(tokenId) ?: return@mapNotNull null
            
            val code = if (isCodeVisible) {
                widgetRepository.generateCode(token)
            } else {
                null
            }
            
            TokenWidgetItem(
                token = token,
                code = code
            )
        }
        
        return MultiTokenWidgetState(
            widgetId = widgetId,
            tokens = tokens,
            isEmpty = tokens.isEmpty()
        )
    }
}
```

---

## 📋 Phase 3: Single Token Widget

### 3.1 Configuration Screen (Compose)
**File:** `widget/single/SingleTokenConfigScreen.kt`

```kotlin
@Composable
fun SingleTokenConfigScreen(
    modifier: Modifier = Modifier,
    onTokenSelected: (tokenId: Long) -> Unit,
    onCancel: () -> Unit,
) {
    val viewModel: SingleTokenConfigVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is SingleTokenConfigUiEffect.TokenSelected -> onTokenSelected(effect.tokenId)
                is SingleTokenConfigUiEffect.Cancel -> onCancel()
            }
        }
    }
    
    SingleTokenConfigScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleTokenConfigScreen(
    modifier: Modifier = Modifier,
    uiState: SingleTokenConfigUiState,
    onEvent: (SingleTokenConfigUiEvent) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Select Token") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SingleTokenConfigUiEvent.Cancel) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar (similar to Home screen)
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { onEvent(SingleTokenConfigUiEvent.UpdateSearch(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            // Token list
            if (uiState.filteredTokens.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.searchQuery.isBlank()) {
                            "No tokens available"
                        } else {
                            "No tokens match your search"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn {
                    items(uiState.filteredTokens, key = { it.id }) { token ->
                        TokenSelectItem(
                            token = token,
                            onClick = { onEvent(SingleTokenConfigUiEvent.SelectToken(token.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TokenSelectItem(
    modifier: Modifier = Modifier,
    token: Token,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Issuer
            Text(
                text = token.issuer,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Name
            if (token.name.isNotBlank()) {
                Text(
                    text = token.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Group
            if (token.group.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = token.group,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
```

**File:** `widget/single/SingleTokenConfigVM.kt`

```kotlin
data class SingleTokenConfigUiState(
    val tokens: List<Token> = emptyList(),
    val searchQuery: String = "",
    val filteredTokens: List<Token> = emptyList(),
)

sealed interface SingleTokenConfigUiEvent {
    data class UpdateSearch(val query: String) : SingleTokenConfigUiEvent
    data class SelectToken(val tokenId: Long) : SingleTokenConfigUiEvent
    data object Cancel : SingleTokenConfigUiEvent
}

sealed interface SingleTokenConfigUiEffect {
    data class TokenSelected(val tokenId: Long) : SingleTokenConfigUiEffect
    data object Cancel : SingleTokenConfigUiEffect
}

class SingleTokenConfigVM(
    private val tokenRepository: TokenRepository,
) : ViewModel() {
    
    val uiState = MutableStateFlow(SingleTokenConfigUiState())
    
    private val uiEffectChannel = Channel<SingleTokenConfigUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)
    
    init {
        loadTokens()
    }
    
    fun onEvent(event: SingleTokenConfigUiEvent) = when (event) {
        is SingleTokenConfigUiEvent.UpdateSearch -> updateSearch(event.query)
        is SingleTokenConfigUiEvent.SelectToken -> selectToken(event.tokenId)
        SingleTokenConfigUiEvent.Cancel -> cancel()
    }
    
    private fun loadTokens() = viewModelScope.launch(Dispatchers.IO) {
        tokenRepository.getAllTokens().collect { tokens ->
            uiState.update { state ->
                state.copy(
                    tokens = tokens,
                    filteredTokens = filterTokens(tokens, state.searchQuery)
                )
            }
        }
    }
    
    private fun updateSearch(query: String) {
        uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredTokens = filterTokens(state.tokens, query)
            )
        }
    }
    
    private fun filterTokens(tokens: List<Token>, query: String): List<Token> {
        if (query.isBlank()) return tokens
        return tokens.filter { token ->
            token.issuer.contains(query, ignoreCase = true) ||
            token.name.contains(query, ignoreCase = true) ||
            token.group.contains(query, ignoreCase = true)
        }
    }
    
    private fun selectToken(tokenId: Long) = viewModelScope.launch(Dispatchers.IO) {
        uiEffectChannel.send(SingleTokenConfigUiEffect.TokenSelected(tokenId))
    }
    
    private fun cancel() = viewModelScope.launch(Dispatchers.IO) {
        uiEffectChannel.send(SingleTokenConfigUiEffect.Cancel)
    }
}
```

### 3.2 Configuration Activity
**File:** `widget/single/SingleTokenConfigActivity.kt`

```kotlin
class SingleTokenConfigActivity : ComponentActivity() {
    
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get widget ID
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        
        // Set result to CANCELED initially
        setResult(RESULT_CANCELED)
        
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        setContent {
            ToptyTheme {
                Surface {
                    SingleTokenConfigScreen(
                        onTokenSelected = { tokenId ->
                            saveConfigAndFinish(tokenId)
                        },
                        onCancel = {
                            finish()
                        }
                    )
                }
            }
        }
    }
    
    private fun saveConfigAndFinish(tokenId: Long) {
        lifecycleScope.launch {
            val repository: WidgetRepository = get()
            
            // Save widget config
            repository.saveWidget(appWidgetId, WidgetType.SINGLE, listOf(tokenId))
            
            // Update widget
            val appWidgetManager = AppWidgetManager.getInstance(this@SingleTokenConfigActivity)
            SingleTokenWidgetReceiver.updateWidget(this@SingleTokenConfigActivity, appWidgetManager, appWidgetId)
            
            // Return success
            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}
```

### 3.3 Widget UI (Glance)
**File:** `widget/single/SingleTokenWidget.kt`

```kotlin
class SingleTokenGlanceWidget : GlanceAppWidget() {
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
            val state = loadSingleWidgetState(context, widgetId)
            
            // If code is visible, schedule next update in 1 second
            if (state.code != null && state.remainingSeconds > 0) {
                LaunchedEffect(state.remainingSeconds) {
                    delay(1000)
                    update(context, id)
                }
            }
            
            GlanceTheme {
                SingleTokenWidgetContent(state)
            }
        }
    }
    
    private suspend fun loadSingleWidgetState(context: Context, widgetId: Int): SingleTokenWidgetState {
        val provider: WidgetStateProvider = get()
        return provider.provideSingleWidgetState(widgetId)
    }
}

@Composable
fun SingleTokenWidgetContent(state: SingleTokenWidgetState) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(16.dp)
            .padding(16.dp)
            .clickable(onClick = actionRevealCode(state.widgetId, state.tokenId))
    ) {
        when {
            state.error != null -> {
                // Error state
                Text(
                    text = state.error,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = GlanceTheme.colors.error
                    )
                )
            }
            
            else -> {
                // Issuer
                Text(
                    text = state.issuer,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )
                
                // Name
                if (state.name.isNotBlank()) {
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = state.name,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
                
                Spacer(modifier = GlanceModifier.height(12.dp))
                
                // Code or prompt
                if (state.code != null) {
                    // Show code
                    Text(
                        text = WidgetUtils.formatCode(state.code),
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.primary
                        )
                    )
                    
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    
                    // Progress bar
                    LinearProgressIndicator(
                        progress = state.progress,
                        color = GlanceTheme.colors.primary,
                        backgroundColor = GlanceTheme.colors.surfaceVariant,
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(4.dp)
                    )
                    
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    
                    // Remaining time
                    Text(
                        text = "${state.remainingSeconds}s",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                } else {
                    // Tap to reveal prompt
                    Text(
                        text = "Tap to reveal code",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}
```

### 3.4 Widget Receiver
**File:** `widget/single/SingleTokenWidgetReceiver.kt`

```kotlin
class SingleTokenWidgetReceiver : GlanceAppWidgetReceiver() {
    
    override val glanceAppWidget: GlanceAppWidget = SingleTokenGlanceWidget()
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // Glance handles updates automatically
    }
    
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        
        // Clean up widget configs and memory state
        appWidgetIds.forEach { widgetId ->
            CoroutineScope(Dispatchers.IO).launch {
                val repository: WidgetRepository = get()
                repository.deleteWidget(widgetId)
            }
        }
    }
    
    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(widgetId)
            CoroutineScope(Dispatchers.Main).launch {
                SingleTokenGlanceWidget().update(context, glanceId)
            }
        }
        
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, SingleTokenWidgetReceiver::class.java)
            )
            widgetIds.forEach { updateWidget(context, appWidgetManager, it) }
        }
    }
}
```

---

## 📋 Phase 4: Multi Token Widget

### 4.1 Configuration Screen (Compose)
**File:** `widget/multi/MultiTokenConfigScreen.kt`

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MultiTokenConfigScreen(
    modifier: Modifier = Modifier,
    uiState: MultiTokenConfigUiState,
    onEvent: (MultiTokenConfigUiEvent) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Select Tokens")
                        Text(
                            "${uiState.selectedTokens.size} selected",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(MultiTokenConfigUiEvent.Cancel) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onEvent(MultiTokenConfigUiEvent.Save) },
                        enabled = uiState.selectedTokens.isNotEmpty()
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Info text
            if (uiState.selectedTokens.size >= MAX_TOKENS) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Maximum $MAX_TOKENS tokens reached",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Search bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { onEvent(MultiTokenConfigUiEvent.UpdateSearch(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            // Token list with checkboxes
            if (uiState.filteredTokens.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.searchQuery.isBlank()) {
                            "No tokens available"
                        } else {
                            "No tokens match your search"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn {
                    items(uiState.filteredTokens, key = { it.id }) { token ->
                        TokenMultiSelectItem(
                            token = token,
                            isSelected = token.id in uiState.selectedTokens,
                            isEnabled = token.id in uiState.selectedTokens || 
                                       uiState.selectedTokens.size < MAX_TOKENS,
                            onToggle = { onEvent(MultiTokenConfigUiEvent.ToggleToken(token.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TokenMultiSelectItem(
    modifier: Modifier = Modifier,
    token: Token,
    isSelected: Boolean,
    isEnabled: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onToggle)
            .padding(16.dp)
            .alpha(if (isEnabled) 1f else 0.5f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            enabled = isEnabled
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = token.issuer,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (token.name.isNotBlank()) {
                Text(
                    text = token.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (token.group.isNotBlank()) {
                Text(
                    text = token.group,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

private const val MAX_TOKENS = 8
```

**File:** `widget/multi/MultiTokenConfigVM.kt`

```kotlin
data class MultiTokenConfigUiState(
    val tokens: List<Token> = emptyList(),
    val searchQuery: String = "",
    val filteredTokens: List<Token> = emptyList(),
    val selectedTokens: Set<Long> = emptySet(),
)

sealed interface MultiTokenConfigUiEvent {
    data class UpdateSearch(val query: String) : MultiTokenConfigUiEvent
    data class ToggleToken(val tokenId: Long) : MultiTokenConfigUiEvent
    data object Save : MultiTokenConfigUiEvent
    data object Cancel : MultiTokenConfigUiEvent
}

sealed interface MultiTokenConfigUiEffect {
    data class TokensSelected(val tokenIds: List<Long>) : MultiTokenConfigUiEffect
    data object Cancel : MultiTokenConfigUiEffect
}

class MultiTokenConfigVM(
    private val tokenRepository: TokenRepository,
) : ViewModel() {
    
    val uiState = MutableStateFlow(MultiTokenConfigUiState())
    
    private val uiEffectChannel = Channel<MultiTokenConfigUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)
    
    init {
        loadTokens()
    }
    
    fun onEvent(event: MultiTokenConfigUiEvent) = when (event) {
        is MultiTokenConfigUiEvent.UpdateSearch -> updateSearch(event.query)
        is MultiTokenConfigUiEvent.ToggleToken -> toggleToken(event.tokenId)
        MultiTokenConfigUiEvent.Save -> save()
        MultiTokenConfigUiEvent.Cancel -> cancel()
    }
    
    private fun loadTokens() = viewModelScope.launch(Dispatchers.IO) {
        tokenRepository.getAllTokens().collect { tokens ->
            uiState.update { state ->
                state.copy(
                    tokens = tokens,
                    filteredTokens = filterTokens(tokens, state.searchQuery)
                )
            }
        }
    }
    
    private fun updateSearch(query: String) {
        uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredTokens = filterTokens(state.tokens, query)
            )
        }
    }
    
    private fun filterTokens(tokens: List<Token>, query: String): List<Token> {
        if (query.isBlank()) return tokens
        return tokens.filter { token ->
            token.issuer.contains(query, ignoreCase = true) ||
            token.name.contains(query, ignoreCase = true) ||
            token.group.contains(query, ignoreCase = true)
        }
    }
    
    private fun toggleToken(tokenId: Long) {
        uiState.update { state ->
            val newSelected = if (tokenId in state.selectedTokens) {
                state.selectedTokens - tokenId
            } else {
                if (state.selectedTokens.size < MAX_TOKENS) {
                    state.selectedTokens + tokenId
                } else {
                    state.selectedTokens
                }
            }
            state.copy(selectedTokens = newSelected)
        }
    }
    
    private fun save() = viewModelScope.launch(Dispatchers.IO) {
        val tokenIds = uiState.value.selectedTokens.toList()
        uiEffectChannel.send(MultiTokenConfigUiEffect.TokensSelected(tokenIds))
    }
    
    private fun cancel() = viewModelScope.launch(Dispatchers.IO) {
        uiEffectChannel.send(MultiTokenConfigUiEffect.Cancel)
    }
    
    companion object {
        private const val MAX_TOKENS = 8
    }
}
```

### 4.2 Widget UI (Glance)
**File:** `widget/multi/MultiTokenWidget.kt`

```kotlin
class MultiTokenGlanceWidget : GlanceAppWidget() {
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
            val state = loadMultiWidgetState(context, widgetId)
            
            // If any code is visible, schedule next update in 1 second
            val hasVisibleCode = state.tokens.any { it.code != null }
            if (hasVisibleCode) {
                val minRemainingSeconds = state.tokens.minOfOrNull { it.remainingSeconds } ?: 0
                if (minRemainingSeconds > 0) {
                    LaunchedEffect(minRemainingSeconds) {
                        delay(1000)
                        update(context, id)
                    }
                }
            }
            
            GlanceTheme {
                MultiTokenWidgetContent(state)
            }
        }
    }
    
    private suspend fun loadMultiWidgetState(context: Context, widgetId: Int): MultiTokenWidgetState {
        val provider: WidgetStateProvider = get()
        return provider.provideMultiWidgetState(widgetId)
    }
}

@Composable
fun MultiTokenWidgetContent(state: MultiTokenWidgetState) {
    LazyColumn(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(16.dp)
    ) {
        if (state.isEmpty) {
            item {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tokens configured",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            }
        } else {
            items(state.tokens) { token ->
                MultiTokenWidgetRow(
                    widgetId = state.widgetId,
                    token = token
                )
                
                // Divider (except last item)
                if (token != state.tokens.last()) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .padding(horizontal = 16.dp)
                            .background(GlanceTheme.colors.outlineVariant)
                    )
                }
            }
        }
    }
}

@Composable
fun MultiTokenWidgetRow(
    widgetId: Int,
    token: TokenWidgetItem,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .clickable(onClick = actionRevealCode(widgetId, token.tokenId))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = GlanceModifier.defaultWeight()
        ) {
            // Issuer
            Text(
                text = token.issuer,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
            
            // Name
            if (token.name.isNotBlank()) {
                Text(
                    text = token.name,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        }
        
        Spacer(modifier = GlanceModifier.width(8.dp))
        
        // Code or prompt
        if (token.code != null) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = WidgetUtils.formatCode(token.code),
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.primary
                    )
                )
                
                Spacer(modifier = GlanceModifier.height(2.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = token.progress,
                        modifier = GlanceModifier
                            .width(50.dp)
                            .height(2.dp),
                        color = GlanceTheme.colors.primary,
                        backgroundColor = GlanceTheme.colors.surfaceVariant
                    )
                    
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    
                    Text(
                        text = "${token.remainingSeconds}s",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            }
        } else {
            Text(
                text = "Tap",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}
```

---

## 📋 Phase 5: Widget Actions

### 5.1 Widget Actions
**File:** `widget/common/WidgetActions.kt`

```kotlin
// Action to reveal code
fun actionRevealCode(widgetId: Int, tokenId: Long?): Action {
    return actionRunCallback<RevealCodeAction>(
        parameters = parametersOf(
            RevealCodeAction.KEY_WIDGET_ID to widgetId,
            RevealCodeAction.KEY_TOKEN_ID to (tokenId ?: -1L)
        )
    )
}

class RevealCodeAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val widgetId = parameters[KEY_WIDGET_ID] ?: return
        
        val tokenRepository: TokenRepository = get()
        val widgetRepository: WidgetRepository = get()
        
        // Get widget to determine token(s)
        val widget = widgetRepository.getWidget(widgetId) ?: return
        val tokenIds = Json.decodeFromString<List<Long>>(widget.tokenIds)
        
        // Get first token and generate code to get timing info
        val token = tokenIds.firstOrNull()?.let { tokenRepository.getToken(it) } ?: return
        val code = widgetRepository.generateCode(token)
        
        // Calculate expiry timestamp from Code object
        // TOTP: uses periodEnd from code, HOTP: 30 seconds from now
        val expiryTimestamp = widgetRepository.calculateNextExpiry(code)
        
        // Store expiry in memory
        WidgetMemoryState.setExpiry(widgetId, expiryTimestamp)
        
        // Update widget immediately (Glance will handle subsequent updates)
        updateWidgetByType(context, widgetId)
    }
    
    companion object {
        val KEY_WIDGET_ID = ActionParameters.Key<Int>("widget_id")
        val KEY_TOKEN_ID = ActionParameters.Key<Long>("token_id")
    }
}

private fun updateWidgetByType(context: Context, widgetId: Int) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    
    // Try both widget types (one will succeed)
    SingleTokenWidgetReceiver.updateWidget(context, appWidgetManager, widgetId)
    MultiTokenWidgetReceiver.updateWidget(context, appWidgetManager, widgetId)
}
```

---

## 📋 Phase 6: Widget Utils

**File:** `widget/common/WidgetUtils.kt`

```kotlin
object WidgetUtils {
    
    // Format code with spaces (e.g., "123456" -> "123 456")
    fun formatCode(code: String): String {
        return code.chunked(3).joinToString(" ")
    }
    
    // Calculate progress (0.0 to 1.0) based on current time and period
    fun calculateProgress(period: Int): Float {
        val now = System.currentTimeMillis() / 1000
        val elapsed = now % period
        return elapsed.toFloat() / period.toFloat()
    }
    
    // Calculate remaining seconds in current period
    fun calculateRemainingSeconds(period: Int): Int {
        val now = System.currentTimeMillis() / 1000
        val elapsed = now % period
        return (period - elapsed).toInt()
    }
}
```

---

## 📋 Phase 7: Koin Module

**File:** `widget/di/WidgetModule.kt`

```kotlin
val WidgetModule = module {
    
    // Repository (in main repository package)
    singleOf(::WidgetRepository)
    
    // State Provider
    factoryOf(::WidgetStateProvider)
    
    // ViewModels
    viewModelOf(::SingleTokenConfigVM)
    viewModelOf(::MultiTokenConfigVM)
}
```

**Add to** `ToptyApplication.kt`:
```kotlin
startKoin {
    modules(ToptyModule, WidgetModule)
}
```

---

## 📋 Phase 8: Android Manifest & Resources

### 8.1 Manifest
**File:** `AndroidManifest.xml`

```xml
<!-- Single Token Widget -->
<receiver
    android:name="com.axondragonscale.topty.widget.single.SingleTokenWidgetReceiver"
    android:exported="true"
    android:label="@string/single_token_widget_label">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/single_token_widget_info" />
</receiver>

<activity
    android:name="com.axondragonscale.topty.widget.single.SingleTokenConfigActivity"
    android:exported="false"
    android:theme="@style/Theme.Topty">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_CONFIGURE" />
    </intent-filter>
</activity>

<!-- Multi Token Widget -->
<receiver
    android:name="com.axondragonscale.topty.widget.multi.MultiTokenWidgetReceiver"
    android:exported="true"
    android:label="@string/multi_token_widget_label">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/multi_token_widget_info" />
</receiver>

<activity
    android:name="com.axondragonscale.topty.widget.multi.MultiTokenConfigActivity"
    android:exported="false"
    android:theme="@style/Theme.Topty">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_CONFIGURE" />
    </intent-filter>
</activity>
```

### 8.2 Widget Info XML
**File:** `res/xml/single_token_widget_info.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="180dp"
    android:minHeight="110dp"
    android:targetCellWidth="3"
    android:targetCellHeight="2"
    android:updatePeriodMillis="0"
    android:configure="com.axondragonscale.topty.widget.single.SingleTokenConfigActivity"
    android:description="@string/single_token_widget_description"
    android:previewImage="@drawable/widget_preview_single"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen" />
```

**File:** `res/xml/multi_token_widget_info.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="250dp"
    android:minHeight="220dp"
    android:targetCellWidth="4"
    android:targetCellHeight="4"
    android:updatePeriodMillis="0"
    android:configure="com.axondragonscale.topty.widget.multi.MultiTokenConfigActivity"
    android:description="@string/multi_token_widget_description"
    android:previewImage="@drawable/widget_preview_multi"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen" />
```

### 8.3 Strings
**File:** `res/values/strings.xml`

```xml
<!-- Widgets -->
<string name="single_token_widget_label">Topty - Single Token</string>
<string name="single_token_widget_description">Display one 2FA code</string>
<string name="multi_token_widget_label">Topty - Multi Token</string>
<string name="multi_token_widget_description">Display multiple 2FA codes</string>
```

---

## 📋 Implementation Order

### **Week 1: Foundation**
- [ ] Day 1: Add Glance dependencies
- [ ] Day 1-2: Create Widget entity, DAO, and database migration
- [ ] Day 2: Create widget package structure
- [ ] Day 2-3: Create WidgetMemoryState and WidgetRepository
- [ ] Day 3: Create widget state models and WidgetStateProvider

### **Week 2: Single Token Widget**
- [ ] Day 1-2: Configuration screen UI (similar to Home screen style)
- [ ] Day 2: SingleTokenConfigVM with search and selection
- [ ] Day 3: SingleTokenConfigActivity
- [ ] Day 3-4: Single token Glance widget UI with self-update
- [ ] Day 4-5: SingleTokenWidgetReceiver and RevealCodeAction

### **Week 3: Multi Token Widget**
- [ ] Day 1-2: Multi-select configuration screen
- [ ] Day 2: MultiTokenConfigVM
- [ ] Day 3: MultiTokenConfigActivity
- [ ] Day 3-4: Multi token Glance widget UI (list layout) with self-update
- [ ] Day 5: MultiTokenWidgetReceiver

### **Week 4: Polish & Testing**
- [ ] Day 1-2: Widget actions, theme matching app theme
- [ ] Day 2-3: WidgetUtils, preview images, widget info XML
- [ ] Day 3-5: Testing all edge cases and scenarios

---

## 📋 Testing Checklist

### Configuration
- [ ] Single widget configuration shows all tokens
- [ ] Multi widget allows selecting up to 8 tokens
- [ ] Search/filter works in both configurations
- [ ] Configuration saves correctly to database
- [ ] Cancel button works properly

### Widget Display
- [ ] Widget shows "Tap to reveal" by default
- [ ] Click reveals code
- [ ] Code format matches app (spaced, monospace)
- [ ] Progress bar animates correctly
- [ ] Remaining seconds countdown works (updates every second)

### Code Visibility & Auto-Hide
- [ ] Code auto-hides after expiry
- [ ] Widget stops updating when code expires
- [ ] Multiple widgets work independently
- [ ] Different multi widgets can show different codes
- [ ] Memory state cleared on widget deletion

### Self-Update Mechanism
- [ ] Widget updates every second while code visible
- [ ] Updates stop after code expires
- [ ] LaunchedEffect triggers correctly
- [ ] No excessive battery drain

### Edge Cases
- [ ] Widget handles deleted tokens gracefully
- [ ] Widget handles no tokens configured
- [ ] If process dies, codes are hidden (security feature)
- [ ] Multiple instances of same widget type work
- [ ] Different widget sizes display correctly
- [ ] Widget deletion cleans up database

### Performance
- [ ] No ANR or crashes
- [ ] Smooth animations
- [ ] Minimal battery impact while code hidden
- [ ] Works on different Android versions

### Theme & Design
- [ ] Widget theme matches app theme
- [ ] Light/dark mode support
- [ ] Colors consistent with app
- [ ] Typography matches app
- [ ] Spacing and padding appropriate

---

## 📋 Key Design Decisions

1. **No Persistent Visibility State**: Code visibility stored only in memory (`WidgetMemoryState`). If process dies, codes are auto-hidden for security.

2. **No WorkManager**: Glance handles all updates via `LaunchedEffect` with 1-second delay. Much simpler and more efficient.

3. **Self-Updating Widgets**: Widget re-triggers its own update every second while code is visible, stops when expired.

4. **Simplified State Using Existing Classes**: Widget state uses existing `Token` and `Code` classes instead of duplicating fields. Benefits:
   - No duplication of token data (issuer, name, group, etc.)
   - `TOTPCode` already has `periodStart` and `periodEnd` for accurate timing
   - `HOTPCode` vs `TOTPCode` handling via sealed class
   - Type-safe and consistent with rest of app

5. **TOTP vs HOTP Handling**:
   - **TOTP**: Uses `periodEnd` from `TOTPCode` for expiry, progress bar shows actual time remaining
   - **HOTP**: Fixed 30 seconds visibility (or could be infinite), no progress bar

6. **No Database Migration**: App is pre-release, so direct database schema update without migration is acceptable.

7. **Independent Widgets**: Each widget instance maintains separate state in memory, allowing true independence.

8. **Repository Location**: WidgetRepository in main `repository/` package for consistency.

9. **Security by Design**: If app process is killed, all visible codes are automatically hidden (memory cleared).

10. **Theme Consistency**: Use Glance Material3 theming to match app's visual style.

11. **Configuration UX**: Reuse existing app patterns (search, card layouts) for familiar user experience.

---

## 📋 How It Works

### User Flow:
1. User adds widget → Configuration screen → Select token(s) → Widget added
2. Widget shows "Tap to reveal code"
3. User taps widget → `RevealCodeAction` triggered
4. Action calculates expiry timestamp, stores in `WidgetMemoryState`
5. Widget updates immediately, shows code + countdown + progress
6. Widget's `LaunchedEffect` schedules next update in 1 second
7. Widget re-renders every second, checking if expired
8. When expired: Memory cleared, widget shows "Tap to reveal"

### Technical Flow:
```
User Tap → RevealCodeAction
    ↓
Generate Code (TOTPCode with periodEnd or HOTPCode)
    ↓
Calculate expiry from Code (use periodEnd or +30s)
    ↓
Store expiry in WidgetMemoryState
    ↓
Trigger widget update
    ↓
Widget renders (provideGlance)
    ↓
Check WidgetMemoryState.isCodeVisible()
    ↓
If visible: Generate Code object, extract code.code string
    ↓
Calculate progress from Code (TOTPCode.periodEnd, HOTP = 0)
    ↓
Show code + progress + countdown (from Code timing)
    ↓
LaunchedEffect schedules next update (1s)
    ↓
Repeat until expired
    ↓
On expiry: Memory cleared, show "Tap to reveal"
```

---

Ready to implement!