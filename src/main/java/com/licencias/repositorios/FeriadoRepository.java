package com.licencias.repositorios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.licencias.entidades.Feriado;

@Repository
public interface FeriadoRepository extends JpaRepository<Feriado, Long> {
	
    
    boolean existsByFecha(LocalDate fecha);

    List<Feriado> findByFechaBetween(LocalDate inicio, LocalDate fin);
    
    
}