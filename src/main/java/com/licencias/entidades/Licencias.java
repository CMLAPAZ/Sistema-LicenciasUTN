package com.licencias.entidades;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "licencias")
public class Licencias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLicencia;

    // Relación con Empleados, garantizando persistencia
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleados empleado;


    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now(); // ✅ Inicialización automática

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    private int diasSolicitados;

    // Estado de la licencia
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoLicencia estado;

    @Column
    private int plusVacacional;

    // Relación con aprobador de licencia
    @ManyToOne
    @JoinColumn(name = "aprobador_id")
    private Usuario aprobador;

    @Column(name = "fecha_aprobacion") 
    private LocalDate fechaAprobacion;

    // Constructor vacío
    public Licencias() {}

    // Constructor con datos principales
    public Licencias(Empleados empleado, LocalDate fechaInicio, LocalDate fechaFin, int diasSolicitados, EstadoLicencia estado) {
        this.empleado = empleado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.diasSolicitados = diasSolicitados;
        this.estado = estado;
        this.plusVacacional = 0;
    }

    // ✅ Getters y Setters corregidos
    public Long getIdLicencia() { return idLicencia; }
    public void setIdLicencia(Long idLicencia) { this.idLicencia = idLicencia; }

    public Empleados getEmpleado() { return empleado; }
    public void setEmpleado(Empleados empleado) { this.empleado = empleado; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public int getDiasSolicitados() { return diasSolicitados; }
    public void setDiasSolicitados(int diasSolicitados) { this.diasSolicitados = diasSolicitados; }

    public EstadoLicencia getEstado() { return estado; }
    public void setEstado(EstadoLicencia estado) { this.estado = estado; } // ✅ Corrección aquí

    public int getPlusVacacional() { return plusVacacional; } // ✅ Corregido nombre
    public void setPlusVacacional(int plusVacacional) { this.plusVacacional = plusVacacional; }

    public Usuario getAprobador() { return aprobador; }
    public void setAprobador(Usuario aprobador) { this.aprobador = aprobador; }

    public LocalDate getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(LocalDate fechaAprobacion) { this.fechaAprobacion = fechaAprobacion; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}

