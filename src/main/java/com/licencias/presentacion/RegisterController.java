package com.licencias.presentacion;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.licencias.entidades.Rol;
import com.licencias.entidades.Usuario;
import com.licencias.repositorios.UsuarioRepository;

@RestController
@RequestMapping("/api")  // Cambié la ruta base para que sea diferente a la web
public class RegisterController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    
    public RegisterController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")  // Ahora esta ruta es diferente
    public String registrarUsuario(@RequestParam String email, @RequestParam String password, @RequestParam String role) {
        // Verificar si ya existe un usuario con ese email
        if (usuarioRepository.existsByEmail(email)) {
            return "Error: El email ya está registrado";  // Enviar un mensaje de error
        }

        // Convertir el rol de String a Enum Rol
        Rol rolEnum = Rol.valueOf(role.toUpperCase());

        // Crear un nuevo objeto Usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setPassword(passwordEncoder.encode(password));  // Encriptar la contraseña
        nuevoUsuario.setRol(rolEnum);  // Asignar el rol como Enum
        nuevoUsuario.setActivo(true);  // El usuario está activo por defecto

        // Guardar el usuario en la base de datos
        usuarioRepository.save(nuevoUsuario);

        return "Usuario registrado exitosamente";  // Respuesta a la API
    }
}



