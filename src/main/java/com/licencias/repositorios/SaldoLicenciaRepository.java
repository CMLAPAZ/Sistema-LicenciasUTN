package com.licencias.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.licencias.entidades.SaldoLicencia;

@Repository
public interface SaldoLicenciaRepository extends JpaRepository<SaldoLicencia, Long> {

    /**
     * 🔍 Buscar todos los saldos de licencia de un empleado por su ID.
     */
    List<SaldoLicencia> findByEmpleado_IdEmpleado(Long idEmpleado);

    /**
     * 🔍 Obtener un saldo de licencia específico por empleado y año.
     */
    Optional<SaldoLicencia> findByEmpleado_IdEmpleadoAndAnio(Long idEmpleado, int anio);

    /**
     * 🔍 Obtener el saldo de licencia más reciente de un empleado (último año registrado).
     */
    Optional<SaldoLicencia> findTopByEmpleado_IdEmpleadoOrderByAnioDesc(Long idEmpleado);

    /**
     * 🔍 Buscar todos los saldos de licencia de múltiples empleados.
     */
    List<SaldoLicencia> findByEmpleado_IdEmpleadoIn(List<Long> empleadosIds);

    /**
     * 🔍 Obtener los saldos de licencia disponibles (donde aún quedan días).
     */
    List<SaldoLicencia> findByEmpleado_IdEmpleadoAndDiasRestantesGreaterThan(Long idEmpleado, int dias);

    /**
     * 🗑️ Eliminar todos los saldos de licencia de un empleado por su ID.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM SaldoLicencia s WHERE s.empleado.idEmpleado = :empleadoId")
    void deleteByEmpleadoId(@Param("empleadoId") Long empleadoId);

    /**
     * 🛠️ Descontar días de saldo de una licencia específica.
     */
    @Transactional
    @Modifying
    @Query("UPDATE SaldoLicencia s SET s.diasRestantes = s.diasRestantes - :dias WHERE s.idSaldo = :saldoId AND s.diasRestantes >= :dias")
    int descontarDiasDeSaldo(@Param("saldoId") Long saldoId, @Param("dias") int dias);
    
    @Query("SELECT s FROM SaldoLicencia s WHERE s.empleado.idEmpleado = :idEmpleado ORDER BY s.anio ASC")
    List<SaldoLicencia> findByEmpleado_IdEmpleadoOrderedByAnioAsc(@Param("idEmpleado") Long idEmpleado);

}

