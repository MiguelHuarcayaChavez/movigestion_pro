package com.movigestion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehiculo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehiculo")
    private Integer idVehiculo;

    @Column(name = "placa", nullable = false, unique = true, length = 15)
    private String placa;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private VehicleStatusEnum estado;

    @Column(name = "fecha_vencimiento_soat", nullable = false)
    private LocalDate fechaVencimientoSoat;

    @Column(name = "fecha_vencimiento_revision", nullable = false)
    private LocalDate fechaVencimientoRevision;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
}
