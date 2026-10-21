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
package com.abogados.escrituras;

import com.abogados.escrituras.service.LicenciaService;
import com.abogados.escrituras.service.MetricsService;
import com.abogados.escrituras.ui.MetricsView;
import com.sun.javafx.scene.control.Properties;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.scene.control.Tab;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        LicenciaService licenciaService = new LicenciaService();
        licenciaService.verificarEstadoPrueba();
        String pais = System.getProperty("pais");
        String lenguaje = System.getProperty("lenguaje");
        Locale nuevoLocale;
        if (pais != null && !pais.equals("") && lenguaje != null && !lenguaje.equals(""))
            nuevoLocale = new Locale.Builder()
                                .setLanguage(lenguaje)
                                .setRegion(pais)
                                .build();
        else
            nuevoLocale = new Locale.Builder()
                                .setLanguage("es")
                                .setRegion("AR")
                                .build();
        Locale.setDefault(nuevoLocale);

            // 2. Cargar el ResourceBundle correspondiente (e.g. messages_es_CR.properties o messages_es_AR.properties)
        ResourceBundle bundle = ResourceBundle.getBundle("messages", nuevoLocale);

        try {
            URL fxmlUrl = getClass().getResource("/com/abogados/escrituras/view/MainView.fxml");
            if (fxmlUrl == null) {
                System.err.println("❌ ERROR: No se encontró el archivo FXML principal /com/abogados/escrituras/view/MainView.fxml");
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(fxmlLoader.load(), 950, 700);

            stage.setTitle("Sistema de Automatización de Escrituras Legales");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            System.err.println("❌ EXCEPCIÓN AL INICIAR LA APLICACIÓN:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
