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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlantillaDTO {

    private String id;
    private String nombre;
    private String categoria; // Ej: "Alquileres", "Vehículos", "Poderes", "Hipotecas"
    private String rutaArchivo;
    private Set<String> variablesDetectadas;

    public PlantillaDTO() {}

    public PlantillaDTO(String id, String nombre, String categoria, String rutaArchivo, Set<String> variablesDetectadas) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.rutaArchivo = rutaArchivo;
        this.variablesDetectadas = variablesDetectadas;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public Set<String> getVariablesDetectadas() { return variablesDetectadas; }
    public void setVariablesDetectadas(Set<String> variablesDetectadas) { this.variablesDetectadas = variablesDetectadas; }

    @Override
    public String toString() {
        return nombre + " (" + categoria + ")";
    }
}
