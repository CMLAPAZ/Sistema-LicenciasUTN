package com.licencias.presentacion;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.licencias.entidades.Departamento;
import com.licencias.entidades.Empleados;
import com.licencias.entidades.SaldoLicencia;
import com.licencias.form.LicenciaForm;
import com.licencias.servicios.EmpleadoService;
import com.licencias.servicios.LicenciaService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/web/empleados") // Prefijo común para todas las rutas de empleados
public class EmpleadoWebController {

    private final EmpleadoService empleadoService;
    private final LicenciaService licenciaService;
    private static final Logger logger = LoggerFactory.getLogger(EmpleadoWebController.class);


    // Constructor para inyectar los servicios
    public EmpleadoWebController(EmpleadoService empleadoService, LicenciaService licenciaService) {
        this.empleadoService = empleadoService;
        this.licenciaService = licenciaService;
    }
    
    
    
    // 📌 Mostrar lista de empleados con filtro de búsqueda
    @GetMapping("/buscar")
    public String buscarEmpleados(@RequestParam(name = "criterio", required = false) String criterio, Model model) {
        List<Empleados> empleados;

        if (criterio != null && !criterio.isEmpty()) {
            empleados = empleadoService.buscarPorNombreOApellidoOLegajo(criterio);
        } else {
            empleados = empleadoService.obtenerTodos();
        }

        model.addAttribute("empleados", empleados);
        return "buscar-empleado"; // 🔹 Nombre del archivo HTML que listará los empleados
    }
    

    /**
     * 📋 Mostrar la lista de empleados en HTML
     */
    @GetMapping
    public String listarEmpleados(Model model) {
        model.addAttribute("empleados", empleadoService.obtenerTodos());
        return "lista-empleados";  // Página que lista todos los empleados
    }
    /**
     * 📌 Eliminar o desactivar un empleado (marcar como INACTIVO en la base de datos)
     *
     * GET  → Muestra un formulario de confirmación con el motivo de baja
     * POST → Recibe el motivo, desactiva al empleado, guarda auditoría y redirige a la lista
     */

    @GetMapping("/{id}/eliminar")
    public String eliminarEmpleadoFormulario(@PathVariable Long id, Model model) {
        logger.info("🛠️ Intentando acceder a la confirmación de eliminación para ID: {}", id);
        System.out.println("🛠️ Intentando acceder a la confirmación de eliminación para ID: " + id);

        Optional<Empleados> empleado = empleadoService.obtenerPorId(id);
        if (empleado.isPresent()) {
            model.addAttribute("empleado", empleado.get());
            logger.info("✅ Se encontró el empleado con ID: {}", id);
            return "confirmar_eliminacion";  // ✅ Debe existir en `src/main/resources/templates`
        }

        logger.warn("⚠️ No se encontró el empleado con ID: {}", id);
        return "redirect:/web/empleados";  // Redirige si no se encuentra el empleado
    }


    @PostMapping("/{id}/eliminar")
    public String eliminarEmpleado(@PathVariable Long id, 
                                   @RequestParam("motivo") String motivo, 
                                   RedirectAttributes redirectAttributes) {
        try {
            if (motivo == null || motivo.trim().isEmpty()) {
                logger.warn("❗ Intento de desactivación sin motivo para ID: {}", id);
                throw new IllegalArgumentException("Debe seleccionar un motivo de baja.");
            }

            logger.info("🧾 Desactivando empleado ID: {} con motivo: {}", id, motivo);
            empleadoService.eliminarEmpleado(id, motivo);
            redirectAttributes.addFlashAttribute("success", "Empleado desactivado correctamente.");
        } catch (Exception e) {
            logger.error("❌ Error al desactivar empleado ID: {} - {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al desactivar el empleado: " + e.getMessage());
        }
        return "redirect:/web/empleados";
    }


 // 📌 Paso 1: Buscar empleado antes de editar
    @GetMapping("/editar")
    public String buscarEmpleadoParaEditar(@RequestParam(name = "criterio", required = false) String criterio, Model model) {
        if (criterio != null && !criterio.isEmpty()) {
            List<Empleados> empleados = empleadoService.buscarPorNombreOApellidoOLegajo(criterio);
            if (!empleados.isEmpty()) {
                model.addAttribute("empleado", empleados.get(0));
            } else {
                model.addAttribute("error", "Empleado no encontrado.");
            }
        }
        return "editar-empleado";
    }

    // 📌 Paso 2: Guardar cambios en empleado
    @PostMapping("/actualizar")
    public String actualizarEmpleado(@ModelAttribute Empleados empleado, RedirectAttributes redirectAttributes) {
        try {
            empleadoService.actualizarEmpleado(empleado);
            redirectAttributes.addFlashAttribute("success", "✅ Empleado actualizado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error al actualizar el empleado.");
        }
        return "redirect:/web/empleados";
    }


    /**
     * 📋 Ver licencias de un empleado
     * GET → http://localhost:8080/web/empleados/{id}/solicitar-licencia
     */
    @GetMapping("/{id}/solicitar-licencia")
    public String solicitarLicencia(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Empleados> empleadoOpt = empleadoService.obtenerPorId(id);

        if (empleadoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Empleado no encontrado.");
            return "redirect:/web/empleados";
        }

        model.addAttribute("empleado", empleadoOpt.get());
        model.addAttribute("licenciaForm", new LicenciaForm());

        return "nueva_licencia"; // Página donde se solicita la nueva licencia
    }

    /**
     * 🆕 Mostrar el formulario para agregar un nuevo empleado
     * GET → http://localhost:8080/web/empleados/nuevo
     */
  
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoEmpleado(Model model) {
        model.addAttribute("empleado", new Empleados());

        // ✅ Enviar los valores reales del Enum Departamento
        model.addAttribute("departamentos", Departamento.values());

        return "nuevo_empleado";  // Página para agregar un empleado
    }

    /**
     * 🆕 Guardar el nuevo empleado
     * POST → http://localhost:8080/web/empleados/guardar
     */
    @PostMapping("/guardar")
    public String guardarEmpleado(@Valid @ModelAttribute Empleados empleado, 
                                  @RequestParam int diasPrimerAnio, 
                                  BindingResult result, 
                                  RedirectAttributes redirectAttributes) {
        try {
            // 🚀 Si hay errores en las validaciones de anotaciones, mostrar mensaje
            if (result.hasErrors()) {
                redirectAttributes.addFlashAttribute("error", "Corrige los errores en el formulario.");
                return "redirect:/web/empleados/nuevo";
            }

            // 📌 **Si el legajo NO está en la base, lo guarda sin problemas**
            boolean legajoExiste = empleadoService.existeLegajo(empleado.getLegajo());
            if (legajoExiste) { 
                redirectAttributes.addFlashAttribute("error", "El legajo " + empleado.getLegajo() + " ya está registrado.");
                return "redirect:/web/empleados/nuevo";
            }

            // 📌 **Validaciones manuales adicionales**
            if (empleado.getDepartamento() == null) {
                redirectAttributes.addFlashAttribute("error", "El departamento es obligatorio.");
                return "redirect:/web/empleados/nuevo";
            }

            if (!empleado.getDni().matches("\\d{7,8}")) {
                redirectAttributes.addFlashAttribute("error", "El DNI debe tener entre 7 y 8 dígitos numéricos.");
                return "redirect:/web/empleados/nuevo";
            }

            // 📌 **Validaciones de fechas**
            LocalDate hoy = LocalDate.now();
            if (empleado.getFechaNacimiento().isAfter(hoy)) {
                redirectAttributes.addFlashAttribute("error", "La fecha de nacimiento no puede ser en el futuro.");
                return "redirect:/web/empleados/nuevo";
            }

            LocalDate edadMinima = empleado.getFechaNacimiento().plusYears(18);
            if (empleado.getFechaIngreso().isBefore(edadMinima)) {
                redirectAttributes.addFlashAttribute("error", "El empleado debe tener al menos 18 años al ingresar.");
                return "redirect:/web/empleados/nuevo";
            }

            // ✅ **GUARDAR el empleado si no existe**
            empleadoService.guardarEmpleado(empleado, diasPrimerAnio);
            redirectAttributes.addFlashAttribute("success", "Empleado agregado con éxito.");
            return "redirect:/web/empleados";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar el empleado: " + e.getMessage());
            return "redirect:/web/empleados/nuevo";
        }
    }
    @GetMapping("/validar-fecha")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validarFecha(@RequestParam String fecha) {
        Map<String, Object> response = new HashMap<>();
        response.put("valida", true); // ✅ Por defecto, la fecha es válida

        try {
            LocalDate fechaIngresada = LocalDate.parse(fecha);
            Set<LocalDate> feriados = licenciaService.obtenerFeriados(); // ✅ Trae los feriados

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



    @GetMapping("/{id}/saldos")
    public String verSaldos(@PathVariable Long id, Model model) {
        // Obtener el empleado con su id usando el servicio
        Empleados empleado = empleadoService.obtenerPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado con id: " + id));

        // Obtener los saldos de licencia del empleado usando el servicio
        List<SaldoLicencia> saldos = empleadoService.obtenerSaldosPorEmpleado(id);

        // Verificar si mostrar advertencia: los saldos del año en curso solo se pueden usar a partir de julio
        boolean mostrarAdvertencia = LocalDate.now().getMonthValue() < 7; // Antes de julio

        // Pasar la información al modelo
        model.addAttribute("empleado", empleado);
        model.addAttribute("saldos", saldos);
        model.addAttribute("mostrarAdvertencia", mostrarAdvertencia);

        return "saldos_licencia"; // Vista que muestra los saldos de licencia
    }
    
    // 📌 Método para mostrar el formulario de edición con el Enum de Departamento
    @GetMapping("/{id}/editar")
    public String editarEmpleado(@PathVariable Long id, Model model) {
        Optional<Empleados> empleado = empleadoService.obtenerPorId(id);

        if (empleado.isPresent()) {
            model.addAttribute("empleado", empleado.get());
            model.addAttribute("departamentos", Arrays.asList(Departamento.values())); // ✅ Enum correcto
            return "editar-empleado"; // Vista Thymeleaf
        } else {
            return "redirect:/web/empleados?error=Empleado no encontrado";
        }
    }
    @GetMapping("/seleccionar/{id}")
    public String seleccionarEmpleado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Empleados> empleado = empleadoService.obtenerPorId(id);

        if (empleado.isPresent()) {
            // 📌 Agregar el ID del empleado como parámetro en la redirección
            redirectAttributes.addAttribute("idEmpleado", empleado.get().getIdEmpleado());
            return "redirect:/web/licencias/nueva";
        } else {
            redirectAttributes.addFlashAttribute("error", "❌ Empleado no encontrado.");
            return "redirect:/web/empleados";
        }
       

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


}
