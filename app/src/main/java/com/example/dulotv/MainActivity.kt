package com.example.dulotv

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.mediaPlaybackRequiresUserGesture = false
            settings.setSupportMultipleWindows(false)
            settings.javaScriptCanOpenWindowsAutomatically = false
            
            isFocusable = true
            isFocusableInTouchMode = true

            webChromeClient = WebChromeClient()

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return false
                    if (url.startsWith("intent://") || (!url.startsWith("http://") && !url.startsWith("https://"))) {
                        return true
                    }
                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    val injectScript = """
                        (function() {
                            // 1. הזרקת עיצוב CSS: הסתרת תפריט עליון והעברת תפריט האייקונים לצד שמאל
                            var style = document.getElementById('tv-override-style');
                            if (!style) {
                                style = document.createElement('style');
                                style.id = 'tv-override-style';
                                document.head.appendChild(style);
                            }
                            style.innerHTML = `
                                /* הסתרת התפריט העליון של הדפדפן/מחשב למניעת כפילות */
                                header, div[class*="header"], div[class*="top-nav"] {
                                    display: none !important;
                                }

                                /* הפיכת תפריט האייקונים לסרגל אנכי בצד שמאל */
                                div[class*="dock"], nav, div[class*="bottom-nav"] {
                                    position: fixed !important;
                                    left: 15px !important;
                                    top: 50% !important;
                                    bottom: auto !important;
                                    right: auto !important;
                                    transform: translateY(-50%) !important;
                                    flex-direction: column !important;
                                    display: flex !important;
                                    z-index: 999999 !important;
                                    background: rgba(15, 23, 42, 0.95) !important;
                                    padding: 12px 8px !important;
                                    border-radius: 20px !important;
                                    border: 1px solid rgba(255, 255, 255, 0.1) !important;
                                }

                                /* סידור האייקונים בטור אנכי */
                                div[class*="dock"] > *, nav > * {
                                    flex-direction: column !important;
                                    margin: 6px 0 !important;
                                }

                                /* הסתרת פופאפים ובאנרים קופצים */
                                [class*="popover"], [class*="tooltip"], [class*="onboarding"],
                                [class*="toast"], [class*="notice"], [class*="banner"], [role="dialog"] {
                                    display: none !important;
                                }
                            `;

                            // 2. הזרקת כפתור חיפוש (🔍) בראש תפריט האייקונים
                            function addSearchIcon() {
                                var dock = document.querySelector('div[class*="dock"]') || document.querySelector('nav');
                                if (dock && !document.getElementById('tv-search-icon')) {
                                    var searchBtn = document.createElement('a');
                                    searchBtn.id = 'tv-search-icon';
                                    searchBtn.href = '/search';
                                    searchBtn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>';
                                    searchBtn.style.cssText = 'display: flex; align-items: center; justify-content: center; padding: 8px; cursor: pointer; border-radius: 50%; margin-bottom: 4px;';
                                    
                                    searchBtn.onclick = function(e) {
                                        e.preventDefault();
                                        window.location.href = 'https://dulo.gd/search';
                                    };

                                    dock.insertBefore(searchBtn, dock.firstChild);
                                }
                            }

                            // 3. לכידת מקש Enter/OK בשלט לניגון ועצירת וידאו
                            document.addEventListener('keydown', function(e) {
                                if (e.keyCode === 13 || e.keyCode === 179) {
                                    var video = document.querySelector('video');
                                    if (video) {
                                        var active = document.activeElement;
                                        var isInteractive = active && (active.tagName === 'BUTTON' || active.tagName === 'A' || active.tagName === 'INPUT');
                                        
                                        if (!isInteractive) {
                                            e.preventDefault();
                                            if (video.paused) {
                                                video.play();
                                            } else {
                                                video.pause();
                                            }
                                        }
                                    }
                                }
                            });

                            // 4. העלמת הודעות קופצות
                            function safeDismiss() {
                                var buttons = document.querySelectorAll('button, a, div[role="button"]');
                                buttons.forEach(function(btn) {
                                    var txt = (btn.innerText || '').trim().toLowerCase();
                                    if (txt === 'got it' || txt === 'skip' || txt === 'close' || txt === 'dismiss' || txt === '✕' || txt === 'x') {
                                        btn.click();
                                    }
                                });
                            }

                            setInterval(function() {
                                addSearchIcon();
                                safeDismiss();
                            }, 400);
                        })();
                    """.trimIndent()

                    view?.evaluateJavascript(injectScript, null)
                }
            }
        }
        
        setContentView(webView)
        webView.loadUrl("https://dulo.gd")

        // מנגנון כפתור 'חזור' בשלט
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }
}