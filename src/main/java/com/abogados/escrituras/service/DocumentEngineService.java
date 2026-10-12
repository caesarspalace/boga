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

import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentEngineService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([A-ZA-z0-9_]+)\\}");

    /**
     * Escanea un archivo .docx en busca de variables con formato ${MI_VARIABLE}
     */
    public Set<String> extraerVariablesDePlantilla(File plantillaDocx) throws IOException {
        Set<String> variables = new LinkedHashSet<>();
        try (InputStream is = new FileInputStream(plantillaDocx);
             XWPFDocument doc = new XWPFDocument(is)) {

            for (XWPFParagraph p : doc.getParagraphs()) {
                extraerVariablesDeTexto(p.getText(), variables);
            }

            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph p : cell.getParagraphs()) {
                            extraerVariablesDeTexto(p.getText(), variables);
                        }
                    }
                }
            }
        }
        return variables;
    }

    private void extraerVariablesDeTexto(String texto, Set<String> variables) {
        Matcher matcher = VARIABLE_PATTERN.matcher(texto);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
    }

    /**
     * Reemplaza los placeholders en la plantilla por los datos reales y genera la escritura final.
     */
    public void generarEscritura(File plantillaDocx, Map<String, String> valores, File archivoSalida) throws IOException {
        try (InputStream is = new FileInputStream(plantillaDocx);
             XWPFDocument doc = new XWPFDocument(is);
             OutputStream os = new FileOutputStream(archivoSalida)) {

            // Reemplazar en párrafos principales
            for (XWPFParagraph p : doc.getParagraphs()) {
                reemplazarVariablesEnParrafo(p, valores);
            }

            // Reemplazar en tablas (si la escritura contiene cuadros o cláusulas tabuladas)
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph p : cell.getParagraphs()) {
                            reemplazarVariablesEnParrafo(p, valores);
                        }
                    }
                }
            }

            doc.write(os);
        }
    }

    private void reemplazarVariablesEnParrafo(XWPFParagraph paragraph, Map<String, String> valores) {
        String textoParrafo = paragraph.getText();
        if (textoParrafo == null || !textoParrafo.contains("${")) {
            return;
        }

        // Reemplazo respetando estilos de Apache POI Runs
        for (Map.Entry<String, String> entry : valores.entrySet()) {
            String key = "${" + entry.getKey() + "}";
            String val = entry.getValue() != null ? entry.getValue() : "";

            if (textoParrafo.contains(key)) {
                // Reemplazo atómico manteniendo la estructura del párrafo
                List<XWPFRun> runs = paragraph.getRuns();
                if (runs != null && !runs.isEmpty()) {
                    String fullText = paragraph.getParagraphText();
                    fullText = fullText.replace(key, val);

                    // Limpiamos los runs y dejamos el primer run con el texto reemplazado conservando estilo
                    for (int i = runs.size() - 1; i > 0; i--) {
                        paragraph.removeRun(i);
                    }
                    XWPFRun firstRun = runs.get(0);
                    firstRun.setText(fullText, 0);
                    textoParrafo = fullText;
                }
            }
        }
    }
}
