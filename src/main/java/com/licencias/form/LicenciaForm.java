package com.licencias.form;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 📄 Formulario para solicitar licencias.
 */
public class LicenciaForm {

    @NotNull(message = "El ID del empleado es obligatorio")
    @Min(value = 1, message = "ID del empleado inválido")
    private Long idEmpleado;

    @NotNull(message = "El legajo del empleado es obligatorio")
    private Integer legajo;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @Min(value = 1, message = "Debe solicitar al menos 1 día")
    private int diasSolicitados;

    // ✅ Cambiar `int` por `Integer` para evitar errores con `null`
    private Integer plusVacacional; // 0 = No, 1 = Sí (ahora permite valores `null`)

    // ✅ Constructor vacío
    public LicenciaForm() {
        this.plusVacacional = 0; // ✅ Siempre inicia en 0 si no se establece otro valor
    }

    // ✅ Constructor con parámetros
    public LicenciaForm(Long idEmpleado, Integer legajo, LocalDate fechaInicio, int diasSolicitados, Integer plusVacacional) {
        this.idEmpleado = idEmpleado;
        this.legajo = legajo;
        this.fechaInicio = fechaInicio;
        this.diasSolicitados = diasSolicitados;
        this.plusVacacional = (plusVacacional != null) ? plusVacacional : 0; // ✅ Si es `null`, se asigna `0`
    }

    // ✅ Getters y Setters
    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public Integer getLegajo() { return legajo; }
    public void setLegajo(Integer legajo) { this.legajo = legajo; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public int getDiasSolicitados() { return diasSolicitados; }
    public void setDiasSolicitados(int diasSolicitados) { this.diasSolicitados = diasSolicitados; }

    public Integer getPlusVacacional() { 
        return (plusVacacional != null) ? plusVacacional : 0; // ✅ Evita retornar `null`
    }
    public void setPlusVacacional(Integer plusVacacional) { 
        this.plusVacacional = (plusVacacional != null) ? plusVacacional : 0; // ✅ Si es `null`, asigna `0`
    }
}
