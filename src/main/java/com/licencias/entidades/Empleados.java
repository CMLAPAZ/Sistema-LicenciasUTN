package com.licencias.entidades;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "empleados", indexes = {
    @Index(name = "idx_legajo", columnList = "legajo"),
    @Index(name = "idx_dni", columnList = "dni"),
    @Index(name = "idx_departamento", columnList = "departamento")
})
public class Empleados {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEmpleado;

    @NotNull(message = "El legajo es obligatorio")
    @Column(nullable = false, unique = true)
    private Integer legajo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede tener más de 50 caracteres")
    @Column(nullable = false, length = 50)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50, message = "El apellido no puede tener más de 50 caracteres")
    @Column(nullable = false, length = 50)
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener exactamente 8 dígitos numéricos")
    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "El departamento es obligatorio")
    @Column(nullable = false, length = 50)
    private Departamento departamento;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaIngreso;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    @JsonIgnore
    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    private List<Licencias> licencias;

    @JsonIgnore
    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    private List<SaldoLicencia> saldoLicencias;

    // 🔹 Estado del empleado (ACTIVO/INACTIVO)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEmpleado estado; 

    // 📌 Auditoría de desactivación
    private LocalDate fechaDesactivacion;
    private String motivoBaja;

    @ManyToOne
    @JoinColumn(name = "desactivado_por")
    private Usuario desactivadoPor;

    // 📌 Constructor por defecto
    public Empleados() {
        this.estado = EstadoEmpleado.ACTIVO; // ✅ Por defecto, el empleado está activo
    }

    // ✅ Getters y Setters
    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }
    public Integer getLegajo() { return legajo; }
    public void setLegajo(Integer legajo) { this.legajo = legajo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public Departamento getDepartamento() { return departamento; }
    public void setDepartamento(Departamento departamento) { this.departamento = departamento; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public List<Licencias> getLicencias() { return licencias; }
    public void setLicencias(List<Licencias> licencias) { this.licencias = licencias; }
    public List<SaldoLicencia> getSaldoLicencias() { return saldoLicencias; }
    public void setSaldoLicencias(List<SaldoLicencia> saldoLicencias) { this.saldoLicencias = saldoLicencias; }

    public EstadoEmpleado getEstado() { return estado; }
    public void setEstado(EstadoEmpleado estado) { this.estado = estado; }

    public LocalDate getFechaDesactivacion() { return fechaDesactivacion; }
    public void setFechaDesactivacion(LocalDate fechaDesactivacion) { this.fechaDesactivacion = fechaDesactivacion; }

    public String getMotivoBaja() { return motivoBaja; }
    public void setMotivoBaja(String motivoBaja) { this.motivoBaja = motivoBaja; }

    public Usuario getDesactivadoPor() { return desactivadoPor; }
    public void setDesactivadoPor(Usuario desactivadoPor) { this.desactivadoPor = desactivadoPor; }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    @Override
    public String toString() {
        return "Empleado{" +
                "idEmpleado=" + idEmpleado +
                ", legajo=" + legajo +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", dni='" + dni + '\'' +
                ", departamento='" + departamento + '\'' +
                ", fechaIngreso=" + fechaIngreso +
                ", fechaNacimiento=" + fechaNacimiento +
                ", estado=" + estado +
                ", fechaDesactivacion=" + fechaDesactivacion +
                ", motivoBaja='" + motivoBaja + '\'' +
                ", desactivadoPor=" + (desactivadoPor != null ? desactivadoPor.getEmail() : "N/A") +
                '}';
    }

}

