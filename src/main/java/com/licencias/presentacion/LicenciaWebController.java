package com.licencias.presentacion;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.licencias.dto.LicenciaDTO;
import com.licencias.entidades.Empleados;
import com.licencias.entidades.EstadoLicencia;
import com.licencias.entidades.Licencias;
import com.licencias.entidades.Rol;
import com.licencias.entidades.Usuario;
import com.licencias.form.LicenciaForm;
import com.licencias.repositorios.AuditoriaRepository;
import com.licencias.repositorios.LicenciaRepository;
import com.licencias.servicios.EmpleadoService;
import com.licencias.servicios.LicenciaService;
import com.licencias.servicios.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/web/licencias")
public class LicenciaWebController {

    private static final Logger logger = LoggerFactory.getLogger(LicenciaWebController.class);

    private final LicenciaService licenciaService;
    private final EmpleadoService empleadoService;
    private final UsuarioService usuarioService;

    @Autowired
    private LicenciaRepository licenciaRepository; // ✅ Agregado correctamente

    public LicenciaWebController(LicenciaService licenciaService, EmpleadoService empleadoService, UsuarioService usuarioService) {
        this.licenciaService = licenciaService;
        this.empleadoService = empleadoService;
        this.usuarioService = usuarioService;
    }
    
    @Autowired
    private AuditoriaRepository auditoriaRepository;


    /**
     * 📌 Página para solicitar una nueva licencia.
     */
    @GetMapping("/nueva")
    public String nuevaLicencia(@RequestParam(name = "legajo", required = false) Integer legajo, Model model, Principal principal) {
        logger.info("🔹 Legajo recibido: {}", legajo);

        if (principal == null) {
            model.addAttribute("error", "Debe iniciar sesión.");
            return "licencias";
        }

        Usuario usuario = usuarioService.buscarPorEmail(principal.getName());
        logger.info("🔹 Usuario autenticado: {} - Rol: {}", usuario.getEmail(), usuario.getRol());

        if (usuario.getRol() == null || (usuario.getRol() != Rol.ADMIN && usuario.getRol() != Rol.AUTORIZADOR)) {
            model.addAttribute("error", "No tiene permisos para solicitar licencias.");
            return "licencias";
        }

        if (legajo != null) {
            logger.info("🔹 Buscando empleado con legajo: {}", legajo);

            Optional<Empleados> empleadoOpt = empleadoService.obtenerPorLegajo(legajo);

            if (empleadoOpt.isPresent()) {
                Empleados empleado = empleadoOpt.get();
                logger.info("✅ Empleado encontrado: {} {}", empleado.getNombre(), empleado.getApellido());

                LicenciaForm licenciaForm = new LicenciaForm();
                licenciaForm.setLegajo(empleado.getLegajo());

                model.addAttribute("empleado", empleado);
                model.addAttribute("licenciaForm", licenciaForm);
            } else {
                logger.warn("⚠️ No se encontró el empleado con legajo: {}", legajo);
                model.addAttribute("error", "❌ Empleado no encontrado.");
                return "redirect:/web/licencias/seleccionar";
            }
        } else {
            logger.warn("⚠️ No se recibió legajo.");
            model.addAttribute("error", "Debe ingresar un legajo válido.");
            return "redirect:/web/licencias/seleccionar";
        }

        logger.info("🔹 Enviando a la vista nueva_licencia");
        return "nueva_licencia";
    }

    @PostMapping("/guardar")
    public String guardarLicencia(@Valid @ModelAttribute LicenciaForm licenciaForm, 
                                  BindingResult result, 
                                  RedirectAttributes redirectAttributes) {
        System.out.println("➡ Recibida solicitud de guardar licencia para legajo: " + licenciaForm.getLegajo());

        if (result.hasErrors()) {
            System.out.println("⛔ Error en el formulario.");
            redirectAttributes.addFlashAttribute("error", "Corrige los errores en el formulario.");
            return "redirect:/web/licencias/nueva";
        }

        try {
            int plusVacacional = licenciaForm.getPlusVacacional();
            System.out.println("📌 Llamando a solicitarLicencia...");
            licenciaService.solicitarLicencia(licenciaForm.getLegajo(), licenciaForm, plusVacacional);
            System.out.println("✔ Licencia solicitada correctamente.");
            redirectAttributes.addFlashAttribute("success", "Licencia guardada correctamente.");
        } catch (Exception e) {
            System.out.println("⛔ Error al guardar licencia: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al guardar la licencia: " + e.getMessage());
        }

        return "redirect:/web/licencias/listar";
    }


    /**
     * 📌 Listar todas las licencias.
     */
    @GetMapping("/listar")
    public String listarLicencias(Model model, Principal principal) {
        if (principal == null) {
            model.addAttribute("error", "Debe iniciar sesión.");
            return "licencias";
        }

        int page = 0; // Primera página
        int size = 10; // Traer 10 registros por página
        List<LicenciaDTO> licencias = licenciaService.obtenerTodas(page, size).getContent();

        model.addAttribute("licencias", licencias);

        return "lista-licencias";
    }

    /**
     * 📌 Aprobar una licencia.
     */
    @PostMapping("/aprobar/{legajo}")
    public String aprobarLicencia(@PathVariable Integer legajo, Principal principal, 
                                  RedirectAttributes redirectAttributes, HttpServletRequest request) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(principal.getName());

            if (!usuario.getRol().equals(Rol.AUTORIZADOR)) {
                redirectAttributes.addFlashAttribute("error", "No tiene permisos para aprobar la licencia.");
                return "redirect:/web/licencias/listar";
            }

            // 🔍 Buscar la licencia pendiente del empleado
            Optional<Licencias> licenciaOpt = licenciaRepository.findByEmpleadoLegajoAndEstado(legajo, EstadoLicencia.PENDIENTE)
                .stream()
                .findFirst();

            if (licenciaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "❌ No se encontró una licencia pendiente para el empleado.");
                return "redirect:/web/licencias/listar";
            }

            Long idLicencia = licenciaOpt.get().getIdLicencia(); // ✅ Obtener el ID de la licencia encontrada

            // ✅ Obtener la IP real del usuario
            String ipOrigen = request.getRemoteAddr();

            // ✅ Aprobar la licencia con la IP real
            licenciaService.aprobarLicencia(idLicencia, usuario.getUsername(), ipOrigen);

            redirectAttributes.addFlashAttribute("success", "Licencia aprobada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al aprobar la licencia: " + e.getMessage());
        }

        return "redirect:/web/licencias/listar";
    }
    /**
     * 📌 Rechazar una licencia.
     */
    @PreAuthorize("hasRole('AUTORIZADOR')")
    @PostMapping("/rechazar/{legajo}")
    public String rechazarLicencia(@PathVariable Integer legajo, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(principal.getName());

            if (!usuario.getRol().equals(Rol.AUTORIZADOR)) {
                redirectAttributes.addFlashAttribute("error", "No tiene permisos para rechazar la licencia.");
                return "redirect:/web/licencias/listar";
            }

            licenciaService.rechazarLicencia(legajo, usuario.getId());
            redirectAttributes.addFlashAttribute("success", "Licencia rechazada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al rechazar la licencia: " + e.getMessage());
        }

        return "redirect:/web/licencias/listar";
    }

    /**
     * 📌 Buscar licencias por legajo o DNI.
     */

    @GetMapping("/buscar")
    public String buscarLicencias(@RequestParam(name = "criterio", required = false) String criterio,
                                  @RequestParam(name = "valor", required = false) String valor,
                                  Model model) {
        List<LicenciaDTO> licenciasEncontradas = new ArrayList<>();

        // 🔹 Si el usuario elige "Ver Todas", ignoramos el valor ingresado
        if ("todos".equalsIgnoreCase(criterio)) {
            licenciasEncontradas = licenciaService.obtenerTodas();
            model.addAttribute("mensaje", "ℹ️ Mostrando todas las licencias.");
        } 
        else if (criterio == null || valor == null || valor.trim().isEmpty()) {
            model.addAttribute("error", "⚠️ Debe ingresar un valor para buscar.");
        } 
        else {
            switch (criterio.toLowerCase()) {
                case "legajo":
                    if (!valor.matches("\\d+")) {
                        model.addAttribute("error", "⚠️ El legajo debe ser un número válido.");
                        return "lista-licencias";
                    }
                    Integer legajo = Integer.parseInt(valor);
                    licenciasEncontradas = licenciaService.obtenerPorLegajo(legajo);

                    if (licenciasEncontradas.isEmpty()) {
                        model.addAttribute("error", "❌ No se encontraron licencias para el legajo ingresado.");
                    }
                    break;
                case "dni":
                    licenciasEncontradas = licenciaService.buscarPorDni(valor);
                    if (licenciasEncontradas.isEmpty()) {
                        model.addAttribute("error", "❌ No se encontraron licencias para el DNI ingresado.");
                    }
                    break;
                default:
                    model.addAttribute("error", "❌ Criterio de búsqueda inválido.");
                    return "lista-licencias";
            }
        }

        model.addAttribute("licencias", licenciasEncontradas);
        return "lista-licencias";
    }

    @GetMapping("/buscar-empleado")
    public String buscarEmpleadoParaLicencia(@RequestParam(name = "criterio", required = false) String criterio,
                                             @RequestParam(name = "valor", required = false) String valor,
                                             Model model) {
        List<Empleados> empleados = empleadoService.obtenerTodos(); // 🔹 Muestra todos al ingresar

        if (criterio != null && !valor.isEmpty()) {
            Optional<Empleados> empleadoEncontrado = Optional.empty();

            switch (criterio.toLowerCase()) {
                case "legajo":
                    try {
                        Integer legajo = Integer.parseInt(valor);
                        empleadoEncontrado = empleadoService.obtenerPorLegajo(legajo);
                    } catch (NumberFormatException e) {
                        model.addAttribute("error", "⚠️ El legajo debe ser un número válido.");
                        return "seleccionar_empleado";
                    }
                    break;
                case "dni":
                    List<Empleados> empleadosPorDni = empleadoService.buscarPorDni(valor);
                    if (!empleadosPorDni.isEmpty()) {
                        empleadoEncontrado = Optional.of(empleadosPorDni.get(0)); // 🔹 Tomamos el primero de la lista
                    }
                    break;
                default:
                    model.addAttribute("error", "❌ Criterio de búsqueda inválido.");
                    return "seleccionar_empleado";
            }

            if (empleadoEncontrado.isPresent()) {
                model.addAttribute("empleado", empleadoEncontrado.get());
                empleados = null; // Oculta la lista de todos los empleados si se hizo una búsqueda
            } else {
                model.addAttribute("error", "❌ No se encontró ningún empleado con ese criterio.");
            }
        }

        model.addAttribute("empleados", empleados);
        return "seleccionar_empleado"; // 🔹 Retorna la vista con la lista de empleados o un empleado específico
    }

    
    @GetMapping("/seleccionar")
    public String seleccionarEmpleado(@RequestParam(name = "criterio", required = false) String criterio,
                                      @RequestParam(name = "valor", required = false) String valor,
                                      Model model) {
        // ✅ Inicializar la variable para evitar el error
        List<Empleados> empleadosEncontrados = List.of();

        if (criterio == null || valor == null || valor.isEmpty()) {
            empleadosEncontrados = empleadoService.obtenerTodos(); // ✅ Lista todos los empleados
        } else {
            switch (criterio.toLowerCase()) {
            case "legajo":
                try {
                    Integer legajo = Integer.parseInt(valor);
                    Optional<Empleados> empleadoOpt = empleadoService.obtenerPorLegajo(legajo);

                    if (empleadoOpt.isPresent()) {
                        model.addAttribute("empleado", empleadoOpt.get());
                    } else {
                        model.addAttribute("error", "❌ No se encontró un empleado con el legajo: " + legajo);
                    }
                } catch (NumberFormatException e) {
                    model.addAttribute("error", "⚠️ El legajo debe ser un número válido.");
                }
                break;

                case "dni":
                    empleadosEncontrados = empleadoService.buscarPorDni(valor); // ✅ Buscar por DNI
                    break;
                default:
                    model.addAttribute("error", "❌ Criterio de búsqueda inválido.");
                    return "seleccionar_empleado";
            }
        }

        // ✅ Asegurar que la variable siempre esté inicializada antes de pasarla al modelo
        model.addAttribute("empleados", empleadosEncontrados);
        return "seleccionar_empleado";
    }


    @GetMapping("/seleccionar/{id}")
    public String seleccionarEmpleado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // 🔍 Buscar al empleado en la base de datos
        Optional<Empleados> empleadoOptional = empleadoService.obtenerPorId(id);

        if (!empleadoOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Empleado no encontrado.");
            return "redirect:/web/empleados";  // 🔙 Redirige a la lista de empleados
        }

        // ✅ Extraer el empleado del Optional
        Empleados empleado = empleadoOptional.get();

        // 🔄 Redirige a la página para solicitar una nueva licencia con el legajo del empleado seleccionado
        return "redirect:/web/licencias/nueva?legajo=" + empleado.getLegajo();
    }
    @GetMapping("/validar-fecha")
    public ResponseEntity<Map<String, Object>> validarFecha(@RequestParam String fecha) {
        Map<String, Object> response = new HashMap<>();
        response.put("valida", true); // ✅ Por defecto, la fecha es válida

        try {
            LocalDate fechaIngresada = LocalDate.parse(fecha);
            Set<LocalDate> feriados = licenciaService.obtenerFeriados(); // ✅ Llamamos al servicio de licencias

            if (fechaIngresada.getDayOfWeek() == DayOfWeek.SATURDAY ||
                fechaIngresada.getDayOfWeek() == DayOfWeek.SUNDAY ||
                feriados.contains(fechaIngresada)) {

                response.put("valida", false);
                response.put("mensaje", "🚫 La fecha seleccionada es un feriado o cae en fin de semana.");
            }

        } catch (Exception e) {
            response.put("valida", false);
            response.put("mensaje", "❌ Error en el formato de la fecha.");
        }

        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/eliminar/{legajo}")
    public String eliminarLicenciaRechazada(@PathVariable Integer legajo, RedirectAttributes redirectAttributes) {
        List<Licencias> rechazadas = licenciaRepository.findByEmpleado_LegajoAndEstado(legajo, EstadoLicencia.RECHAZADA);

        if (!rechazadas.isEmpty()) {
            Licencias licencia = rechazadas.get(0);

            auditoriaRepository.eliminarPorIdLicencia(licencia.getIdLicencia()); // ✅ primero
            licenciaRepository.delete(licencia);                                  // ✅ luego

            redirectAttributes.addFlashAttribute("mensaje", "✅ Licencia rechazada eliminada correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("error", "⚠️ No se encontró licencia rechazada para este legajo.");
        }

        return "redirect:/web/licencias/listar";
    }

    @GetMapping("/imprimir/{legajo}")
    public String imprimirLicencia(@PathVariable Integer legajo, Model model) {
        Optional<Licencias> licenciaOpt = licenciaRepository.findByEmpleado_LegajoAndEstado(legajo, EstadoLicencia.APROBADA)
                                                            .stream().findFirst();
        if (licenciaOpt.isEmpty()) {
            model.addAttribute("error", "No se encontró una licencia aprobada para imprimir.");
            return "redirect:/web/licencias/listar";
        }

        Licencias licencia = licenciaOpt.get();
        int anio = licencia.getFechaInicio().getYear();

        // Suponemos que tenés una forma de obtener el saldo restante (reemplazá con tu lógica)
        int saldo = empleadoService.obtenerSaldosPorEmpleado(licencia.getEmpleado().getIdEmpleado())
        	    .stream()
        	    .filter(s -> s.getAnio() == anio)
        	    .map(s -> s.getDiasRestantes())
        	    .findFirst()
        	    .orElse(0);


        model.addAttribute("licencia", licencia);
        model.addAttribute("saldoRestante", saldo);
        model.addAttribute("anio", anio);

        return "nota-solicitud";
    }


}












