package com.licencias.servicios;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // ✅ CORRECTO
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.licencias.dto.LicenciaDTO;
import com.licencias.entidades.Auditoria;
import com.licencias.entidades.Empleados;
import com.licencias.entidades.EstadoEmpleado;
import com.licencias.entidades.EstadoLicencia;
import com.licencias.entidades.Licencias;
import com.licencias.entidades.Rol;
import com.licencias.entidades.SaldoLicencia;
import com.licencias.entidades.Usuario;
import com.licencias.form.LicenciaForm;
import com.licencias.repositorios.AuditoriaRepository;
import com.licencias.repositorios.EmpleadoRepository;
import com.licencias.repositorios.LicenciaRepository;
import com.licencias.repositorios.SaldoLicenciaRepository;
import com.licencias.repositorios.UsuarioRepository;

import jakarta.servlet.http.HttpServletRequest;



@Service
public class LicenciaService {

    private final LicenciaRepository licenciaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final SaldoLicenciaRepository saldoLicenciaRepository;
    private final FeriadoService feriadoService;

    private static final Logger logger = LoggerFactory.getLogger(LicenciaService.class);

    public LicenciaService(
            LicenciaRepository licenciaRepository,
            EmpleadoRepository empleadoRepository,
            UsuarioRepository usuarioRepository,
            AuditoriaRepository auditoriaRepository,
            SaldoLicenciaRepository saldoLicenciaRepository,
            FeriadoService feriadoService) {

        this.licenciaRepository = licenciaRepository;
        this.empleadoRepository = empleadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.saldoLicenciaRepository = saldoLicenciaRepository;
        this.feriadoService = feriadoService;
    }
    
    @Autowired
    private AuditoriaService auditoriaService; 

    /**
     * 📌 Obtener todas las licencias sin paginación, solo de empleados ACTIVOS.
     */
    public List<LicenciaDTO> obtenerTodas() {
        return licenciaRepository.findAll()
                .stream()
                .filter(licencia -> licencia.getEmpleado().getEstado() == EstadoEmpleado.ACTIVO) // Filtrar empleados activos
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * 📌 Obtener todas las licencias con paginación, solo de empleados ACTIVOS.
     */
    public Page<LicenciaDTO> obtenerTodas(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Licencias> licenciasPaginadas = licenciaRepository.findAll(pageable); // ✅ corregido

        List<LicenciaDTO> licenciasActivas = licenciasPaginadas
                .stream()
                .filter(licencia -> licencia.getEmpleado().getEstado() == EstadoEmpleado.ACTIVO)
                .map(this::convertirADTO)
                .collect(Collectors.toList());

        return new PageImpl<>(licenciasActivas, pageable, licenciasActivas.size());
    }



   

    /**
     * 📌 Obtener licencias por legajo.
     */
    public List<LicenciaDTO> obtenerPorLegajo(Integer legajo) {
        return licenciaRepository.findByEmpleadoLegajo(legajo)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * 📌 Buscar licencias por DNI.
     */
    public List<LicenciaDTO> buscarPorDni(String dni) {
        return licenciaRepository.findByEmpleadoDni(dni)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * 📌 Obtener los feriados desde la base de datos.
     */
    public Set<LocalDate> obtenerFeriados() {
        return feriadoService.obtenerFeriados();
    }

    /**
     * 📌 Aprobar una licencia y descontar saldo.
     */
    @Transactional
    public void aprobarLicencia(Long idLicencia, String usuarioResponsable, String ipOrigen) {
        Licencias licencia = licenciaRepository.findById(idLicencia)
            .orElseThrow(() -> new IllegalArgumentException("❌ No se encontró la licencia con ID: " + idLicencia));

        Empleados empleado = licencia.getEmpleado();
        int diasSolicitados = licencia.getDiasSolicitados();

        List<SaldoLicencia> saldos = saldoLicenciaRepository.findByEmpleado_IdEmpleadoOrderedByAnioAsc(empleado.getIdEmpleado());

        if (saldos.isEmpty()) {
            throw new IllegalStateException("⚠️ No hay saldo registrado para el empleado.");
        }

        int diasRestantesPorDescontar = diasSolicitados;

        for (SaldoLicencia saldo : saldos) {
            if (diasRestantesPorDescontar <= 0) break;

            int saldoDisponible = saldo.getDiasRestantes();
            int descuento = Math.min(saldoDisponible, diasRestantesPorDescontar);

            int filasActualizadas = saldoLicenciaRepository.descontarDiasDeSaldo(saldo.getIdSaldo(), descuento);

            if (filasActualizadas == 0) {
                throw new IllegalStateException("❌ Error al actualizar saldo para el año " + saldo.getAnio());
            }

            diasRestantesPorDescontar -= descuento;
        }

        if (diasRestantesPorDescontar > 0) {
            throw new IllegalStateException("❌ No hay suficiente saldo de licencia para aprobar esta solicitud.");
        }

        licencia.setEstado(EstadoLicencia.APROBADA);
        licenciaRepository.save(licencia);

        Auditoria auditoria = new Auditoria(
            empleado,
            licencia,
            "APROBACION",
            "Se aprobó la licencia con ID " + licencia.getIdLicencia() +
                " y se descontaron " + diasSolicitados + " días del saldo.",
            usuarioResponsable,
            ipOrigen
        );

        auditoriaRepository.save(auditoria);
    }

    
    @Transactional
    public void rechazarLicencia(Integer legajo, Long aprobadorId) {
        Licencias licencia = licenciaRepository.findByEmpleadoLegajoAndEstado(legajo, EstadoLicencia.PENDIENTE)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encontraron licencias pendientes para el legajo: " + legajo));

        Usuario aprobador = usuarioRepository.findById(aprobadorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + aprobadorId));

        licencia.setEstado(EstadoLicencia.RECHAZADA);
        licencia.setAprobador(aprobador);
        licenciaRepository.save(licencia);
    }
    
    @Transactional
    public void eliminarLicenciaRechazada(Long idLicencia) {
        Licencias licencia = licenciaRepository.findById(idLicencia)
                .orElseThrow(() -> new IllegalArgumentException("❌ No se encontró la licencia con ID: " + idLicencia));

        if (!licencia.getEstado().equals(EstadoLicencia.RECHAZADA)) {
            throw new IllegalStateException("⚠️ Solo se pueden eliminar licencias que estén en estado RECHAZADA.");
        }

        // Elimina auditorías primero
        
        licenciaRepository.delete(licencia);                // Luego la licencia
    }

    
    @Transactional
    public void cambiarEstadoLicencia(Integer legajo, EstadoLicencia nuevoEstado, Long aprobadorId) {
        List<Licencias> licenciasPendientes = licenciaRepository.findByEmpleadoLegajoAndEstado(legajo, EstadoLicencia.PENDIENTE);

        if (licenciasPendientes.isEmpty()) {
            throw new IllegalStateException("No se encontraron licencias pendientes para el legajo: " + legajo);
        }

        Usuario aprobador = usuarioRepository.findById(aprobadorId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + aprobadorId));

        if (!aprobador.getRol().equals(Rol.AUTORIZADOR)) {
            throw new IllegalStateException("El usuario no tiene permisos para aprobar/rechazar licencias.");
        }

        Licencias licencia = licenciasPendientes.get(0);
        licencia.setEstado(nuevoEstado);
        licencia.setAprobador(aprobador);
        Licencias licenciaGuardada = licenciaRepository.save(licencia); // 👈 esto es CLAVE

        Empleados empleado = licenciaGuardada.getEmpleado();

        auditoriaService.registrarAccion(
            empleado,
            licenciaGuardada,
            "APROBACION",
            "Se aprobó la licencia con ID " + licenciaGuardada.getIdLicencia(),
            aprobador.getEmail(),
            "127.0.0.1" // podés luego sacar esta IP del request real
        );
    }

    @Transactional
    public LicenciaDTO solicitarLicencia(Integer legajo, LicenciaForm licenciaForm, int plusVacacional) {
        Empleados empleado = empleadoRepository.findByLegajo(legajo)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado con legajo: " + legajo));

        LocalDate fechaInicio = licenciaForm.getFechaInicio();
        int diasSolicitados = licenciaForm.getDiasSolicitados();

        Set<LocalDate> feriados = feriadoService.obtenerFeriados();
        LocalDate fechaFin = calcularFechaFin(fechaInicio, diasSolicitados, feriados);

        boolean existeSolapamiento = licenciaRepository
                .existsByEmpleadoLegajoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        legajo, fechaFin, fechaInicio);

        if (existeSolapamiento) {
            throw new IllegalStateException("⚠️ Ya existe una licencia para ese empleado en el período solicitado.");
        }

        Licencias licencia = new Licencias(empleado, fechaInicio, fechaFin, diasSolicitados, EstadoLicencia.PENDIENTE);
        licencia.setPlusVacacional(plusVacacional);
        System.out.println("👤 Empleado a asociar: " + empleado.getIdEmpleado() + " - " + empleado.getNombreCompleto());
        System.out.println("✅ ¿ID null? " + (empleado.getIdEmpleado() == null));
        licencia.setPlusVacacional(plusVacacional);

        System.out.println("🧪 ID empleado: " + empleado.getIdEmpleado());
        System.out.println("🧪 ¿Es null? " + (empleado.getIdEmpleado() == null));
        System.out.println("🧪 Empleado completo: " + empleado);

        licenciaRepository.save(licencia);

        licenciaRepository.save(licencia);

        // ✅ Registrar auditoría
        String emailUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
        String ipOrigen = obtenerIpCliente(); // ⚠️ Asegúrate que este método exista o lo implementamos

        Auditoria auditoria = new Auditoria(
            empleado,
            licencia,
            "SOLICITUD",
            "El empleado solicitó una licencia desde " + fechaInicio + " hasta " + fechaFin + " (" + diasSolicitados + " días)",
            emailUsuario,
            ipOrigen
        );
        auditoriaRepository.save(auditoria);

        return convertirADTO(licencia);
    }
    private String obtenerIpCliente() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr != null) {
            HttpServletRequest request = attr.getRequest();
            return request.getRemoteAddr();
        }
        return "127.0.0.1"; // Fallback por si no se pudo obtener
    }


    /**
     * 📌 Calcula la fecha de fin evitando fines de semana y feriados.
     */
    private LocalDate calcularFechaFin(LocalDate fechaInicio, int diasSolicitados, Set<LocalDate> feriados) {
        LocalDate fechaFin = fechaInicio;
        int diasContados = 0;

        while (diasContados < diasSolicitados - 1) { // -1 porque el inicio ya cuenta
            fechaFin = fechaFin.plusDays(1); // Avanza un día

            if (esDiaHabil(fechaFin, feriados)) {
                diasContados++;
            }
        }
        return fechaFin;
    }

    /**
     * ✅ Verifica si una fecha es día hábil.
     */
    private boolean esDiaHabil(LocalDate fecha, Set<LocalDate> feriados) {
        return fecha.getDayOfWeek() != DayOfWeek.SATURDAY &&
               fecha.getDayOfWeek() != DayOfWeek.SUNDAY &&
               !feriados.contains(fecha);
    }


    /**
     * 📌 Convertir entidad `Licencias` a DTO.
     */
    private LicenciaDTO convertirADTO(Licencias licencia) {
        return new LicenciaDTO(
                licencia.getEmpleado().getLegajo(),
                licencia.getEmpleado().getNombre() + " " + licencia.getEmpleado().getApellido(),
                licencia.getFechaInicio(),
                licencia.getFechaFin(),
                licencia.getDiasSolicitados(),
                licencia.getEstado().name(),
                licencia.getPlusVacacional()
        );
    }
}
