package com.uhadawnbells.uha.player

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.uhadawnbells.uha.databinding.ActivityPdfViewerBinding
import com.google.firebase.storage.FirebaseStorage

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewerBinding
    private lateinit var errorText: TextView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        errorText = binding.errorText

        val pdfUrl = intent.getStringExtra("pdf_url") ?: run {
            showError("No PDF URL provided")
            return
        }

        setupToolbar()
        setupWebView()
        loadPdf(pdfUrl)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra("title") ?: "PDF Viewer"
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.allowFileAccessFromFileURLs = true
            settings.allowUniversalAccessFromFileURLs = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.domStorageEnabled = true

            webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    showError("PDF loading failed. Please try again later.")
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    binding.progressBar.progress = newProgress
                    binding.progressBar.visibility = if (newProgress == 100) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun loadPdf(pdfUrl: String) {
        if (pdfUrl.startsWith("https://")) {
            // Direct HTTPS URL
            showPdf(pdfUrl)
        } else if (pdfUrl.startsWith("gs://")) {
            // Firebase Storage reference
            try {
                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    showPdf(uri.toString())
                }.addOnFailureListener {
                    showError("PDF unavailable. Please check your connection.")
                }
            } catch (e: Exception) {
                showError("Invalid PDF reference")
            }
        } else {
            showError("Unsupported PDF URL format")
        }
    }

    private fun showPdf(pdfUrl: String) {
        hideError()
        val url = "https://docs.google.com/gview?embedded=true&url=${Uri.encode(pdfUrl)}"
        binding.webView.loadUrl(url)
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

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        binding.webView.destroy()
        super.onDestroy()
    }
}