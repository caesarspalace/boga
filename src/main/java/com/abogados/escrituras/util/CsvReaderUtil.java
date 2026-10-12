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
package com.abogados.escrituras.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CsvReaderUtil {

    /**
     * Lee un archivo CSV delimitado por comas (,) o punto y coma (;) 
     * y devuelve una lista de mapas con los datos, donde la clave es el nombre 
     * exacto del encabezado (correspondiente a la variable o condición de la plantilla).
     */
    public static List<Map<String, String>> leerCsvComoRegistros(File archivoCsv) throws IOException {
        List<Map<String, String>> registros = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivoCsv))) {
            String lineaEncabezados = br.readLine();
            if (lineaEncabezados == null) return registros;

            // Detectar si usa coma (,) o punto y coma (;) como delimitador común en LATAM
            String separador = lineaEncabezados.contains(";") ? ";" : ",";
            String[] encabezados = lineaEncabezados.split(separador);

            // Limpiar comillas iniciales/finales de los encabezados si las hubiera
            for (int i = 0; i < encabezados.length; i++) {
                encabezados[i] = encabezados[i].trim().replaceAll("^\"|\"$", "");
            }

            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String[] valores = linea.split(separador, -1);
                Map<String, String> registroFila = new HashMap<>();

                for (int i = 0; i < encabezados.length && i < valores.length; i++) {
                    String valor = valores[i].trim().replaceAll("^\"|\"$", "");
                    registroFila.put(encabezados[i], valor);
                }
                registros.add(registroFila);
            }
        }
        return registros;
    }
}