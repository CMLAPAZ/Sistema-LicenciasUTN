package com.licencias.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/web/login", "/web/register", "/static/**").permitAll() // Rutas públicas
                .anyRequest().authenticated() // Todo lo demás requiere autenticación
            )
            .formLogin(login -> login
                .loginPage("/web/login") // Página de inicio de sesión personalizada
                .defaultSuccessUrl("/web", true) // Redirige al inicio tras login exitoso
                .failureUrl("/web/login?error=true") // Redirige al login si hay error
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")  // URL para cerrar sesión
                .logoutSuccessUrl("/web/login?logout")  // Redirige tras cerrar sesión
                .permitAll()
            );

        return http.build();
    }

    /**
     * 🔴 **Contraseñas SIN encriptar (solo para pruebas)**
     * ⚠️ **No usar en producción**
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // No aplica encriptación a las contraseñas
    }

    /**
     * 📌 Proporciona el AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}

