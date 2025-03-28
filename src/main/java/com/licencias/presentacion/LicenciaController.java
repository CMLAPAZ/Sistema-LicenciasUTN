package com.licencias.presentacion;

import java.security.Principal;
import java.util.List;
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

import com.licencias.dto.LicenciaDTO;
import com.licencias.entidades.Empleados;
import com.licencias.entidades.EstadoLicencia;
import com.licencias.entidades.Licencias;
import com.licencias.entidades.Rol;
import com.licencias.entidades.Usuario;
import com.licencias.form.LicenciaForm;
import com.licencias.servicios.EmpleadoService;
import com.licencias.servicios.LicenciaService;
import com.licencias.servicios.UsuarioService;

@RestController
@RequestMapping("/api/licencias")
public class LicenciaController {

    @Autowired
    private LicenciaService licenciaService;
    
    @Autowired
    private EmpleadoService empleadoService;
    
    @Autowired
    private UsuarioService usuarioService;


    /**
     * 📌 Obtener todas las licencias
     * GET → /api/licencias
     */
    @GetMapping
    public ResponseEntity<List<LicenciaDTO>> obtenerTodas() {
        try {
            List<LicenciaDTO> licencias = licenciaService.obtenerTodas();
            return ResponseEntity.ok(licencias);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 📌 Solicitar una licencia para un empleado
     * POST → /api/licencias/{id}/solicitar-licencia
     */
    @PostMapping("/{id}/solicitar-licencia")
    public ResponseEntity<LicenciaDTO> solicitarLicencia(@PathVariable Long id, @RequestBody LicenciaForm licenciaForm) {
        try {
            // 🔹 Buscar el empleado por ID
            Optional<Empleados> empleadoOpt = empleadoService.obtenerPorId(id);
            if (empleadoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Empleados empleado = empleadoOpt.get();
            int plusVacacional = licenciaForm.getPlusVacacional();

            // 🔹 Ahora se usa el legajo en lugar del ID
            LicenciaDTO licenciaDTO = licenciaService.solicitarLicencia(empleado.getLegajo(), licenciaForm, plusVacacional);

            return ResponseEntity.status(HttpStatus.CREATED).body(licenciaDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * 📌 Aprobar una licencia
     * PUT → /api/licencias/aprobar/{legajo}/{aprobadorId}
     */
    @PutMapping("/aprobar/{legajo}/{aprobadorId}")
    public ResponseEntity<String> aprobarLicencia(@PathVariable Integer legajo, @PathVariable Long aprobadorId, Principal principal) {
        try {
            // 🔹 Validar usuario autenticado
            Usuario usuario = usuarioService.buscarPorEmail(principal.getName());
            if (!usuario.getRol().equals(Rol.AUTORIZADOR)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tiene permisos para aprobar licencias.");
            }

            // 🔹 Llamar al servicio para cambiar el estado de la licencia
            licenciaService.cambiarEstadoLicencia(legajo, EstadoLicencia.APROBADA, aprobadorId);

            return ResponseEntity.ok("Licencia aprobada correctamente.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Error inesperado: " + e.getMessage());
        }
    }

    /**
     * 📌 Buscar licencias por legajo o devolver todas si no hay filtro
     * GET → /api/licencias/buscar
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<LicenciaDTO>> buscarLicencias(@RequestParam(name = "criterio", required = false) String criterio,
                                                             @RequestParam(name = "valor", required = false) String valor) {
        List<LicenciaDTO> licencias;

        try {
            if ("legajo".equalsIgnoreCase(criterio)) {
                Integer legajo = Integer.parseInt(valor);
                licencias = licenciaService.obtenerPorLegajo(legajo);
            } else if ("dni".equalsIgnoreCase(criterio)) {
                licencias = licenciaService.buscarPorDni(valor);
            } else {
                licencias = licenciaService.obtenerTodas();
            }

            return ResponseEntity.ok(licencias);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * 📌 Endpoint de prueba para verificar que la API está funcionando
     * GET → /api/licencias/test
     */
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("✅ API de Licencias funcionando correctamente.");
    }
    
  

}

