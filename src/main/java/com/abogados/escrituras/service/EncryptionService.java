/*
 * Copyright (C) 2026 Adrian Tabak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.abogados.escrituras.service;

import com.abogados.escrituras.util.EnigmaReload;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class EncryptionService {

    // Token por defecto (Paso de parámetro/semilla para EnigmaReload)
    private static final Long DEFAULT_TOKEN = 987654321L;

    /**
     * Encripta una cadena de texto (ej. JSON sin formato) utilizando EnigmaReload.
     */
    public String encriptarTexto(String jsonTexto, Long token) {
        EnigmaReload enigma = new EnigmaReload(token);
        return enigma.getEncriptado(jsonTexto);
    }

    /**
     * Desencripta una cadena de hexadecimales utilizando EnigmaReload.
     */
    public String desencriptarTexto(String textoEncriptado, Long token) {
        EnigmaReload enigma = new EnigmaReload(token);
        return enigma.getDesencriptado(textoEncriptado);
    }

    /**
     * Lee un archivo .enc, lo desencripta y retorna el contenido JSON des-serializable.
     */
    public String desencriptarArchivo(File archivo, Long token) throws Exception {
        if (!archivo.exists()) return "[]";

        try (RandomAccessFile raf = new RandomAccessFile(archivo, "r")) {
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = raf.readLine()) != null) {
                sb.append(linea);
            }
            if (sb.length() == 0) return "[]";
            
            return desencriptarTexto(sb.toString(), token);
        }
    }

    /**
     * Encripta una cadena JSON y la guarda directamente en el archivo .enc de destino.
     */
    public void guardarEncriptado(File archivo, String jsonTexto, Long token) throws Exception {
        String contenidoEncriptado = encriptarTexto(jsonTexto, token);
        
        // Nos aseguramos de que existan los directorios principales
        if (archivo.getParentFile() != null && !archivo.getParentFile().exists()) {
            archivo.getParentFile().mkdirs();
        }

        try (RandomAccessFile raf = new RandomAccessFile(archivo, "rw")) {
            raf.setLength(0); // Limpia el archivo anterior
            raf.writeBytes(contenidoEncriptado);
        }
    }

    // Sobrecargas usando el TOKEN por defecto del sistema
    public String desencriptarArchivo(File archivo) throws Exception {
        return desencriptarArchivo(archivo, DEFAULT_TOKEN);
    }

    public void guardarEncriptado(File archivo, String jsonTexto) throws Exception {
        guardarEncriptado(archivo, jsonTexto, DEFAULT_TOKEN);
    }
}