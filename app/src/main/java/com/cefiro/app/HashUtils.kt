package com.cefiro.app

import java.security.MessageDigest

object HashUtils {
    fun sha256(data: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data)
    }
}