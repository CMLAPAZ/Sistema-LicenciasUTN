package com.licencias.servicios;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.licencias.entidades.Feriado;
import com.licencias.repositorios.FeriadoRepository;

@Service
public class FeriadoService {


    private FeriadoRepository feriadoRepository;
    
 // 🔹 Inyección de dependencia a través del constructor
    public FeriadoService(FeriadoRepository feriadoRepository) {
        this.feriadoRepository = feriadoRepository;
    }

    /**
     * 📌 Obtiene todos los feriados del año actual desde la base de datos
     */
    public Set<LocalDate> obtenerFeriados() {
        int anioActual = LocalDate.now().getYear();
        return feriadoRepository.findByFechaBetween(
                LocalDate.of(anioActual, 1, 1),
                LocalDate.of(anioActual, 12, 31)
        ).stream()
         .map(Feriado::getFecha)
         .collect(Collectors.toSet());
    }
    
}
