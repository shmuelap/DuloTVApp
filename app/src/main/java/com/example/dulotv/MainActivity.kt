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
                            // 1. הסתרת התפריט העליון של הדפדפן/מחשב למניעת כפילות
                            var style = document.getElementById('tv-override-style');
                            if (!style) {
                                style = document.createElement('style');
                                style.id = 'tv-override-style';
                                document.head.appendChild(style);
                            }
                            style.innerHTML = `
                                header, [class*="header"], [class*="top-nav"], [class*="TopNav"] {
                                    display: none !important;
                                }
                                [class*="popover"], [class*="tooltip"], [class*="onboarding"],
                                [class*="toast"], [class*="notice"], [class*="banner"], [role="dialog"] {
                                    display: none !important;
                                }
                            `;

                            // 2. איתור תפריט האייקונים והפיכתו לסרגל צדי אנכי + הזרקת חיפוש
                            function enforceLeftSidebarAndSearch() {
                                var allNavs = document.querySelectorAll('div, nav, footer');
                                var targetNav = null;

                                for (var i = 0; i < allNavs.length; i++) {
                                    var el = allNavs[i];
                                    var className = (el.className || '').toString().toLowerCase();
                                    if (className.includes('dock') || className.includes('bottom') || className.includes('menu') || className.includes('nav')) {
                                        if (el.children.length >= 3 && el.children.length <= 10) {
                                            targetNav = el;
                                            break;
                                        }
                                    }
                                }

                                if (targetNav) {
                                    // הכרחת עיצוב סרגל צד אנכי בצד שמאל
                                    targetNav.style.position = 'fixed';
                                    targetNav.style.left = '20px';
                                    targetNav.style.top = '50%';
                                    targetNav.style.bottom = 'auto';
                                    targetNav.style.right = 'auto';
                                    targetNav.style.transform = 'translateY(-50%)';
                                    targetNav.style.flexDirection = 'column';
                                    targetNav.style.display = 'flex';
                                    targetNav.style.zIndex = '999999';
                                    targetNav.style.background = 'rgba(15, 23, 42, 0.95)';
                                    targetNav.style.padding = '14px 10px';
                                    targetNav.style.borderRadius = '24px';
                                    targetNav.style.border = '1px solid rgba(255, 255, 255, 0.15)';

                                    // סידור פריטי התפריט בטור
                                    for (var j = 0; j < targetNav.children.length; j++) {
                                        targetNav.children[j].style.margin = '8px 0';
                                        targetNav.children[j].style.flexDirection = 'column';
                                    }

                                    // הזרקת כפתור החיפוש בראש הרשימה
                                    if (!document.getElementById('tv-search-btn')) {
                                        var searchBtn = document.createElement('a');
                                        searchBtn.id = 'tv-search-btn';
                                        searchBtn.href = 'https://dulo.gd/search';
                                        searchBtn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#38BDF8" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>';
                                        searchBtn.style.cssText = 'display: flex; align-items: center; justify-content: center; padding: 10px; cursor: pointer; border-radius: 50%; background: rgba(255,255,255,0.1); margin-bottom: 8px; text-decoration: none;';
                                        
                                        searchBtn.onclick = function(e) {
                                            e.preventDefault();
                                            window.location.href = 'https://dulo.gd/search';
                                        };

                                        targetNav.insertBefore(searchBtn, targetNav.firstChild);
                                    }
                                }
                            }

                            // 3. לכידת כפתור Enter בשלט לעצירת/הפעלת סרט
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

                            // 4. סגירת הודעות קופצות
                            function safeDismiss() {
                                var buttons = document.querySelectorAll('button, a, div[role="button"]');
                                buttons.forEach(function(btn) {
                                    var txt = (btn.innerText || '').trim().toLowerCase();
                                    if (txt === 'got it' || txt === 'skip' || txt === 'close' || txt === 'dismiss' || txt === '✕' || txt === 'x') {
                                        btn.click();
                                    }
                                });
                            }

                            // הרצה אקטיבית כל 300 מילי-שניות לדריסת שינויי React
                            setInterval(function() {
                                enforceLeftSidebarAndSearch();
                                safeDismiss();
                            }, 300);
                        })();
                    """.trimIndent()

                    view?.evaluateJavascript(injectScript, null)
                }
            }
        }
        
        setContentView(webView)
        webView.loadUrl("https://dulo.gd")

        // מנגנון ניווט אחורה בשלט
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