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

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConditionalBlockProcessor {

    // Patrón para detectar etiquetas como ${#if NOMBRE_VARIABLE} o ${/if}
    private static final Pattern IF_START_PATTERN = Pattern.compile("\\$\\{#if\\s+([A-Z0-9_]+)\\}");
    private static final Pattern IF_END_PATTERN = Pattern.compile("\\$\\{/if\\}");

    /**
     * Procesa el documento Word evaluando bloques condicionales basados en el contexto.
     * 
     * @Document doc El documento Apache POI cargado
     * @Map<String, Object> context Mapa con las variables y sus valores (ej. "CON_HIPOTECA" -> true)
     */
    public static void processConditionalBlocks(XWPFDocument doc, Map<String, Object> context) {
        List<XWPFParagraph> paragraphs = doc.getParagraphs();
        
        boolean evaluating = false;
        boolean keepCurrentBlock = true;
        String currentVariable = "";

        // Recorremos los párrafos de forma secuencial
        for (int i = 0; i < paragraphs.size(); i++) {
            XWPFParagraph p = paragraphs.get(i);
            String text = p.getText();

            // 1. Detectar inicio de bloque condicional (${#if VAR})
            Matcher startMatcher = IF_START_PATTERN.matcher(text);
            if (startMatcher.find()) {
                currentVariable = startMatcher.group(1);
                evaluating = true;
                
                // Evaluamos si la condición es verdadera en el contexto
                Object val = context.get(currentVariable);
                keepCurrentBlock = (val instanceof Boolean && (Boolean) val);

                // Remueve la etiqueta visualmente del párrafo
                removeTagFromParagraph(p, startMatcher.group(0));
                continue;
            }

            // 2. Detectar fin de bloque condicional (${/if})
            Matcher endMatcher = IF_END_PATTERN.matcher(text);
            if (endMatcher.find() && evaluating) {
                evaluating = false;
                removeTagFromParagraph(p, endMatcher.group(0));
                continue;
            }

            // 3. Si estamos dentro de un bloque condicional que debe omitirse, borramos el contenido del párrafo
            if (evaluating && !keepCurrentBlock) {
                clearParagraphText(p);
            }
        }
    }

    private static void removeTagFromParagraph(XWPFParagraph paragraph, String tag) {
        for (XWPFRun run : paragraph.getRuns()) {
            String text = run.getText(0);
            if (text != null && text.contains(tag)) {
                run.setText(text.replace(tag, ""), 0);
            }
        }
    }

    private static void clearParagraphText(XWPFParagraph paragraph) {
        // En lugar de remover el párrafo completo (lo que puede corromper tablas o índices de POI),
        // vaciamos sus runs para que no ocupe espacio impreso en el documento final.
        for (XWPFRun run : paragraph.getRuns()) {
            run.setText("", 0);
        }
    }
}