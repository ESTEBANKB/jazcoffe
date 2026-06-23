# jazcoffe
JazzCoffee POS - Sistema de Punto de Venta

# ☕ JazzCoffee POS - Sistema de Punto de Venta

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-GUI-blue?style=for-the-badge)
![Hibernate](https://img.shields.io/badge/Hibernate-ORM-yellow?style=for-the-badge)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)

## 📸 Capturas de Pantalla de la Interfaz

### 1. Control de Acceso (Login)
![Login](pantallazos%20interfaz/login.png)

### 2. Módulo de Caja y Ventas
![Módulo de Caja](pantallazos%20interfaz/caja.png)

### 3. Control de Inventario
![Inventario](pantallazos%20interfaz/inventario.png)

### 4. Gestión de Productos
![Productos](pantallazos%20interfaz/productos.png)

### 5. Administración de Categorías
![Categorías](pantallazos%20interfaz/categorias.png)

### 6. Dashboard y Ventas en Tiempo Real
![Ventas en Tiempo Real](pantallazos%20interfaz/ventas_tiempo_real.png)

JazzCoffee es una aplicación de escritorio robusta diseñada para centralizar y optimizar la operación comercial, el inventario y las ventas de una cafetería universitaria. Desarrollada bajo el patrón arquitectónico **MVC (Modelo-Vista-Controlador)**, la plataforma resuelve problemas críticos como las largas filas de espera y los desfaces manuales de stock.

---

## 🚀 Características Principales

* **Punto de Venta (POS Ágil):** Registro rápido de transacciones, cálculo automático de subtotales, impuestos y gestión de múltiples métodos de pago (simulados).
* **Control de Inventario Riguroso:** Monitoreo en tiempo real de existencias con alertas automáticas de stock mínimo y trazabilidad de movimientos.
* **Venta Inteligente y Fidelización:** Sugerencia predictiva de combos y asignación de puntos de fidelidad para la comunidad estudiantil.
* **Seguridad por Roles:** Autenticación protegida con hashing de contraseñas mediante **BCrypt** y restricción de vistas según el perfil.
* **Dashboard Analítico:** Panel visual con métricas de ingresos y gráficos de rendimiento en tiempo real.
* **Sostenibilidad:** Facturación digital interactiva con exportación nativa a PDF mediante **OpenPDF**, reduciendo el uso de papel térmico en un 95%.

---

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java 21
* **Interfaz Gráfica:** JavaFX (FXML)
* **Persistencia / ORM:** Hibernate
* **Base de Datos:** PostgreSQL (Producción) / H2 (Desarrollo/Pruebas)
* **Seguridad:** BCrypt Hashing
* **Reportes:** OpenPDF

---

## 📐 Arquitectura del Sistema

El proyecto implementa una arquitectura **multi-capas** para asegurar la mantenibilidad y escalabilidad del código:

* `com.cafeuniv.ppi.model`: Entidades de datos mapeadas con Hibernate.
* `com.cafeuniv.ppi.view`: Archivos FXML descriptivos de la interfaz gráfica.
* `com.cafeuniv.ppi.controller`: Lógica de control que gestiona los eventos de la UI.
* `com.cafeuniv.ppi.service`: Capa de negocio donde se gestiona la atomicidad de las transacciones (ACID).

---

## 💻 Instalación y Configuración Previa

### Requisitos locales
1. **JDK 21** o superior instalado.
2. **PostgreSQL** activo (en caso de usar el entorno de producción).

### Pasos para ejecución
1. Clona este repositorio:
```bash
   git clone [https://github.com/tu-usuario/jazzcoffee-pos.git](https://github.com/ESTEBANKB/jazcoffe)

2.Abre PowerShell o CMD y ve a la carpeta del proyecto ejemplo
cd "c:\Users\esteb\Desktop\univercidad\trabajos2semestre\PPI"
luego Ejecuta la aplicación con el Maven que ya viene en el proyecto: 
.\apache-maven-3.9.6\bin\mvn.cmd javafx:run
