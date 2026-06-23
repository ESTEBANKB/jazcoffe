package com.cafeuniv.ppi.service;

import com.cafeuniv.ppi.config.HibernateUtil;
import com.cafeuniv.ppi.domain.Producto;
import com.cafeuniv.ppi.domain.Venta;
import com.cafeuniv.ppi.domain.VentaDetalle;
import org.hibernate.Session;
import org.hibernate.LockOptions;
import org.hibernate.Transaction;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de punto de venta.
 *
 * Responsabilidades:
 * - Confirmar una venta de forma transaccional.
 * - Validar stock y actualizar inventario con bloqueo pesimista.
 * - Registrar detalle de venta y movimiento de inventario.
 * 
 * USO DE COLECCIONES:
 * - Recibe List<VentaDetalle> detalles como parámetro (lista de productos a vender).
 * - Itera sobre esta lista con un for-each para procesar cada producto.
 * - NO usa arreglos, solo List de Java Collections Framework.
 */
public class PosService {
    /**
     * Confirma la venta calculando totales, procesando el pago y afectando inventario.
     *
     * @param detalles LISTA de líneas de venta (producto, cantidad, precio)
     * @param descuentoGlobal descuento total aplicado a la venta
     * @param metodoPago medio de pago
     * @param usuarioId identificador del cajero/usuario
     * @return entidad Venta persistida
     */
    public Venta confirmarVenta(List<VentaDetalle> detalles, double descuentoGlobal, String metodoPago, Long usuarioId) {
        System.out.println("=== CONFIRMANDO VENTA ===");
        System.out.println("Detalles: " + detalles.size() + " productos");
        System.out.println("Descuento: " + descuentoGlobal);
        System.out.println("Método pago: " + metodoPago);
        System.out.println("Usuario ID: " + usuarioId);
        
        // Lista para productos con stock bajo después de la venta
        List<Producto> productosStockBajo = new ArrayList<>();
        
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            System.out.println("Transacción iniciada");

            double subtotal = 0.0;
            // ITERACIÓN sobre la LISTA detalles para calcular subtotal
            for (VentaDetalle d : detalles) {
                subtotal += d.getPrecioUnitario() * d.getCantidad();
                System.out.println("Producto: " + d.getProducto().getNombre() + " - Cantidad: " + d.getCantidad() + " - Precio: " + d.getPrecioUnitario());
            }
            double total = Math.max(0.0, subtotal - descuentoGlobal);
            System.out.println("Subtotal: " + subtotal + " - Total: " + total);

            // Simular pago antes de afectar inventario y confirmar
            PaymentService paymentService = new PaymentService();
            PaymentService.PaymentResult pr = paymentService.pay(metodoPago, total);
            if (!pr.approved) {
                throw new IllegalStateException("Pago rechazado: " + pr.message);
            }
            System.out.println("Pago aprobado. Referencia: " + pr.reference);

            Venta venta = new Venta();
            venta.setUsuarioId(usuarioId);
            venta.setMetodoPago(metodoPago);
            venta.setEstado("confirmada");
            venta.setTotal(total);
            venta.setRefPago(pr.reference);
            session.persist(venta);
            System.out.println("Venta persistida. ID: " + venta.getId());

            // SEGUNDA ITERACIÓN sobre la LISTA detalles para procesar cada producto
            for (VentaDetalle d : detalles) {
                if (d.getCantidad() <= 0) throw new IllegalArgumentException("Cantidad inválida");
                if (d.getPrecioUnitario() < 0) throw new IllegalArgumentException("Precio unitario inválido");

                Producto p = session.get(Producto.class, d.getProducto().getId(), LockOptions.UPGRADE);
                if (p == null) throw new IllegalArgumentException("Producto no encontrado");
                if (p.getStockActual() < d.getCantidad()) throw new IllegalStateException("Stock insuficiente para " + p.getNombre());

                System.out.println("Procesando producto: " + p.getNombre() + " - Stock actual: " + p.getStockActual() + " - Cantidad a vender: " + d.getCantidad());

                p.setStockActual(p.getStockActual() - d.getCantidad());
                session.merge(p);
                
                // Verificar si el stock quedó bajo después de la venta
                int stockDespuesVenta = p.getStockActual();
                int stockMinimo = p.getStockMin();
                if (stockMinimo > 0 && stockDespuesVenta <= stockMinimo) {
                    // Guardar datos del producto con stock bajo (copiar valores para evitar problemas de sesión)
                    // Copiar el nombre y valores de stock antes de que se cierre la sesión
                    String nombreProducto = p.getNombre();
                    Producto productoInfo = new Producto();
                    productoInfo.setNombre(nombreProducto);
                    productoInfo.setStockActual(stockDespuesVenta);
                    productoInfo.setStockMin(stockMinimo);
                    productosStockBajo.add(productoInfo);
                }

                d.setVenta(venta);
                d.setProducto(p);
                session.persist(d);

                com.cafeuniv.ppi.domain.MovimientoInventario m = new com.cafeuniv.ppi.domain.MovimientoInventario();
                m.setProducto(p);
                m.setTipo("salida");
                m.setCantidad(d.getCantidad());
                m.setMotivo("venta");
                m.setUsuarioId(usuarioId);
                session.persist(m);
                
                System.out.println("Detalle y movimiento guardados para: " + p.getNombre());
            }

            tx.commit();
            System.out.println("Transacción confirmada exitosamente. Venta ID: " + venta.getId());
            
            // Mostrar alertas de productos con stock bajo después de commit
            if (!productosStockBajo.isEmpty()) {
                Platform.runLater(() -> {
                    for (Producto productoBajo : productosStockBajo) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("⚠️ Stock Agotándose");
                        alert.setHeaderText("El producto se está agotando");
                        alert.setContentText("Producto: " + productoBajo.getNombre() + 
                                "\n\nStock actual: " + productoBajo.getStockActual() + 
                                "\nStock mínimo: " + productoBajo.getStockMin() +
                                "\n\n¡Surte el producto cuanto antes!");
                        alert.showAndWait();
                    }
                });
            }
            
            return venta;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("ERROR al confirmar venta: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}


