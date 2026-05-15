package com.cefiro.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayInputStream

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var contenedor: ContenedorCifrado

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Clave fija IDÉNTICA a la de empaquetar.py
        val clave = "12345678901234567890123456789012".toByteArray(Charsets.UTF_8)

        contenedor = ContenedorCifrado(clave)
        try {
            contenedor.cargarDesdeRecurso(resources, R.raw.contenedor)
            val archivos = contenedor.listarArchivos()
            Toast.makeText(this, "✅ Contenedor cargado: ${archivos.size} archivos\n${archivos.joinToString()}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Error al cargar contenedor: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

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
                try {
                    val url = request?.url?.path?.removePrefix("/") ?: return null
                    val ruta = if (url.isEmpty()) "index.html" else url

                    if (contenedor.existe(ruta)) {
                        val datos = contenedor.obtenerArchivo(ruta)
                        if (datos != null) {
                            val mime = when {
                                ruta.endsWith(".html") -> "text/html"
                                ruta.endsWith(".css")  -> "text/css"
                                ruta.endsWith(".js")   -> "application/javascript"
                                ruta.endsWith(".png")  -> "image/png"
                                ruta.endsWith(".jpg") || ruta.endsWith(".jpeg") -> "image/jpeg"
                                ruta.endsWith(".svg")  -> "image/svg+xml"
                                else -> "application/octet-stream"
                            }
                            return WebResourceResponse(
                                mime, "UTF-8", 200, "OK",
                                mapOf("Cache-Control" to "no-cache"),
                                ByteArrayInputStream(datos)
                            )
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }
        }

        // Cargar la URL que será interceptada
        webView.loadUrl("http://localhost/index.html")
    }
}