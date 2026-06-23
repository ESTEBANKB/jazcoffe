package com.cafeuniv.ppi.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Movimiento de inventario (entrada, salida o ajuste).
 * Registra cada cambio en el stock de productos.
 * 
 * USO DE COLECCIONES:
 * Esta clase NO usa listas ni arreglos directamente. Representa una
 * entidad individual. Las listas se usan a nivel de repositorio para
 * consultar múltiples movimientos.
 */
@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false, length = 20)
    private String tipo; // entrada | salida | ajuste

    @Column(nullable = false)
    private int cantidad;

    private String motivo;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}


