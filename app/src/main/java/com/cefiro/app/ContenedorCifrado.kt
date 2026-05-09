package com.cefiro.app

import android.content.res.Resources
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.util.zip.Inflater
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class ContenedorCifrado(private val clave: ByteArray) {

    data class Entrada(val nombre: String, val offset: Int, val tamano: Int)

    private val entradas = mutableMapOf<String, Entrada>()
    private var bloqueDatos = byteArrayOf()

    fun cargarDesdeRecurso(resources: Resources, resId: Int) {
        val rawBytes = resources.openRawResource(resId).readBytes()
        val dis = DataInputStream(ByteArrayInputStream(rawBytes))

        val magic = ByteArray(4)
        dis.readFully(magic)
        if (magic.toString(Charsets.UTF_8) != "CEF1") {
            throw IllegalArgumentException("Formato de contenedor no válido")
        }

        val numArchivos = dis.readShort().toInt() and 0xFFFF
        for (i in 0 until numArchivos) {
            val longNombre = dis.readShort().toInt() and 0xFFFF
            val nombreBytes = ByteArray(longNombre)
            dis.readFully(nombreBytes)
            val nombre = String(nombreBytes, Charsets.UTF_8)
            val offset = dis.readInt()
            val tamano = dis.readInt()
            entradas[nombre] = Entrada(nombre, offset, tamano)
        }
        bloqueDatos = dis.readBytes()
        dis.close()
    }

    fun existe(ruta: String): Boolean = entradas.containsKey(ruta)

    fun obtenerArchivo(ruta: String): ByteArray? {
        val entrada = entradas[ruta] ?: return null
        val bloque = bloqueDatos.copyOfRange(entrada.offset, entrada.offset + entrada.tamano)
        val nonce = bloque.copyOfRange(0, 12)
        val tag = bloque.copyOfRange(12, 28)
        val cifrado = bloque.copyOfRange(28, bloque.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(clave, "AES")
        val gcmSpec = GCMParameterSpec(128, nonce)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)

        val datosConTag = cifrado + tag
        val comprimido = cipher.doFinal(datosConTag)

        val inflater = Inflater()
        inflater.setInput(comprimido)
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            output.write(buffer, 0, count)
        }
        inflater.end()
        return output.toByteArray()
    }

    fun listarArchivos(): List<String> = entradas.keys.toList()
}