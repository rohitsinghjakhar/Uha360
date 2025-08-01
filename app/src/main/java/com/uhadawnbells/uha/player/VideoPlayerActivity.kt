package com.uhadawnbells.uha.player

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.uhadawnbells.uha.databinding.ActivityVideoPlayerBinding
import com.google.firebase.storage.FirebaseStorage

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayerBinding
    private var isVideoFullscreen = false
    private var videoViewContainer: FrameLayout? = null
    private lateinit var errorText: TextView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        errorText = binding.errorText

        val videoUrl = intent.getStringExtra("video_url") ?: run {
            showError("No video URL provided")
            return
        }

        setupToolbar()
        setupWebView(videoUrl)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra("title") ?: "Video Player"
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(videoUrl: String) {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true

            webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    showError("Video loading failed. Please try again later.")
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    binding.progressBar.progress = newProgress
                    binding.progressBar.visibility = if (newProgress == 100) View.GONE else View.VISIBLE
                }

                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    if (view is FrameLayout) {
                        videoViewContainer = view
                        binding.fullscreenView.addView(videoViewContainer)
                        binding.fullscreenView.visibility = View.VISIBLE
                        binding.webView.visibility = View.GONE
                        isVideoFullscreen = true
                    }
                }

                override fun onHideCustomView() {
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

            if (videoUrl.startsWith("https://")) {
                // Direct HTTPS URL
                loadVideo(videoUrl)
            } else if (videoUrl.startsWith("gs://")) {
                // Firebase Storage reference
                try {
                    val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl)
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        loadVideo(uri.toString())
                    }.addOnFailureListener {
                        showError("Video unavailable. Please check your connection.")
                    }
                } catch (e: Exception) {
                    showError("Invalid video reference")
                }
            } else {
                showError("Unsupported video URL format")
            }
        }
    }

    private fun loadVideo(videoUrl: String) {
        hideError()
        val videoHtml = """
            <html>
                <head>
                    <style>
                        body { margin: 0; padding: 0; background-color: black; }
                        video { width: 100%; height: 100%; }
                    </style>
                </head>
                <body>
                    <video controls autoplay>
                        <source src="$videoUrl" type="video/mp4">
                        Your browser does not support the video tag.
                    </video>
                </body>
            </html>
        """.trimIndent()
        binding.webView.loadDataWithBaseURL(null, videoHtml, "text/html", "UTF-8", null)
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        errorText.text = message
        errorText.visibility = View.VISIBLE
        binding.webView.visibility = View.GONE
    }

    private fun hideError() {
        errorText.visibility = View.GONE
        binding.webView.visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBackPressed() {
        if (isVideoFullscreen) {
            binding.webView.webChromeClient?.onHideCustomView()
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