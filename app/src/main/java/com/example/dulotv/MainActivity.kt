package com.example.dulotv

import android.os.Bundle
import android.webkit.WebChromeClient
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
            settings.mediaPlaybackRequiresUserGesture = false
            settings.setSupportMultipleWindows(false)
            settings.javaScriptCanOpenWindowsAutomatically = false

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
                            // 1. הזרקת CSS להפיכת התפריט לסרגל צדי אנכי בצד שמאל
                            var style = document.createElement('style');
                            style.id = 'custom-tv-styles';
                            style.innerHTML = `
                                div[class*="dock"], div[class*="menu"], nav[class*="dock"] {
                                    position: fixed !important;
                                    left: 15px !important;
                                    top: 50% !important;
                                    bottom: auto !important;
                                    right: auto !important;
                                    transform: translateY(-50%) !important;
                                    flex-direction: column !important;
                                    display: flex !important;
                                    z-index: 999999 !important;
                                    padding: 12px 8px !important;
                                    border-radius: 24px !important;
                                }
                                div[class*="dock"] > *, div[class*="menu"] > *, nav[class*="dock"] > * {
                                    flex-direction: column !important;
                                    margin: 6px 0 !important;
                                }
                                [class*="popover"], [class*="tooltip"], [class*="onboarding"],
                                [class*="toast"], [class*="notice"], [class*="banner"], [role="dialog"] {
                                    display: none !important;
                                }
                            `;
                            if (!document.getElementById('custom-tv-styles')) {
                                document.head.appendChild(style);
                            }

                            // 2. הזרקה רציפה של כפתור החיפוש המקורי בראש התפריט האנכי
                            function injectSearchButton() {
                                var nav = document.querySelector('nav') || 
                                          document.querySelector('div[class*="dock"]') || 
                                          document.querySelector('div[class*="menu"]');
                                
                                if (nav) {
                                    var container = nav.querySelector('div') || nav;
                                    
                                    if (!document.getElementById('custom-tv-search')) {
                                        var searchBtn = document.createElement('button');
                                        searchBtn.id = 'custom-tv-search';
                                        searchBtn.setAttribute('tabindex', '0');
                                        searchBtn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>';
                                        searchBtn.style.cssText = 'background: transparent; border: none; padding: 8px; cursor: pointer; display: flex; align-items: center; justify-content: center; border-radius: 50%; margin-bottom: 4px;';
                                        
                                        searchBtn.onclick = function(e) {
                                            e.preventDefault();
                                            e.stopPropagation();
                                            var siteSearch = document.querySelector('button[aria-label*="search"], a[href*="search"], [class*="search"]');
                                            if (siteSearch && siteSearch !== searchBtn) {
                                                siteSearch.click();
                                            } else {
                                                window.location.href = 'https://dulo.gd/search';
                                            }
                                        };
                                        
                                        container.insertBefore(searchBtn, container.firstChild);
                                    }
                                }
                            }

                            // 3. סגירת הודעות קופצות
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
                                injectSearchButton();
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
    }
}