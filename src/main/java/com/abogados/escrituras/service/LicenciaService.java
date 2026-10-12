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

import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

public class LicenciaService {

    private static final String DIRECTOPRIO_DATA = "data";
    private static final String ARCHIVO_LICENCIA = "data/app.dat";
    private static final String DIRECTORIO_PLANTILLAS = "templates"; // Ajusta la ruta a tu carpeta de plantillas
    private static final int DIAS_PRUEBA = 30;

    /**
     * Inicializa la comprobación del estado de la prueba.
     * Debe llamarse al arrancar la aplicación.
     */
    public void verificarEstadoPrueba() {
        try {
            File dirData = new File(DIRECTOPRIO_DATA);
            if (!dirData.exists()) {
                dirData.mkdirs();
            }

            Path pathLicencia = Paths.get(ARCHIVO_LICENCIA);

            if (!Files.exists(pathLicencia)) {
                // Si el archivo no existe, creamos uno nuevo o detectamos tamper/borrado
                crearOVerificarLicencia(pathLicencia);
            } else {
                // Si el archivo existe, validar fechas e identificador de archivo
                validarPeriodoPrueba(pathLicencia);
            }
        } catch (Exception e) {
            System.err.println("❌ Fallo en la verificación de seguridad: " + e.getMessage());
            ejecutarEliminacionYSalida("Se detectó una inconsistencia o fallo en el sistema de licencias.");
        }
    }

    private void crearOVerificarLicencia(Path pathLicencia) throws IOException {
        // Crear el archivo de licencia por primera vez
        Files.createFile(pathLicencia);

        // Obtener el identificador único del archivo en el sistema de archivos (FileKey)
        BasicFileAttributes attrs = Files.readAttributes(pathLicencia, BasicFileAttributes.class);
        Object fileKey = attrs.fileKey();
        String fileKeyStr = fileKey != null ? fileKey.toString() : "KEY_DEFAULT";

        // Guardar metadatos dentro del archivo
        Properties props = new Properties();
        props.setProperty("fechaCreacion", Instant.now().toString());
        props.setProperty("fileKey", fileKeyStr);

        try (var out = Files.newOutputStream(pathLicencia)) {
            props.store(out, "Sistema de Control de Prueba - No modificar");
        }

        System.out.println("✅ Archivo de prueba inicializado correctamente.");
    }

    private void validarPeriodoPrueba(Path pathLicencia) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(pathLicencia, BasicFileAttributes.class);
        
        // 1. Validar Identificador Único del Archivo (FileKey para Win/Mac/Linux)
        Object currentFileKey = attrs.fileKey();
        String currentKeyStr = currentFileKey != null ? currentFileKey.toString() : "KEY_DEFAULT";

        Properties props = new Properties();
        try (var in = Files.newInputStream(pathLicencia)) {
            props.load(in);
        }

        String storedKey = props.getProperty("fileKey", "");
        String fechaCreacionStr = props.getProperty("fechaCreacion", "");

        // Si la clave guardada no coincide con el FileKey real del SO (archivo reemplazado/copiado)
        if (!storedKey.equals(currentKeyStr) || fechaCreacionStr.isBlank()) {
            ejecutarEliminacionYSalida("El archivo de licencia ha sido alterado o reemplazado.");
            return;
        }

        // 2. Comprobar fecha de creación contra el FileTime del SO y la fecha registrada
        FileTime fileTime = attrs.creationTime();
        Instant fechaCreacionSO = fileTime.toInstant();
        Instant fechaCreacionArchivo = Instant.parse(fechaCreacionStr);

        // Usar la fecha más antigua registrada entre el SO y el contenido del archivo
        Instant fechaInicio = fechaCreacionSO.isBefore(fechaCreacionArchivo) ? fechaCreacionSO : fechaCreacionArchivo;
        long diasTranscurridos = ChronoUnit.DAYS.between(fechaInicio, Instant.now());

        if (diasTranscurridos > DIAS_PRUEBA) {
            ejecutarEliminacionYSalida("El período de prueba de " + DIAS_PRUEBA + " días ha expirado.");
        } else {
            System.out.println("ℹ️ Período de prueba activo. Días restantes: " + (DIAS_PRUEBA - diasTranscurridos));
        }
    }

    /**
     * Muestra la alerta de expiración/error, purga las carpetas data y plantillas, y cierra la app.
     */
    private void ejecutarEliminacionYSalida(String mensajeMotivo) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Período de Prueba Finalizado");
            alert.setHeaderText("Acceso Restringido");
            alert.setContentText(mensajeMotivo + "\nLa aplicación se cerrará inmediatamente.");
            alert.showAndWait();

            // Purgar archivos locales
            purgarDirectorio(new File(DIRECTOPRIO_DATA));
            purgarDirectorio(new File(DIRECTORIO_PLANTILLAS));

            // Salir de la aplicación JavaFX
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * Elimina todos los archivos y subcarpetas dentro de una ruta.
     */
    private void purgarDirectorio(File directorio) {
        if (directorio.exists() && directorio.isDirectory()) {
            File[] archivos = directorio.listFiles();
            if (archivos != null) {
                for (File archivo : archivos) {
                    if (archivo.isDirectory()) {
                        purgarDirectorio(archivo);
                    }
                    archivo.delete();
                }
            }
        }
    }
}