package com.licencias.entidades;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAuditoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleados empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_licencia", nullable = true)
    private Licencias licencia; // Puede ser null según el tipoAccion

    @Column(nullable = false, length = 50)
    private String tipoAccion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String detalleAccion;

    @Column(nullable = false)
    private LocalDateTime fechaHoraAccion;

    @Column(nullable = false, length = 50)
    private String usuarioResponsable;

    @Column(length = 39)
    private String ipOrigen;

    // 🔹 Constructor requerido por JPA
    protected Auditoria() {}

    // 🔹 Constructor sin licencia
    public Auditoria(Empleados empleado, String tipoAccion, String detalleAccion, String usuarioResponsable, String ipOrigen) {
        validarAccion(tipoAccion, null); // validación defensiva
        this.empleado = empleado;
        this.tipoAccion = tipoAccion.toUpperCase();
        this.detalleAccion = detalleAccion;
        this.usuarioResponsable = usuarioResponsable;
        this.fechaHoraAccion = LocalDateTime.now();
        setIpOrigen(ipOrigen);
    }

    // 🔹 Constructor con licencia
    public Auditoria(Empleados empleado, Licencias licencia, String tipoAccion, String detalleAccion, String usuarioResponsable, String ipOrigen) {
        validarAccion(tipoAccion, licencia); // validación defensiva
        this.empleado = empleado;
        this.licencia = licencia;
        this.tipoAccion = tipoAccion.toUpperCase();
        this.detalleAccion = detalleAccion;
        this.usuarioResponsable = usuarioResponsable;
        this.fechaHoraAccion = LocalDateTime.now();
        setIpOrigen(ipOrigen);
    }

    // 🔒 Validación de negocio
    private void validarAccion(String tipoAccion, Licencias licencia) {
        if ("APROBACION".equalsIgnoreCase(tipoAccion) || "RECHAZO".equalsIgnoreCase(tipoAccion)) {
            if (licencia == null || licencia.getIdLicencia() == null) {
                throw new IllegalArgumentException("La acción '" + tipoAccion.toUpperCase() + "' requiere una licencia válida y persistida.");
            }
        }
    }

    // 🔹 Getters
    public Long getIdAuditoria() {
        return idAuditoria;
    }

    public Empleados getEmpleado() {
        return empleado;
    }

    public Licencias getLicencia() {
        return licencia;
    }

    public String getTipoAccion() {
        return tipoAccion;
    }

    public String getDetalleAccion() {
        return detalleAccion;
    }

    public LocalDateTime getFechaHoraAccion() {
        return fechaHoraAccion;
    }

    public String getUsuarioResponsable() {
        return usuarioResponsable;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    // 🔹 Setters útiles
    public void setLicencia(Licencias licencia) {
        this.licencia = licencia;
    }

    public void setIpOrigen(String ipOrigen) {
        if (ipOrigen != null && ipOrigen.length() > 39) {
            throw new IllegalArgumentException("La dirección IP excede los 39 caracteres permitidos");
        }
        this.ipOrigen = ipOrigen;
    }

    public void setFechaHoraAccion(LocalDateTime fechaHoraAccion) {
        this.fechaHoraAccion = fechaHoraAccion;
    }

    @Override
    public String toString() {
        return "Auditoria{" +
                "idAuditoria=" + idAuditoria +
                ", empleado=" + (empleado != null ? empleado.getNombreCompleto() : "N/A") +
                ", licencia=" + (licencia != null ? licencia.getIdLicencia() : "N/A") +
                ", tipoAccion='" + tipoAccion + '\'' +
                ", detalleAccion='" + detalleAccion + '\'' +
                ", fechaHoraAccion=" + fechaHoraAccion +
                ", usuarioResponsable='" + usuarioResponsable + '\'' +
                ", ipOrigen='" + ipOrigen + '\'' +
                '}';
    }
}

