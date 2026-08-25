package com.example.dulotv

import android.os.Bundle
import android.view.KeyEvent
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
            
            // הגדרות חשובות לניווט עם שלט D-Pad
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
                            // 1. הזרקת CSS ממוקד עבור התפריט
                            var style = document.createElement('style');
                            style.id = 'tv-custom-css';
                            style.innerHTML = `
                                #tv-sidebar {
                                    position: fixed !important;
                                    left: 15px !important;
                                    top: 50% !important;
                                    bottom: auto !important;
                                    right: auto !important;
                                    transform: translateY(-50%) !important;
                                    flex-direction: column !important;
                                    width: auto !important;
                                    height: auto !important;
                                    padding: 15px 10px !important;
                                    border-radius: 20px !important;
                                    background: rgba(15, 23, 42, 0.95) !important;
                                    z-index: 999999 !important;
                                    display: flex !important;
                                }
                                #tv-sidebar > * {
                                    margin: 10px 0 !important;
                                }
                                [class*="popover"], [class*="tooltip"], [class*="onboarding"],
                                [class*="toast"], [class*="notice"], [class*="banner"], [role="dialog"] {
                                    display: none !important;
                                }
                            `;
                            if (!document.getElementById('tv-custom-css')) {
                                document.head.appendChild(style);
                            }

                            // 2. איתור דינמי של התפריט התחתון והפיכתו לסרגל צד + חיפוש
                            function setupSidebar() {
                                var elements = document.querySelectorAll('div, nav');
                                elements.forEach(function(el) {
                                    var compStyle = window.getComputedStyle(el);
                                    // מזהה אלמנטים שמקובעים לתחתית המסך
                                    if (compStyle.position === 'fixed' && compStyle.bottom === '0px' && el.childElementCount >= 4) {
                                        el.id = 'tv-sidebar';
                                        
                                        if (!document.getElementById('custom-tv-search')) {
                                            var searchBtn = document.createElement('button');
                                            searchBtn.id = 'custom-tv-search';
                                            searchBtn.innerHTML = '🔍';
                                            searchBtn.style.cssText = 'background: transparent; border: none; font-size: 24px; padding: 8px; cursor: pointer; color: white; display: flex; justify-content: center;';
                                            
                                            searchBtn.onclick = function(e) {
                                                e.preventDefault();
                                                window.location.href = 'https://dulo.gd/search';
                                            };
                                            el.insertBefore(searchBtn, el.firstChild);
                                        }
                                    }
                                });
                            }

                            // 3. לכידת כפתור Enter בשלט להפעלת סרטים
                            document.addEventListener('keydown', function(e) {
                                // 13 זה כפתור ה-Enter/Center בשלט
                                if (e.keyCode === 13 || e.keyCode === 179) {
                                    var video = document.querySelector('video');
                                    if (video) {
                                        // מוודא שאנחנו לא עומדים על כפתור אחר כרגע
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

                            // 4. העלמת פופאפים בטוחה
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
                                setupSidebar();
                                safeDismiss();
                            }, 500);
                        })();
                    """.trimIndent()

                    view?.evaluateJavascript(injectScript, null)
                }
            }
        }
        
        setContentView(webView)
        webView.loadUrl("https://dulo.gd")

        // מנגנון ניהול כפתור ה'חזור' (Back Button)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack() // חוזר דף אחד אחורה באתר
                } else {
                    finish() // סוגר את האפליקציה אם אנחנו בדף הראשי
                }
            }
        })
    }
}