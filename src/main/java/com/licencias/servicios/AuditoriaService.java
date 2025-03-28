package com.licencias.servicios;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licencias.entidades.Auditoria;
import com.licencias.entidades.Empleados;
import com.licencias.entidades.Licencias;
import com.licencias.repositorios.AuditoriaRepository;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    /**
     * Registra una nueva acción en la auditoría del sistema.
     *
     * @param empleado Empleado que realiza la acción
     * @param licencia Licencia afectada (opcional, puede ser null)
     * @param tipoAccion Tipo de acción (Ej: "CREACION", "MODIFICACION", "ELIMINACION")
     * @param detalleAccion Detalle descriptivo de la acción
     * @param usuarioResponsable Usuario que realizó la acción
     * @param ipOrigen Dirección IP desde donde se realizó la acción
     */
    @Transactional
    public void registrarAccion(Empleados empleado, Licencias licencia, String tipoAccion, String detalleAccion, String usuarioResponsable, String ipOrigen) {

        // 🔒 Validación por tipo de acción
        if (tipoAccion != null && tipoAccion.equalsIgnoreCase("APROBACION")) {
            if (licencia == null || licencia.getIdLicencia() == null) {
                throw new IllegalArgumentException("No se puede registrar auditoría de APROBACIÓN sin una licencia válida y persistida.");
            }
        }

        Auditoria auditoria;

        if (licencia != null) {
            auditoria = new Auditoria(empleado, licencia, tipoAccion, detalleAccion, usuarioResponsable, ipOrigen);
        } else {
            auditoria = new Auditoria(empleado, tipoAccion, detalleAccion, usuarioResponsable, ipOrigen);
        }
        
        auditoriaRepository.save(auditoria);
    }


    /**
     * Obtiene todas las auditorías de un empleado específico.
     */
    public List<Auditoria> obtenerAuditoriasPorEmpleado(Long empleadoId) {
        return auditoriaRepository.findByEmpleadoIdEmpleado(empleadoId);
    }

    /**
     * Obtiene todas las auditorías de una licencia específica.
     */
    public List<Auditoria> obtenerAuditoriasPorLicencia(Long licenciaId) {
        return auditoriaRepository.findByLicenciaIdLicencia(licenciaId);
    }

    /**
     * Obtiene todas las auditorías en un rango de fechas.
     */
    public List<Auditoria> obtenerAuditoriasPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return auditoriaRepository.findByFechaHoraAccionBetween(inicio, fin);
    }

    /**
     * Obtiene auditorías por tipo de acción (Ej: "CREACION", "MODIFICACION").
     */
    public List<Auditoria> obtenerAuditoriasPorTipo(String tipoAccion) {
        return auditoriaRepository.findByTipoAccion(tipoAccion.toUpperCase());
    }
}
