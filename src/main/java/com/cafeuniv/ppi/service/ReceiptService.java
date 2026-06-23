package com.cafeuniv.ppi.service;

import com.cafeuniv.ppi.domain.Venta;
import com.cafeuniv.ppi.domain.VentaDetalle;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utilidades para construir, exportar e imprimir recibos.
 * 
 * USO DE COLECCIONES:
 * - Recibe List<VentaDetalle> detalles como parámetro en buildReceipt().
 * - Itera sobre la lista con for-each para construir cada línea del recibo.
 * - Usa StringBuilder para concatenar strings (no es una lista pero acumula datos).
 */
public class ReceiptService {
    /**
     * Construye el contenido textual del recibo a partir de la venta y sus detalles.
     * Formato profesional y completo para impresión.
     * 
     * @param venta entidad Venta con metadatos
     * @param detalles LISTA de VentaDetalle con los productos vendidos
     * @param descuento descuento global en pesos
     * @return String con formato de recibo listo para PDF/impresora
     */
    public String buildReceipt(Venta venta, List<VentaDetalle> detalles, double descuento) {
        if (venta == null || detalles == null) {
            throw new IllegalArgumentException("Venta y detalles no pueden ser nulos");
        }

        StringBuilder sb = new StringBuilder();
        
        // ENCABEZADO
        sb.append("═══════════════════════════════════════\n");
        sb.append("       🎵 JAZZ COFFEE - RECIBO 🎵\n");
        sb.append("═══════════════════════════════════════\n\n");
        
        // INFORMACIÓN DE TRANSACCIÓN
        sb.append("Transacción: ").append(String.format("%06d", venta.getId())).append("\n");
        if (venta.getFecha() != null) {
            sb.append("Fecha: ")
              .append(venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
              .append("\n");
        }
        sb.append("Cajero: ").append(venta.getUsuarioId() != null ? "Usuario #" + venta.getUsuarioId() : "Sistema")
          .append("\n");
        sb.append("Método: ").append(venta.getMetodoPago() != null ? venta.getMetodoPago() : "Desconocido")
          .append("\n");
        sb.append("───────────────────────────────────────\n\n");
        
        // DETALLES DE PRODUCTOS (ENCABEZADOS)
        // Usamos el MISMO formato que en las líneas de detalle para que las columnas
        // queden perfectamente alineadas con "Cant", "Unit" y "Subtotal".
        sb.append(String.format("%-22s %5s  %11s %13s\n",
                "Descripción", "Cant", "Unit", "Subtotal"));
        sb.append("───────────────────────────────────────\n");
        
        double subtotal = 0.0;
        // ITERACIÓN sobre la LISTA detalles para construir las líneas del recibo
        for (VentaDetalle d : detalles) {
            if (d.getProducto() == null) continue;
            
            String nombre = d.getProducto().getNombre();
            // Truncar nombre si es muy largo (20 caracteres máximo)
            if (nombre.length() > 19) {
                nombre = nombre.substring(0, 19);
            }
            
            double precioUnit = d.getPrecioUnitario();
            int cantidad = d.getCantidad();
            double sub = precioUnit * cantidad;
            subtotal += sub;
            
            // Formato: Nombre(22) Cant(5) Precio(11) Subtotal(13) -> más espacio entre columnas
            sb.append(String.format("%-22s %5d  $%10.2f $%12.2f\n", 
                nombre, cantidad, precioUnit, sub));
        }
        
        if (detalles.isEmpty()) {
            sb.append("[Sin productos]\n");
        }
        
        sb.append("───────────────────────────────────────\n");
        
        // TOTALES
        double descuentoAbsoluto = Math.max(0, descuento);
        double total = Math.max(0.0, subtotal - descuentoAbsoluto);
        
        sb.append(String.format("Subtotal:              $%10.2f\n", subtotal));
        if (descuentoAbsoluto > 0) {
            sb.append(String.format("Descuento (-):         -$%9.2f\n", descuentoAbsoluto));
        }
        sb.append(String.format("Impuestos:             $%10.2f\n", 0.0)); // Placeholder para impuestos futuros
        sb.append("═══════════════════════════════════════\n");
        sb.append(String.format("TOTAL A PAGAR:         $%10.2f\n", total));
        sb.append("═══════════════════════════════════════\n\n");
        
        // ESTADO Y REFERENCIA
        sb.append("Estado: ").append(venta.getEstado() != null ? venta.getEstado() : "CONFIRMADA")
          .append("\n");
        if (venta.getRefPago() != null && !venta.getRefPago().isBlank()) {
            sb.append("Referencia: ").append(venta.getRefPago()).append("\n");
        }
        
        sb.append("\n───────────────────────────────────────\n");
        sb.append("¡Gracias por su compra!\n");
        sb.append("═══════════════════════════════════════\n");
        
        return sb.toString();
    }

    /**
     * Exporta el recibo a un archivo de texto (.txt) en la carpeta receipts/.
     * Se usa como fallback si falla la generación de PDF.
     * 
     * @param receiptContent contenido textual del recibo
     * @param ventaId ID de la venta para nombrar el archivo
     * @return Path del archivo TXT creado
     * @throws IOException si falla la creación del archivo
     */
    public Path exportToFile(String receiptContent, Long ventaId) throws IOException {
        // VALIDACIÓN: contenido no vacío
        if (receiptContent == null || receiptContent.isBlank()) {
            throw new IllegalArgumentException("El contenido del recibo no puede estar vacío");
        }

        System.out.println("=== EXPORTANDO RECIBO A ARCHIVO TXT ===");
        System.out.println("Venta ID: " + ventaId);
        
        // CREAR directorio si no existe
        Path dir = Paths.get("receipts");
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                System.out.println("Directorio 'receipts' creado: " + dir.toAbsolutePath());
            } else {
                System.out.println("Directorio 'receipts' ya existe: " + dir.toAbsolutePath());
            }
        } catch (Exception e) {
            throw new IOException("No se pudo crear el directorio 'receipts': " + e.getMessage(), e);
        }
        
        // CREAR nombre del archivo
        String filename = "receipt_" + (ventaId != null ? String.format("%06d", ventaId) : System.currentTimeMillis()) + ".txt";
        Path file = dir.resolve(filename);
        System.out.println("Archivo a crear: " + file.toAbsolutePath());
        
        try {
            // Escribir contenido en UTF-8
            Files.write(file, receiptContent.getBytes(StandardCharsets.UTF_8));
            System.out.println("✓ Archivo de texto creado exitosamente: " + file.toAbsolutePath());
            System.out.println("  Tamaño: " + Files.size(file) + " bytes");
        } catch (FileNotFoundException e) {
            throw new IOException("No se pudo escribir en la ubicación: " + file.toAbsolutePath(), e);
        } catch (SecurityException e) {
            throw new IOException("Permisos insuficientes para crear el archivo", e);
        } catch (Exception e) {
            System.err.println("ERROR al crear archivo TXT: " + e.getMessage());
            throw new IOException("Error exportando recibo a TXT: " + e.getMessage(), e);
        }
        
        return file;
    }

    /**
     * Exporta el recibo a PDF usando OpenPDF con fuente monoespaciada.
     * Incluye validaciones y manejo robusto de errores.
     * 
     * @param receiptContent contenido textual del recibo
     * @param ventaId ID de la venta para nombrar el archivo
     * @return Path del archivo PDF creado
     * @throws IOException si falla la creación del PDF
     */
    public Path exportToPdf(String receiptContent, Long ventaId) throws IOException {
        // VALIDACIÓN: contenido no vacío
        if (receiptContent == null || receiptContent.isBlank()) {
            throw new IllegalArgumentException("El contenido del recibo no puede estar vacío");
        }

        System.out.println("=== EXPORTANDO RECIBO A PDF ===");
        System.out.println("Venta ID: " + ventaId);
        
        // CREAR directorio si no existe
        Path dir = Paths.get("receipts");
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                System.out.println("Directorio 'receipts' creado: " + dir.toAbsolutePath());
            } else {
                System.out.println("Directorio 'receipts' ya existe: " + dir.toAbsolutePath());
            }
        } catch (Exception e) {
            throw new IOException("No se pudo crear el directorio 'receipts': " + e.getMessage(), e);
        }
        
        // CREAR nombre del archivo
        String filename = "receipt_" + (ventaId != null ? String.format("%06d", ventaId) : System.currentTimeMillis()) + ".pdf";
        Path file = dir.resolve(filename);
        System.out.println("Archivo PDF a crear: " + file.toAbsolutePath());

        // GENERAR PDF con OpenPDF
        com.lowagie.text.Document document = new com.lowagie.text.Document();
        FileOutputStream fos = null;
        
        try {
            // Abrir stream de salida
            fos = new FileOutputStream(file.toFile());
            
            // Crear escritor PDF
            com.lowagie.text.pdf.PdfWriter.getInstance(document, fos);
            document.open();

            // Configurar fuente monoespaciada (tipo courier para facturas)
            com.lowagie.text.Font monoFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.COURIER, 9, com.lowagie.text.Font.NORMAL
            );
            
            // Crear párrafo con el contenido del recibo
            com.lowagie.text.Paragraph paragraph = new com.lowagie.text.Paragraph(receiptContent, monoFont);
            paragraph.setLeading(10f); // Espaciado entre líneas
            paragraph.setAlignment(com.lowagie.text.Element.ALIGN_LEFT);
            
            // Agregar párrafo al documento
            document.add(paragraph);
            
            System.out.println("✓ PDF creado exitosamente: " + file.toAbsolutePath());
            System.out.println("  Tamaño: " + Files.size(file) + " bytes");
            
        } catch (FileNotFoundException e) {
            throw new IOException("No se pudo escribir en la ubicación: " + file.toAbsolutePath(), e);
        } catch (SecurityException e) {
            throw new IOException("Permisos insuficientes para crear el archivo PDF", e);
        } catch (Exception e) {
            System.err.println("ERROR al crear PDF: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error generando PDF: " + e.getMessage(), e);
        } finally {
            // CERRAR documento
            if (document.isOpen()) {
                try {
                    document.close();
                } catch (Exception e) {
                    System.err.println("Error al cerrar documento PDF: " + e.getMessage());
                }
            }
            
            // CERRAR stream
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar FileOutputStream: " + e.getMessage());
                }
            }
        }
        
        return file;
    }

    /**
     * Imprime el recibo textual a través de JavaFX PrinterJob.
     * Abre un cuadro de diálogo para seleccionar impresora y configuración.
     * 
     * @param receiptContent contenido textual del recibo
     * @return true si la impresión fue exitosa, false si falló o fue cancelada
     */
    public boolean printReceipt(String receiptContent) {
        if (receiptContent == null || receiptContent.isBlank()) {
            System.err.println("ERROR: Contenido del recibo vacío, no se puede imprimir");
            return false;
        }

        try {
            System.out.println("=== IMPRIMIENDO RECIBO ===");
            
            // Crear área de texto con el contenido del recibo
            TextArea textArea = new TextArea(receiptContent);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 10;");
            textArea.setPrefSize(480, 640);

            // Crear escena temporal para renderizar
            StackPane root = new StackPane(textArea);
            Scene scene = new Scene(root);
            Stage tempStage = new Stage();
            tempStage.setScene(scene);

            // Crear job de impresión
            PrinterJob printJob = PrinterJob.createPrinterJob();
            if (printJob == null) {
                System.err.println("ERROR: No hay impresoras disponibles");
                return false;
            }
            
            // Mostrar cuadro de diálogo de impresión
            System.out.println("Abriendo cuadro de diálogo de impresión...");
            boolean proceed = printJob.showPrintDialog(tempStage);
            
            if (!proceed) {
                System.out.println("Impresión cancelada por el usuario");
                tempStage.close();
                return false;
            }
            
            // Ejecutar impresión
            boolean printed = printJob.printPage(textArea);
            if (printed) {
                printJob.endJob();
                System.out.println("✓ Recibo impreso exitosamente");
            } else {
                System.err.println("ERROR: La impresora rechazó la página");
            }
            
            tempStage.close();
            return printed;
            
        } catch (Exception e) {
            System.err.println("ERROR al imprimir recibo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}


