package com.licencias.repositorios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.licencias.entidades.EstadoLicencia;
import com.licencias.entidades.Licencias;

@Repository
public interface LicenciaRepository extends JpaRepository<Licencias, Long> {

    // Buscar todas las licencias de un empleado por su legajo
	List<Licencias> findByEmpleadoLegajo(Integer legajo);
	
	@Query("SELECT l FROM Licencias l WHERE l.empleado.dni = :dni")
	List<Licencias> findByEmpleadoDni(@Param("dni") String dni);


    // Buscar licencias por el nombre del empleado (de forma insensible a mayúsculas/minúsculas)
    List<Licencias> findByEmpleado_NombreContainingIgnoreCase(String nombre);

    // Buscar licencias de un empleado por legajo con paginación
    Page<Licencias> findByEmpleado_Legajo(Integer legajo, Pageable pageable);

    // Buscar licencias de un empleado por legajo y con un rango de fechas
    List<Licencias> findByEmpleado_LegajoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
        Integer legajo, LocalDate fechaInicio, LocalDate fechaFin
    );

    // Verificar si existe alguna licencia para el legajo en el rango de fechas especificado
    boolean existsByEmpleado_LegajoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
        Integer legajo, LocalDate fechaInicio, LocalDate fechaFin
    );

    // Buscar todas las licencias sin ningún filtro (método básico)
    List<Licencias> findAll();

    // Si necesitas buscar por estado de la licencia, podrías agregarlo:
    List<Licencias> findByEstado(EstadoLicencia estado);
 // Método para encontrar licencias de un empleado por su legajo y estado
    List<Licencias> findByEmpleado_LegajoAndEstado(Integer legajo, EstadoLicencia estado);
    
 // 🔹 Buscar licencias de un empleado con estado específico
    List<Licencias> findByEmpleadoLegajoAndEstado(Integer legajo, EstadoLicencia estado);

    // 🔹 Verificar si existe superposición de licencias
    boolean existsByEmpleadoLegajoAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
        Integer legajo, LocalDate fechaInicio, LocalDate fechaFin
    );
    @Query("SELECT l FROM Licencias l WHERE l.empleado.estado = 'ACTIVO'")
    List<Licencias> findLicenciasDeEmpleadosActivos();

    @Query("SELECT l FROM Licencias l WHERE l.empleado.legajo = :legajo AND l.empleado.estado = 'ACTIVO'")
    List<Licencias> findByEmpleadoLegajoActivo(Integer legajo);


}
