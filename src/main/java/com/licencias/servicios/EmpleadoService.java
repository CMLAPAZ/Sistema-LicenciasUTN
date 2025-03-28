package com.licencias.servicios;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.licencias.entidades.Auditoria;
import com.licencias.entidades.Empleados;
import com.licencias.entidades.EstadoEmpleado;
import com.licencias.entidades.SaldoLicencia;
import com.licencias.entidades.Usuario;
import com.licencias.repositorios.AuditoriaRepository;
import com.licencias.repositorios.EmpleadoRepository;
import com.licencias.repositorios.SaldoLicenciaRepository;
import com.licencias.repositorios.UsuarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class EmpleadoService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private SaldoLicenciaRepository saldoLicenciaRepository;
    
    @Autowired
    private AuditoriaRepository auditoriaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;



    /**
     * 📋 Obtener todos los empleados
     */
    public List<Empleados> obtenerTodos() {
        return empleadoRepository.findAll();
    }

    /**
     * 🔍 Buscar un empleado por ID
     */
    public Optional<Empleados> obtenerPorId(Long id) {
        return empleadoRepository.findById(id);
    }

    /**
     * 🔍 Buscar un empleado por legajo
     */
    public Optional<Empleados> obtenerPorLegajo(Integer legajo) {
        return empleadoRepository.findByLegajo(legajo);
    }

    

    // 🔍 Buscar empleados por DNI (puede haber varios)
    public List<Empleados> buscarPorDni(String dni) {
        return empleadoRepository.findByDni(dni);
    }
    
    // 🔍 Buscar empleados por nombre, apellido o legajo
    public List<Empleados> buscarPorNombreOApellidoOLegajo(String criterio) {
        if (criterio == null || criterio.trim().isEmpty()) {
            return Collections.emptyList(); // ✅ Devuelve lista vacía si no hay criterio
        }
        return empleadoRepository.findByNombreOrApellidoOrLegajo(criterio);
    }
    
    public boolean existeLegajo(Integer legajo) {
        return empleadoRepository.findByLegajo(legajo).isPresent();
    }


    /**
     * 🆕 Guardar un empleado con verificación de legajo y DNI
     */
    @Transactional
    public Empleados guardarEmpleado(Empleados empleado, int diasPrimerAnio) {
        // 🔎 Validaciones antes de guardar

        // ✅ Verificamos si el legajo ya está registrado (debe ser único)
        if (empleadoRepository.findByLegajo(empleado.getLegajo()).isPresent()) {
            throw new IllegalStateException("❌ ERROR: El legajo ya está registrado.");
        }

        // ✅ Verificamos si el DNI ya está registrado (la lista no debe estar vacía)
        if (!empleadoRepository.findByDni(empleado.getDni()).isEmpty()) {
            throw new IllegalStateException("❌ ERROR: El DNI ya está registrado.");
        }

        // 💾 Guardamos el empleado en la base de datos
        Empleados nuevoEmpleado = empleadoRepository.save(empleado);

        // 📌 Aseguramos que el ID fue asignado correctamente
        entityManager.flush();  
        entityManager.refresh(nuevoEmpleado);

        if (nuevoEmpleado.getIdEmpleado() == null) {
            throw new IllegalStateException("❌ ERROR: No se pudo guardar el empleado.");
        }

        // 🏆 Inicializar los saldos de licencia
        inicializarSaldosLicencia(nuevoEmpleado, diasPrimerAnio);

        return nuevoEmpleado;
    }

    private static final Logger logger = LoggerFactory.getLogger(EmpleadoService.class);

    /**
     * 📌 Inicializar los saldos de licencia de un empleado al ingresar
     */
    @Transactional
    public void inicializarSaldosLicencia(Empleados empleado, int diasPrimerAnio) {
        if (empleado.getIdEmpleado() == null) {
            logger.error("❌ ERROR: El ID del empleado es nulo.");
            throw new IllegalStateException("❌ ERROR: El ID del empleado es nulo.");
        }

        int anioIngreso = empleado.getFechaIngreso().getYear();
        int anioActual = LocalDate.now().getYear();

        logger.info("🛠️ Inicializando saldos para el empleado: {} - ID: {} - Fecha Ingreso: {}",
            empleado.getNombre(), empleado.getIdEmpleado(), empleado.getFechaIngreso());

        // 🔁 Generar saldos desde el año de ingreso hasta el actual
        for (int anio = anioIngreso; anio <= anioActual; anio++) {
            int diasOtorgados = (anio == anioIngreso) ? diasPrimerAnio : calcularDiasPorAntiguedad(anioIngreso, anio);
            
            logger.info("📌 Procesando Año: {} - Días Otorgados: {}", anio, diasOtorgados);

            if (saldoLicenciaRepository.findByEmpleado_IdEmpleadoAndAnio(empleado.getIdEmpleado(), anio).isEmpty()) {
                logger.info("🔍 No existe saldo para el año {}. Creando nuevo saldo...", anio);
                SaldoLicencia saldo = new SaldoLicencia();
                saldo.setEmpleado(empleado);
                saldo.setAnio(anio);
                saldo.setDiasTotales(diasOtorgados);
                saldo.setDiasRestantes(diasOtorgados);
                
                try {
                    saldoLicenciaRepository.save(saldo); // 💾 Guardamos el saldo de licencia
                    logger.info("✅ Saldo guardado: Año: {} - Días Totales: {} - Empleado ID: {}",
                        saldo.getAnio(), saldo.getDiasTotales(), saldo.getEmpleado().getIdEmpleado());
                } catch (Exception e) {
                    logger.error("⛔ ERROR al guardar saldo para el año {}: {}", anio, e.getMessage());
                }
            } else {
                logger.info("✅ El saldo para el año {} ya existe. No se crea duplicado.", anio);
            }
        }
    }

    /**
     * 📌 Calcular los días de licencia según la antigüedad
     */
    public int calcularDiasPorAntiguedad(int anioIngreso, int anioEvaluado) {
        int antiguedad = anioEvaluado - anioIngreso;
        if (antiguedad < 5) return 10;
        if (antiguedad < 10) return 15;
        if (antiguedad < 15) return 20;
        if (antiguedad < 20) return 25;
        return 30;
    }

    /**
     * 📌 Obtener los saldos de licencia de un empleado
     */
    public List<SaldoLicencia> obtenerSaldosPorEmpleado(Long idEmpleado) {
        return saldoLicenciaRepository.findByEmpleado_IdEmpleado(idEmpleado);
    }

 // 📌 MÉTODO 1: Para API REST (Recibe ID y JSON)
    @Transactional
    public Empleados actualizarEmpleado(Long id, Empleados empleadoActualizado) {
        return empleadoRepository.findById(id).map(empleado -> {
            // ✅ Asegurar que ID y Legajo NO SE MODIFIQUEN
            empleado.setNombre(empleadoActualizado.getNombre());
            empleado.setApellido(empleadoActualizado.getApellido());
            empleado.setDni(empleadoActualizado.getDni());
            empleado.setFechaIngreso(empleadoActualizado.getFechaIngreso());
            empleado.setFechaNacimiento(empleadoActualizado.getFechaNacimiento());
            empleado.setDepartamento(empleadoActualizado.getDepartamento());
            empleado.setEstado(empleadoActualizado.getEstado());

            return empleadoRepository.save(empleado); // 💾 Guardamos los cambios
        }).orElseThrow(() -> new IllegalArgumentException("❌ No se encontró el empleado con ID: " + id));
    }

    // 📌 MÉTODO 2: Para WebController (Thymeleaf, recibe solo `Empleado`)
    @Transactional
    public Empleados actualizarEmpleado(Empleados empleadoActualizado) {
        return empleadoRepository.findById(empleadoActualizado.getIdEmpleado()).map(empleado -> {
            // ✅ Asegurar que ID y Legajo NO SE MODIFIQUEN
            empleado.setNombre(empleadoActualizado.getNombre());
            empleado.setApellido(empleadoActualizado.getApellido());
            empleado.setDni(empleadoActualizado.getDni());
            empleado.setFechaIngreso(empleadoActualizado.getFechaIngreso());
            empleado.setFechaNacimiento(empleadoActualizado.getFechaNacimiento());
            empleado.setDepartamento(empleadoActualizado.getDepartamento());
            empleado.setEstado(empleadoActualizado.getEstado());

            return empleadoRepository.save(empleado);
        }).orElseThrow(() -> new IllegalArgumentException("❌ No se encontró el empleado con ID: " + empleadoActualizado.getIdEmpleado()));
    }
    
    
    

    @Transactional
    public void eliminarEmpleado(Long id, String motivo) {
        String emailUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("➡️ Eliminando empleado ID={} por usuario {}", id, emailUsuario);

        Empleados empleado = empleadoRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Empleado no encontrado"));

        if (empleado.getEstado() == EstadoEmpleado.INACTIVO) {
            throw new IllegalStateException("El empleado ya está inactivo.");
        }

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
            .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + emailUsuario));

        empleado.setEstado(EstadoEmpleado.INACTIVO);
        empleado.setFechaDesactivacion(LocalDate.now());
        empleado.setMotivoBaja(motivo);
        empleado.setDesactivadoPor(usuario);
        empleadoRepository.save(empleado);

        auditoriaRepository.save(new Auditoria(
            empleado,
            "BAJA_EMPLEADO",
            "Empleado desactivado por " + usuario.getEmail() + ". Motivo: " + motivo,
            usuario.getEmail(),
            obtenerIpCliente()
        ));

        logger.info("✅ Empleado desactivado correctamente: {}", empleado.getNombreCompleto());
    }

    private String obtenerIpCliente() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getRemoteAddr();
        }
        return "IP no disponible";
    }


    /**
     * ✅ Registrar acción en la auditoría
     */
    private void registrarAuditoria(Empleados empleado, Usuario usuario, String accion, String detalle) {
        Auditoria auditoria = new Auditoria(
                empleado,
                null, // No aplica a licencias
                accion,
                detalle,
                usuario.getEmail(),
                "Sistema"
        );
        auditoriaRepository.save(auditoria);
    }

    

}