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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class LocalPersonaRepository {

    private static final String DIRECTORIO_BASE = "data/personas/";
    private final ObjectMapper objectMapper;

    public LocalPersonaRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Formato JSON legible
        crearDirectorioSiNoExiste();
    }

    private void crearDirectorioSiNoExiste() {
        try {
            Path path = Paths.get(DIRECTORIO_BASE);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.err.println("❌ No se pudo crear la carpeta de almacenamiento local: " + e.getMessage());
        }
    }

    /**
     * Serializa y guarda la persona en un archivo .json usando su cédula
     */
    public boolean guardarPersona(PersonaDTO persona) {
        if (persona == null || persona.getIdentificacion() == null || persona.getIdentificacion().isBlank()) {
            return false;
        }

        String fileName = DIRECTORIO_BASE + persona.getIdentificacion().trim() + ".json";
        File file = new File(fileName);

        try {
            objectMapper.writeValue(file, persona);
            System.out.println("💾 Persona serializada correctamente en: " + file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error al serializar los datos de la persona: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deserializa y busca una persona por su número de cédula
     */
    public Optional<PersonaDTO> buscarPorIdentificacion(String identificacion) {
        if (identificacion == null || identificacion.isBlank()) {
            return Optional.empty();
        }

        String fileName = DIRECTORIO_BASE + identificacion.trim() + ".json";
        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println("ℹ️ No se encontró ningún registro local para la cédula: " + identificacion);
            return Optional.empty();
        }

        try {
            PersonaDTO persona = objectMapper.readValue(file, PersonaDTO.class);
            return Optional.of(persona);
        } catch (IOException e) {
            System.err.println("❌ Error al deserializar el archivo JSON: " + e.getMessage());
            return Optional.empty();
        }
    }
}
