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

import com.abogados.escrituras.model.PersonaDTO;
import com.abogados.escrituras.util.EnigmaReload;
import javafx.scene.control.TextField;

import java.util.Map;

public class AutoFillService {

    /**
     * Aplica los datos de una PersonaDTO a un mapa de campos de la UI.
     */
    public static void autoCompletarPorRol(String rol, PersonaDTO persona, Map<String, TextField> mapaCamposUI) {
        if (persona == null || rol == null || rol.isBlank()) return;

        String sufijo = rol.toUpperCase().trim(); // Ej: "VENDEDOR" o "COMPRADOR"
        EnigmaReload er = new EnigmaReload(8746578493L);
        asignarSiExiste("NOMBRE_" + sufijo, er.getDesencriptado(persona.getNombreCompleto()), mapaCamposUI);
        asignarSiExiste("CEDULA_" + sufijo, persona.getIdentificacion(), mapaCamposUI);
        asignarSiExiste("ESTADO_CIVIL_" + sufijo, er.getDesencriptado(persona.getEstadoCivil()), mapaCamposUI);
        asignarSiExiste("DOMICILIO_" + sufijo, er.getDesencriptado(persona.getDomicilio()), mapaCamposUI);
        asignarSiExiste("OCUPACION_" + sufijo, er.getDesencriptado(persona.getOcupacion()), mapaCamposUI);

        // Nuevas variables mapeadas
        asignarSiExiste("CONYUGE_" + sufijo, er.getDesencriptado(persona.getNombreConyuge()), mapaCamposUI);
        asignarSiExiste("ESPOSA_" + sufijo, er.getDesencriptado(persona.getNombreConyuge()), mapaCamposUI);
        asignarSiExiste("ESPOSO_" + sufijo, er.getDesencriptado(persona.getNombreConyuge()), mapaCamposUI);
        asignarSiExiste("TELEFONO_" + sufijo, er.getDesencriptado(persona.getTelefono()), mapaCamposUI);
        asignarSiExiste("TEL_" + sufijo, er.getDesencriptado(persona.getTelefono()), mapaCamposUI);
    }

    private static void asignarSiExiste(String claveVariable, String valor, Map<String, TextField> mapaCamposUI) {
        if (mapaCamposUI.containsKey(claveVariable) && valor != null) {
            mapaCamposUI.get(claveVariable).setText(valor);
        }
    }
}
