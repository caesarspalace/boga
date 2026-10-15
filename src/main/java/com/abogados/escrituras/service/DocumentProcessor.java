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

import com.abogados.escrituras.util.TextTransformer; // <-- Importamos el transformador
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentProcessor {

    // MODIFICADO: Soporta variables simples ${VARIABLE} y variables con modificador ${VARIABLE:modificador}
    private static final Pattern PATRON_VARIABLE = Pattern.compile("\\$\\{([A-Za-z0-9_]+)(?::([A-Za-z0-9_]+))?\\}");
    
    // Patrones para bloques condicionales ${#if VAR} y ${/if}
    private static final Pattern PATRON_IF_START = Pattern.compile("\\$\\{#if\\s+([A-Z0-9_]+)\\}");
    private static final Pattern PATRON_IF_END = Pattern.compile("\\$\\{/if\\}");
    private static final Pattern PATRON_IF_EXTRACT = Pattern.compile("\\$\\{#if\\s+([A-Z0-9_]+)\\}");
    
    /**
     * Extrae todas las variables únicas (tanto normales como condicionales) del archivo .docx
     */
    public Set<String> extraerVariables(File archivoDocx) {
        Set<String> variables = new LinkedHashSet<>();

        try (FileInputStream fis = new FileInputStream(archivoDocx);
             XWPFDocument document = new XWPFDocument(fis)) {

            for (XWPFParagraph p : document.getParagraphs()) {
                extraerPatrones(p.getText(), variables);
                extraerPatronesIf(p.getText(), variables);
            }

            for (XWPFTable t : document.getTables()) {
                for (XWPFTableRow r : t.getRows()) {
                    for (XWPFTableCell c : r.getTableCells()) {
                        for (XWPFParagraph p : c.getParagraphs()) {
                            extraerPatrones(p.getText(), variables);
                            extraerPatronesIf(p.getText(), variables);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error al extraer variables de la plantilla: " + e.getMessage());
        }

        return variables;
    }

    private void extraerPatrones(String texto, Set<String> destino) {
        if (texto == null) return;
        Matcher matcher = PATRON_VARIABLE.matcher(texto);
        while (matcher.find()) {
            destino.add(matcher.group(1)); // Grupo 1 es el nombre de la variable limpia
        }
    }

    private void extraerPatronesIf(String texto, Set<String> destino) {
        if (texto == null) return;
        Matcher matcher = PATRON_IF_EXTRACT.matcher(texto);
        while (matcher.find()) {
            destino.add(matcher.group(1));
        }
    }   

    /**
     * Reemplaza las variables en la plantilla y guarda el resultado preservando el formato
     */
    public boolean procesarYGuardar(File archivoOrigen, File archivoDestino, Map<String, String> valores) {
        try (FileInputStream fis = new FileInputStream(archivoOrigen);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            // 1. Procesar bloques condicionales
            procesarBloquesCondicionales(document.getParagraphs(), valores);
            for (XWPFTable t : document.getTables()) {
                for (XWPFTableRow r : t.getRows()) {
                    for (XWPFTableCell c : r.getTableCells()) {
                        procesarBloquesCondicionales(c.getParagraphs(), valores);
                    }
                }
            }

            // 2. Reemplazar variables (con soporte para modificadores de formato)
            reemplazarEnParrafos(document.getParagraphs(), valores);

            for (XWPFTable t : document.getTables()) {
                for (XWPFTableRow r : t.getRows()) {
                    for (XWPFTableCell c : r.getTableCells()) {
                        reemplazarEnParrafos(c.getParagraphs(), valores);
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(archivoDestino)) {
                document.write(fos);
            }

            return true;

        } catch (Exception e) {
            System.err.println("❌ Error al procesar el archivo Word: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reemplaza variables en los párrafos aplicando transformaciones de formato si se especifican.
     */
    private void reemplazarEnParrafos(List<XWPFParagraph> parrafos, Map<String, String> valores) {
        for (XWPFParagraph p : parrafos) {
            List<XWPFRun> runs = p.getRuns();
            if (runs == null || runs.isEmpty()) continue;

            // CASO 1: La variable está completa dentro de un solo Run
            for (XWPFRun run : runs) {
                String textoRun = run.getText(0);
                if (textoRun != null && textoRun.contains("${")) {
                    Matcher matcher = PATRON_VARIABLE.matcher(textoRun);
                    StringBuffer sb = new StringBuffer();
                    
                    while (matcher.find()) {
                        String fullTag = matcher.group(0); // Ej: "${PRECIO:letras}"
                        String varName = matcher.group(1); // Ej: "PRECIO"
                        String modifier = matcher.group(2); // Ej: "letras" (puede ser null)

                        if (valores.containsKey(varName)) {
                            String rawVal = valores.get(varName);
                            // Aplicamos la transformación mediante TextTransformer
                            String transformedVal = TextTransformer.transform(rawVal, modifier);
                            matcher.appendReplacement(sb, Matcher.quoteReplacement(transformedVal));
                        } else {
                            matcher.appendReplacement(sb, Matcher.quoteReplacement(fullTag));
                        }
                    }
                    matcher.appendTail(sb);
                    run.setText(sb.toString(), 0);
                }
            }

            // CASO 2: Word fragmentó la variable a lo largo de varios Runs contiguos
            String textoParrafo = p.getText();
            if (textoParrafo != null && textoParrafo.contains("${")) {
                reemplazarVariablesFraccionadas(p, valores);
            }
        }
    }

    /**
     * Resuelve casos fraccionados aplicando transformadores de formato.
     */
    private void reemplazarVariablesFraccionadas(XWPFParagraph p, Map<String, String> valores) {
        Matcher matcher = PATRON_VARIABLE.matcher(p.getText());
        while (matcher.find()) {
            String fullTag = matcher.group(0);
            String varName = matcher.group(1);
            String modifier = matcher.group(2);

            if (!valores.containsKey(varName)) continue;

            String rawVal = valores.get(varName);
            String val = TextTransformer.transform(rawVal, modifier);

            List<XWPFRun> runs = p.getRuns();
            StringBuilder sb = new StringBuilder();
            int startRunIndex = -1;
            int endRunIndex = -1;

            for (int i = 0; i < runs.size(); i++) {
                XWPFRun run = runs.get(i);
                String text = run.getText(0);
                if (text == null) continue;

                int startIndex = sb.length();
                sb.append(text);
                int endIndex = sb.length();

                int keyIndex = sb.toString().indexOf(fullTag);
                if (keyIndex != -1) {
                    if (startRunIndex == -1 && keyIndex < endIndex) {
                        startRunIndex = i;
                    }
                    if (keyIndex + fullTag.length() <= endIndex) {
                        endRunIndex = i;
                        break;
                    }
                }
            }

            if (startRunIndex != -1 && endRunIndex != -1) {
                XWPFRun startRun = runs.get(startRunIndex);
                String fullText = sb.toString();
                String nuevoTexto = fullText.replace(fullTag, val);

                boolean isBold = startRun.isBold();
                boolean isItalic = startRun.isItalic();
                String color = startRun.getColor();
                String fontFamily = startRun.getFontFamily();
                Double fontSize = startRun.getFontSizeAsDouble();

                startRun.setText(nuevoTexto, 0);
                startRun.setBold(isBold);
                startRun.setItalic(isItalic);
                if (color != null) startRun.setColor(color);
                if (fontFamily != null) startRun.setFontFamily(fontFamily);
                if (fontSize != null) startRun.setFontSize(fontSize);

                for (int i = startRunIndex + 1; i <= endRunIndex; i++) {
                    runs.get(i).setText("", 0);
                }
            } else {
                break;
            }
        }
    }

    private void procesarBloquesCondicionales(List<XWPFParagraph> parrafos, Map<String, String> valores) {
        boolean evaluating = false;
        boolean keepCurrentBlock = true;
        List<XWPFParagraph> parrafosAEliminar = new ArrayList<>();

        for (XWPFParagraph p : parrafos) {
            String text = p.getText();
            if (text == null) continue;

            Matcher startMatcher = PATRON_IF_START.matcher(text);
            if (startMatcher.find()) {
                String varName = startMatcher.group(1);
                evaluating = true;

                String val = valores.get(varName);
                keepCurrentBlock = (val != null && (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("1") || !val.isEmpty()));

                removerEtiquetaDelParrafo(p, startMatcher.group(0));
                
                if (p.getText().contains("${/if}")) {
                    Matcher endMatcherSameLine = PATRON_IF_END.matcher(p.getText());
                    if (endMatcherSameLine.find()) {
                        removerEtiquetaDelParrafo(p, endMatcherSameLine.group(0));
                    }
                    evaluating = false;
                }
                continue;
            }

            Matcher endMatcher = PATRON_IF_END.matcher(text);
            if (endMatcher.find() && evaluating) {
                evaluating = false;
                removerEtiquetaDelParrafo(p, endMatcher.group(0));
                continue;
            }

            if (evaluating && !keepCurrentBlock) {
                parrafosAEliminar.add(p);
            }
        }
        
        for (XWPFParagraph p : parrafosAEliminar) {
            eliminarParrafoFisico(p);
        }
    }

    private void removerEtiquetaDelParrafo(XWPFParagraph paragraph, String tag) {
        for (XWPFRun run : paragraph.getRuns()) {
            String textoRun = run.getText(0);
            if (textoRun != null && textoRun.contains(tag)) {
                run.setText(textoRun.replace(tag, ""), 0);
            }
        }
    }

    private void eliminarParrafoFisico(XWPFParagraph paragraph) {
        for (XWPFRun run : paragraph.getRuns()) {
            run.setText("", 0);
        }
        try {
            XWPFDocument doc = paragraph.getDocument();
            if (doc != null) {
                int pos = doc.getPosOfParagraph(paragraph);
                if (pos != -1) {
                    doc.removeBodyElement(pos);
                }
            }
        } catch (Exception e) {
            paragraph.setSpacingAfter(0);
            paragraph.setSpacingBefore(0);
        }
    }

    /**
     * Realiza la generación masiva de documentos a partir de los registros del CSV.
     */
    public int procesarLotesCsv(File archivoPlantilla, File carpetaSalida, List<Map<String, String>> registrosCsv, String prefijoNombre) {
        int contadorExitosos = 0;

        if (!carpetaSalida.exists()) {
            carpetaSalida.mkdirs();
        }

        for (int i = 0; i < registrosCsv.size(); i++) {
            Map<String, String> datosFila = registrosCsv.get(i);

            String identificador = datosFila.getOrDefault("COMPRADOR_NOMBRE", datosFila.getOrDefault("CEDULA", "Registro_" + (i + 1)));
            identificador = identificador.replaceAll("[^a-zA-Z0-9_-]", "_");

            File archivoDestino = new File(carpetaSalida, prefijoNombre + "_" + (i + 1) + "_" + identificador + ".docx");

            try {
                boolean exito = procesarYGuardarIndividual(archivoPlantilla, archivoDestino, datosFila);
                if (exito) {
                    contadorExitosos++;
                }
            } catch (Exception e) {
                System.err.println("❌ Error al generar documento para la fila " + (i + 1) + ": " + e.getMessage());
            }
        }

        return contadorExitosos;
    }

    private boolean procesarYGuardarIndividual(File origen, File destino, Map<String, String> valores) {
        try (FileInputStream fis = new FileInputStream(origen);
             XWPFDocument document = new XWPFDocument(fis)) {

            procesarBloquesCondicionales(document.getParagraphs(), valores);
            
            for (XWPFTable t : document.getTables()) {
                for (XWPFTableRow r : t.getRows()) {
                    for (XWPFTableCell c : r.getTableCells()) {
                        procesarBloquesCondicionales(c.getParagraphs(), valores);
                        reemplazarVariablesEnParrafosLote(c.getParagraphs(), valores);
                    }
                }
            }

            reemplazarVariablesEnParrafosLote(document.getParagraphs(), valores);

            try (FileOutputStream fos = new FileOutputStream(destino)) {
                document.write(fos);
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reemplaza las variables simples y con modificadores para el procesamiento por lotes.
     */
    private void reemplazarVariablesEnParrafosLote(List<XWPFParagraph> parrafos, Map<String, String> valores) {
        for (XWPFParagraph p : parrafos) {
            for (XWPFRun run : p.getRuns()) {
                String text = run.getText(0);
                if (text != null && text.contains("${")) {
                    Matcher matcher = PATRON_VARIABLE.matcher(text);
                    StringBuffer sb = new StringBuffer();
                    
                    while (matcher.find()) {
                        String fullTag = matcher.group(0);
                        String varName = matcher.group(1);
                        String modifier = matcher.group(2);

                        if (valores.containsKey(varName)) {
                            String rawVal = valores.get(varName);
                            String transformedVal = TextTransformer.transform(rawVal, modifier);
                            matcher.appendReplacement(sb, Matcher.quoteReplacement(transformedVal));
                        } else {
                            matcher.appendReplacement(sb, Matcher.quoteReplacement(fullTag));
                        }
                    }
                    matcher.appendTail(sb);
                    run.setText(sb.toString(), 0);
                }
            }
        }
    }
}