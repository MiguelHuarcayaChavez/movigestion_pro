package com.movigestion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "envio")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_envio")
    private Integer idEnvio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_administrador", nullable = false)
    private User admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transportista")
    private User driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehiculo")
    private Vehicle vehicle;

    @Column(name = "destino_latitud", precision = 10, scale = 8, nullable = false)
    private BigDecimal destinoLatitud;

    @Column(name = "destino_longitud", precision = 11, scale = 8, nullable = false)
    private BigDecimal destinoLongitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private TripStatusEnum estado;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;
}
