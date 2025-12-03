package com.example.inzynierkaallegroolx.ui.components

import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError

@Composable
fun AllegroLoginDialog(
    authUrl: String,
    onDismiss: () -> Unit,
    onCodeCaught: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true

                        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        //wyłączenie cache pomaga na bład: No such file
                        cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
                    }

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val url = request?.url.toString()

                            //sprawdzamy, czy URL zawiera nasz endpoint callbacka
                            if (url != null && url.contains("/integrations/oauth/allegro/callback")) {
                                val uri = Uri.parse(url)
                                val code = uri.getQueryParameter("code")
                                if (code != null) {
                                    onCodeCaught(code)
                                    return true
                                }
                            }
                            return false
                        }
                        //pozwala ładować stronę mimo legit no scam certyfikatu Ngroka
                        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                            handler?.proceed()
                        }
                        //sprawdzanie bledyów
                        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                            super.onReceivedError(view, request, error)
                            android.util.Log.e("AllegroWebView", "BŁĄD ŁADOWANIA: ${error?.description}")
                        }

                    }
                    loadUrl(authUrl)
                }
            }
        )
    }
}