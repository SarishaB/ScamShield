/*
 *
 *  * Copyright (C) 2025 AKS-Labs (original author)
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.akslabs.circletosearch.ui

import android.graphics.Bitmap
import android.graphics.Rect
import com.akslabs.circletosearch.CircleToSearchAccessibilityService
import android.util.Base64
import android.view.Display
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BorderOuter
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.webkit.WebSettingsCompat
import com.akslabs.circletosearch.data.SearchEngine
import com.akslabs.circletosearch.data.ScamDetectionApi
import com.akslabs.circletosearch.ui.theme.OverlayGradientColors
import com.akslabs.circletosearch.utils.ImageUtils
import com.akslabs.circletosearch.utils.QrResult
import com.akslabs.circletosearch.utils.QrResultWithBounds
import com.akslabs.circletosearch.utils.QrScanner
import com.akslabs.circletosearch.ui.qrResultShortLabel
import com.akslabs.circletosearch.utils.UIPreferences
import android.net.Uri
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import androidx.webkit.WebViewFeature
import com.akslabs.circletosearch.data.isDirectUpload
import kotlin.math.max
import kotlin.math.min
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import android.os.Build
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleToSearchScreen(
    screenshot: Bitmap?,
    onClose: () -> Unit,
    searchModeOverride: Boolean? = null,
    copyTextManager: com.akslabs.circletosearch.ui.components.CopyTextOverlayManager? = null,
    onCopyText: () -> Unit = {},
    onExitCopyMode: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Initialize preferences
    val uiPreferences = remember { UIPreferences(context) }
    
    // Reactive search method state — observed via flow so it is always in sync
    // with whatever set it (home screen, settings sheet, or accessibility actions).
    // Reading from the flow avoids the SharedPreferences in-memory cache lag
    // that occurs when a write comes from a different activity instance.
    val isGoogleLensOnly by uiPreferences.observeUseGoogleLensOnly()
        .collectAsState(initial = uiPreferences.isUseGoogleLensOnly())
    
    // Effective state: use override if provided by trigger, else use global preference
    val effectiveLensOnly = searchModeOverride ?: isGoogleLensOnly
    
    
    // Material You logic for colors
    val isDark = isSystemInDarkTheme()
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColor && isDark -> dynamicDarkColorScheme(context)
        dynamicColor && !isDark -> dynamicLightColorScheme(context)
        else -> MaterialTheme.colorScheme // Fallback standard
    }
    //Colors for dark theme
    val barBgColor = if (isDark) Color(0xFF0D0D0D) else colorScheme.surface
    val bubbleColor = if (isDark) Color(0xFF1F1F1F) else colorScheme.secondaryContainer.copy(alpha = 0.9f)
    val contentColor = if (isDark) Color.White else colorScheme.onSecondaryContainer

    //Haptic feedback
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    //Slide-in animation
    var isUIVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isUIVisible = true // Déclenche l'animation à l'ouverture
    }

    // Search Engines Order Logic
    val preferredOrder = remember(uiPreferences.getSearchEngineOrder()) {
        val allEngines = SearchEngine.values()
        val orderString = uiPreferences.getSearchEngineOrder()
        if (orderString == null) allEngines
        else {
            val preferredNames = orderString.split(",")
            val ordered = mutableListOf<SearchEngine>()
            preferredNames.forEach { name ->
                allEngines.find { it.name == name }?.let { ordered.add(it) }
            }
            allEngines.forEach { if (!ordered.contains(it)) ordered.add(it) }
            ordered
        }
    }
    val searchEngines = preferredOrder
    
    // Copy Mode internal state
    var isCopyMode by remember { mutableStateOf(false) }

    // Support Settings Sheet
    var showSettingsScreen by remember { mutableStateOf(false) }

    // Resizing state
    var isResizing by remember { mutableStateOf(false) }
    var activeHandle by remember { mutableStateOf<String?>(null) } // "tl", "tr", "bl", "br"
    
    // QR Scanner state
    var showQrSheet by remember { mutableStateOf(false) }
    var qrScanBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var detectedQrCodes by remember { mutableStateOf<List<QrResultWithBounds>>(emptyList()) }
    var selectedQrResult by remember { mutableStateOf<QrResultWithBounds?>(null) }
    
    // Phase 44: Smart Entity Extractor
    var isEntityExtractMode by remember { mutableStateOf(false) }
    var detectedEntities by remember { mutableStateOf<List<SmartEntity>>(emptyList()) }
    var isExtractingEntities by remember { mutableStateOf(false) }
    
    // Search State
    var selectedEngine by remember(searchEngines) { mutableStateOf<SearchEngine>(searchEngines.first()) }
    var searchUrl by remember { mutableStateOf<String?>(null) }
    var hostedImageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Desktop Mode - Per Tab
    // We use a set to track which engines are in desktop mode
    val initialDesktopMode = uiPreferences.isDesktopMode() // Global default
    var desktopModeEngines by remember { mutableStateOf<Set<SearchEngine>>(if(initialDesktopMode) searchEngines.toSet() else emptySet()) }
    
    var isDarkMode by remember { mutableStateOf(uiPreferences.isDarkMode()) }
    var showGradientBorder by remember { mutableStateOf(uiPreferences.isShowGradientBorder()) }
    
    // Back gesture handler for QR sheet
    // We use a high-priority check or ensure it's at the very top of the hierarchy
    BackHandler(enabled = showQrSheet) {
        showQrSheet = false
        selectedQrResult = null
    }

    // Track initialized engines for Smart Loading
    val initializedEngines = remember { mutableStateListOf<SearchEngine>() }
    
    // Save global preference if user toggles it for the MAIN engine (Google) - Optional choice, 
    // or we just keep it per session. Let's keep it simple: no auto-save of per-tab state to verify complex persistence yet.
    // simpler: If user toggles, we just update the state.
    
    // Helper to check desktop mode
    fun isDesktop(engine: SearchEngine) = desktopModeEngines.contains(engine)
    
    // Removed auto-save of isDesktopMode for now as it is complex with per-tab
    // We could save "If ALL are desktop" or just the active one? 
    // User requested "depending on opened tab", so per-session state is safer.
    
    LaunchedEffect(isDarkMode) {
        uiPreferences.setDarkMode(isDarkMode)
    }
    
    LaunchedEffect(showGradientBorder) {
        uiPreferences.setShowGradientBorder(showGradientBorder)
    }
    
    // Cache for preloaded URLs to avoid re-uploading/re-generating
    val preloadedUrls = remember { mutableMapOf<SearchEngine, String>() }
    
    // WebView Cache
    val webViews = remember { mutableMapOf<SearchEngine, WebView>() }
    
    // Update User Agent dynamically when desktop mode changes for a specific engine
    // This is now handled in the AndroidView update block or individual engine effects
    // BUT we need to force reload if the state changes.
    LaunchedEffect(desktopModeEngines) {
        webViews.forEach { (engine, wv) ->
             val isDesktop = desktopModeEngines.contains(engine)
             val newUserAgent = if (isDesktop) {
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            } else {
                "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
            }
            
            if (wv.settings.userAgentString != newUserAgent) {
                wv.settings.userAgentString = newUserAgent
                wv.reload()
                android.util.Log.d("CircleToSearch", "Desktop mode changed for $engine - Reloaded")
            }
        }
    }
    
    // Update WebViews when dark mode changes
    LaunchedEffect(isDarkMode) {
        webViews.values.forEach { wv ->
            try {
                // Update WebViewClient for dark mode
                if (isDarkMode) {
                    wv.webViewClient = object : android.webkit.WebViewClient() {
                        override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            val darkModeCSS = """
                                javascript:(function() {
                                    var style = document.createElement('style');
                                    style.innerHTML = `
                                        html { filter: invert(1) hue-rotate(180deg) !important; background: #000 !important; }
                                        img, video, [style*="background-image"] { filter: invert(1) hue-rotate(180deg) !important; }
                                    `;
                                    document.head.appendChild(style);
                                })()
                            """.trimIndent()
                            view?.loadUrl(darkModeCSS)
                        }
                    }
                } else {
                    wv.webViewClient = android.webkit.WebViewClient()
                }
                wv.reload()
                android.util.Log.d("CircleToSearch", "Dark mode changed to: $isDarkMode - Reloaded WebViews")
            } catch (e: Exception) {
                android.util.Log.e("CircleToSearch", "Error updating dark mode", e)
            }
        }
    }
    
    // Bottom Sheet State
    val scaffoldState = androidx.compose.material3.rememberBottomSheetScaffoldState(
        bottomSheetState = androidx.compose.material3.rememberStandardBottomSheetState(
            initialValue = androidx.compose.material3.SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    // Drawing State
    val currentPathPoints = remember { mutableStateListOf<Offset>() }
    
    // Selection State
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var selectionRect by remember { mutableStateOf<Rect?>(null) }

    // Scam detection state. A new circle/selection triggers exactly one backend request.
    var scamResult by remember { mutableStateOf<ScamDetectionApi.AnalyzeResponse?>(null) }
    var scamLoading by remember { mutableStateOf(false) }
    var scamError by remember { mutableStateOf<String?>(null) }
    var scamRequestId by remember { mutableStateOf(0) }
    val selectionAnim = remember { androidx.compose.animation.core.Animatable(0f) }
    
    // Lifecycle reset: When screenshot changes, reset selection and modes
    LaunchedEffect(screenshot) {
        if (screenshot != null) {
            isCopyMode = false
            selectionRect = null
            selectedBitmap = null
            isSearching = false
            scamResult = null
            scamLoading = false
            scamError = null
            currentPathPoints.clear()
            selectionAnim.snapTo(0f)
        }
    }
    
    
    // searchEngines moved to top
    // val searchEngines = SearchEngine.values()

    // Gradient Animation
    val alphaAnim by animateFloatAsState(
        targetValue = if (screenshot != null) 1f else 0f,
        animationSpec = tween(1000), label = "alpha"
    )

    // Auto-scan entire screenshot for QR codes when it arrives
    LaunchedEffect(screenshot) {
        android.util.Log.d("CircleToSearch", "Auto-scan Effect: screenshot=${screenshot != null}")
        if (screenshot != null) {
            val found = withContext(Dispatchers.Default) {
                android.util.Log.d("CircleToSearch", "Auto-scan starting...")
                val res = QrScanner.scanBitmapAll(screenshot!!)
                android.util.Log.d("CircleToSearch", "Auto-scan finished: found ${res.size} codes")
                res
            }
            detectedQrCodes = found
        } else {
            detectedQrCodes = emptyList()
        }
    }

    // Helper to create and configure WebView
    fun createWebView(ctx: android.content.Context, engine: SearchEngine): WebView {
        return WebView(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            
            // Caching & Performance - must be set on WebView directly
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                
                // Performance & UI
                setRenderPriority(WebSettings.RenderPriority.HIGH)
                
                // Caching for Speed
                cacheMode = WebSettings.LOAD_DEFAULT // Was LOAD_CACHE_ELSE_NETWORK - caused refresh issues
                
                // Zoom support
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                
                useWideViewPort = true
                loadWithOverviewMode = true
                
                userAgentString = if (isDesktop(engine)) {
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                } else {
                    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                }
                
                // Dark Mode - Use CSS injection for universal compatibility
                // Algorithmic darkening alone doesn't work on many websites
                android.util.Log.d("CircleToSearch", "Dark mode enabled: $isDarkMode")
            }
            
            // UI Tweaks
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            // Enable Third-Party Cookies
            android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            webViewClient = object : WebViewClient() {
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // Inject dark mode CSS if enabled
                    if (isDarkMode) {
                        val darkModeCSS = """
                            javascript:(function() {
                                var style = document.createElement('style');
                                style.innerHTML = `
                                    html { filter: invert(1) hue-rotate(180deg) !important; background: #000 !important; }
                                    img, video, [style*="background-image"] { filter: invert(1) hue-rotate(180deg) !important; }
                                `;
                                document.head.appendChild(style);
                            })()
                        """.trimIndent()
                        view?.loadUrl(darkModeCSS)
                        android.util.Log.d("CircleToSearch", "Dark mode CSS injected for: $url")
                    }
                }
            }
            isNestedScrollingEnabled = true
            setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        v.parent.requestDisallowInterceptTouchEvent(true)
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        v.parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
                false
            }
        }
    }

    // Back Handler Logic
    BackHandler(enabled = true) {
        if (isCopyMode) {
            isCopyMode = false
            onExitCopyMode()
            return@BackHandler
        }
        val currentWebView = webViews[selectedEngine]
        if (currentWebView != null && currentWebView.canGoBack()) {
            currentWebView.goBack()
        } else if (scaffoldState.bottomSheetState.currentValue == androidx.compose.material3.SheetValue.Expanded) {
             scope.launch { scaffoldState.bottomSheetState.partialExpand() }
        } else if (scaffoldState.bottomSheetState.currentValue == androidx.compose.material3.SheetValue.PartiallyExpanded) {
             scope.launch { scaffoldState.bottomSheetState.hide() }
        } else {
            onClose()
        }
    }

    Surface(
        color = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        androidx.compose.material3.BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = (androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp * 0.55f), // Dynamic 55% peek
            containerColor = Color.Transparent,
            sheetContainerColor = Color.Transparent,
            sheetContentColor = MaterialTheme.colorScheme.onSurface,
            sheetDragHandle = { BottomSheetDefaults.DragHandle() },
            sheetSwipeEnabled = true,
            sheetContent = {
                // Bottom Sheet Content (Results)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(800.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tabs - Polished UI
                ScrollableTabRow(
                    selectedTabIndex = searchEngines.indexOf(selectedEngine),
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    divider = {},
                    indicator = {}
                ) {
                    searchEngines.forEach { engine ->
                        val selected = selectedEngine == engine
                        val transition = androidx.compose.animation.core.updateTransition(targetState = selected, label = "TabSelect")
                        val scale by transition.animateFloat(label = "Scale") { if (it) 1.05f else 1f }
                        val alpha by transition.animateFloat(label = "Alpha") { if (it) 1f else 0.7f }

                        Tab(
                            selected = selected,
                            onClick = { selectedEngine = engine },
                            modifier = Modifier.graphicsLayer { 
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            },
                            text = {
                                Text(
                                    engine.name,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    modifier = Modifier
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .border(
                                             width = 1.dp,
                                             color = if(selected) MaterialTheme.colorScheme.primary.copy(alpha=0.5f) else Color.Transparent,
                                             shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }

                // Reset everything when bitmap changes (new area selected)
                LaunchedEffect(selectedBitmap) {
                    hostedImageUrl = null
                    searchUrl = null
                    preloadedUrls.clear()
                    initializedEngines.clear() // Reset smart loading
                    // Do NOT destroy webviews here to keep them cached if possible? 
                    // PROBABLY safer to destroy to avoid stale state from previous searches.
                    webViews.values.forEach { 
                        it.stopLoading()
                        it.clearHistory()
                        it.clearCache(true)
                        it.loadUrl("about:blank")
                        it.destroy() 
                    }
                    webViews.clear()
                }

                // The original web/image-search flow has been replaced by our scam-detection API.
                // No search engine is contacted and no image is uploaded to a third-party host.
                LaunchedEffect(selectedBitmap, scamRequestId) {
                    val bitmap = selectedBitmap ?: return@LaunchedEffect
                    scamLoading = true
                    scamError = null
                    scamResult = null
                    try {
                        scamResult = ScamDetectionApi.analyze(bitmap, context)
                    } catch (e: Exception) {
                        android.util.Log.e("CircleToSearch", "Scam detection request failed", e)
                        scamError = e.message ?: "Unable to reach the scam-detection backend"
                    } finally {
                        scamLoading = false
                    }
                }

                // Update searchUrl when engine changes
                LaunchedEffect(selectedEngine, preloadedUrls) {
                    if (preloadedUrls.containsKey(selectedEngine)) {
                        searchUrl = preloadedUrls[selectedEngine]
                    }
                }

                // Memory Optimization: REMOVED Aggressive Cleanup
                // User Requirement: "dont refresh tabs when user switch tabs keep them in background"
                // We keep them alive.
                /* 
                LaunchedEffect(selectedEngine) {
                     // ... (Cleanup logic removed)
                }
                */

                Box(modifier = Modifier.fillMaxSize()) {
                    // Show loading only if the SELECTED engine isn't ready or just starting
                    if (isLoading || (preloadedUrls.containsKey(selectedEngine) && !webViews.containsKey(selectedEngine))) {
                         // We delay showing the loader slightly to avoid flicker if WebView attaches instantly
                        var showLoader by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(100)
                            showLoader = true
                        }
                        
                        if (showLoader || isLoading) {
                             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                ContainedLoadingIndicatorSample()
                             }
                        }
                    }

                    // Dynamic Settings Update (User Agent etc) - Now handled at top level
                    // Cleanup on Dispose
                    DisposableEffect(Unit) {
                        onDispose {
                            webViews.values.forEach { 
                                it.stopLoading()
                                it.clearHistory()
                                it.clearCache(true)
                                it.loadUrl("about:blank")
                                it.destroy() 
                            }
                            webViews.clear()
                        }
                    }

                    // Render WebViews
                    searchEngines.forEach { engine ->
                         // Logic: Render if it's in the initialized set (Smart Loading)
                         // This ensures we don't load everything at once, but once loaded, we keep it.
                         if (false && initializedEngines.contains(engine) && preloadedUrls.containsKey(engine)) {
                             val url = preloadedUrls[engine]!!
                             val isSelected = (engine == selectedEngine)
                             
                             androidx.compose.runtime.key(engine) {
                                AndroidView(
                                    factory = { ctx ->
                                        if (webViews.containsKey(engine)) {
                                            // Should not happen with key(), but safety check
                                            val v = webViews[engine]!!
                                            (v.parent as? ViewGroup)?.removeView(v)
                                            
                                            val swipeRefresh = SwipeRefreshLayout(ctx).apply {
                                                layoutParams = ViewGroup.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.MATCH_PARENT
                                                )
                                            }
                                            swipeRefresh.addView(v)
                                            swipeRefresh.setOnRefreshListener {
                                                v.reload()
                                                swipeRefresh.isRefreshing = false
                                            }
                                            swipeRefresh
                                        } else {
                                            val swipeRefresh = SwipeRefreshLayout(ctx).apply {
                                                layoutParams = ViewGroup.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.MATCH_PARENT
                                                )
                                            }
                                            val webView = createWebView(ctx, engine)
                                            // Apply current settings
                                             if (isDesktop(engine)) {
                                                 webView.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                                             }
                                            
                                            webViews[engine] = webView
                                            webView.loadUrl(url)
                                            
                                            swipeRefresh.addView(webView)
                                            swipeRefresh.setOnRefreshListener {
                                                webView.reload()
                                                swipeRefresh.isRefreshing = false
                                            }
                                            swipeRefresh
                                        }
                                    },
                                    update = { swipeRefresh ->
                                        var webView: WebView? = null
                                        for (i in 0 until swipeRefresh.childCount) {
                                            val child = swipeRefresh.getChildAt(i)
                                            if (child is WebView) {
                                                webView = child
                                                break
                                            }
                                        }
                                        
                                        if (webView != null) {
                                            if (webView.url != url && url != webView.originalUrl) {
                                                webView.loadUrl(url)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .zIndex(if (isSelected) 1f else 0f)
                                        .graphicsLayer { 
                                            alpha = if (isSelected) 1f else 0f 
                                        }
                                )
                             }
                        }
                    }
                }
            }
        }
    ) { _ ->
        // Root Box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent) // Changed from Black to Transparent
        ) {
            // Close button for Copy Mode (Top Left)

            // 1. Screenshot Layer
            if (screenshot != null && !isCopyMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // Required for BlendMode.Clear to work in child Canvas
                            compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                        }
                ) {
                    Image(
                        bitmap = screenshot.asImageBitmap(),
                        contentDescription = "Screenshot",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )


                    // Tint Overlay (Punch-out style)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 0f
                        val dimAlpha = 0.15f
                        
                        // 1. Draw global dim
                        drawRect(Color.Black.copy(alpha = dimAlpha))
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = OverlayGradientColors.map { it.copy(alpha = dimAlpha) }
                            )
                        )
                        
                        // 2. Clear selection area if it exists
                        if (selectionRect != null && selectionAnim.value > 0f) {
                            val rect = selectionRect!!
                            val progress = selectionAnim.value
                            val holeRect = androidx.compose.ui.geometry.Rect(
                                rect.left.toFloat(), 
                                rect.top.toFloat(), 
                                rect.right.toFloat(), 
                                rect.bottom.toFloat()
                            )
                            
                            // Punch the hole
                            drawRoundRect(
                                color = Color.Transparent,
                                topLeft = holeRect.topLeft,
                                size = holeRect.size,
                                cornerRadius = CornerRadius(48f),
                                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                            )
                        }
                    }
                }
            }

            // 2. Gradient Border Layer (Overlaying screenshot, clipped to rounded corners)
            if (showGradientBorder && !isCopyMode) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isUIVisible,
                    enter = androidx.compose.animation.fadeIn(animationSpec = tween(700))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 8.dp,
                                brush = Brush.verticalGradient(
                                    colors = OverlayGradientColors.map { it.copy(alpha = 0.5f) }
                                ),
                                shape = RoundedCornerShape(24.dp) // Rounded corners for device
                            )
                            .clip(RoundedCornerShape(24.dp))
                    )
                }
            }

            // 3. Drawing Canvas (Interactive Layer)
            if (!isCopyMode) {                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val rect = selectionRect
                                    if (rect != null && selectionAnim.value == 1f) {
                                        val handleSize = 64f // px for hit testing
                                        val tl = Offset(rect.left.toFloat(), rect.top.toFloat())
                                        val tr = Offset(rect.right.toFloat(), rect.top.toFloat())
                                        val bl = Offset(rect.left.toFloat(), rect.bottom.toFloat())
                                        val br = Offset(rect.right.toFloat(), rect.bottom.toFloat())
                                        
                                        when {
                                            (offset - tl).getDistance() < handleSize -> { isResizing = true; activeHandle = "tl" }
                                            (offset - tr).getDistance() < handleSize -> { isResizing = true; activeHandle = "tr" }
                                            (offset - bl).getDistance() < handleSize -> { isResizing = true; activeHandle = "bl" }
                                            (offset - br).getDistance() < handleSize -> { isResizing = true; activeHandle = "br" }
                                            else -> { isResizing = false; activeHandle = null }
                                        }
                                        
                                        if (isResizing) return@detectDragGestures
                                    }

                                    // Clear previous state if starting new draw
                                    currentPathPoints.clear()
                                    currentPathPoints.add(offset)
                                    selectionRect = null
                                    scope.launch { selectionAnim.snapTo(0f) }
                                },
                                onDrag = { change, _ ->
                                    if (isResizing && activeHandle != null) {
                                        val rect = selectionRect ?: return@detectDragGestures
                                        val pos = change.position
                                        val newRect = android.graphics.Rect(rect)
                                        when (activeHandle) {
                                            "tl" -> { newRect.left = pos.x.toInt(); newRect.top = pos.y.toInt() }
                                            "tr" -> { newRect.right = pos.x.toInt(); newRect.top = pos.y.toInt() }
                                            "bl" -> { newRect.left = pos.x.toInt(); newRect.bottom = pos.y.toInt() }
                                            "br" -> { newRect.right = pos.x.toInt(); newRect.bottom = pos.y.toInt() }
                                        }
                                        // Basic validation (min size)
                                        if (newRect.width() > 20 && newRect.height() > 20) {
                                            selectionRect = newRect
                                        }
                                    } else {
                                        currentPathPoints.add(change.position)
                                    }
                                },
                                onDragEnd = {
                                    if (isResizing) {
                                        isResizing = false
                                        activeHandle = null
                                        // Update cropped bitmap after resize
                                        if (screenshot != null && selectionRect != null) {
                                            selectedBitmap = ImageUtils.cropBitmap(screenshot, selectionRect!!)
                                        }
                                    } else if (currentPathPoints.isNotEmpty()) {
                                        var minX = Float.MAX_VALUE
                                        var minY = Float.MAX_VALUE
                                        var maxX = Float.MIN_VALUE
                                        var maxY = Float.MIN_VALUE
                                        currentPathPoints.forEach { p ->
                                            minX = kotlin.math.min(minX, p.x)
                                            minY = kotlin.math.min(minY, p.y)
                                            maxX = kotlin.math.max(maxX, p.x)
                                            maxY = kotlin.math.max(maxY, p.y)
                                        }

                                        val border = 20
                                        val rect = android.graphics.Rect(
                                            (minX - border).toInt().coerceAtLeast(0),
                                            (minY - border).toInt().coerceAtLeast(0),
                                            (maxX + border).toInt().coerceAtMost(screenshot?.width ?: 0),
                                            (maxY + border).toInt().coerceAtMost(screenshot?.height ?: 0)
                                        )

                                        if (rect.width() > 10 && rect.height() > 10) {
                                            selectionRect = rect
                                            currentPathPoints.clear() // Hide the drawn circle
                                            if (screenshot != null) {
                                                selectedBitmap = ImageUtils.cropBitmap(screenshot!!, rect)
                                            }
                                            scope.launch {
                                                selectionAnim.animateTo(1f, tween(600))
                                                isSearching = true
                                            }
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    // Draw current path (Real-time)
                    if (currentPathPoints.size > 1) {
                        val path = Path().apply {
                            moveTo(currentPathPoints.first().x, currentPathPoints.first().y)
                            for (i in 1 until currentPathPoints.size) {
                                lineTo(currentPathPoints[i].x, currentPathPoints[i].y)
                            }
                        }
                        
                        // Glow
                        drawPath(
                            path = path,
                            brush = Brush.linearGradient(OverlayGradientColors),
                            style = Stroke(width = 30f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                            alpha = 0.6f
                        )
                        // Core
                        drawPath(
                            path = path,
                            color = Color.White,
                            style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }

                    // Draw Lens Animation (Rounded Corner Brackets)
                    if (selectionRect != null && selectionAnim.value > 0f) {
                        val rect = selectionRect!!
                        val progress = selectionAnim.value
                        val left = rect.left.toFloat()
                        val top = rect.top.toFloat()
                        val right = rect.right.toFloat()
                        val bottom = rect.bottom.toFloat()
                        
                        val width = right - left
                        val height = bottom - top
                        val cornerRadius = 48f // Slightly smaller radius
                        val armLength = min(width, height) * 0.15f // Slightly shorter arms

                        // Top Left
                        val tlPath = Path().apply {
                            moveTo(left, top + armLength)
                            lineTo(left, top + cornerRadius)
                            arcTo(
                                rect = androidx.compose.ui.geometry.Rect(left, top, left + 2 * cornerRadius, top + 2 * cornerRadius),
                                startAngleDegrees = 180f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(left + armLength, top)
                        }
                        // Top Right
                        val trPath = Path().apply {
                            moveTo(right - armLength, top)
                            lineTo(right - cornerRadius, top)
                            arcTo(
                                rect = androidx.compose.ui.geometry.Rect(right - 2 * cornerRadius, top, right, top + 2 * cornerRadius),
                                startAngleDegrees = 270f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(right, top + armLength)
                        }
                        // Bottom Right
                        val brPath = Path().apply {
                            moveTo(right, bottom - armLength)
                            lineTo(right, bottom - cornerRadius)
                            arcTo(
                                rect = androidx.compose.ui.geometry.Rect(right - 2 * cornerRadius, bottom - 2 * cornerRadius, right, bottom),
                                startAngleDegrees = 0f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(right - armLength, bottom)
                        }
                        // Bottom Left
                        val blPath = Path().apply {
                            moveTo(left + armLength, bottom)
                            lineTo(left + cornerRadius, bottom)
                            arcTo(
                                rect = androidx.compose.ui.geometry.Rect(left, bottom - 2 * cornerRadius, left + 2 * cornerRadius, bottom),
                                startAngleDegrees = 90f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(left, bottom - armLength)
                        }

                        val bracketAlpha = progress
                        val bracketStroke = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        
                        // Draw Brackets (Solid White)
                        listOf(tlPath, trPath, brPath, blPath).forEach { p ->
                            drawPath(p, Color.White, style = bracketStroke, alpha = bracketAlpha)
                        }
                        
                        // Optional: Flash effect inside
                         drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            cornerRadius = CornerRadius(48f),
                            style = Stroke(width = 4f),
                            alpha = (1f - progress) * 0.5f
                         )
                    }
                }

            }

            // 4. Header (Top)
            androidx.compose.animation.AnimatedVisibility(
                visible = isUIVisible && !isCopyMode && !isEntityExtractMode,
                enter = androidx.compose.animation.slideInVertically(
                    initialOffsetY = { -it }, // Commence au-dessus de l'écran (-100%)
                    animationSpec = tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ),
                modifier = Modifier.align(Alignment.TopCenter).zIndex(2000f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick =
                            {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onClose()
                            },
                        modifier = Modifier
                            .background(Color.Gray.copy(alpha = 0.5f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "ScamShield",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    // Action Button (Menu)
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                            .size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            showMenu = true
                        }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }

                        androidx.compose.material3.DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                            tonalElevation = 6.dp
                        ) {
                            // Keep only controls that are useful for the scam-detection workflow.
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(if (isDarkMode) "Light Mode" else "Dark Mode") },
                                leadingIcon = {
                                    Icon(
                                        if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    isDarkMode = !isDarkMode
                                    showMenu = false
                                }
                            )
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(if (showGradientBorder) "Hide Border" else "Show Border") },
                                leadingIcon = {
                                    Icon(Icons.Default.BorderOuter, contentDescription = null)
                                },
                                onClick = {
                                    showGradientBorder = !showGradientBorder
                                    showMenu = false
                                }
                            )
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("Settings") },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                },
                                onClick = {
                                    showSettingsScreen = true
                                    showMenu = false
                                }
                            )
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("Close") },
                                leadingIcon = {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                },
                                onClick = {
                                    showMenu = false
                                    onClose()
                                }
                            )
                        }
                    }
                }
            }
            // 5. Bottom Bar — Material 3 Expressive two-row card
            // State for Copy Text mode
            var isCopyTextTriggered by remember { mutableStateOf(false) }

            androidx.compose.animation.AnimatedVisibility(
                visible = isUIVisible && !isCopyMode && !isEntityExtractMode,
                enter = slideInVertically(
                    initialOffsetY = { it }, // slides up from below
                    animationSpec = tween(300, easing = androidx.compose.animation.core.CubicBezierEasing(0f, 0f, 0.2f, 1f))
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { it }, // slides back down
                    animationSpec = tween(200, easing = androidx.compose.animation.core.CubicBezierEasing(0.4f, 0f, 1f, 1f))
                ) + fadeOut(animationSpec = tween(200)),
                modifier = Modifier.align(Alignment.BottomCenter).zIndex(2000f)
            ) {
                // ── Outer card container ──────────────────────────────────────────
                androidx.compose.material3.Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 8.dp,
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // ── ROW 1: ScamShield status ─────────────────────────
                        androidx.compose.material3.Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(23.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "ScamShield",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Text(
                                        text = when {
                                            scamLoading -> "Analyzing selected content…"
                                            scamResult != null -> "Analysis complete"
                                            scamError != null -> "Analysis failed — try again"
                                            selectedBitmap != null -> "Ready to analyze selected content"
                                            else -> "Circle something to check it"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (scamLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                                }
                            }
                        }

                        // ── ROW 2: Action buttons ───────────────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 0.dp), // Maximize space for labels
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            @Composable
                            fun BottomBarButton(
                                label: String, 
                                icon: @Composable () -> Unit, 
                                enabled: Boolean = true,
                                onClick: () -> Unit
                            ) {
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        FilledTonalIconButton(
                                            onClick = { 
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                onClick() 
                                            },
                                            enabled = enabled,
                                            modifier = Modifier.size(48.dp),
                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                                contentColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                            )
                                        ) { icon() }
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Text(
                                            text = label, 
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.5.sp, 
                                                lineHeight = 12.sp,
                                                letterSpacing = (-0.4).sp,
                                                fontWeight = FontWeight.Medium
                                            ), 
                                            color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f), 
                                            textAlign = TextAlign.Center, 
                                            maxLines = 1,
                                            overflow = TextOverflow.Visible,
                                            modifier = Modifier.padding(horizontal = 0.dp)
                                        )
                                    }
                                }
                            }

                            // Copy Text
                            BottomBarButton("Copy Text", { Icon(painterResource(id = com.akslabs.circletosearch.R.drawable.ocr), null, modifier = Modifier.size(20.dp)) }) {
                                copyTextManager?.setOcrOnlyMode()
                                isCopyMode = true
                                isCopyTextTriggered = true
                            }

                            // QR Scan
                            BottomBarButton("Scan QR", { Icon(Icons.Default.QrCode, null) }) {
                                qrScanBitmap = selectedBitmap ?: screenshot
                                showQrSheet = true
                            }

                        }
                    }
                }
            }

            // Custom scam-detection result UI. This replaces the old web-search result surface.
            if (selectedBitmap != null && !isCopyMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(3000f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    ScamDetectionPanel(
                        result = scamResult,
                        loading = scamLoading,
                        error = scamError,
                        onRetry = { scamRequestId++ },
                        onClose = {
                            selectedBitmap = null
                            selectionRect = null
                            scamResult = null
                            scamError = null
                            scamLoading = false
                            onClose()
                        }
                    )
                }
            }

            // Phase 44: Smart Entity Extractor Exit Overlay (Full Screen tap capture)
            if (isEntityExtractMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(2550f)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                isEntityExtractMode = false
                            }
                        }
                )
            }

            // Copy Text overlay integration (Activity-based)
            if (isCopyMode && copyTextManager != null) {
                AndroidView(
                    factory = { ctx ->
                        copyTextManager.getOverlayView(onDismiss = {
                            isCopyMode = false
                            onExitCopyMode()
                        })
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(150f)
                )
            }

            // 4. Selection Actions (Share) — Positioned at the very end for absolute top-layer rendering
            if (selectionRect != null && selectionAnim.value == 1f && !isCopyMode) {
                val rect = selectionRect!!
                val density = androidx.compose.ui.platform.LocalDensity.current
                val leftPx = rect.left.toFloat()
                val topPx = rect.top.toFloat()
                val rightPx = rect.right.toFloat()
                val bottomPx = rect.bottom.toFloat()
                
                val leftDp = with(density) { leftPx.toDp() }
                val topDp = with(density) { topPx.toDp() }
                val rightDp = with(density) { rightPx.toDp() }
                val bottomDp = with(density) { bottomPx.toDp() }
                val widthDp = rightDp - leftDp
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(2000f) // Highest Z-index
                ) {
                    val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
                    val screenHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp
                    val centerX = (leftDp + rightDp) / 2
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (centerX - 125.dp).coerceIn(0.dp, screenWidth - 250.dp),
                                y = if (topPx > 200f) (topDp - 72.dp).coerceAtLeast(16.dp) else (bottomDp + 16.dp).coerceAtMost(screenHeight - 80.dp)
                            )
                            .width(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Wrapped in Surface to match Bottom Bar tonal environment
                        androidx.compose.material3.Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = CircleShape,
                            tonalElevation = 4.dp,
                            shadowElevation = 8.dp
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // 1. SHARE BUTTON
                                androidx.compose.material3.FilledTonalButton(
                                    onClick = {
                                        if (selectedBitmap != null) {
                                            scope.launch {
                                                try {
                                                    val fileName = "selection_${java.util.UUID.randomUUID()}.png"
                                                    val path = ImageUtils.saveBitmap(context, selectedBitmap!!, fileName)
                                                    val file = java.io.File(path)
                                                    val uri = androidx.core.content.FileProvider.getUriForFile(context, "com.akslabs.circletosearch.fileprovider", file)
                                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply { 
                                                        type = "image/png"
                                                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                                        clipData = android.content.ClipData.newRawUri("Selection", uri)
                                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Selection").apply { 
                                                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) 
                                                    })
                                                } catch (e: Exception) {
                                                    android.util.Log.e("CircleToSearch", "Failed to share selection", e)
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.height(48.dp),
                                    shape = CircleShape,
                                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color.Transparent, // Parent surface handles color
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    elevation = null,
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp)
                                ) {
                                    Text(
                                        "Share", 
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
                                        )
                                    )
                                }

                                // Separator
                                androidx.compose.material3.VerticalDivider(
                                    modifier = Modifier.height(24.dp).padding(horizontal = 2.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )

                                // 2. SAVE BUTTON
                                androidx.compose.material3.FilledTonalButton(
                                    onClick = {
                                        if (selectedBitmap != null) {
                                            val success = ImageUtils.saveToGallery(context, selectedBitmap!!)
                                            if (success) {
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                android.widget.Toast.makeText(context, "Saved to Gallery", android.widget.Toast.LENGTH_SHORT).show()
                                            } else {
                                                android.widget.Toast.makeText(context, "Failed to save", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.height(48.dp),
                                    shape = CircleShape,
                                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    elevation = null,
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp)
                                ) {
                                    Text(
                                        "Save", 
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

        // --- NEW: QR Overlay Chips (High Layer) ---
        if (screenshot != null && !isCopyMode) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize().zIndex(2500f)) {
                val screenWidth = maxWidth
                val screenHeight = maxHeight
                val bitmapWidth = screenshot.width.toFloat()
                val bitmapHeight = screenshot.height.toFloat()

                android.util.Log.d("CircleToSearch", "Rendering QR Chips: count=${detectedQrCodes.size}")
                detectedQrCodes.forEach { qr ->
                    qr.bounds?.let { bounds ->
                        val chipX = (bounds.centerX() / bitmapWidth) * screenWidth.value
                        val chipY = (bounds.centerY() / bitmapHeight) * screenHeight.value
                        val isUrl = qr.result is QrResult.Url

                        // Using a Box with pointerInput to consume taps and prevent circling
                        Box(
                            modifier = Modifier
                                .offset(x = chipX.dp - 24.dp, y = chipY.dp - 24.dp)
                                .size(48.dp)
                                .shadow(6.dp, CircleShape)
                                .background(Color.White, CircleShape)
                                .border(1.5.dp, if(isUrl) Color(0xFF1A73E8) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                                .pointerInput(qr) {
                                    detectTapGestures {
                                        selectedQrResult = qr
                                        qrScanBitmap = null 
                                        showQrSheet = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.QrCode, 
                                null, 
                                tint = if(isUrl) Color(0xFF1A73E8) else MaterialTheme.colorScheme.primary, 
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        val label = qrResultShortLabel(qr.result)
                        if (label.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .offset(x = chipX.dp + 28.dp, y = chipY.dp - 12.dp)
                                    .pointerInput(qr) {
                                        detectTapGestures {
                                            selectedQrResult = qr
                                            qrScanBitmap = null 
                                            showQrSheet = true
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.95f),
                                tonalElevation = 4.dp,
                                shadowElevation = 4.dp,
                                border = if(isUrl) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1A73E8).copy(alpha = 0.3f)) else null
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if(isUrl) Color(0xFF1A73E8) else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if(isUrl) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Phase 44: Smart Entity Extractor Overlay Chips ---
        if (isEntityExtractMode && screenshot != null && !isCopyMode) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize().zIndex(2600f)) {
                val screenWidth = maxWidth
                val screenHeight = maxHeight
                val bitmapWidth = screenshot.width.toFloat()
                val bitmapHeight = screenshot.height.toFloat()

                if (isExtractingEntities) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            androidx.compose.material3.CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(16.dp))
                            Text("Extracting links & info...", color = Color.White)
                        }
                    }
                }

                detectedEntities.forEach { entity ->
                    val chipX = (entity.bounds.centerX() / bitmapWidth) * screenWidth.value
                    val chipY = (entity.bounds.centerY() / bitmapHeight) * screenHeight.value

                    // Main Chip Icon
                    Box(
                        modifier = Modifier
                            .offset(x = chipX.dp - 24.dp, y = chipY.dp - 24.dp)
                            .size(48.dp)
                            .shadow(6.dp, CircleShape)
                            .background(Color.White, CircleShape)
                            .border(1.5.dp, entity.sourceColor, CircleShape)
                            .clickable {
                                try {
                                    // Auto-copy clicked content to clipboard
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText(entity.typeName, entity.text)
                                    clipboard.setPrimaryClip(clip)

                                    val intent = when(entity) {
                                        is SmartEntity.Url -> android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(if (!entity.text.startsWith("http")) "http://${entity.text}" else entity.text))
                                        is SmartEntity.Email -> android.content.Intent(android.content.Intent.ACTION_SENDTO, android.net.Uri.parse("mailto:${entity.text}"))
                                        is SmartEntity.Phone -> android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${entity.text}"))
                                        is SmartEntity.Upi -> android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("upi://pay?pa=${entity.text}"))
                                    }
                                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                    isEntityExtractMode = false
                                } catch (e: Exception) {}
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(entity.icon, null, tint = entity.sourceColor, modifier = Modifier.size(24.dp))
                    }
                    
                    // Label Chip
                    Surface(
                        modifier = Modifier
                            .offset(x = chipX.dp + 28.dp, y = chipY.dp - 12.dp)
                            .clickable {
                                try {
                                    val intent = when(entity) {
                                        is SmartEntity.Url -> android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(if (!entity.text.startsWith("http")) "http://${entity.text}" else entity.text))
                                        is SmartEntity.Email -> android.content.Intent(android.content.Intent.ACTION_SENDTO, android.net.Uri.parse("mailto:${entity.text}"))
                                        is SmartEntity.Phone -> android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${entity.text}"))
                                        is SmartEntity.Upi -> android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("upi://pay?pa=${entity.text}"))
                                    }
                                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                    isEntityExtractMode = false
                                } catch (e: Exception) {}
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.95f),
                        tonalElevation = 4.dp,
                        shadowElevation = 4.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, entity.sourceColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = entity.text,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = entity.sourceColor,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // QR Code Result Sheet
        if (showQrSheet) {
            // Full-screen invisible overlay to catch "click outside"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2500f)
                    .background(Color.Black.copy(alpha = 0.01f)) // Almost invisible but catches taps
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { 
                        showQrSheet = false
                        selectedQrResult = null
                    }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
                    .zIndex(3000f),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Ensure BackHandler is active when sheet is shown
                androidx.activity.compose.BackHandler {
                    showQrSheet = false
                    selectedQrResult = null
                }

                QrCodeResultSheet(
                    context = context,
                    bitmap = qrScanBitmap,
                    onDismiss = { 
                        showQrSheet = false
                        selectedQrResult = null
                    },
                    initialResults = detectedQrCodes,
                    initialPage = if (selectedQrResult != null) detectedQrCodes.indexOf(selectedQrResult!!) else 0
                )
            }
        }

        if (showSettingsScreen) {
            SettingsScreen(
                uiPreferences = uiPreferences,
                onDismissRequest = { showSettingsScreen = false }
            )
        }
    }
}
}
}

// --- Phase 44: Smart Entity Extractor Models ---
sealed class SmartEntity(val text: String, val bounds: android.graphics.RectF, val typeName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val sourceColor: Color) {
    class Url(text: String, bounds: android.graphics.RectF) : SmartEntity(text, bounds, "Link", Icons.Default.Link, Color(0xFF1A73E8))
    class Email(text: String, bounds: android.graphics.RectF) : SmartEntity(text, bounds, "Email", Icons.Default.Email, Color(0xFF1A73E8))
    class Phone(text: String, bounds: android.graphics.RectF) : SmartEntity(text, bounds, "Phone", Icons.Default.Phone, Color(0xFF43A047))
    class Upi(text: String, bounds: android.graphics.RectF) : SmartEntity(text, bounds, "UPI", Icons.Default.Person, Color(0xFF8E24AA))
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@androidx.compose.ui.tooling.preview.Preview
@androidx.compose.runtime.Composable
fun ContainedLoadingIndicatorSample() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        androidx.compose.material3.ContainedLoadingIndicator()
    }
}
