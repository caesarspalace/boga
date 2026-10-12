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

import com.abogados.escrituras.model.PersonaDTO;
import com.abogados.escrituras.service.LocalPersonaRepository;
import com.abogados.escrituras.util.EnigmaReload;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Optional;

public class ModuloIdentidadController {

    @FXML private TextField txtIdentificacion;
    @FXML private TextField txtNombreCompleto;
    @FXML private TextField txtEstadoCivil;
    @FXML private TextField txtOcupacion;
    @FXML private TextField txtDomicilio;
    @FXML private TextField txtNombreConyuge;
    @FXML private TextField txtTelefono;
    @FXML private Label lblEstado;

    private final LocalPersonaRepository repository = new LocalPersonaRepository();
    final private EnigmaReload er = new EnigmaReload(8746578493L);
    @FXML
    private void handleBuscarPersona() {
        String id = txtIdentificacion.getText();
        if (id == null || id.isBlank()) {
            lblEstado.setText("⚠️ Ingrese un número de cédula/identificación para buscar.");
            return;
        }

        Optional<PersonaDTO> opt = repository.buscarPorIdentificacion(id.trim());
        if (opt.isPresent()) {
            PersonaDTO p = opt.get();
            txtNombreCompleto.setText(p.getNombreCompleto() != null ? er.getDesencriptado(p.getNombreCompleto()) : "");
            txtEstadoCivil.setText(p.getEstadoCivil() != null ? er.getDesencriptado(p.getEstadoCivil()) : "");
            txtOcupacion.setText(p.getOcupacion() != null ? er.getDesencriptado(p.getOcupacion()) : "");
            txtDomicilio.setText(p.getDomicilio() != null ? er.getDesencriptado(p.getDomicilio()) : "");
            txtNombreConyuge.setText(p.getNombreConyuge() != null ? er.getDesencriptado(p.getNombreConyuge()) : "");
            txtTelefono.setText(p.getTelefono() != null ? er.getDesencriptado(p.getTelefono()) : "");

            lblEstado.setText("✅ Persona encontrada.");
        } else {
            lblEstado.setText("ℹ️ No se encontró la persona. Puede registrarla llenando el formulario.");
            limpiarFormularioExceptoId();
        }
    }

    @FXML
    private void handleGuardarPersona() {
        String id = txtIdentificacion.getText();
        String nombreCompleto = txtNombreCompleto.getText();

        if (id == null || id.isBlank() || nombreCompleto == null || nombreCompleto.isBlank()) {
            lblEstado.setText("⚠️ Los campos Identificación y Nombre Completo son obligatorios.");
            return;
        }

        PersonaDTO p = new PersonaDTO(
                id.trim(),
                er.getEncriptado(nombreCompleto.trim()),
                er.getEncriptado(txtEstadoCivil.getText()),
                er.getEncriptado(txtOcupacion.getText()),
                er.getEncriptado(txtDomicilio.getText()),
                er.getEncriptado(txtNombreConyuge.getText()),
                er.getEncriptado(txtTelefono.getText())
        );

        boolean exito = repository.guardarPersona(p);
        if (exito) {
            lblEstado.setText("✅ Persona guardada exitosamente en JSON local.");
        } else {
            lblEstado.setText("❌ Error al guardar los datos de la persona.");
        }
    }

    @FXML
    private void handleLimpiar() {
        txtIdentificacion.clear();
        limpiarFormularioExceptoId();
        lblEstado.setText("Formulario limpio.");
    }

    private void limpiarFormularioExceptoId() {
        txtNombreCompleto.clear();
        txtEstadoCivil.clear();
        txtOcupacion.clear();
        txtDomicilio.clear();
        txtNombreConyuge.clear();
        txtTelefono.clear();
    }
}
