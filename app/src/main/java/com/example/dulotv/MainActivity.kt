package com.example.dulotv

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            
            // חסימת פתיחת חלונות חדשים ופופאפים אוטומטיים
            settings.setSupportMultipleWindows(false)
            settings.javaScriptCanOpenWindowsAutomatically = false

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return false
                    
                    // חסימת פרוטוקולים לא מוכרים (כמו intent://) וכתובות שאינן web רגיל
                    if (url.startsWith("intent://") || !url.startsWith("http")) {
                        return true // חסימת הקישור והשארת המשתמש במסך הנוכחי
                    }
                    
                    return false // אישור טעינה לקישורים תקינים
                }
            }
        }
        
        setContentView(webView)
        webView.loadUrl("https://dulo.gd")
    }
}