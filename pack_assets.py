import os
import struct
import subprocess
from Crypto.Cipher import AES
from Crypto.Random import get_random_bytes

SRC_DIR = "web_src"            # carpeta con tus archivos originales
OUTPUT_DAT = "web_shield.dat"  # archivo cifrado de salida

def minify_html(content):
    r = subprocess.run(
        ["html-minifier", "--collapse-whitespace", "--remove-comments"],
        input=content,
        capture_output=True,
        text=True
    )
    return r.stdout

def obfuscate_js(content):
    r = subprocess.run(
        ["javascript-obfuscator", "--compact", "true", "--string-array", "true"],
        input=content,
        capture_output=True,
        text=True
    )
    return r.stdout

def minify_css(content):
    r = subprocess.run(
        ["cleancss", "--level", "2"],
        input=content,
        capture_output=True,
        text=True
    )
    return r.stdout

# Recopila y procesa todos los archivos
files = {}
for root, _, filenames in os.walk(SRC_DIR):
    for fn in filenames:
        full_path = os.path.join(root, fn)
        # Ruta relativa dentro de web_src (ej: "css/estilo.css")
        rel_path = os.path.relpath(full_path, SRC_DIR).replace("\\", "/")

        with open(full_path, "r", encoding="utf-8") as f:
            raw_content = f.read()

        ext = os.path.splitext(fn)[1].lower()
        if ext == ".html":
            processed = minify_html(raw_content)
        elif ext == ".js":
            processed = obfuscate_js(raw_content)
        elif ext == ".css":
            processed = minify_css(raw_content)
        else:
            # imágenes u otros binarios se leerían en modo binario, aquí asumimos texto
            processed = raw_content

        files[rel_path] = processed.encode("utf-8")
        print(f"✔ Procesado: {rel_path} ({len(raw_content)} → {len(processed)} bytes)")

# Empaquetado binario en memoria
buf = bytearray()
# Escribir número total de archivos (4 bytes, big-endian)
buf.extend(struct.pack(">I", len(files)))

for name, data in files.items():
    name_bytes = name.encode("utf-8")
    # Longitud del nombre (1 byte)
    buf.extend(struct.pack(">B", len(name_bytes)))
    # Nombre en UTF-8
    buf.extend(name_bytes)
    # Longitud del contenido (4 bytes)
    buf.extend(struct.pack(">I", len(data)))
    # Contenido
    buf.extend(data)

# Cifrar el paquete binario con AES-256-GCM
master_key = get_random_bytes(32)   # clave de 256 bits aleatoria
nonce = get_random_bytes(12)        # número usado una vez
cipher = AES.new(master_key, AES.MODE_GCM, nonce=nonce)
ciphertext, tag = cipher.encrypt_and_digest(buf)

# Guardar todo en el archivo .dat (nonce + tag + ciphertext)
with open(OUTPUT_DAT, "wb") as f:
    f.write(nonce + tag + ciphertext)

print("\n✅ Archivo cifrado creado:", OUTPUT_DAT)
print("🔑 Clave maestra (copia esta línea completa):")
print(master_key.hex())