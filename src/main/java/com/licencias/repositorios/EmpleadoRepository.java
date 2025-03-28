package com.licencias.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.licencias.entidades.Empleados;

public interface EmpleadoRepository extends JpaRepository<Empleados, Long> {

    /**
     * 🔍 Buscar un empleado por legajo (valor único).
     */
    Optional<Empleados> findByLegajo(Integer legajo);

    /**
     * 🔍 Buscar empleados por DNI (puede haber varios).
     */
    List<Empleados> findByDni(String dni);

    /**
     * 📋 Verificar si un empleado existe por ID.
     */
    boolean existsById(Long id);

    /**
     * 📋 Obtener todos los empleados.
     */
    List<Empleados> findAll();
    

    /**
     * 📌 Buscar empleados por Nombre, Apellido o Legajo (ignora mayúsculas).
     */
    @Query("SELECT e FROM Empleados e WHERE " +
            "LOWER(e.nombre) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
            "LOWER(e.apellido) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
            "CAST(e.legajo AS string) LIKE CONCAT('%', :criterio, '%')")
    List<Empleados> findByNombreOrApellidoOrLegajo(@Param("criterio") String criterio);
}
   

