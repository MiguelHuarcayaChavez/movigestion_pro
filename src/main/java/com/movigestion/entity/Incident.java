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
@Table(name = "incidencia")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incidencia")
    private Integer idIncidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_envio", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transportista", nullable = false)
    private User driver;

    @Enumerated(EnumType.STRING)
    @Column(name = "clasificacion", nullable = false)
    private IncidentTypeEnum clasificacion;

    @Column(name = "descripcion", columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Column(name = "latitud_reporte", precision = 10, scale = 8, nullable = false)
    private BigDecimal latitudReporte;

    @Column(name = "longitud_reporte", precision = 11, scale = 8, nullable = false)
    private BigDecimal longitudReporte;

    @CreationTimestamp
    @Column(name = "fecha_reporte", updatable = false)
    private LocalDateTime fechaReporte;
}
