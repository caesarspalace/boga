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
package com.abogados.escrituras.model;

import java.io.File;

public class PlantillaEscritura {
    public enum Categoria {
        VEHICULOS,
        INMUEBLES,
        PROCESOS_SUCESORIOS,
        BANCARIOS_AGENCIAS,
        DONACIONES,
        CONTRATOS_GENERALES
    }

    private String id;
    private String nombre;
    private Categoria categoria;
    private File archivoPlantilla; // Archivo .docx

    public PlantillaEscritura(String id, String nombre, Categoria categoria, File archivoPlantilla) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.archivoPlantilla = archivoPlantilla;
    }

    // Getters y Setters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public Categoria getCategoria() { return categoria; }
    public File getArchivoPlantilla() { return archivoPlantilla; }

    @Override
    public String toString() {
        return nombre;
    }
}
