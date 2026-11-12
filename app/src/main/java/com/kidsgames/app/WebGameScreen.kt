package com.kidsgames.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.webkit.WebViewAssetLoader
import java.io.ByteArrayInputStream

/** An HTML5 game bundled under assets/games/<folder>/index.html. */
data class WebGame(
    val folder: String,
    val label: String,
    val emoji: String,
    val color: Color,
    val landscape: Boolean,
)

val WEB_GAMES = listOf(
    WebGame("carros", "Carros", "🚗", Color(0xFFE53935), landscape = false),
    WebGame("pacman", "Pac-Man", "👻", Color(0xFF3949AB), landscape = true),
    // Built by CI from a pinned upstream commit (see .github/workflows/build.yml).
    WebGame("kart-royale", "Karts", "🏎️", Color(0xFF00897B), landscape = true),
)

private const val ASSET_HOST_URL = "https://appassets.androidplatform.net/assets/games/"

/**
 * Runs a bundled HTML5 game offline. Every request is answered from the app's
 * assets; anything else gets an empty 404, so a game can never reach the network.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebGameScreen(game: WebGame, onHome: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(game) {
        activity?.requestedOrientation = if (game.landscape) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        val controller = activity?.window?.let { window ->
            WindowCompat.getInsetsController(window, window.decorView)
        }
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            controller?.show(WindowInsetsCompat.Type.systemBars())
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val loader = WebViewAssetLoader.Builder()
                    .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(ctx))
                    .build()
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.mediaPlaybackRequiresUserGesture = false
                    setBackgroundColor(android.graphics.Color.BLACK)
                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?,
                        ): WebResourceResponse? {
                            val uri = request?.url ?: return null
                            return loader.shouldInterceptRequest(uri)
                                ?: WebResourceResponse(
                                    "text/plain", "utf-8", 404, "Blocked",
                                    emptyMap(), ByteArrayInputStream(ByteArray(0))
                                )
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?,
                        ): Boolean = true
                    }
                    loadUrl(ASSET_HOST_URL + game.folder + "/index.html")
                }
            },
            onRelease = { it.destroy() }
        )
        Box(
            Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0x99FFFFFF))
                .clickable(onClick = onHome),
            contentAlignment = Alignment.Center
        ) {
            Text("🏠", fontSize = 24.sp)
        }
    }
}
