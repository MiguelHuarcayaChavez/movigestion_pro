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
}
