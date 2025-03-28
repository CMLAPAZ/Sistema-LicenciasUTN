package com.licencias.dto;

import java.time.LocalDate;



public class LicenciaDTO {
    private final Integer legajo;
    private final String empleado;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final int diasSolicitados;
    private final String estado;
    private final int anio;
    private final int plusVacacional;

    // Constructor para cuando se pasan los valores directamente
    public LicenciaDTO(Integer legajo, String empleado, LocalDate fechaInicio, LocalDate fechaFin, 
                       int diasSolicitados, String estado, int i) {
        this.legajo = legajo;
        this.empleado = empleado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.diasSolicitados = diasSolicitados;
        this.estado = estado;
        this.plusVacacional = i;
        this.anio = fechaInicio.getYear(); // Asumiendo que el año es la fecha de inicio
    }


    // ✅ Getters
    public Integer getLegajo() { return legajo; }
    public String getEmpleado() { return empleado; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public int getDiasSolicitados() { return diasSolicitados; }
    public String getEstado() { return estado; }
    public int getAnio() { return anio; }
    public String getPlusVacacional() {
        return plusVacacional == 1 ? "Sí" : "No";
    }

    @Override
    public String toString() {
        return "LicenciaDTO{" +
                "legajo=" + legajo +
                ", empleado='" + empleado + '\'' +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", diasSolicitados=" + diasSolicitados +
                ", estado='" + estado + '\'' +
                ", anio=" + anio +
                ", plusVacacional=" + getPlusVacacional() +
                '}';
    }
}

