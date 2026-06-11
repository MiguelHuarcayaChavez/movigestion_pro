package com.movigestion.repository;

import com.movigestion.entity.Trip;
import com.movigestion.entity.TripStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Integer> {
    
    @Query("SELECT COUNT(t) > 0 FROM Trip t WHERE t.driver.id = :idTransportista AND t.estado = :estado")
    boolean existsByIdTransportistaAndEstado(@Param("idTransportista") Integer idTransportista, @Param("estado") TripStatusEnum estado);

    @Query("SELECT COUNT(t) > 0 FROM Trip t WHERE t.vehicle.idVehiculo = :idVehiculo AND t.estado = :estado")
    boolean existsByIdVehiculoAndEstado(@Param("idVehiculo") Integer idVehiculo, @Param("estado") TripStatusEnum estado);

    @Query("SELECT t FROM Trip t WHERE t.driver.id = :idTransportista AND t.estado = :estado")
    List<Trip> findByIdTransportistaAndEstado(@Param("idTransportista") Integer idTransportista, @Param("estado") TripStatusEnum estado);

    @Query("SELECT COUNT(t) FROM Trip t WHERE YEAR(t.fechaCreacion) = YEAR(CURRENT_DATE) AND MONTH(t.fechaCreacion) = MONTH(CURRENT_DATE)")
    long countTripsCurrentMonth();

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.estado = 'COMPLETADO' AND YEAR(t.fechaCreacion) = YEAR(CURRENT_DATE) AND MONTH(t.fechaCreacion) = MONTH(CURRENT_DATE)")
    long countCompletedTripsCurrentMonth();

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.estado = 'EN_CAMINO'")
    long countDriversEnRuta();
}
