package com.licencias.repositorios;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.licencias.entidades.Auditoria;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findByEmpleadoIdEmpleado(Long empleadoId);

    List<Auditoria> findByLicenciaIdLicencia(Long licenciaId);

    List<Auditoria> findByTipoAccion(String tipoAccion);

    List<Auditoria> findByFechaHoraAccionBetween(LocalDateTime inicio, LocalDateTime fin);
    @Transactional
    @Modifying
    @Query("DELETE FROM Auditoria a WHERE a.licencia.idLicencia = :idLicencia")
    void eliminarPorIdLicencia(@Param("idLicencia") Long idLicencia);
// ✅ ahora sí funciona bien
}
