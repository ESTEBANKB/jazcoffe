package com.cafeuniv.ppi.domain;

import jakarta.persistence.*;

/**
 * El símbolo @ en Java se utiliza para crear anotaciones (annotations). 
 * Las anotaciones son metadatos que proporcionan información sobre el código 
 * sin afectar directamente su funcionamiento, pero que pueden ser procesadas 
 * por herramientas, frameworks o el compilador.
 */


/**
 * Catálogo de producto con precios y stock.
 */
@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(nullable = false)
    private double precio;

    @Column(nullable = false)
    private double costo;

    @Column(name = "stock_actual", nullable = false)
    private int stockActual;

    @Column(name = "stock_min", nullable = false)
    private int stockMin;

    @Column(name = "image_path", length = 255)
    private String imagePath;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public double getCosto() { return costo; }
    public void setCosto(double costo) { this.costo = costo; }
    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }
    public int getStockMin() { return stockMin; }
    public void setStockMin(int stockMin) { this.stockMin = stockMin; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    @Override
    public String toString() {
        return nombre != null ? nombre : "";
    }
}


