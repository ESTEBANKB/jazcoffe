package com.cafeuniv.ppi.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de cabecera de venta.
 * Contiene totales, método de pago, estado y referencia de pago.
 * 
 * USO DE COLECCIONES:
 * - Utiliza List<VentaDetalle> detalles = new ArrayList<>() para almacenar
 *   las líneas de detalle de cada venta (uno a muchos).
 * - Esta es una lista dinámica que crece según se agreguen productos a la venta.
 */
@Entity
@Table(name = "ventas")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false)
    private double total;

    @Column(name = "metodo_pago", nullable = false)
    private String metodoPago; // efectivo|tarjeta|online (mock)

    @Column(nullable = false)
    private String estado; // confirmada|anulada

    @Column(name = "ref_pago")
    private String refPago;

    /**
     * LISTA de detalles de venta (líneas de productos vendidos).
     * Una venta tiene múltiples detalles, cada uno representando un producto
     * con su cantidad y precio.
     * Tipo: List<VentaDetalle> - ArrayList dinámico que puede crecer.
     */
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<VentaDetalle> detalles = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getRefPago() { return refPago; }
    public void setRefPago(String refPago) { this.refPago = refPago; }
    public List<VentaDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<VentaDetalle> detalles) { this.detalles = detalles; }
}


