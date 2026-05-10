package com.cefiro.app

import android.annotation.SuppressLint
import android.content.pm.PackageManager
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

        try {
            // Usamos clave fija de prueba (debe coincidir con empaquetar.py)
            val clave = "12345678901234567890123456789012".toByteArray(Charsets.UTF_8)

            contenedor = ContenedorCifrado(clave)
            contenedor.cargarDesdeRecurso(resources, R.raw.contenedor)
            Toast.makeText(this, "✅ Contenedor cargado: ${contenedor.listarArchivos().size} archivos", Toast.LENGTH_LONG).show()

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
                        Toast.makeText(this@MainActivity, "Interceptando: $ruta", Toast.LENGTH_SHORT).show()

                        if (contenedor.existe(ruta)) {
                            val datos = contenedor.obtenerArchivo(ruta)
                            if (datos != null) {
                                val mime = when {
                                    ruta.endsWith(".html") -> "text/html"
                                    ruta.endsWith(".css")  -> "text/css"
                                    ruta.endsWith(".js")   -> "application/javascript"
                                    ruta.endsWith(".png")  -> "image/png"
                                    else -> "application/octet-stream"
                                }
                                val headers = mapOf(
                                    "Access-Control-Allow-Origin" to "*",
                                    "Cache-Control" to "no-cache, no-store, must-revalidate"
                                )
                                return WebResourceResponse(mime, "UTF-8", 200, "OK", headers,
                                    ByteArrayInputStream(datos))
                            } else {
                                Toast.makeText(this@MainActivity, "❌ datos nulos para $ruta", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@MainActivity, "❌ No existe: $ruta", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    return super.shouldInterceptRequest(view, request)
                }
            }

            webView.loadUrl("http://localhost/index.html")
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Error general: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun obtenerClave(): ByteArray {
        // Clave fija de prueba
        return "12345678901234567890123456789012".toByteArray(Charsets.UTF_8)
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}