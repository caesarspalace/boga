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
import java.time.LocalDateTime;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HistorialRegistroDTO {

    private String id;
    private String fechaHora;
    private String nombrePlantilla;
    private String clientePrincipal; // Ej: Nombre o Cédula
    private String rutaArchivoGenerado;
    private Map<String, String> valoresUtilizados;

    public HistorialRegistroDTO() {}

    public HistorialRegistroDTO(String id, String fechaHora, String nombrePlantilla, 
                                String clientePrincipal, String rutaArchivoGenerado, 
                                Map<String, String> valoresUtilizados) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.nombrePlantilla = nombrePlantilla;
        this.clientePrincipal = clientePrincipal;
        this.rutaArchivoGenerado = rutaArchivoGenerado;
        this.valoresUtilizados = valoresUtilizados;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public String getNombrePlantilla() { return nombrePlantilla; }
    public void setNombrePlantilla(String nombrePlantilla) { this.nombrePlantilla = nombrePlantilla; }

    public String getClientePrincipal() { return clientePrincipal; }
    public void setClientePrincipal(String clientePrincipal) { this.clientePrincipal = clientePrincipal; }

    public String getRutaArchivoGenerado() { return rutaArchivoGenerado; }
    public void setRutaArchivoGenerado(String rutaArchivoGenerado) { this.rutaArchivoGenerado = rutaArchivoGenerado; }

    public Map<String, String> getValoresUtilizados() { return valoresUtilizados; }
    public void setValoresUtilizados(Map<String, String> valoresUtilizados) { this.valoresUtilizados = valoresUtilizados; }
}
