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
            settings.setSupportMultipleWindows(false)
            settings.javaScriptCanOpenWindowsAutomatically = false

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return false
                    
                    if (url.startsWith("intent://") || !url.startsWith("http")) {
                        return true
                    }
                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    val injectScript = """
                        (function() {
                            // 1. הזרקת CSS להסטת הניווט לשמאל והעלמת באנרים/חלוניות הודעה
                            var style = document.createElement('style');
                            style.innerHTML = `
                                div[class*="dock"], div[class*="menu"], nav, footer {
                                    left: 20px !important;
                                    right: auto !important;
                                    transform: none !important;
                                }
                                /* הסתרת דיאלוגים, באנרים ופופאפים של האתר והנגן */
                                [class*="popover"], [class*="tooltip"], [class*="onboarding"],
                                [class*="toast"], [class*="notice"], [class*="banner"], [role="dialog"] {
                                    display: none !important;
                                }
                            `;
                            document.head.appendChild(style);

                            // 2. מנגנון זיהוי והסרה אקטיבי לחלוניות הודעה ("Got it", "VPN", "Skip")
                            function dismissOverlays() {
                                // לחיצה אוטומטית על כפתורי אישור/סגירה
                                var buttons = document.querySelectorAll('button, a, div[role="button"]');
                                buttons.forEach(function(btn) {
                                    var txt = (btn.innerText || '').trim().toLowerCase();
                                    if (txt === 'got it' || txt === 'skip' || txt === 'close' || txt === 'dismiss' || txt === '✕' || txt === 'x') {
                                        btn.click();
                                    }
                                });

                                // הסרת אלמנטים המכילים את טקסט ההודעה
                                var elements = document.querySelectorAll('div, section, article');
                                elements.forEach(function(el) {
                                    var text = el.innerText || '';
                                    if (text.includes('VPN is no longer needed') || 
                                        text.includes('WE\'VE UPGRADED') || 
                                        text.includes('Tap, pause') || 
                                        text.includes('reveal controls')) {
                                        if (el.children.length < 15) {
                                            el.style.display = 'none';
                                            el.remove();
                                        }
                                    }
                                });
                            }

                            // הרצה מחזורית לניקוי חלונות שקופצים בדיליי לאחר טעינת הדף
                            setInterval(dismissOverlays, 400);

                            // 3. הוספת כפתור חיפוש לתפריט
                            setTimeout(function() {
                                var navBar = document.querySelector('nav') || 
                                             document.querySelector('div[class*="dock"]') || 
                                             document.querySelector('div[class*="menu"]');
                                             
                                if (navBar && !document.getElementById('custom-tv-search')) {
                                    var searchBtn = document.createElement('button');
                                    searchBtn.id = 'custom-tv-search';
                                    searchBtn.innerHTML = '🔍';
                                    searchBtn.style.cssText = 'background: transparent; border: none; color: white; font-size: 22px; padding: 8px 12px; cursor: pointer; display: inline-flex; align-items: center; justify-content: center;';
                                    
                                    searchBtn.onclick = function() {
                                        var existingSearch = document.querySelector('button[aria-label*="search"], [class*="search"], a[href*="search"]');
                                        if (existingSearch) {
                                            existingSearch.click();
                                        } else {
                                            window.location.href = 'https://dulo.gd/search';
                                        }
                                    };
                                    
                                    navBar.insertBefore(searchBtn, navBar.firstChild);
                                }
                            }, 1200);
                        })();
                    """.trimIndent()

                    view?.evaluateJavascript(injectScript, null)
                }
            }
        }
        
        setContentView(webView)
        webView.loadUrl("https://dulo.gd")
    }
}