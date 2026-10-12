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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentProcessor {

    // Extrae únicamente el nombre interno de la variable ignorando signos de puntuación adyacentes
    private static final Pattern PATRON_VARIABLE = Pattern.compile("\\$\\{([A-Za-z0-9_]+)\\}");
    // Patrones para bloques condicionales ${#if VAR} y ${/if}
    private static final Pattern PATRON_IF_START = Pattern.compile("\\$\\{#if\\s+([A-Z0-9_]+)\\}");
    private static final Pattern PATRON_IF_END = Pattern.compile("\\$\\{/if\\}");
   private static final Pattern PATRON_IF_EXTRACT = Pattern.compile("\\$\\{#if\\s+([A-Z0-9_]+)\\}");
    /**
     * Extrae todas las variables únicas con formato ${VARIABLE} del archivo .docx
     */
    /**
     * Extrae todas las variables únicas (tanto normales como condicionales) del archivo .docx[cite: 1]
     */
    public Set<String> extraerVariables(File archivoDocx) {
        Set<String> variables = new LinkedHashSet<>();

        try (FileInputStream fis = new FileInputStream(archivoDocx);
             XWPFDocument document = new XWPFDocument(fis)) {

            for (XWPFParagraph p : document.getParagraphs()) {
                extraerPatrones(p.getText(), variables);
                extraerPatronesIf(p.getText(), variables); // <-- Extraer también condicionales
            }

            for (XWPFTable t : document.getTables()) {
                for (XWPFTableRow r : t.getRows()) {
                    for (XWPFTableCell c : r.getTableCells()) {
                        for (XWPFParagraph p : c.getParagraphs()) {
                            extraerPatrones(p.getText(), variables);
                            extraerPatronesIf(p.getText(), variables); // <-- Extraer también en tablas
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
            destino.add(matcher.group(1));
        }
    }
    // NUEVO: Método auxiliar para extraer las variables de los bloques #if
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
            // 1. Procesr bloques condicionales tanto en párrafos principales como en tablas
            procesarBloquesCondicionales(document.getParagraphs(), valores);
            for (XWPFTable t : document.getTables()) {
                for (XWPFTableRow r : t.getRows()) {
                    for (XWPFTableCell c : r.getTableCells()) {
                        procesarBloquesCondicionales(c.getParagraphs(), valores);
                    }
                }
            }
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
     * Reemplaza variables en los párrafos preservando formato incluso ante signos de puntuación adyacentes (, . ;)
     */
    private void reemplazarEnParrafos(List<XWPFParagraph> parrafos, Map<String, String> valores) {
        for (XWPFParagraph p : parrafos) {
            List<XWPFRun> runs = p.getRuns();
            if (runs == null || runs.isEmpty()) continue;

            // CASO 1: La variable ${VARIABLE} está completa dentro de un solo Run
            for (XWPFRun run : runs) {
                String textoRun = run.getText(0);
                if (textoRun != null && textoRun.contains("${")) {
                    for (Map.Entry<String, String> entry : valores.entrySet()) {
                        String key = "${" + entry.getKey() + "}";
                        String val = entry.getValue() != null ? entry.getValue() : "";
                        if (textoRun.contains(key)) {
                            // Reemplazo usando reemplazo literal seguro
                            textoRun = textoRun.replace(key, val);
                        }
                    }
                    run.setText(textoRun, 0);
                }
            }

            // CASO 2: Word fragmentó la variable ${VARIABLE} a lo largo de varios Runs contiguos
            String textoParrafo = p.getText();
            if (textoParrafo != null && textoParrafo.contains("${")) {
                reemplazarVariablesFraccionadas(p, valores);
            }
        }
    }

    /**
     * Resuelve casos fragmentados asegurando que signos como comas o puntos se mantengan intactos
     */
    private void reemplazarVariablesFraccionadas(XWPFParagraph p, Map<String, String> valores) {
        for (Map.Entry<String, String> entry : valores.entrySet()) {
            String key = "${" + entry.getKey() + "}";
            String val = entry.getValue() != null ? entry.getValue() : "";

            while (p.getText().contains(key)) {
                List<XWPFRun> runs = p.getRuns();
                StringBuilder sb = new StringBuilder();

                int startRunIndex = -1;
                int endRunIndex = -1;

                // Identificar los runs exactos donde se encuentra el patrón ${KEY}
                for (int i = 0; i < runs.size(); i++) {
                    XWPFRun run = runs.get(i);
                    String text = run.getText(0);
                    if (text == null) continue;

                    int startIndex = sb.length();
                    sb.append(text);
                    int endIndex = sb.length();

                    int keyIndex = sb.toString().indexOf(key);
                    if (keyIndex != -1) {
                        if (startRunIndex == -1 && keyIndex < endIndex) {
                            startRunIndex = i;
                        }
                        if (keyIndex + key.length() <= endIndex) {
                            endRunIndex = i;
                            break;
                        }
                    }
                }

                if (startRunIndex != -1 && endRunIndex != -1) {
                    XWPFRun startRun = runs.get(startRunIndex);

                    // Reconstruir la cadena preservando puntuación posterior
                    String fullText = sb.toString();
                    String nuevoTexto = fullText.replace(key, val);

                    // Preservar estilo del primer Run
                    boolean isBold = startRun.isBold();
                    boolean isItalic = startRun.isItalic();
                    //Underline underlinePattern = startRun.getUnderline();
                    String color = startRun.getColor();
                    String fontFamily = startRun.getFontFamily();
                    Double fontSize = startRun.getFontSizeAsDouble();

                    startRun.setText(nuevoTexto, 0);
                    startRun.setBold(isBold);
                    startRun.setItalic(isItalic);
                    //if (underlinePattern != null) startRun.setUnderline(underlinePattern);
                    if (color != null) startRun.setColor(color);
                    if (fontFamily != null) startRun.setFontFamily(fontFamily);
                    if (fontSize != null) startRun.setFontSize(fontSize);

                    // Vaciar únicamente los runs fragmentados pertenecientes al marcador
                    for (int i = startRunIndex + 1; i <= endRunIndex; i++) {
                        runs.get(i).setText("", 0);
                    }
                } else {
                    break;
                }
            }
        }
    }
 /**
     * Evalúa y filtra los bloques condicionales evitando modificaciones concurrentes en la colección.
     */
    private void procesarBloquesCondicionales(List<XWPFParagraph> parrafos, Map<String, String> valores) {
        boolean evaluating = false;
        boolean keepCurrentBlock = true;
        
        // Lista temporal para recolectar los párrafos falsos sin romper el bucle activo
        List<XWPFParagraph> parrafosAEliminar = new ArrayList<>();

        for (XWPFParagraph p : parrafos) {
            String text = p.getText();
            if (text == null) continue;

            // 1. Verificamos si este párrafo contiene el inicio de un bloque
            Matcher startMatcher = PATRON_IF_START.matcher(text);
            if (startMatcher.find()) {
                String varName = startMatcher.group(1);
                evaluating = true;

                String val = valores.get(varName);
                keepCurrentBlock = (val != null && (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("1") || !val.isEmpty()));

                // Removemos la etiqueta de inicio del documento visualmente
                removerEtiquetaDelParrafo(p, startMatcher.group(0));
                
                // Si el mismo párrafo contiene también el cierre ${/if}, cerramos de inmediato en esta misma línea
                if (p.getText().contains("${/if}")) {
                    Matcher endMatcherSameLine = PATRON_IF_END.matcher(p.getText());
                    if (endMatcherSameLine.find()) {
                        removerEtiquetaDelParrafo(p, endMatcherSameLine.group(0));
                    }
                    evaluating = false;
                }
                continue;
            }

            // 2. Verificamos si este párrafo contiene el cierre del bloque (${/if})
            Matcher endMatcher = PATRON_IF_END.matcher(text);
            if (endMatcher.find() && evaluating) {
                // Primero apagamos el estado de evaluación ANTES de limpiar o tocar el párrafo,
                // asegurando que esta línea de cierre y todo lo que siga hacia abajo NO se borren.
                evaluating = false;
                
                // Removemos la etiqueta de cierre ${/if} para que no se imprima
                removerEtiquetaDelParrafo(p, endMatcher.group(0));
                continue;
            }

            // 3. Si el bloque es falso, acumulamos el párrafo para borrarlo al salir del bucle
            if (evaluating && !keepCurrentBlock) {
                parrafosAEliminar.add(p);
            }
        }
        
        // 4. Eliminación segura fuera del bucle iterativo para evitar ConcurrentModificationException
        for (XWPFParagraph p : parrafosAEliminar) {
            eliminarParrafoFisico(p);
        }
        
      
    }
    /**
     * Evalúa y filtra los bloques condicionales (${#if VAR} ... ${/if}) de forma estricta.
     */
    /*private void procesarBloquesCondicionales(List<XWPFParagraph> parrafos, Map<String, String> valores) {
        boolean evaluating = false;
        boolean keepCurrentBlock = true;

        for (XWPFParagraph p : parrafos) {
            String text = p.getText();
            if (text == null) continue;

            // 1. Verificamos si este párrafo contiene el inicio de un bloque
            Matcher startMatcher = PATRON_IF_START.matcher(text);
            if (startMatcher.find()) {
                String varName = startMatcher.group(1);
                evaluating = true;

                String val = valores.get(varName);
                keepCurrentBlock = (val != null && (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("1") || !val.isEmpty()));

                // Removemos la etiqueta de inicio del documento visualmente
                removerEtiquetaDelParrafo(p, startMatcher.group(0));
                
                // Si el mismo párrafo contiene también el cierre ${/if}, cerramos de inmediato en esta misma línea
                if (p.getText().contains("${/if}")) {
                    Matcher endMatcherSameLine = PATRON_IF_END.matcher(p.getText());
                    if (endMatcherSameLine.find()) {
                        removerEtiquetaDelParrafo(p, endMatcherSameLine.group(0));
                    }
                    evaluating = false;
                }
                continue;
            }

            // 2. Verificamos si este párrafo contiene el cierre del bloque (${/if})
            Matcher endMatcher = PATRON_IF_END.matcher(text);
            if (endMatcher.find() && evaluating) {
                // Primero apagamos el estado de evaluación ANTES de limpiar o tocar el párrafo,
                // asegurando que esta línea de cierre y todo lo que siga hacia abajo NO se borren.
                evaluating = false;
                
                // Removemos la etiqueta de cierre ${/if} para que no se imprima
                removerEtiquetaDelParrafo(p, endMatcher.group(0));
                continue;
            }

            // 3. Si estamos dentro de un bloque condicional falso, vaciamos este párrafo específico
            if (evaluating && !keepCurrentBlock) {
                limpiarTextoParrafo(p);
            }
        }
    }*/

    private void removerEtiquetaDelParrafo(XWPFParagraph paragraph, String tag) {
        for (XWPFRun run : paragraph.getRuns()) {
            String textoRun = run.getText(0);
            if (textoRun != null && textoRun.contains(tag)) {
                run.setText(textoRun.replace(tag, ""), 0);
            }
        }
    }
/**
     * Remueve físicamente el párrafo del documento XML de Word de forma segura.
     */
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
     * Elimina por completo el párrafo del documento para evitar líneas en blanco o saltos de espacio.
     */
    private void limpiarTextoParrafo(XWPFParagraph paragraph) {
        // Opción limpia: Borramos el texto de sus runs primero
        for (XWPFRun run : paragraph.getRuns()) {
            run.setText("", 0);
        }
        
        // Si el párrafo está dentro del cuerpo principal del documento, 
        // podemos removerlo buscando su índice en el documento padre.
        try {
            XWPFDocument doc = paragraph.getDocument();
            if (doc != null) {
                int pos = doc.getParagraphs().indexOf(paragraph);
                if (pos != -1) {
                    doc.removeBodyElement(doc.getPosOfParagraph(paragraph));
                }
            }
        } catch (Exception e) {
            // Fallback si la estructura XML difiere: dejamos el párrafo vacío sin runs
            paragraph.setSpacingAfter(0);
            paragraph.setSpacingBefore(0);
        }
    } 
    /**
     * Realiza la generación masiva de documentos a partir de una lista de registros (datos del CSV).
     * 
     * @param archivoPlantilla Archivo base .docx
     * @param carpetaSalida    Carpeta donde se guardarán todos los documentos generados
     * @param registrosCsv     Lista de mapas obtenida del CsvReaderUtil
     * @param prefijoNombre    Prefijo para los archivos generados (ej: "Escritura_")
     * @return El número de documentos generados exitosamente.
     */
    public int procesarLotesCsv(File archivoPlantilla, File carpetaSalida, List<Map<String, String>> registrosCsv, String prefijoNombre) {
        int contadorExitosos = 0;

        if (!carpetaSalida.exists()) {
            carpetaSalida.mkdirs();
        }

        for (int i = 0; i < registrosCsv.size(); i++) {
            Map<String, String> datosFila = registrosCsv.get(i);

            // Intentamos buscar un identificador único en los datos para el nombre del archivo (ej. NOMBRE o CEDULA)
            String identificador = datosFila.getOrDefault("COMPRADOR_NOMBRE", datosFila.getOrDefault("CEDULA", "Registro_" + (i + 1)));
            // Limpiamos caracteres especiales para que sea un nombre de archivo válido
            identificador = identificador.replaceAll("[^a-zA-Z0-9_-]", "_");

            File archivoDestino = new File(carpetaSalida, prefijoNombre + "_" + (i + 1) + "_" + identificador + ".docx");

            try {
                // Reutilizamos tu lógica existente de procesamiento individual por cada fila
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

    /**
     * Método auxiliar interno para asegurar que el procesamiento individual devuelva éxito o maneje la lectura limpia.
     * (Si ya tienes un método que procesa individualmente, puedes adaptarlo o llamarlo directamente aquí).
     */
    private boolean procesarYGuardarIndividual(File origen, File destino, Map<String, String> valores) {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(origen);
             org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument(fis)) {

            // 1. Procesar bloques condicionales y eliminar los falsos
            procesarBloquesCondicionales(document.getParagraphs(), valores);
            
            // También revisar tablas por si hay bloques condicionales dentro de ellas
            for (org.apache.poi.xwpf.usermodel.XWPFTable t : document.getTables()) {
                for (org.apache.poi.xwpf.usermodel.XWPFTableRow r : t.getRows()) {
                    for (org.apache.poi.xwpf.usermodel.XWPFTableCell c : r.getTableCells()) {
                        procesarBloquesCondicionales(c.getParagraphs(), valores);
                        reemplazarVariablesEnParrafos(c.getParagraphs(), valores);
                    }
                }
            }

            // 2. Reemplazar variables simples en párrafos normales
            reemplazarVariablesEnParrafos(document.getParagraphs(), valores);

            // 3. Guardar el archivo resultante del lote
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(destino)) {
                document.write(fos);
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reemplaza las variables simples ${VARIABLE} en la lista de párrafos.
     */
    private void reemplazarVariablesEnParrafos(List<org.apache.poi.xwpf.usermodel.XWPFParagraph> parrafos, Map<String, String> valores) {
        for (org.apache.poi.xwpf.usermodel.XWPFParagraph p : parrafos) {
            for (org.apache.poi.xwpf.usermodel.XWPFRun run : p.getRuns()) {
                String text = run.getText(0);
                if (text != null && text.contains("${")) {
                    for (Map.Entry<String, String> entry : valores.entrySet()) {
                        String target = "${" + entry.getKey() + "}";
                        if (text.contains(target)) {
                            text = text.replace(target, entry.getValue() != null ? entry.getValue() : "");
                        }
                    }
                    run.setText(text, 0);
                }
            }
        }
    }
}