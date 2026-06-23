# 📋 Documentación Completa de Funcionalidades - JazzCoffee POS

## 🎯 Visión General del Proyecto

**JazzCoffee POS** es un sistema de punto de venta moderno desarrollado en **JavaFX** con **Hibernate** como ORM, diseñado para gestionar cafeterías universitarias. La aplicación permite a administradores y cajeros gestionar productos, ventas, inventario y usuarios de forma eficiente.

- **Tecnología**: JavaFX 22.0.2 + Hibernate 6.6.1 + Java 21
- **Base de Datos**: PostgreSQL (producción) / H2 (desarrollo) / SQLite (alternativa)
- **Autenticación**: BCrypt para contraseñas
- **Generación de Reportes**: PDFs de recibos
- **Migraciones**: Flyway para versionado de BD

---

## 📦 Arquitectura del Proyecto

```
src/main/java/com/cafeuniv/ppi/
├── App.java                          # Punto de entrada JavaFX
├── Bootstrap.java                    # Inicialización (migraciones + datos)
├── config/                           # Configuración
│   ├── HibernateUtil.java           # Singleton SessionFactory
│   ├── FlywayMigrator.java          # Control de migraciones BD
│   └── LoggerConfig.java            # Configuración de logging
├── domain/                           # Entidades JPA
│   ├── Rol.java                     # Roles de usuario (ADMIN, CAJERO)
│   ├── Usuario.java                 # Usuarios del sistema
│   ├── Categoria.java               # Categorías de productos
│   ├── Producto.java                # Catálogo de productos
│   ├── Venta.java                   # Cabecera de ventas
│   ├── VentaDetalle.java            # Líneas de detalle en ventas
│   └── MovimientoInventario.java    # Registro de cambios de stock
├── repository/                       # Acceso a datos (DAOs)
│   ├── UsuarioRepository.java
│   ├── ProductoRepository.java
│   ├── CategoriaRepository.java
│   ├── VentaRepository.java
│   ├── VentaDetalleRepository.java
│   └── MovimientoInventarioRepository.java
├── service/                          # Lógica de negocio
│   ├── AuthService.java             # Autenticación con BCrypt
│   ├── PosService.java              # Confirmación de ventas
│   ├── InventarioService.java       # Movimientos de stock
│   ├── CarritoService.java          # Gestión del carrito
│   ├── PaymentService.java          # Procesamiento de pagos (mock)
│   ├── ReceiptService.java          # Generación de recibos PDF
│   ├── PermissionService.java       # Control de permisos
│   ├── SessionContext.java          # Contexto de sesión global
│   └── PerformanceMonitor.java      # Monitoreo de rendimiento
└── controller/                       # Controladores JavaFX
    ├── WelcomeController.java       # Pantalla de bienvenida
    ├── LoginController.java         # Login (administrador)
    ├── LoginClienteController.java  # Login (cliente/cajero)
    ├── DashboardController.java     # Dashboard administrativo
    ├── DashboardClienteController.java # Dashboard del cliente
    ├── PosController.java           # Pantalla de punto de venta
    ├── ProductosController.java     # CRUD de productos
    ├── CategoriasController.java    # Gestión de categorías
    ├── MovimientosController.java   # Registro de movimientos
    ├── UsuariosController.java      # Gestión de usuarios
    └── RegistroClienteController.java # Registro de nuevos clientes
```

---

## 🔐 Módulo 1: Autenticación y Gestión de Usuarios

### 1.1 Sistema de Autenticación
**Responsable**: `AuthService` + `LoginController` + `LoginClienteController`

#### Funcionalidades:
- ✅ **Login con BCrypt**: Autenticación segura con hash de contraseña
  - Método: `authenticate(String emailOrUser, String password)`
  - Valida credenciales y estado activo del usuario
  - Retorna objeto `Usuario` o `null` si falla

- ✅ **Dos Perfiles de Acceso**:
  - **Admin**: Acceso a todas las funciones administrativas
  - **Cajero**: Acceso limitado a POS y consultas básicas

- ✅ **Persistencia de Sesión**: 
  - `SessionContext` almacena usuario actual en memoria
  - Mantiene información durante toda la sesión

#### Ejemplo de Flujo:
```
Usuario ingresa email + contraseña
        ↓
AuthService.authenticate() verifica BCrypt
        ↓
✓ Válido → SessionContext.setUsuarioActual()
✗ Inválido → Mostrar error y solicitar reintento
```

---

### 1.2 Gestión de Usuarios (CRUD)
**Responsable**: `UsuariosController` + `UsuarioRepository`

#### Funcionalidades:
- ✅ **Crear Usuario**:
  - Email único y validado
  - Contraseña convertida a BCrypt
  - Rol asignado (ADMIN o CAJERO)
  - Estado activo/inactivo configurable

- ✅ **Listar Usuarios**:
  - Tabla con todas las cuentas del sistema
  - Muestra: ID, Nombre, Email, Rol, Estado

- ✅ **Editar Usuario**:
  - Modificar nombre, email, rol y estado
  - Opción de cambiar contraseña (genera nuevo hash)

- ✅ **Eliminar Usuario**:
  - Desactiva el usuario (soft delete)
  - Mantiene historial de auditoría

#### Datos en BD:
```sql
-- Tabla: usuarios
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    hash VARCHAR(255) NOT NULL,        -- BCrypt hash
    rol_id BIGINT NOT NULL REFERENCES roles(id),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    permisos_json TEXT                 -- JSON de permisos personalizados
);
```

---

## 🛍️ Módulo 2: Catálogo de Productos

### 2.1 Gestión de Categorías
**Responsable**: `CategoriasController` + `CategoriaRepository`

#### Funcionalidades:
- ✅ **Crear Categoría**:
  - Nombre único (ej: "Bebidas", "Comidas")
  - Validación de duplicados

- ✅ **Listar Categorías**:
  - Muestra todas las categorías disponibles
  - Usadas en filtros y combos del POS

- ✅ **Editar Categoría**:
  - Cambiar nombre manteniendo integridad referencial

- ✅ **Eliminar Categoría**:
  - Solo si no tiene productos asociados

#### Datos de Prueba:
```
- Bebidas (Café Americano, Capuchino, Latte, etc.)
- Comidas (Sandwich, Pastel, Galleta, etc.)
```

---

### 2.2 Catálogo de Productos (CRUD)
**Responsable**: `ProductosController` + `ProductoRepository`

#### Funcionalidades:
- ✅ **Crear Producto**:
  - Nombre único en el sistema
  - Seleccionar categoría
  - Definir precio de venta (público)
  - Definir costo (compra/producción)
  - Stock inicial
  - Stock mínimo (para alertas)

- ✅ **Listar Productos**:
  - Tabla con: ID, Nombre, Categoría, Precio, Costo, Stock, Stock Mín
  - Colores en stock: Verde (OK), Amarillo (bajo), Rojo (crítico)

- ✅ **Editar Producto**:
  - Modificar precios y stock
  - Cambiar categoría
  - Actualizar stock mínimo

- ✅ **Eliminar Producto**:
  - Eliminación lógica (se marca inactivo)

#### Cálculos Automáticos:
- **Margen de Ganancia**: `(Precio - Costo) / Precio * 100%`
- **Rentabilidad**: Mostrada visualmente en dashboard

#### Datos de Ejemplo:
```
Producto: Café Americano
├── Categoría: Bebidas
├── Precio Venta: $5.000
├── Costo: $2.000
├── Margen: 60%
├── Stock Actual: 50
└── Stock Mínimo: 10

Producto: Capuchino
├── Categoría: Bebidas
├── Precio Venta: $7.000
├── Costo: $3.000
├── Margen: 57.14%
├── Stock Actual: 40
└── Stock Mínimo: 8
```

---

## 💰 Módulo 3: Sistema de Punto de Venta (POS)

### 3.1 Carrito de Compras
**Responsable**: `PosController` + `CarritoService` + `CarritoItem`

#### Funcionalidades:
- ✅ **Agregar Producto al Carrito**:
  - Seleccionar producto del combo
  - Ingresar cantidad
  - Validar disponibilidad de stock
  - Calcular subtotal: `cantidad × precio`

- ✅ **Actualizar Cantidad**:
  - Aumentar o disminuir items existentes
  - Validar stock disponible
  - Recalcular totales automáticamente

- ✅ **Eliminar Producto del Carrito**:
  - Quitar línea específica
  - Recalcular totales

- ✅ **Vista del Carrito**:
  - Tabla con: Producto, Cantidad, Precio Unit, Subtotal
  - Totales actualizados en tiempo real

#### Estructura del Carrito:
```
ObservableList<VentaDetalle> carrito = [
    {producto: Café Americano, cantidad: 2, precio: 5000, subtotal: 10000},
    {producto: Capuchino, cantidad: 1, precio: 7000, subtotal: 7000},
    {producto: Sandwich, cantidad: 1, precio: 9000, subtotal: 9000}
]
Total Bruto: $26.000
```

---

### 3.2 Cálculo de Totales
**Responsable**: `PosController` + `PosService`

#### Funcionalidades:
- ✅ **Subtotal**:
  - Suma de (cantidad × precio unitario) para cada item
  - Formula: `Σ(cantidad_i × precio_i)`

- ✅ **Descuento Global**:
  - Descuento en porcentaje (%) aplicado al total
  - Ej: 10% descuento sobre $26.000 = $2.600
  - Se resta automáticamente del total

- ✅ **Cálculo del Total Final**:
  - Formula: `(Subtotal - Descuento) = Total Neto`
  - Actualización en tiempo real en label

- ✅ **Impuestos (Futuros)**:
  - Arquitectura preparada para agregar IVA o impuestos
  - Actualmente: sin impuestos

#### Ejemplo de Cálculo:
```
Subtotal:           $26.000
Descuento (10%):    -$2.600
─────────────────────────────
TOTAL A PAGAR:      $23.400
```

---

### 3.3 Métodos de Pago
**Responsable**: `PaymentService` + `PosController`

#### Métodos Disponibles:
- ✅ **Efectivo**:
  - Pago directo sin validación adicional
  - Genera recibo inmediato

- ✅ **Tarjeta de Crédito/Débito**:
  - Simulación de transacción
  - En producción: integración con gateway (ej: Stripe, Paypal)

- ✅ **Pago en Línea (Mock)**:
  - Simulado para demostración
  - Genera código de referencia temporal

#### Validaciones:
- Cantidad mínima para pagar ($0 o mayor)
- Validación de método seleccionado
- Confirmación antes de procesar

---

### 3.4 Confirmación de Venta (Transacción ACID)
**Responsable**: `PosService.confirmarVenta()`

#### Proceso Transaccional:
```
1. BLOQUEO PESIMISTA de productos en carrito
   ├─ Valida stock disponible
   └─ Previene race conditions

2. CREACIÓN de cabecera Venta
   ├─ Registra fecha/hora
   ├─ Usuario (cajero)
   ├─ Método de pago
   └─ Total final

3. CREACIÓN de VentaDetalle para cada item
   └─ Guarda: producto, cantidad, precio unitario

4. ACTUALIZACIÓN de Stock
   ├─ Resta cantidad vendida del stock actual
   └─ Registra movimiento de inventario

5. COMMIT transaccional
   └─ Si todo OK: persiste. Si error: ROLLBACK automático
```

#### Seguridad:
- Transacción ACID garantiza consistencia
- Si falla: reversión automática
- No hay ventas parciales
- Validación de stock previo a commit

#### Alertas:
- Si stock queda bajo (< stock mínimo): notificación visual
- Sugerencia automática de reorden

---

### 3.5 Sistema de Fichó (Cliente/Credito)
**Responsable**: `PosController` + `CarritoService` + `ClienteCarrito` (DTO)

#### Funcionalidades:
- ✅ **Crear Ficho**:
  - Ingreso de número/código único
  - Asociación con cliente (ficticio o real)
  - Monto inicial

- ✅ **Cargar Ficho**:
  - Buscar ficho existente
  - Validar estado (activo/pagado)
  - Sumar compras al saldo

- ✅ **Aplicar Compra a Ficho**:
  - Registra venta contra crédito
  - Descuenta del saldo disponible
  - Mantiene historial de transacciones

- ✅ **Pagar Ficho**:
  - Liquidación parcial o total
  - Genera recibo de pago
  - Cierre automático si saldo = 0

#### Gestión de Ficthos:
```
Ficho #001
├── Cliente: Juan Pérez
├── Saldo Inicial: $100.000
├── Compra 1: -$15.000 (Café)
├── Compra 2: -$9.000 (Sandwich)
├── Saldo Actual: $76.000
└── Estado: Activo

Opción: Ingresar monto + pagar → Saldo liquidado
```

---

## 📦 Módulo 4: Gestión de Inventario

### 4.1 Movimientos de Inventario
**Responsable**: `MovimientosController` + `InventarioService` + `MovimientoInventario`

#### Tipos de Movimiento:
- ✅ **Entrada**:
  - Compra de productos nuevos
  - Reposición de stock
  - Incrementa `stock_actual`

- ✅ **Salida**:
  - Venta (automática vía POS)
  - Donación/descarte
  - Decrementa `stock_actual`

- ✅ **Ajuste**:
  - Corrección por faltante
  - Corrección por sobrante
  - Reconteo manual

#### Campos de Cada Movimiento:
```
MovimientoInventario {
    id: Long,
    producto: Producto,
    tipo: "entrada" | "salida" | "ajuste",
    cantidad: int,
    motivo: String,
    usuarioId: Long,
    fecha: LocalDateTime
}
```

#### Validaciones:
- Cantidad > 0
- Producto existe
- Salida no puede exceder stock actual
- Usuario autorizado
- Fecha automática (servidor)

#### Registro de Ejemplo:
```
Movimiento: Entrada
├── Producto: Café Americano
├── Cantidad: 50 unidades
├── Motivo: Compra proveedor
├── Usuario: Admin
├── Fecha: 2024-11-22 14:30:15
└── Nuevo Stock: 100

Movimiento: Salida
├── Producto: Café Americano
├── Cantidad: 2 unidades
├── Motivo: Venta POS #1234
├── Usuario: Cajero Demo
├── Fecha: 2024-11-22 14:31:05
└── Nuevo Stock: 98
```

---

### 4.2 Alertas de Stock
**Responsable**: `ProductosController` + Lógica en UI

#### Alertas Implementadas:
- ✅ **Stock Crítico** (< Stock Mínimo):
  - Color ROJO en tabla
  - Mensaje de alerta
  - Sugerencia de reorden

- ✅ **Stock Bajo** (entre mín y umbral):
  - Color AMARILLO
  - Notificación discreta

- ✅ **Stock Normal** (> umbral):
  - Color VERDE
  - Sin alerta

#### Ejemplo:
```
Producto: Café Americano
├── Stock Actual: 5
├── Stock Mínimo: 10
├── Estado: 🔴 CRÍTICO
└── Acción Sugerida: Realizar compra urgente
```

---

## 📊 Módulo 5: Reportes y Recibos

### 5.1 Generación de Recibos (PDF)
**Responsable**: `ReceiptService` + OpenPDF

#### Contenido del Recibo:
```
═══════════════════════════════════════
          JAZZ COFFEE - RECIBO
═══════════════════════════════════════
Fecha:     22/11/2024 14:35:20
Cajero:    Cajero Demo
Transacción: #1234

───────────────────────────────────────
Descripción          Cant  Precio Total
───────────────────────────────────────
Café Americano        2    5.000 10.000
Capuchino             1    7.000  7.000
Sandwich              1    9.000  9.000
───────────────────────────────────────
Subtotal:                        26.000
Descuento (10%):                -2.600
Impuesto:                            0
───────────────────────────────────────
TOTAL A PAGAR:                  23.400
Método Pago: Efectivo

Referencia: RCT-20241122-1234
═══════════════════════════════════════
¡Gracias por su compra!
```

#### Opciones:
- ✅ Ver recibo en pantalla
- ✅ Descargar PDF
- ✅ Enviar por email (futuro)
- ✅ Imprimir directamente

---

### 5.2 Dashboard Administrativo (Reportes)
**Responsable**: `DashboardController`

#### Métricas Mostradas:
- ✅ **Ventas Totales** (hoy, semana, mes)
  - Número de transacciones
  - Monto total en efectivo
  - Monto total en tarjeta

- ✅ **Productos Más Vendidos**:
  - Top 5 productos por cantidad
  - Top 5 por ingresos generados

- ✅ **Rendimiento de Categorías**:
  - Comparativa: Bebidas vs Comidas
  - Margen promedio por categoría

- ✅ **Estado del Inventario**:
  - Productos con stock bajo
  - Valor total en inventario
  - Rotación de stock promedio

- ✅ **Indicadores Financieros**:
  - Margen de ganancia promedio
  - Tickete promedio
  - Unidades vendidas vs esperadas

---

### 5.3 Dashboard del Cliente (Cajero)
**Responsable**: `DashboardClienteController`

#### Información Disponible:
- ✅ **Mis Ventas del Día**:
  - Total de transacciones procesadas
  - Monto total manejado
  - Método de pago usado

- ✅ **Resumen Rápido**:
  - Productos más vendidos por este cajero
  - Pago promedio

- ✅ **Alertas Personales**:
  - Productos con bajo stock
  - Discrepancias en caja (futuro)

---

## 🔒 Módulo 6: Control de Permisos y Seguridad

### 6.1 Roles del Sistema
**Responsable**: `PermissionService` + `Rol` + `Usuario`

#### Roles Disponibles:
- ✅ **ADMIN**:
  - Acceso total al sistema
  - Gestión de usuarios
  - Configuración de productos
  - Acceso a reportes completos

- ✅ **CAJERO**:
  - Acceso a POS (tomar pedidos)
  - Consulta de productos
  - Ver su propio dashboard
  - No puede: editar precios, crear usuarios, eliminar ventas

#### Matriz de Permisos (JSON):
```json
{
  "ADMIN": [
    "USUARIOS_VIEW",
    "USUARIOS_CREATE",
    "USUARIOS_EDIT",
    "USUARIOS_DELETE",
    "PRODUCTOS_VIEW",
    "PRODUCTOS_EDIT",
    "POS_ACCESS",
    "REPORTES_VIEW",
    "INVENTARIO_MANAGE"
  ],
  "CAJERO": [
    "PRODUCTOS_VIEW",
    "POS_ACCESS",
    "REPORTES_OWN"
  ]
}
```

---

### 6.2 Seguridad de Contraseñas
**Responsable**: `AuthService` + BCrypt

#### Políticas:
- ✅ **Hash BCrypt**:
  - Costo computacional: 10 (seguro pero rápido)
  - No se guardan contraseñas en texto plano
  - Imposible desencriptar

- ✅ **Cambio de Contraseña**:
  - Solo usuarios autenticados
  - Validación de contraseña anterior
  - Generación de nuevo hash

- ✅ **Recuperación** (Futuro):
  - Email de confirmación
  - Token temporal
  - Nueva contraseña temporal

---

## 📈 Módulo 7: Monitoreo de Rendimiento

### 7.1 Monitor Integrado
**Responsable**: `PerformanceMonitor` + Scripts PowerShell

#### Métricas Capturadas:
- ✅ **CPU Usage** (%):
  - Promedio del proceso Java
  - Picos de consumo

- ✅ **Memoria (RAM)**:
  - Heap usage (Java)
  - Memoria total del sistema

- ✅ **I/O Disk**:
  - Bytes leídos/segundo
  - Bytes escritos/segundo

- ✅ **Hilos (Threads)**:
  - Cantidad activa
  - Picos observados

#### Reporte Generado:
```csv
timestamp,cpu_percent,memory_mb,io_read_bps,io_write_bps,threads
2024-11-22 14:30:00,15.5,512.3,102400,51200,12
2024-11-22 14:30:05,12.2,515.1,0,25600,12
2024-11-22 14:30:10,18.7,520.8,204800,102400,13
```

#### Resumen Automático:
```
═══════════════════════════════════════════
Reporte de Rendimiento - JazzCoffee
Fecha: 22/11/2024 14:35
═══════════════════════════════════════════

CPU:
  Promedio:     15.2 %
  Máximo:       22.1 %
  Mínimo:        8.5 %

Memoria (RAM):
  Promedio:     518 MB
  Máximo:       645 MB
  Mínimo:       512 MB

I/O Lectura:
  Promedio:     76.8 KB/s
  Total:        2.3 MB

I/O Escritura:
  Promedio:     51.2 KB/s
  Total:        1.5 MB

Duración Monitoreo: 5 minutos
═══════════════════════════════════════════
```

---

## 🗄️ Módulo 8: Base de Datos

### 8.1 Esquema de Datos
**Responsable**: Flyway + `V1__init_schema.sql`

#### Tablas Principales:
```sql
-- Usuarios y Seguridad
roles (id, nombre)
usuarios (id, nombre, email, hash, rol_id, activo, permisos_json)

-- Catálogo
categorias (id, nombre)
productos (id, nombre, categoria_id, precio, costo, stock_actual, stock_min)

-- Ventas
ventas (id, fecha, usuario_id, total, metodo_pago, estado, ref_pago)
venta_detalles (id, venta_id, producto_id, cantidad, precio_unitario)

-- Inventario
movimientos_inventario (id, producto_id, tipo, cantidad, motivo, usuario_id, fecha)

-- Control de Caja (Futuro)
cierres_caja (id, usuario_id, fecha_inicio, fecha_fin, total_ventas, total_efectivo, total_tarjeta, diferencias)
```

### 8.2 Migraciones
**Responsable**: Flyway + SQL versioning

#### Versiones:
- `V1__init_schema.sql`: Tablas iniciales
- `V2__add_indices.sql`: Optimización de consultas
- `V3__add_constraints.sql`: Integridad referencial
- `V4__seed_demo.sql`: Datos de prueba

#### Flujo de Migraciones:
```
Arranque App
    ↓
Bootstrap.init() → FlywayMigrator.migrate()
    ↓
Verificar versión en BD
    ↓
Ejecutar scripts V1, V2, V3, V4 (si no existen)
    ↓
BD lista para operación
```

---

## 🎨 Módulo 9: Interfaz de Usuario (UI)

### 9.1 Pantallas (FXML)
**Responsable**: JavaFX Controllers + FXML files

#### Flujo de Navegación:
```
welcome.fxml (Pantalla Inicio)
    ├─ login.fxml (Admin/Gerente)
    │   └─ dashboard.fxml (Panel Principal Admin)
    │       ├─ productos.fxml (Gestión Productos)
    │       ├─ categorias.fxml (Gestión Categorías)
    │       ├─ usuarios.fxml (Gestión Usuarios)
    │       ├─ movimientos.fxml (Registro Inventario)
    │       └─ Reportes (Integrados en Dashboard)
    │
    └─ login-cliente.fxml (Cajero)
        └─ dashboard-cliente.fxml (Panel Cajero)
            └─ pos.fxml (Punto de Venta)
```

### 9.2 Componentes Principales:
- ✅ **Tablas JavaFX**: ObservableList + PropertyValueFactory
- ✅ **ComboBox**: Selección de categorías y productos
- ✅ **TextField/TextArea**: Entrada de datos
- ✅ **Labels y Buttons**: Navegación y acciones
- ✅ **Gráficos**: Charts para reportes (futuro)

---

## 🚀 Módulo 10: Flujos de Negocio Completos

### 10.1 Flujo de Venta Completo
```
1. Cajero Login
   ├─ Email: cajero@local
   ├─ Password: [cualquiera - verificado con BCrypt]
   └─ Sesión iniciada ✓

2. Acceso a POS
   ├─ Carga lista de productos
   ├─ Muestra categorías disponibles
   └─ Carrito vacío listo

3. Tomar Pedido
   ├─ Selecciona Producto (Combo)
   ├─ Ingresa Cantidad
   ├─ Agrega al Carrito
   └─ Repetir paso 3 para más productos

4. Aplicar Descuentos
   ├─ Ingresa % descuento (opcional)
   ├─ Visualiza nuevo total
   └─ O aplica Ficho si cliente tiene crédito

5. Seleccionar Pago
   ├─ Efectivo
   ├─ Tarjeta
   └─ Ficho

6. Confirmar Venta
   ├─ Transacción ACID inicia
   ├─ Valida stock de cada producto
   ├─ Crea Venta + VentaDetalle
   ├─ Actualiza stock
   ├─ Registra movimiento inventario
   ├─ Commit o Rollback automático
   └─ Transacción completada ✓

7. Recibo
   ├─ Genera PDF automático
   ├─ Opción: Imprimir
   ├─ Opción: Enviar por email
   └─ Guarda en carpeta receipts/

8. Nueva Venta
   └─ Carrito se limpia, vuelve al paso 3
```

### 10.2 Flujo de Reposición de Stock
```
1. Admin ve Dashboard → Alertas de Stock Bajo

2. Abre Movimientos de Inventario

3. Selecciona Tipo: "Entrada"

4. Selecciona Producto con bajo stock

5. Ingresa:
   ├─ Cantidad a comprar (ej: 50)
   ├─ Motivo: "Compra a proveedor"
   └─ Submite

6. Validación:
   ├─ Producto existe ✓
   ├─ Cantidad > 0 ✓
   ├─ Usuario autorizado ✓
   └─ Transacción persiste

7. Stock Actualizado:
   ├─ Stock Anterior: 5
   ├─ + Cantidad: 50
   └─ Stock Nuevo: 55 ✓

8. Dashboard Actualizado:
   └─ Alerta desaparece (stock > mínimo)
```

### 10.3 Flujo de Crear Nuevo Producto
```
1. Admin → Pantalla Productos

2. Click "Nuevo Producto"

3. Ingresa:
   ├─ Nombre: "Latte Grande"
   ├─ Categoría: "Bebidas" (combo)
   ├─ Precio Venta: 8000
   ├─ Costo: 3500
   ├─ Stock Inicial: 0
   └─ Stock Mínimo: 5

4. Validación:
   ├─ Nombre único ✓
   ├─ Categoría existe ✓
   ├─ Precios válidos ✓
   └─ Cantidades positivas ✓

5. Guardar:
   └─ INSERT en tabla productos ✓

6. Confirmación:
   ├─ Mensaje "Producto creado"
   ├─ Se limpia formulario
   └─ Tabla se recarga automáticamente

7. En POS:
   └─ "Latte Grande" aparece disponible ✓
```

---

## 📋 Módulo 11: Entidades JPA

### 11.1 Relaciones de Datos
```
┌─────────────┐
│   Usuario   │
├─────────────┤
│ id (PK)     │
│ nombre      │
│ email (U)   │
│ hash        │
│ rol_id (FK) │──┐
│ activo      │  │
└─────────────┘  │
                 │ ManyToOne
                 │
                 ▼
            ┌─────────┐
            │   Rol   │
            ├─────────┤
            │ id (PK) │
            │ nombre  │
            └─────────┘

┌──────────────┐      ┌────────────┐
│  Producto    │      │ Categoría  │
├──────────────┤      ├────────────┤
│ id (PK)      │      │ id (PK)    │
│ nombre       │      │ nombre (U) │
│ categoria_id ├─────►│            │
│ precio       │ (FK) └────────────┘
│ costo        │
│ stock_actual │
│ stock_min    │
└──────────────┘

      ┌──────────────┐
      │    Venta     │
      ├──────────────┤
      │ id (PK)      │
      │ fecha        │
      │ usuario_id   │
      │ total        │
      │ metodo_pago  │
      │ estado       │
      └──────────────┘
            │
            │ OneToMany
            │
      ┌─────▼──────────┐
      │  VentaDetalle  │
      ├────────────────┤
      │ id (PK)        │
      │ venta_id (FK)  │
      │ producto_id(FK)│
      │ cantidad       │
      │ precio_unit    │
      └────────────────┘

┌────────────────────┐      ┌──────────────┐
│ MovimientoInventario│      │  Producto   │
├────────────────────┤      ├──────────────┤
│ id (PK)            │      │ id (PK)      │
│ producto_id (FK)   ├─────►│              │
│ tipo               │ (FK) └──────────────┘
│ cantidad           │
│ motivo             │
│ usuario_id         │
│ fecha              │
└────────────────────┘
```

---

## 🛠️ Módulo 12: Configuración y Deployment

### 12.1 Perfiles de Ejecución
**Responsable**: `application.properties`, `application-dev.properties`, `application-prod.properties`

#### Desarrollo (dev):
```properties
# Base de Datos: H2 (en memoria)
spring.datasource.url=jdbc:h2:mem:cafebar
spring.h2.console.enabled=true
spring.hibernate.hbm2ddl.auto=create-drop

# Logging
logging.level.root=DEBUG
logging.level.org.hibernate=DEBUG
```

#### Producción (prod):
```properties
# Base de Datos: PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/jazzcoffe
spring.datasource.username=admin
spring.datasource.password=${DB_PASSWORD}
spring.hibernate.hbm2ddl.auto=validate

# Logging
logging.level.root=INFO
logging.level.org.hibernate=INFO
```

### 12.2 Build y Ejecución
```bash
# Compilar
mvn clean compile

# Ejecutar tests
mvn test

# Empaquetar (JAR)
mvn package

# Ejecutar aplicación
mvn javafx:run

# Build para producción
mvn clean package -Pproduction
```

---

## 📊 Módulo 13: Casos de Uso (Use Cases)

### UC-001: Autenticarse en el Sistema
```
Actor: Usuario (Admin o Cajero)
Precondición: Aplicación iniciada
Flujo:
  1. Sistema muestra pantalla de login
  2. Usuario ingresa email
  3. Usuario ingresa contraseña
  4. Sistema valida credenciales con BCrypt
  5. ✓ Válidas → Muestra dashboard
  6. ✗ Inválidas → Muestra error, intenta nuevamente
```

### UC-002: Registrar Nueva Venta
```
Actor: Cajero
Precondición: Autenticado, POS abierto
Flujo:
  1. Cajero selecciona productos del combo
  2. Ingresa cantidades
  3. Agrega al carrito (validar stock)
  4. Repeats 1-3 hasta terminar
  5. Ingresa descuento (opcional)
  6. Selecciona método de pago
  7. Confirma venta
  8. Transacción ACID procesa:
     - Valida stock final
     - Crea Venta + VentaDetalle
     - Actualiza stock
     - Registra movimientos
  9. Transacción exitosa → Genera recibo PDF
  10. Muestra confirmación, carrito se limpia
```

### UC-003: Gestionar Productos (CRUD)
```
Actor: Administrador
Flujo:
  Crear:
    1. Ingresa nombre, categoría, precio, costo, stock
    2. Valida nombre único
    3. Crea producto ✓
  
  Leer:
    1. Ve tabla con todos productos
    2. Colorea por estado de stock
  
  Actualizar:
    1. Selecciona producto de tabla
    2. Edita campos permitidos
    3. Guarda cambios
  
  Eliminar:
    1. Selecciona producto
    2. Confirma eliminación
    3. Marca como inactivo (soft delete)
```

### UC-004: Reporte de Ventas del Día
```
Actor: Administrador
Precondición: Dashboard abierto
Flujo:
  1. Sistema calcula automáticamente:
     - Total ventas hoy
     - Número de transacciones
     - Ingresos por método de pago
     - Productos más vendidos
  2. Muestra en dashboard con gráficos
  3. Admin puede exportar a PDF (futuro)
```

---

## ⚡ Módulo 14: Características Especiales

### 14.1 Transacciones ACID
- **Atomicidad**: Una venta completa o no ocurre
- **Consistencia**: BD siempre en estado válido
- **Aislamiento**: Ventas simultáneas no interfieren (bloqueo pesimista)
- **Durabilidad**: Datos persisten incluso con crash

### 14.2 Bloqueo Pesimista (Pessimistic Locking)
```java
// En PosService.confirmarVenta()
Producto p = session.get(Producto.class, productoId, LockOptions.UPGRADE);
// Bloquea fila hasta fin de transacción
// Previene race conditions
```

### 14.3 Manejo de Errores
```
Escenarios manejados:
  ✓ Stock insuficiente → Rollback + Mensaje
  ✓ Producto no existe → Rollback + Mensaje
  ✓ Usuario no autorizado → Rollback + Mensaje
  ✓ Conexión BD perdida → Rollback + Reintentos
  ✓ Datos inválidos → Validación + Mensaje claro
```

---

## 📚 Módulo 15: Tecnologías Usadas

| Tecnología | Versión | Propósito |
|-----------|---------|----------|
| Java | 21 | Lenguaje principal |
| JavaFX | 22.0.2 | Framework UI desktop |
| Hibernate ORM | 6.6.1 | Mapeo objeto-relacional |
| PostgreSQL | 15+ | BD producción |
| H2 | 2.3.232 | BD desarrollo (en memoria) |
| SQLite | 3.46.0 | BD alternativa |
| BCrypt | 0.10.2 | Hash de contraseñas |
| Flyway | 10.16.0 | Migraciones BD |
| OpenPDF | Latest | Generación recibos PDF |
| Logback | 1.5.7 | Logging estructurado |
| HikariCP | 5.1.0 | Connection pooling |
| Maven | 3.9.6 | Build tool |

---

## 🔍 Módulo 16: Estadísticas del Código

```
Archivos:
  - Java: ~15 clases principales
  - FXML: 11 pantallas
  - SQL: 4 migraciones Flyway
  - Propiedades: 3 perfiles (dev/prod/test)

Líneas de Código:
  - Backend (Controllers + Services): ~2000 LOC
  - Domain (Entidades): ~500 LOC
  - Repositories: ~800 LOC
  - Frontend (FXML): ~3000 LOC
  - Total: ~6300 LOC

Complejidad:
  - Transacciones: Moderada (ACID + Pessimistic Locking)
  - Validaciones: Completa (lado servidor)
  - Seguridad: Buena (BCrypt + permisos)
  - Escalabilidad: Media (single DB, sin caché distribuído)
```

---

## 🎯 Módulo 17: Funcionalidades Futuras

```
[  ] Carrito persistente (guardar y recuperar)
[  ] Sistema de clientes con historial
[  ] Descuentos por cliente VIP
[  ] Reportes avanzados con gráficos
[  ] Integración con métodos de pago reales (Stripe, PayPal)
[  ] Notificaciones por email
[  ] Control de caja (cierre de turno)
[  ] Auditoría de cambios
[  ] Backup automático BD
[  ] Multi-sucursal
[  ] Integración con POS físico (impresora, lector de códigos)
```

---

## 📞 Contacto y Soporte

**Proyecto**: JazzCoffee POS  
**Versión**: 0.1.0  
**Última Actualización**: 22 de Noviembre 2024  
**Desarrollador**: Equipo PPI - Cafetería Universitaria  
**Licencia**: Propietario  

---

**Fin de Documentación** ✓
