package com.licencias.entidades;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {  // ✅ Implementa UserDetails para Spring Security

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email; // Usamos 'email' como identificador único en lugar de 'username'

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)  // Usamos EnumType.STRING para almacenar el rol como cadena en la base de datos
    @Column(nullable = false)
    private Rol rol; // Usamos el Rol enum para representar los roles

    @Column(nullable = false)
    private boolean activo; // Para habilitar o deshabilitar usuarios
    
    private static final long serialVersionUID = 1L;

    // Constructor
    public Usuario() {}

    public Usuario(String email, String password, Rol rol, boolean activo) {
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.activo = activo;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // 🔹 Spring Security: Define el rol del usuario
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + rol.name())); // Usamos el rol como enum
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return activo; }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", email='" + email + '\'' +  // Mostramos el email
                ", rol=" + rol +  // Mostramos el rol como enum
                ", activo=" + activo +
                '}';
    }

    @Override
    public String getUsername() {
        return this.email;  // Ahora Spring Security usará el email como username
    }

}
