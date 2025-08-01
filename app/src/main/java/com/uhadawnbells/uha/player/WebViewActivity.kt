package com.uhadawnbells.uha.player

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.uhadawnbells.uha.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private var isVideoFullscreen = false
    private var videoViewContainer: FrameLayout? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra("url") ?: run {
            finish()
            return
        }

        val title = intent.getStringExtra("title") ?: "Content Viewer"
        val isVideo = intent.getBooleanExtra("is_video", false)

        setupToolbar(title)
        setupWebView(url, isVideo)
    }

    private fun setupToolbar(title: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = title
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(url: String, isVideo: Boolean) {
        binding.progressBar.visibility = View.VISIBLE

        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.setSupportZoom(!isVideo) // Disable zoom for videos
            settings.builtInZoomControls = !isVideo
            settings.displayZoomControls = false
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.mediaPlaybackRequiresUserGesture = false

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(url)
                    return false
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    binding.progressBar.visibility = View.GONE
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    binding.progressBar.progress = newProgress
                    if (newProgress == 100) {
                        binding.progressBar.visibility = View.GONE
                    }
                }

                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    if (view is FrameLayout) {
                        // Enter fullscreen for video
                        videoViewContainer = view
                        binding.fullscreenView.addView(videoViewContainer)
                        binding.fullscreenView.visibility = View.VISIBLE
                        binding.webView.visibility = View.GONE
                        isVideoFullscreen = true
                    }
                }

                override fun onHideCustomView() {
                    // Exit fullscreen for video
                    if (isVideoFullscreen) {
                        binding.fullscreenView.visibility = View.GONE
                        binding.webView.visibility = View.VISIBLE
                        videoViewContainer?.let {
                            binding.fullscreenView.removeView(it)
                        }
                        videoViewContainer = null
                        isVideoFullscreen = false
                    }
                }
            }

            // Load content based on type
            if (url.endsWith(".pdf")) {
                // Use Google Docs viewer for PDFs
                loadUrl("https://docs.google.com/gview?embedded=true&url=$url")
            } else {
                // For videos, use HTML5 video tag
                if (isVideo) {
                    val videoHtml = """
                        <html>
                            <body style="margin:0;padding:0;">
                                <video width="100%" height="100%" controls autoplay>
                                    <source src="$url" type="video/mp4">
                                    Your browser does not support the video tag.
                                </video>
                            </body>
                        </html>
                    """.trimIndent()
                    loadDataWithBaseURL(null, videoHtml, "text/html", "UTF-8", null)
                } else {
                    // Regular web content
                    loadUrl(url)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBackPressed() {
        if (isVideoFullscreen) {
            binding.webView.webChromeClient?.onHideCustomView()
        } else if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        binding.webView.destroy()
        super.onDestroy()
    }
}