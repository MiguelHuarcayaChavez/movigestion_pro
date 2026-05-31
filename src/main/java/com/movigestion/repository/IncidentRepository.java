package com.movigestion.repository;

import com.movigestion.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Integer> {
    
    @Query("SELECT i FROM Incident i WHERE i.driver.id = :idTransportista ORDER BY i.fechaReporte DESC")
    List<Incident> findByIdTransportistaOrderByFechaReporteDesc(@Param("idTransportista") Integer idTransportista);
}
