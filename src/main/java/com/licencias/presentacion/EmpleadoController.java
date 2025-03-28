package com.licencias.presentacion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.licencias.entidades.Empleados;
import com.licencias.entidades.SaldoLicencia;
import com.licencias.servicios.EmpleadoService;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;
    
    

    /**
     * 📋 Obtener todos los empleados
     */
    @GetMapping
    public List<Empleados> obtenerTodos() {
        return empleadoService.obtenerTodos();
    }

    /**
     * 🔍 Obtener un empleado por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Empleados> obtenerEmpleadoPorId(@PathVariable Long id) {
        return empleadoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    /**
     * 🔍 Obtener un empleado por legajo
     */
    @GetMapping("/legajo/{legajo}")
    public ResponseEntity<?> obtenerPorLegajo(@PathVariable Integer legajo) {
        Optional<Empleados> empleadoOpt = empleadoService.obtenerPorLegajo(legajo);

        if (empleadoOpt.isPresent()) {
            return ResponseEntity.ok(empleadoOpt.get()); // ✅ Retorna el empleado si existe
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("❌ No se encontró un empleado con legajo: " + legajo);
        }
    }


    /**
     * ➕ Crear un nuevo empleado
     */
    @PostMapping
    public ResponseEntity<?> crearEmpleado(@RequestBody Empleados empleado, @RequestParam int diasPrimerAnio) {
        try {
            Empleados nuevoEmpleado = empleadoService.guardarEmpleado(empleado, diasPrimerAnio);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoEmpleado);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ " + e.getMessage());
        }
    }

    @RestController
    @RequestMapping("/api/empleados")
    public class EmpleadoRestController {

        @Autowired
        private EmpleadoService empleadoService;

        /**
         * ✏️ Actualizar un empleado existente (API REST)
         */
        @PutMapping("/{id}")
        public ResponseEntity<?> actualizarEmpleado(@PathVariable Long id, @RequestBody Empleados empleadoActualizado) {
            try {
                if (empleadoActualizado == null) {
                    return ResponseEntity.badRequest().body("❌ El cuerpo de la solicitud está vacío.");
                }

                Empleados empleadoActualizadoResponse = empleadoService.actualizarEmpleado(id, empleadoActualizado);
                return ResponseEntity.ok(empleadoActualizadoResponse);

            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ No se encontró el empleado con ID: " + id);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Error al actualizar el empleado: " + e.getMessage());
            }
        }
    }

    /**
     * 🔍 Obtener los saldos de un empleado.
     */
    @GetMapping("/{id}/saldos")
    public ResponseEntity<?> obtenerSaldosPorEmpleado(@PathVariable Long id) {
        Optional<Empleados> empleadoOpt = empleadoService.obtenerPorId(id);
        if (empleadoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ No se encontró el empleado con ID: " + id);
        }

        Empleados empleado = empleadoOpt.get();
        List<SaldoLicencia> saldos = empleadoService.obtenerSaldosPorEmpleado(empleado.getIdEmpleado());

        // 📌 Construcción de la respuesta
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("empleado", empleado);
        respuesta.put("saldos", saldos); // 🔹 Se añadió la lista de saldos

        return ResponseEntity.ok(respuesta);
    }

    /**
     * ❌ Eliminar un empleado por ID
     */
    @PostMapping("/empleado/eliminar/{id}")
    public String eliminarEmpleado(@PathVariable Long id, @RequestParam String motivo, RedirectAttributes redirectAttributes) {
        try {
            empleadoService.eliminarEmpleado(id, motivo);
            redirectAttributes.addFlashAttribute("success", "Empleado desactivado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empleados";
    }


    
}


