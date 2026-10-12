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

import com.abogados.escrituras.model.HistorialRegistroDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HistorialRepository {

    private final File archivoHistorial = new File("data/historial_escrituras.enc");
    private final ObjectMapper mapper = new ObjectMapper();
    private final EncryptionService encryptionService = new EncryptionService();

    public List<HistorialRegistroDTO> obtenerHistorial() {
        if (!archivoHistorial.exists()) return new ArrayList<>();
        try {
            String jsonDesencriptado = encryptionService.desencriptarArchivo(archivoHistorial);
            return mapper.readValue(jsonDesencriptado, new TypeReference<List<HistorialRegistroDTO>>() {});
        } catch (Exception e) {
            System.err.println("❌ Error al cargar el historial con EnigmaReload: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean registrarEscritura(HistorialRegistroDTO registro) {
        List<HistorialRegistroDTO> historial = obtenerHistorial();
        historial.add(0, registro); // Orden inverso (más reciente primero)

        try {
            String jsonTexto = mapper.writeValueAsString(historial);
            encryptionService.guardarEncriptado(archivoHistorial, jsonTexto);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error al guardar en el historial: " + e.getMessage());
            return false;
        }
    }
}