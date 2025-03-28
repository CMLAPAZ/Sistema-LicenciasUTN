package com.licencias.servicios;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licencias.entidades.Rol;
import com.licencias.entidades.Usuario;
import com.licencias.repositorios.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 📌 Cargar un usuario por su correo electrónico para autenticación en Spring Security.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("❌ Usuario no encontrado con el email: " + email));

        if (!usuario.isActivo()) {
            throw new DisabledException("⚠️ Usuario inactivo: " + email);
        }

        return User.withUsername(usuario.getEmail())
                   .password(usuario.getPassword())
                   .authorities(new SimpleGrantedAuthority(usuario.getRol().name()))
                   .disabled(!usuario.isActivo()) 
                   .build();
    }

    /**
     * 📌 Registrar un nuevo usuario con correo electrónico y contraseña encriptada.
     */
    @Transactional
    public Usuario registrarUsuario(String email, String password, String rol) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("❌ El correo electrónico ya está en uso.");
        }

        Rol rolEnum = convertirStringARol(rol);

        Usuario nuevoUsuario = new Usuario(email, passwordEncoder.encode(password), rolEnum, true);

        logger.info("✅ Usuario registrado: {} con rol {}", email, rolEnum);

        return usuarioRepository.save(nuevoUsuario);
    }

    /**
     * 📌 Obtener todos los usuarios registrados.
     */
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * 📌 Buscar un usuario por ID.
     */
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * 📌 Buscar usuario por correo electrónico.
     */
    public Optional<Usuario> obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * 📌 Actualizar rol de un usuario.
     */
    @Transactional
    public Usuario actualizarRol(Long id, String nuevoRol) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("❌ Usuario no encontrado."));

        Rol rolEnum = convertirStringARol(nuevoRol);

        usuario.setRol(rolEnum);

        logger.info("🔄 Se actualizó el rol de {} a {}", usuario.getEmail(), rolEnum);

        return usuarioRepository.save(usuario);
    }

    /**
     * 📌 Activar/Desactivar usuario.
     */
    @Transactional
    public Usuario cambiarEstado(Long id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("❌ Usuario no encontrado."));

        usuario.setActivo(activo);

        logger.info("🔄 Estado del usuario {} actualizado a {}", usuario.getEmail(), activo ? "Activo" : "Inactivo");

        return usuarioRepository.save(usuario);
    }

    /**
     * 📌 Eliminar usuario.
     */
    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("❌ Usuario no encontrado para eliminar."));

        usuarioRepository.deleteById(id);

        logger.warn("⚠️ Usuario eliminado: {}", usuario.getEmail());
    }

    /**
     * 📌 Buscar un usuario por su email.
     */
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + email));
    }

    /**
     * 📌 Convertir String a Enum Rol.
     */
    private Rol convertirStringARol(String rol) {
        try {
            return Rol.valueOf(rol.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.error("❌ Error: Rol no válido ({}) - Valores aceptados: {}", rol, java.util.Arrays.toString(Rol.values()));
            throw new IllegalArgumentException("❌ Rol inválido. Valores permitidos: " + java.util.Arrays.toString(Rol.values()));
        }
    }
}
