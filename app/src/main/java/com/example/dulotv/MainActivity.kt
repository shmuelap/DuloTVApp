package com.example.dulotv

import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var myWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        myWebView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.setSupportMultipleWindows(false)
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    injectAdBlocker()
                }
            }
        }

        setContentView(myWebView)
        myWebView.loadUrl("https://dulo.gd")
    }

    private fun injectAdBlocker() {
        val jsScript = """
            window.open = function() { return null; };
            setInterval(function() {
                let ads = document.querySelectorAll('iframe[src*="ads"], div[class*="ad"], div[id*="pop"]');
                ads.forEach(ad => ad.remove());
                let video = document.querySelector('video');
                if (video) {
                    video.style.width = '100vw';
                    video.style.height = '100vh';
                }
            }, 1000);
        """.trimIndent()
        myWebView.evaluateJavascript(jsScript, null)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val videoJs = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                "let v = document.querySelector('video'); if (v) { v.paused ? v.play() : v.pause(); }"
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                "let v = document.querySelector('video'); if (v) { v.currentTime += 10; }"
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                "let v = document.querySelector('video'); if (v) { v.currentTime -= 10; }"
            }
            else -> null
        }

        return if (videoJs != null) {
            myWebView.evaluateJavascript(videoJs, null)
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }
}