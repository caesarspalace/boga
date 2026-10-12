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

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class GeneradorPlantillaPrueba {

    public static void main(String[] args) {
        String rutaDestino = "plantilla_prueba_compraventa.docx";

        try (XWPFDocument document = new XWPFDocument()) {

            XWPFParagraph titulo = document.createParagraph();
            titulo.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun runTitulo = titulo.createRun();
            runTitulo.setText("ESCRITURA PÚBLICA DE COMPRAVENTA DE INMUEBLE");
            runTitulo.setBold(true);
            runTitulo.setFontSize(14);
            runTitulo.setFontFamily("Arial");
            runTitulo.addBreak();
            runTitulo.addBreak();

            XWPFParagraph cuerpo = document.createParagraph();
            cuerpo.setAlignment(ParagraphAlignment.BOTH);

            // Uso explicito de formato ${VARIABLE}
            agregarTexto(cuerpo, "NÚMERO ${NUMERO_ESCRITURA}. En la ciudad de San José, a las ");
            agregarTexto(cuerpo, "${HORA_ESCRITURA} horas del día ${FECHA_ESCRITURA}. ");
            agregarTexto(cuerpo, "Ante mí, ${NOMBRE_NOTARIO}, Notario Público con oficina en San José, comparecen:\n\n");

            agregarTexto(cuerpo, "Por una parte, el señor/a ${NOMBRE_VENDEDOR}, mayor, ");
            agregarTexto(cuerpo, "${ESTADO_CIVIL_VENDEDOR}, vecino/a de ${DOMICILIO_VENDEDOR}, ");
            agregarTexto(cuerpo, "con cédula de identidad número ${CEDULA_VENDEDOR}, en adelante denominado EL VENDEDOR; ");
            agregarTexto(cuerpo, "y por otra parte, el señor/a ${NOMBRE_COMPRADOR}, mayor, ");
            agregarTexto(cuerpo, "${ESTADO_CIVIL_COMPRADOR}, vecino/a de ${DOMICILIO_COMPRADOR}, ");
            agregarTexto(cuerpo, "con cédula de identidad número ${CEDULA_COMPRADOR}, en adelante denominado EL COMPRADOR.\n\n");

            agregarTexto(cuerpo, "PRIMERO: Manifiesta EL VENDEDOR que es dueño del inmueble inscrito en el Registro Nacional bajo el Sistema de Folio Real, ");
            agregarTexto(cuerpo, "Matrícula número ${NUMERO_FINCA}, situado en la Provincia de ${PROVINCIA_FINCA}, ");
            agregarTexto(cuerpo, "Cantón ${CANTON_FINCA}, Distrito ${DISTRITO_FINCA}.\n\n");

            agregarTexto(cuerpo, "SEGUNDO: Que por el precio de ${PRECIO_VENTA_COLONES} Colones (CRC ${PRECIO_VENTA_NUMEROS}), ");
            agregarTexto(cuerpo, "EL VENDEDOR vende y traspasa libre de gravámenes el inmueble descrito a EL COMPRADOR.\n\n");

            agregarTexto(cuerpo, "Así lo dicen y otorgan. Leída esta escritura a los comparecientes, la encuentran conforme y firmamos en San José.");

            File archivoSalida = new File(rutaDestino);
            try (FileOutputStream out = new FileOutputStream(archivoSalida)) {
                document.write(out);
            }

            System.out.println("✅ Plantilla con formato ${VAR} creada en: " + archivoSalida.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("❌ Error al generar la plantilla: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void agregarTexto(XWPFParagraph paragraph, String texto) {
        XWPFRun run = paragraph.createRun();
        run.setText(texto);
        run.setFontSize(11);
        run.setFontFamily("Arial");
    }
}
