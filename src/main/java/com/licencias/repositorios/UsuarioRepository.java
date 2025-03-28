package com.licencias.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.licencias.entidades.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);  // Buscar usuario por correo electrónico
    boolean existsByEmail(String email);  // Verificar si el correo electrónico ya está registrado
}
