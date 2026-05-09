package com.cefiro.app

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayInputStream

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var contenedor: ContenedorCifrado

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val clave = obtenerClave()

        contenedor = ContenedorCifrado(clave)
        contenedor.cargarDesdeRecurso(resources, R.raw.contenedor)

        webView = findViewById(R.id.webview)
        webView.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url?.path?.removePrefix("/") ?: return null
                val ruta = if (url.isEmpty()) "index.html" else url

                if (contenedor.existe(ruta)) {
                    val datos = contenedor.obtenerArchivo(ruta) ?: return null
                    val mime = when {
                        ruta.endsWith(".html") -> "text/html"
                        ruta.endsWith(".css")  -> "text/css"
                        ruta.endsWith(".js")   -> "application/javascript"
                        ruta.endsWith(".png")  -> "image/png"
                        ruta.endsWith(".jpg") || ruta.endsWith(".jpeg") -> "image/jpeg"
                        ruta.endsWith(".svg")  -> "image/svg+xml"
                        ruta.endsWith(".json") -> "application/json"
                        else -> "application/octet-stream"
                    }
                    val headers = mapOf(
                        "Access-Control-Allow-Origin" to "*",
                        "Cache-Control" to "no-cache, no-store, must-revalidate"
                    )
                    return WebResourceResponse(
                        mime, "UTF-8", 200, "OK", headers,
                        ByteArrayInputStream(datos)
                    )
                }
                return super.shouldInterceptRequest(view, request)
            }
        }

        webView.loadUrl("http://localhost/index.html")
    }

    private fun obtenerClave(): ByteArray {
        val pm = packageManager
        val packageName = packageName
        val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_SIGNATURES
        }

        val packageInfo = pm.getPackageInfo(packageName, flags)
        val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            packageInfo.signingInfo?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            packageInfo.signatures
        }

        val certBytes = signatures?.firstOrNull()?.toByteArray()
            ?: throw IllegalStateException("No se pudo obtener la firma")

        return HashUtils.sha256(certBytes)
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}