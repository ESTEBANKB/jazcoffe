# 🎫 Guía de Uso - Generador de Recibos PDF/TXT

**Componente**: `ReceiptService`  
**Versión**: 1.1 (Mejorada)  
**Última Actualización**: 22 de Noviembre 2024

---

## 📋 Resumen Rápido

El `ReceiptService` genera recibos profesionales en dos formatos:
- **PDF**: Formato de documento portátil (principal)
- **TXT**: Formato texto (fallback si falla PDF)

Incluye opciones de **impresión directa** con selección de impresora.

---

## 🔧 Métodos Disponibles

### 1️⃣ `buildReceipt(Venta venta, List<VentaDetalle> detalles, double descuento)`

**Propósito**: Construir el contenido textual del recibo

**Parámetros**:
- `venta` (Venta): Entidad con datos de la transacción
- `detalles` (List<VentaDetalle>): Productos vendidos
- `descuento` (double): Descuento global en pesos

**Retorno**: String con formato de recibo

**Ejemplo de Uso**:
```java
// En PosController.onConfirmar()
Venta venta = posService.confirmarVenta(carrito, descuento, metodo, usuarioId);
List<VentaDetalle> detalles = new ArrayList<>(carrito);
String contenidoRecibo = receiptService.buildReceipt(venta, detalles, descuento);
```

**Formato del Contenido**:
```
═══════════════════════════════════════
       🎵 JAZZ COFFEE - RECIBO 🎵
═══════════════════════════════════════

Transacción: 000001
Fecha: 22/11/2024 14:35:20
Cajero: Usuario #1
Método: Efectivo
───────────────────────────────────────

Descripción         Cant  Unit    Subtotal
───────────────────────────────────────
Café Americano       2    $5.00   $10.00
───────────────────────────────────────

Subtotal:              $  26.00
Descuento (-):         -$  2.60
Impuestos:             $   0.00
═══════════════════════════════════════
TOTAL A PAGAR:         $  23.40
═══════════════════════════════════════
```

---

### 2️⃣ `exportToPdf(String receiptContent, Long ventaId)`

**Propósito**: Guardar recibo como archivo PDF

**Parámetros**:
- `receiptContent` (String): Contenido del recibo (desde `buildReceipt()`)
- `ventaId` (Long): ID de la venta (para nombre del archivo)

**Retorno**: `Path` del archivo PDF creado

**Lanza Excepciones**:
- `IllegalArgumentException`: Si el contenido está vacío
- `IOException`: Si falla la creación del archivo

**Ejemplo de Uso**:
```java
try {
    Path pdfFile = receiptService.exportToPdf(contenidoRecibo, venta.getId());
    System.out.println("PDF guardado en: " + pdfFile.toAbsolutePath());
} catch (IOException e) {
    System.err.println("Error al generar PDF: " + e.getMessage());
}
```

**Ubicación de Archivos**:
```
proyecto/
└── receipts/
    ├── receipt_000001.pdf   ← Venta #1
    ├── receipt_000002.pdf   ← Venta #2
    └── receipt_000003.pdf   ← Venta #3
```

**Características**:
- ✅ Crea directorio `receipts/` automáticamente si no existe
- ✅ Nombres numerados: `receipt_000001.pdf` (6 dígitos)
- ✅ Formato monoespaciado (Courier)
- ✅ Tamaño de fuente: 9pt
- ✅ Espaciado entre líneas: 10pt

---

### 3️⃣ `exportToFile(String receiptContent, Long ventaId)`

**Propósito**: Guardar recibo como archivo TXT (fallback)

**Parámetros**:
- `receiptContent` (String): Contenido del recibo
- `ventaId` (Long): ID de la venta

**Retorno**: `Path` del archivo TXT creado

**Lanza Excepciones**:
- `IllegalArgumentException`: Si el contenido está vacío
- `IOException`: Si falla la creación del archivo

**Ejemplo de Uso**:
```java
try {
    // Si falla PDF, usar TXT como fallback
    Path txtFile = receiptService.exportToFile(contenidoRecibo, venta.getId());
    System.out.println("Fallback TXT guardado en: " + txtFile.toAbsolutePath());
} catch (IOException e) {
    System.err.println("Error al generar TXT: " + e.getMessage());
}
```

**Características**:
- ✅ Codificación UTF-8
- ✅ Mismo nombre que PDF, extensión `.txt`
- ✅ Formato texto plano (legible en cualquier editor)

---

### 4️⃣ `printReceipt(String receiptContent)`

**Propósito**: Imprimir recibo usando impresora del sistema

**Parámetros**:
- `receiptContent` (String): Contenido del recibo

**Retorno**: `boolean` (true si imprimió, false si canceló o falló)

**Ejemplo de Uso**:
```java
if (receiptService.printReceipt(contenidoRecibo)) {
    System.out.println("Impresión exitosa");
} else {
    System.out.println("Impresión cancelada o falló");
}
```

**Flujo de Impresión**:
```
1. Crear TextArea con contenido
2. Mostrar diálogo de impresora
   ├─ Usuario selecciona impresora
   ├─ Usuario configura (color, orientación, papel)
   └─ Usuario presiona Imprimir o Cancelar
3. Si OK → Enviar a impresora
4. Si Cancelar → Retornar false
5. Limpiar recursos
```

**Características**:
- ✅ Diálogo de configuración de impresora
- ✅ Fuente monoespaciada (Courier New, 10pt)
- ✅ Manejo seguro de recursos
- ✅ Logs detallados de resultado

---

## 🔄 Flujo Completo de Generación de Recibo

### Paso 1: Confirmar Venta
```java
// En PosController.onConfirmar()
Venta venta = posService.confirmarVenta(carrito, descuento, metodo, usuarioId);
```

### Paso 2: Construir Recibo
```java
String contenido = receiptService.buildReceipt(venta, new ArrayList<>(carrito), descuento);
```

### Paso 3: Exportar (PDF + Fallback TXT)
```java
Path archivo;
try {
    archivo = receiptService.exportToPdf(contenido, venta.getId());
} catch (Exception ex) {
    // Si falla PDF, guardar como TXT
    archivo = receiptService.exportToFile(contenido, venta.getId());
}
```

### Paso 4: Mostrar Confirmación
```java
new Alert(Alert.AlertType.INFORMATION, 
    "Venta #" + venta.getId() + " confirmada. Recibo: " + archivo.toAbsolutePath()
).showAndWait();
```

### Paso 5 (Opcional): Imprimir
```java
if (usuario.pidioImprimir()) {
    receiptService.printReceipt(contenido);
}
```

---

## ⚠️ Manejo de Errores

### Escenario 1: Falta Directorio `receipts/`
```
ANTES: java.io.FileNotFoundException
DESPUÉS: Se crea automáticamente ✅
```

### Escenario 2: Contenido Vacío
```
TRY:
    receiptService.buildReceipt(null, null, 0);
    
CATCH:
    IllegalArgumentException: "Venta y detalles no pueden ser nulos"
```

### Escenario 3: Sin Permisos de Escritura
```
TRY:
    receiptService.exportToPdf(contenido, 1L);
    
CATCH:
    IOException: "Permisos insuficientes para crear el archivo PDF"
```

### Escenario 4: Sin Impresoras
```
if (receiptService.printReceipt(contenido)) {
    // OK
} else {
    // No hay impresoras o usuario canceló
    System.err.println("ERROR: No hay impresoras disponibles");
}
```

---

## 📊 Validaciones Automáticas

| Validación | Cuándo | Acción |
|-----------|--------|--------|
| Venta null | `buildReceipt()` | Lanza `IllegalArgumentException` |
| Detalles null | `buildReceipt()` | Lanza `IllegalArgumentException` |
| Contenido vacío | `exportToPdf()` | Lanza `IllegalArgumentException` |
| Contenido vacío | `exportToFile()` | Lanza `IllegalArgumentException` |
| Contenido vacío | `printReceipt()` | Retorna `false` |
| Producto sin nombre | `buildReceipt()` | Se salta (continue) |
| Producto null | `buildReceipt()` | Se salta (continue) |

---

## 🎨 Personalización del Formato

### Cambiar el Nombre de la Tienda

**Archivo**: `ReceiptService.java`, línea ~49

```java
// ACTUAL
sb.append("       🎵 JAZZ COFFEE - RECIBO 🎵\n");

// CAMBIAR A
sb.append("       🏪 MI CAFETERIA - RECIBO 🏪\n");
```

### Cambiar Tamaño de Fuente PDF

**Archivo**: `ReceiptService.java`, línea ~195

```java
// ACTUAL (9pt)
com.lowagie.text.Font monoFont = new com.lowagie.text.Font(
    com.lowagie.text.Font.COURIER, 9, com.lowagie.text.Font.NORMAL
);

// CAMBIAR A (10pt)
com.lowagie.text.Font monoFont = new com.lowagie.text.Font(
    com.lowagie.text.Font.COURIER, 10, com.lowagie.text.Font.NORMAL
);
```

### Agregar Información Adicional (RUC, Dirección, etc.)

Editar `buildReceipt()` para agregar campos:

```java
sb.append("RUC: 1234567890\n");
sb.append("Dirección: Carrera 5 #12-34\n");
sb.append("Teléfono: +57 1 234 5678\n");
```

---

## 📁 Estructura de Archivos Generados

### Archivo PDF
```
receipt_000001.pdf
├─ Tamaño: ~5-10 KB (texto monoespaciado)
├─ Formato: Portable Document Format
├─ Fuente: Courier (monoespaciado)
├─ Codificación: PDF/UTF-8
└─ Nombre: receipt_XXXXXX.pdf (6 dígitos)
```

### Archivo TXT
```
receipt_000001.txt
├─ Tamaño: ~2-5 KB
├─ Formato: Texto plano
├─ Codificación: UTF-8
├─ Editor: Cualquiera (Notepad, VSCode, etc.)
└─ Nombre: receipt_XXXXXX.txt (6 dígitos)
```

---

## 🖨️ Impresión

### Requisitos:
- ✅ Al menos una impresora instalada en el sistema
- ✅ Permisos para acceder a dispositivos de impresión
- ✅ Drivers de impresora actualizados

### Opciones de Impresión (en el diálogo):
- **Impresora**: Seleccionar dispositivo
- **Orientación**: Vertical (Portrait) o Horizontal (Landscape)
- **Tamaño de Papel**: Letter, A4, etc.
- **Color**: A color o B/N
- **Márgenes**: Configurables

### Problemas Comunes:

| Problema | Causa | Solución |
|----------|-------|----------|
| "No hay impresoras" | Sin impresoras instaladas | Instalar driver de impresora |
| Recibo cortado | Papel incorrecto | Cambiar tamaño en diálogo |
| Letra pequeña | Zoom del navegador | Aumentar zoom en sistema |
| Impresión lenta | Impresora lejana (red) | Usar impresora local o esperr |

---

## 📊 Ejemplo Completo en PosController

```java
@FXML
public void onConfirmar() {
    try {
        // 1. Obtener descuento
        double descuento = 0.0;
        try { 
            descuento = Double.parseDouble(descuentoField.getText()); 
        } catch (Exception ignored) {}
        
        // 2. Obtener método de pago
        String metodo = metodoPagoCombo.getSelectionModel().getSelectedItem();
        
        // 3. Confirmar venta (transacción ACID)
        Venta venta = posService.confirmarVenta(
            new ArrayList<>(carrito), 
            descuento, 
            metodo, 
            1L  // usuarioId
        );
        
        // 4. Construir recibo
        String contenidoRecibo = receiptService.buildReceipt(
            venta, 
            new ArrayList<>(carrito), 
            descuento
        );
        
        // 5. Exportar a PDF (con fallback a TXT)
        java.nio.file.Path archivoRecibo;
        try {
            archivoRecibo = receiptService.exportToPdf(contenidoRecibo, venta.getId());
        } catch (Exception ex) {
            // Fallback a TXT si falla PDF
            archivoRecibo = receiptService.exportToFile(contenidoRecibo, venta.getId());
        }
        
        // 6. Mostrar confirmación
        new Alert(
            Alert.AlertType.INFORMATION, 
            "Venta #" + venta.getId() + " confirmada.\n" +
            "Recibo: " + archivoRecibo.getFileName()
        ).showAndWait();
        
        // 7. Limpiar carrito
        carrito.clear();
        updateTotal();
        
        // 8. Recargar productos
        productoCombo.setItems(
            FXCollections.observableArrayList(productoRepo.findAll())
        );
        
    } catch (Exception e) {
        new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
    }
}
```

---

## ✅ Checklist de Integración

- [ ] Importar `ReceiptService` en controlador
- [ ] Crear instancia: `ReceiptService receiptService = new ReceiptService();`
- [ ] Llamar `buildReceipt()` después de confirmar venta
- [ ] Llamar `exportToPdf()` o `exportToFile()` para guardar
- [ ] Capturar excepciones `IOException`
- [ ] Mostrar mensajes de éxito/error al usuario
- [ ] Probar con varios productos
- [ ] Probar con descuentos
- [ ] Probar impresión (si hay impresora)
- [ ] Verificar archivos en carpeta `receipts/`

---

## 🚀 Tips de Optimización

### Guardar Sin GUI (para background jobs):
```java
String contenido = receiptService.buildReceipt(venta, detalles, 0);
receiptService.exportToPdf(contenido, venta.getId());
// No llamar printReceipt() en thread de BD
```

### Imprimir Múltiples Recibos:
```java
for (Venta v : ventasDelDia) {
    String contenido = receiptService.buildReceipt(v, v.getDetalles(), 0);
    receiptService.printReceipt(contenido);
}
```

### Enviar por Email (Futuro):
```java
// Extensión propuesta
Path pdfFile = receiptService.exportToPdf(contenido, venta.getId());
emailService.enviarACliente(venta.getEmail(), pdfFile);
```

---

## 📝 Notas Finales

- Los recibos se guardan automáticamente en `receipts/`
- No hay límite de recibos guardados (considerar archivar después de 30 días)
- Los IDs de transacción se formatean como `%06d` (6 dígitos)
- Usar `printReceipt()` solo desde hilo de UI (JavaFX)
- Las excepciones son específicas para mejor debugging

---

**Versión**: 1.1  
**Última Actualización**: 22 de Noviembre 2024  
**Estado**: ✅ PRODUCCIÓN  
