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
package com.abogados.escrituras.controller;

import com.abogados.escrituras.model.HistorialRegistroDTO;
import com.abogados.escrituras.model.PersonaDTO;
import com.abogados.escrituras.model.PlantillaDTO;
import com.abogados.escrituras.service.AutoFillService;
import com.abogados.escrituras.service.DocumentProcessor;
import com.abogados.escrituras.service.HistorialRepository;
import com.abogados.escrituras.service.LocalPersonaRepository;
import com.abogados.escrituras.service.MetricsService;
import com.abogados.escrituras.ui.MetricsView;
import com.abogados.escrituras.util.EnigmaReload;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class TemplateManagerController {

    @FXML private Button btnSeleccionarPlantilla;
    @FXML private GridPane gridCamposDinamicos;
    @FXML private Label lblEstado;
    @FXML private Button btnGenerarEscritura;
    
    private final Map<String, TextField> mapaCamposUI = new HashMap<>();
    private final LocalPersonaRepository personaRepository = new LocalPersonaRepository();
    private final DocumentProcessor documentProcessor = new DocumentProcessor();
    private final HistorialRepository historialRepository = new HistorialRepository();

    private File archivoPlantillaSeleccionada;
    private PlantillaDTO plantillaActual;
    private EnigmaReload er = new EnigmaReload(8746578493L);;
    private MetricsService metricsService = new MetricsService();       
    @FXML
    public void initialize() {
        btnGenerarEscritura.setDisable(true);
    }

    /**
     * Carga el archivo .docx seleccionado por el usuario
     */
    @FXML
    private void handleCargarPlantilla() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Plantilla de Escritura (.docx)");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Documentos de Word (*.docx)", "*.docx")
        );

        File archivoSeleccionado = fileChooser.showOpenDialog(btnSeleccionarPlantilla.getScene().getWindow());

        if (archivoSeleccionado != null) {
            this.archivoPlantillaSeleccionada = archivoSeleccionado;
            lblEstado.setText("📄 Plantilla cargada: " + archivoSeleccionado.getName());

            // Escanear variables ${VAR} en la plantilla
            Set<String> variables = documentProcessor.extraerVariables(archivoSeleccionado);

            if (variables.isEmpty()) {
                lblEstado.setText("⚠️ No se encontraron variables con formato ${VARIABLE} en la plantilla.");
                btnGenerarEscritura.setDisable(true);
            } else {
                cargarCamposDinamicos(variables);
                btnGenerarEscritura.setDisable(false);
            }
        }
    }
    
    public void cargarPlantillaDTO(PlantillaDTO plantilla) {
        if (plantilla == null) {
            return;
        }

        this.plantillaActual = plantilla;

        if (plantilla.getRutaArchivo() != null) {
            File archivoDocx = new File(plantilla.getRutaArchivo());
            if (archivoDocx.exists()) {
                this.archivoPlantillaSeleccionada = archivoDocx;
                // Escanear variables ${VAR} en la plantilla
                Set<String> variables = documentProcessor.extraerVariables(archivoDocx);

                if (variables.isEmpty()) {
                    lblEstado.setText("⚠️ No se encontraron variables con formato ${VARIABLE} en la plantilla.");
                    btnGenerarEscritura.setDisable(true);
                } else {
                    cargarCamposDinamicos(variables);
                    btnGenerarEscritura.setDisable(false);
                }
            }
        }
    }
    /**
     * Dibuja los campos en el formulario y vincula el evento de autocompletado si hay cédulas
     */
    public void cargarCamposDinamicos(Set<String> variables) {
        gridCamposDinamicos.getChildren().clear();
        mapaCamposUI.clear();

        int row = 0;
        for (String var : variables) {
            Label label = new Label(var + ":");
            label.getStyleClass().add("label-field-name");

            TextField textField = new TextField();
            textField.setPromptText("Ingrese " + var.toLowerCase().replace("_", " "));
            textField.getStyleClass().add("text-field");

            gridCamposDinamicos.add(label, 0, row);
            gridCamposDinamicos.add(textField, 1, row);

            mapaCamposUI.put(var, textField);

            // Escuchar autocompletado si el campo corresponde a una Cédula
            if (var.startsWith("CEDULA_")) {
                String rol = var.replace("CEDULA_", "");
                configurarAutoCompletadoCedula(textField, rol);
            }

            row++;
        }
    }

    /**
     * Al perder el foco un campo CEDULA_*, busca el archivo JSON local y completa los datos
     */
    private void configurarAutoCompletadoCedula(TextField campoCedula, String rol) {
        campoCedula.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Perdió el foco
                String cedula = campoCedula.getText();
                if (cedula != null && !cedula.isBlank()) {
                    Optional<PersonaDTO> optPersona = personaRepository.buscarPorIdentificacion(cedula);

                    if (optPersona.isPresent()) {
                        PersonaDTO persona = optPersona.get();
                        AutoFillService.autoCompletarPorRol(rol, persona, mapaCamposUI);
                        lblEstado.setText("✅ Datos cargados automáticamente para " + rol + ": " + er.getDesencriptado(persona.getNombreCompleto()));
                    } else {
                        lblEstado.setText("ℹ️ Cédula " + cedula + " no registrada localmente. Ingrese datos manualmente.");
                    }
                }
            }
        });
    }

    /**
     * Genera y guarda el documento sustituyendo los valores ingresados y lo registra en el historial
     */
    @FXML
    private void handleGenerarEscritura() {
        if (archivoPlantillaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No hay ninguna plantilla seleccionada.");
            return;
        }

        Map<String, String> valoresVariables = new HashMap<>();
        for (Map.Entry<String, TextField> entry : mapaCamposUI.entrySet()) {
            valoresVariables.put(entry.getKey(), entry.getValue().getText());
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Escritura Generada");
        fileChooser.setInitialFileName("Escritura_Generada.docx");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Documentos de Word (*.docx)", "*.docx")
        );

        File archivoDestino = fileChooser.showSaveDialog(btnGenerarEscritura.getScene().getWindow());

        if (archivoDestino != null) {
            boolean exito = documentProcessor.procesarYGuardar(
                archivoPlantillaSeleccionada,
                archivoDestino,
                valoresVariables
            );

            if (exito) {
                // REGISTRO EN EL HISTORIAL ENCRIPTADO (ENIGMARELOAD)
                registrarEnHistorial(archivoDestino, valoresVariables);
                metricsService.registrarGeneracion(archivoPlantillaSeleccionada.getName(), 1, false);
                mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Escritura Generada",
                    "El documento se ha generado exitosamente en:\n" + archivoDestino.getAbsolutePath()
                );
                lblEstado.setText("✅ Escritura generada y registrada en el historial.");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Ocurrió un fallo al generar el documento Word.");
            }
        }
    }

    /**
     * Crea y guarda la entrada en la bitácora encriptada
     */
    private void registrarEnHistorial(File archivoDestino, Map<String, String> valoresVariables) {
        try {
            String fechaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String clientePrincipal = "Cargado por CSV";
            if (valoresVariables != null){
            // Intenta extraer el nombre del cliente/compareciente si existe un campo NOMBRE_*
             clientePrincipal = valoresVariables.entrySet().stream()
                    .filter(e -> e.getKey().startsWith("NOMBRE_") && e.getValue() != null && !e.getValue().isBlank())
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse("Cliente No Especificado");
            }
            HistorialRegistroDTO registro = new HistorialRegistroDTO(
                    UUID.randomUUID().toString(),
                    fechaActual,
                    archivoPlantillaSeleccionada.getName(),
                    clientePrincipal,
                    archivoDestino.getAbsolutePath(),
                    valoresVariables
            );

            historialRepository.registrarEscritura(registro);
        } catch (Exception e) {
            System.err.println("❌ Fallo al registrar el evento en el historial: " + e.getMessage());
        }
    }
/**
     * Método ejecutado al hacer clic en el botón de "Generación Masiva por CSV" en la UI.
     */
    @FXML
    public void handleGeneracionMasivaCsv() {
        Stage stage = (Stage) btnSeleccionarPlantilla.getScene().getWindow();

        if (archivoPlantillaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Plantilla no seleccionada", "Por favor, seleccione primero un archivo de plantilla base .docx.");
            return;
        }

        // Selector de archivo CSV
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Archivo CSV con Datos");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        File archivoCsv = fileChooser.showOpenDialog(stage);

        if (archivoCsv == null) return;

        // Selector de carpeta de destino
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar Carpeta de Destino para los Documentos");
        File carpetaSalida = directoryChooser.showDialog(stage);

        if (carpetaSalida == null) return;

        try {
            List<Map<String, String>> registros = com.abogados.escrituras.util.CsvReaderUtil.leerCsvComoRegistros(archivoCsv);

            if (registros.isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "CSV Vacío", "El archivo CSV seleccionado no contiene registros o está mal formateado.");
                return;
            }

            DocumentProcessor processor = new DocumentProcessor();
            
            int totalGenerados = processor.procesarLotesCsv(archivoPlantillaSeleccionada, carpetaSalida, registros, "Escritura");
            registrarEnHistorial(carpetaSalida, null);
            metricsService.registrarGeneracion(archivoPlantillaSeleccionada.getName(), totalGenerados, true);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Generación Masiva Exitosa", 
                    "Se han generado exitosamente " + totalGenerados + " de " + registros.size() + " documentos.\nGuardados en: " + carpetaSalida.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error en Generación Masiva", "Ocurrió un error al procesar el archivo CSV: " + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}