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

import com.abogados.escrituras.model.PlantillaDTO;
import com.abogados.escrituras.service.MetricsService;
import com.abogados.escrituras.ui.MetricsView;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class MainController {
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabHistorial;
    @FXML private Tab tabTemplateManager;
    

    // JavaFX inyecta el controlador del fx:include asignándole el sufijo "Controller"
    @FXML private CatalogoHistorialController historialViewController;
    @FXML private TemplateManagerController templateManagerController;
    
    @FXML
    public void initialize() {
        // Escucha el evento de selección de la pestaña del Historial para recargar los registros desencriptados
        if (tabHistorial != null) {
            tabHistorial.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected && historialViewController != null) {
                    historialViewController.cargarHistorial();
                    historialViewController.setOnPlantillaSeleccionada(this::alSeleccionarPlantilla);
                }
            });
        }
        Tab tabMetricas = new Tab("Métricas & Estadísticas");
        tabMetricas.setContent(new MetricsView(new MetricsService()));
        mainTabPane.getTabs().add(tabMetricas);
    }
    
    private void alSeleccionarPlantilla(PlantillaDTO plantilla) {
        if (plantilla == null) return;

        System.out.println("🔄 Transferiendo plantilla al generador: " + plantilla.getNombre());
// 1. Pasar el DTO al controlador del gestor de plantillas
        if (templateManagerController != null) {
            templateManagerController.cargarPlantillaDTO(plantilla);
        }

        // 2. Cambiar automáticamente a la pestaña del Editor/Gestor
        if (mainTabPane != null && tabTemplateManager != null) {
            mainTabPane.getSelectionModel().select(tabTemplateManager);
        }
        
    }
}