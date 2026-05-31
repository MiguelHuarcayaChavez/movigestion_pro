package com.movigestion.repository;

import com.movigestion.entity.Vehicle;
import com.movigestion.entity.VehicleStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    List<Vehicle> findByEstado(VehicleStatusEnum estado);
    boolean existsByPlaca(String placa);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(v) FROM Vehicle v WHERE v.estado = :estado")
    long countByEstado(@org.springframework.data.repository.query.Param("estado") VehicleStatusEnum estado);
}
