package com.licencias.presentacion;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.licencias.entidades.Rol;
import com.licencias.entidades.Usuario;
import com.licencias.repositorios.UsuarioRepository;

@Controller
public class RegisterWebController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    
    public RegisterWebController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/web/register")
    public String mostrarFormularioRegistro() {
        return "register";  // Vista de registro
    }

    @PostMapping("/web/register")
    public String registrarUsuario(@RequestParam String email, @RequestParam String password, @RequestParam String role) {
        // ✅ Verificar si ya existe un usuario con ese email
        if (usuarioRepository.existsByEmail(email)) {
            return "redirect:/web/login?error=email";  // Evita duplicados
        }

        // ✅ Declaramos rolEnum antes del try para que esté disponible después
        Rol rolEnum = null;
        try {
            rolEnum = Rol.valueOf(role.toUpperCase());  // Convertimos el String a Enum
        } catch (IllegalArgumentException e) {
            return "redirect:/web/register?error=rol_invalido";  // Evita errores si el rol no existe
        }

        // ✅ Creamos el usuario después de validar el rol
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setPassword(passwordEncoder.encode(password));  // Encriptamos la contraseña
        nuevoUsuario.setRol(rolEnum);  // Asignamos el rol validado
        nuevoUsuario.setActivo(true);  // Usuario activo por defecto

        // ✅ Guardamos el usuario en la base de datos
        usuarioRepository.save(nuevoUsuario);

        // ✅ Redirigir al login después del registro exitoso
        return "redirect:/web/login";
    }
}

