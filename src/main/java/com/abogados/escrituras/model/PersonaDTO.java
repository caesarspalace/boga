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
import com.abogados.escrituras.util.EnigmaReload;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonaDTO {

    private String identificacion;
    private String nombreCompleto;
    private String estadoCivil;
    private String ocupacion;
    private String domicilio;
    private String nombreConyuge;
    private String telefono;
    final private EnigmaReload er = new EnigmaReload(123L);
    public PersonaDTO() {
    }

    public PersonaDTO(String identificacion, String nombreCompleto, String estadoCivil, 
                      String ocupacion, String domicilio, String nombreConyuge, String telefono) {
        this.identificacion = identificacion;
        this.nombreCompleto = nombreCompleto;
        this.estadoCivil = estadoCivil;
        this.ocupacion = ocupacion;
        this.domicilio = domicilio;
        this.nombreConyuge = nombreConyuge;
        this.telefono = telefono;
    }

    // Getters y Setters
    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getEstadoCivil() { return estadoCivil; }
    public void setEstadoCivil(String estadoCivil) { this.estadoCivil = estadoCivil; }

    public String getOcupacion() { return ocupacion; }
    public void setOcupacion(String ocupacion) { this.ocupacion = ocupacion; }

    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }

    public String getNombreConyuge() { return nombreConyuge; }
    public void setNombreConyuge(String nombreConyuge) { this.nombreConyuge = nombreConyuge; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
