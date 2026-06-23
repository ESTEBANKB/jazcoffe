# Ejemplo de Reporte de Rendimiento - JazzCoffee POS

## Información General

- **Fecha de Prueba:** 2025-01-XX
- **Versión de la Aplicación:** 0.1.0
- **Sistema Operativo:** Windows 10/11
- **Hardware:**
  - Procesador: [Especificar]
  - RAM Total: [Especificar] GB
  - Disco: [Especificar] (SSD/HDD)

---

## Métricas por Escenario

### 1. Inicio de Aplicación

| Métrica | Valor | Observaciones |
|---------|-------|---------------|
| CPU % Promedio | X.X % | Pico inicial durante carga de clases |
| Memoria RAM (MB) | XXX MB | Memoria inicial después del arranque |
| Disco Lectura (MB/s) | X.XXX MB/s | Carga de archivos y configuración |
| Disco Escritura (MB/s) | X.XXX MB/s | Creación de archivos temporales |
| Tiempo de Inicio | XX segundos | Desde ejecución hasta ventana visible |

**Capturas de Pantalla:**
- `rendimiento_001_inicio.png`
- `rendimiento_002_inicio_estable.png`

---

### 2. Login y Carga Inicial

| Métrica | Valor | Observaciones |
|---------|-------|---------------|
| CPU % Promedio | X.X % | Durante autenticación y carga de dashboard |
| Memoria RAM (MB) | XXX MB | Incremento por carga de datos |
| Disco Lectura (MB/s) | X.XXX MB/s | Consultas a base de datos |
| Disco Escritura (MB/s) | X.XXX MB/s | Logs y sesión |

**Capturas de Pantalla:**
- `rendimiento_003_login.png`
- `rendimiento_004_dashboard_cargado.png`

---

### 3. Navegación Normal

| Métrica | Valor | Observaciones |
|---------|-------|---------------|
| CPU % Promedio | X.X % | Navegación entre módulos |
| Memoria RAM (MB) | XXX MB | Uso estable durante navegación |
| Disco Lectura (MB/s) | X.XXX MB/s | Carga de vistas FXML |
| Disco Escritura (MB/s) | X.XXX MB/s | Mínima escritura |

**Capturas de Pantalla:**
- `rendimiento_005_navegacion_productos.png`
- `rendimiento_006_navegacion_pos.png`

---

### 4. Carga de Productos (Catálogo Grande)

| Métrica | Valor | Observaciones |
|---------|-------|---------------|
| CPU % Promedio | X.X % | Pico durante carga de datos |
| Memoria RAM (MB) | XXX MB | Incremento por datos cargados |
| Disco Lectura (MB/s) | X.XXX MB/s | Lectura de base de datos |
| Disco Escritura (MB/s) | X.XXX MB/s | Cache o índices |
| Tiempo de Carga | XX segundos | Para XXX productos |

**Capturas de Pantalla:**
- `rendimiento_007_carga_productos.png`

---

### 5. Procesamiento de Venta (POS)

| Métrica | Valor | Observaciones |
|---------|-------|---------------|
| CPU % Promedio | X.X % | Durante confirmación de venta |
| Memoria RAM (MB) | XXX MB | Uso durante transacción |
| Disco Lectura (MB/s) | X.XXX MB/s | Validación de stock |
| Disco Escritura (MB/s) | X.XXX MB/s | Guardado de venta y actualización de inventario |
| Tiempo de Procesamiento | X.XX segundos | Por venta |

**Capturas de Pantalla:**
- `rendimiento_008_procesamiento_venta.png`

---

### 6. Generación de Recibo PDF

| Métrica | Valor | Observaciones |
|---------|-------|---------------|
| CPU % Promedio | X.X % | Durante generación de PDF |
| Memoria RAM (MB) | XXX MB | Incremento temporal |
| Disco Lectura (MB/s) | X.XXX MB/s | Lectura de plantilla |
| Disco Escritura (MB/s) | X.XXX MB/s | Escritura del archivo PDF |
| Tiempo de Generación | X.XX segundos | Por recibo |

**Capturas de Pantalla:**
- `rendimiento_009_generacion_pdf.png`

---

### 7. Uso Continuo (5 minutos)

| Métrica | Valor | Observaciones |
|---------|-------|---------------|
| CPU % Promedio | X.X % | Uso promedio durante 5 minutos |
| Memoria RAM (MB) | XXX MB | Uso estable |
| Disco Lectura (MB/s) | X.XXX MB/s | Promedio de lectura |
| Disco Escritura (MB/s) | X.XXX MB/s | Promedio de escritura |
| Pico de CPU | XX.X % | Valor máximo registrado |
| Pico de Memoria | XXX MB | Valor máximo registrado |

**Capturas de Pantalla:**
- `rendimiento_010_uso_continuo_1min.png`
- `rendimiento_011_uso_continuo_3min.png`
- `rendimiento_012_uso_continuo_5min.png`

---

## Resumen General

### Promedios Totales

| Métrica | Promedio | Mínimo | Máximo |
|---------|----------|--------|--------|
| CPU % | X.X % | X.X % | XX.X % |
| Memoria RAM (MB) | XXX MB | XXX MB | XXX MB |
| Disco Lectura (MB/s) | X.XXX MB/s | X.XXX MB/s | X.XXX MB/s |
| Disco Escritura (MB/s) | X.XXX MB/s | X.XXX MB/s | X.XXX MB/s |

---

## Análisis y Conclusiones

### Rendimiento General
- ✅ **Excelente** / ⚠️ **Aceptable** / ❌ **Requiere Optimización**

### Observaciones:
1. [Observación 1 sobre CPU]
2. [Observación 2 sobre Memoria]
3. [Observación 3 sobre Disco I/O]
4. [Observación 4 sobre tiempos de respuesta]

### Recomendaciones:
1. [Recomendación 1]
2. [Recomendación 2]
3. [Recomendación 3]

---

## Archivos Adjuntos

- `rendimiento_report.csv` - Datos completos en formato CSV
- `rendimiento_report_resumen.txt` - Resumen generado automáticamente
- Carpeta `rendimiento_capturas/` - Todas las capturas de pantalla

---

## Notas Adicionales

- [Cualquier nota adicional sobre el entorno de prueba]
- [Condiciones especiales durante la prueba]
- [Problemas encontrados o limitaciones]

