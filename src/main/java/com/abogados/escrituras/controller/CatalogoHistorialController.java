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
import com.abogados.escrituras.model.PlantillaDTO;
import com.abogados.escrituras.service.DocumentProcessor;
import com.abogados.escrituras.service.HistorialRepository;
import com.abogados.escrituras.service.PlantillaRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class CatalogoHistorialController {

    // Componentes del Catalogo
    @FXML private ComboBox<String> cbCategoria;
    @FXML private ListView<PlantillaDTO> listPlantillas;
    @FXML private TextField txtNombrePlantilla;
    @FXML private Label lblRutaPlantilla;
    @FXML private Button btnCargarPlantilla;

    // Tabla de Historial
    @FXML private TableView<HistorialRegistroDTO> tableHistorial;
    @FXML private TableColumn<HistorialRegistroDTO, String> colFecha;
    @FXML private TableColumn<HistorialRegistroDTO, String> colPlantilla;
    @FXML private TableColumn<HistorialRegistroDTO, String> colCliente;
    @FXML private TableColumn<HistorialRegistroDTO, String> colRuta;

    private final PlantillaRepository plantillaRepo = new PlantillaRepository();
    private final HistorialRepository historialRepo = new HistorialRepository();
    private final DocumentProcessor documentProcessor = new DocumentProcessor();
    private File archivoSeleccionado;
    private Consumer<PlantillaDTO> alSeleccionarPlantillaListener;
    @FXML
    public void initialize() {
        configurarTablaHistorial();
        configurarEventosCatalogo();
        cargarCategorias();
        cargarCatalogo();
        cargarHistorial();
    }
    
    public void setOnPlantillaSeleccionada(Consumer<PlantillaDTO> listener) {
        this.alSeleccionarPlantillaListener = listener;
    }
    /**
     * Configura el listener de doble clic sobre la lista, celda personalizada y eventos de la vista
     */
    private void configurarEventosCatalogo() {
        if (listPlantillas != null) {
            listPlantillas.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(PlantillaDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getNombre() == null) {
                        setText(null);
                    } else {
                        setText(item.getNombre());
                    }
                }
            });

            // Evento de doble clic primario sobre un registro de la lista
            listPlantillas.setOnMouseClicked((MouseEvent event) -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    notificarPlantillaSeleccionada();
                }
            });
        }

        if (btnCargarPlantilla != null) {
            btnCargarPlantilla.setOnAction(e -> notificarPlantillaSeleccionada());
        }

        if (cbCategoria != null) {
            cbCategoria.setOnAction(e -> filtrarPorCategoria());
        }
    }
    
    /**
     * Envia la plantilla elegida al listener asignado (MainController)
     */
    private void notificarPlantillaSeleccionada() {
        if (listPlantillas == null) return;

        PlantillaDTO seleccionada = listPlantillas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            if (alSeleccionarPlantillaListener != null) {
                System.out.println("✅ Plantilla seleccionada en catalogo: " + seleccionada.getNombre());
                alSeleccionarPlantillaListener.accept(seleccionada);
            } else {
                System.err.println("⚠️️ No hay ningun listener configurado para recibir la plantilla.");
            }
        }
    }
    /**
     * Filtra las plantillas segun la categoria seleccionada
     */
    private void filtrarPorCategoria() {
        if (cbCategoria == null || listPlantillas == null) return;

        String categoria = cbCategoria.getValue();
        List<PlantillaDTO> todas = plantillaRepo.obtenerTodas();

        if (categoria == null || categoria.equalsIgnoreCase("Todas")) {
            listPlantillas.setItems(FXCollections.observableArrayList(todas));
        } else {
            List<PlantillaDTO> filtradas = todas.stream()
                    .filter(p -> p.getCategoria() != null && p.getCategoria().equalsIgnoreCase(categoria))
                    .collect(Collectors.toList());
            listPlantillas.setItems(FXCollections.observableArrayList(filtradas));
        }
    }
    private void configurarTablaHistorial() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
        colPlantilla.setCellValueFactory(new PropertyValueFactory<>("nombrePlantilla"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("clientePrincipal"));
        colRuta.setCellValueFactory(new PropertyValueFactory<>("rutaArchivoGenerado"));
    }

    private void cargarCategorias() {
        cbCategoria.setItems(FXCollections.observableArrayList(
                "Todas","Contratos de Alquiler", "Traspaso de Vehiculos", "Poderes", "Hipotecas / Prenda", "General"
        ));
    }

    @FXML
    private void handleSeleccionarArchivo() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos Word", "*.docx"));
        File f = fc.showOpenDialog(null);
        if (f != null) {
            this.archivoSeleccionado = f;
            lblRutaPlantilla.setText(f.getAbsolutePath());
            if (txtNombrePlantilla.getText().isBlank()) {
                txtNombrePlantilla.setText(f.getName().replace(".docx", ""));
            }
        }
    }

    @FXML
    private void handleRegistrarPlantilla() {
        if (archivoSeleccionado == null || txtNombrePlantilla.getText().isBlank()) {
            mostrarAlerta("Atencion", "Debe seleccionar un archivo .docx y asignar un nombre.");
            return;
        }

        Set<String> vars = documentProcessor.extraerVariables(archivoSeleccionado);
        String cat = cbCategoria.getValue() != null ? cbCategoria.getValue() : "General";

        PlantillaDTO dto = new PlantillaDTO(
                UUID.randomUUID().toString(),
                txtNombrePlantilla.getText().trim(),
                cat,
                archivoSeleccionado.getAbsolutePath(),
                vars
        );

        if (plantillaRepo.guardarPlantilla(dto)) {
            cargarCatalogo();
            limpiarFormularioPlantilla();
        }
    }

    private void cargarCatalogo() {
        listPlantillas.setItems(FXCollections.observableArrayList(plantillaRepo.obtenerTodas()));
    }

    public void cargarHistorial() {
        tableHistorial.setItems(FXCollections.observableArrayList(historialRepo.obtenerHistorial()));
    }

    private void limpiarFormularioPlantilla() {
        archivoSeleccionado = null;
        txtNombrePlantilla.clear();
        lblRutaPlantilla.setText("No se ha seleccionado archivo.");
    }

    private void mostrarAlerta(String titulo, String msj) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msj);
        alert.showAndWait();
    }
}