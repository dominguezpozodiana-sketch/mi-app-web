import os
import sys
import struct
import zlib
import hashlib
from Crypto.Cipher import AES

# --- CONFIGURACIÓN ---
CARPETA_WEB = "mi_web"
SALIDA_RAW = "app/src/main/res/raw/contenedor.cef"
CLAVE_PRUEBA = b"12345678901234567890123456789012"  # 32 bytes para fase 1

def recolectar_archivos(ruta_base):
    archivos = []
    for raiz, dirs, nombres in os.walk(ruta_base):
        for nombre in nombres:
            ruta_completa = os.path.join(raiz, nombre)
            ruta_relativa = os.path.relpath(ruta_completa, ruta_base).replace("\\", "/")
            with open(ruta_completa, "rb") as f:
                datos = f.read()
            archivos.append((ruta_relativa, datos))
    return archivos

def empaquetar():
    print("[1/4] Recolectando archivos de '{}'...".format(CARPETA_WEB))
    if not os.path.exists(CARPETA_WEB):
        print("ERROR: No se encuentra la carpeta '{}'".format(CARPETA_WEB))
        sys.exit(1)
    archivos = recolectar_archivos(CARPETA_WEB)
    print("    Encontrados {} archivos.".format(len(archivos)))

    entradas = {}
    print("[2/4] Comprimiendo y cifrando con AES-256-GCM...")
    for ruta, datos in archivos:
        comprimido = zlib.compress(datos, 9)
        cipher = AES.new(CLAVE_PRUEBA, AES.MODE_GCM)
        cifrado, tag = cipher.encrypt_and_digest(comprimido)
        entradas[ruta] = {
            "nonce": cipher.nonce,
            "tag": tag,
            "datos": cifrado
        }
        print("    {}: {} bytes -> {} bytes (comprimido) -> {} bytes (cifrado)".format(
            ruta, len(datos), len(comprimido), len(cifrado)))

    # Generar cabecera y datos
    print("[3/4] Generando contenedor .cef...")
    with open(SALIDA_RAW, "wb") as f:
        # Magic number
        f.write(b"CEF1")
        # Número de archivos
        f.write(struct.pack(">H", len(entradas)))

        # Calcular offsets y escribir índice
        offset_actual = 0
        bloques = b""
        for ruta, info in entradas.items():
            nonce = info["nonce"]
            tag = info["tag"]
            datos_cif = info["datos"]
            bloque = nonce + tag + datos_cif
            tamano_bloque = len(bloque)

            nombre_bytes = ruta.encode("utf-8")
            f.write(struct.pack(">H", len(nombre_bytes)))
            f.write(nombre_bytes)
            f.write(struct.pack(">I", offset_actual))
            f.write(struct.pack(">I", tamano_bloque))

            bloques += bloque
            offset_actual += tamano_bloque

        # Escribir bloque de datos
        f.write(bloques)

    print("[4/4] Contenedor creado: {} ({} bytes)".format(SALIDA_RAW, os.path.getsize(SALIDA_RAW)))
    print("¡Listo! Ya puedes compilar la app.")

if __name__ == "__main__":
    empaquetar()