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

import com.abogados.escrituras.model.PlantillaDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlantillaRepository {

    private final File archivoCatalogo = new File("data/plantillas_catalogo.enc");
    private final ObjectMapper mapper = new ObjectMapper();
    private final EncryptionService encryptionService = new EncryptionService();

    public List<PlantillaDTO> obtenerTodas() {
        if (!archivoCatalogo.exists()) return new ArrayList<>();
        try {
            String jsonDesencriptado = encryptionService.desencriptarArchivo(archivoCatalogo);
            return mapper.readValue(jsonDesencriptado, new TypeReference<List<PlantillaDTO>>() {});
        } catch (Exception e) {
            System.err.println("❌ Error al cargar catálogo de plantillas con EnigmaReload: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean guardarPlantilla(PlantillaDTO plantilla) {
        List<PlantillaDTO> lista = obtenerTodas();
        lista.removeIf(p -> p.getId().equals(plantilla.getId()));
        lista.add(plantilla);

        try {
            String jsonTexto = mapper.writeValueAsString(lista);
            encryptionService.guardarEncriptado(archivoCatalogo, jsonTexto);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error al guardar plantilla en el catálogo: " + e.getMessage());
            return false;
        }
    }
}