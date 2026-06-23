# jazcoffe
JazzCoffee POS - Sistema de Punto de Venta

# ☕ JazzCoffee POS - Sistema de Punto de Venta

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-GUI-blue?style=for-the-badge)
![Hibernate](https://img.shields.io/badge/Hibernate-ORM-yellow?style=for-the-badge)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)

JazzCoffee es una aplicación de escritorio robusta diseñada para centralizar y optimizar la operación comercial, el inventario y las ventas de una cafetería universitaria. Desarrollada bajo el patrón arquitectónico **MVC (Modelo-Vista-Controlador)**, la plataforma resuelve problemas críticos como las largas filas de espera y los desfaces manuales de stock[cite: 1, 2, 3, 4].

---

## 🚀 Características Principales

* **Punto de Venta (POS Ágil):** Registro rápido de transacciones, cálculo automático de subtotales, impuestos y gestión de múltiples métodos de pago (simulados).
* **Control de Inventario Riguroso:** Monitoreo en tiempo real de existencias con alertas automáticas de stock mínimo y trazabilidad de movimientos.
* **Venta Inteligente y Fidelización:** Sugerencia predictiva de combos y asignación de puntos de fidelidad para la comunidad estudiantil.
* **Seguridad por Roles:** Autenticación protegida con hashing de contraseñas mediante **BCrypt** y restricción de vistas según el perfil[cite: 2, 3].
* **Dashboard Analítico:** Panel visual con métricas de ingresos y gráficos de rendimiento en tiempo real[cite: 1, 3].
* **Sostenibilidad:** Facturación digital interactiva con exportación nativa a PDF mediante **OpenPDF**, reduciendo el uso de papel térmico en un 95%.

---

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java 21[cite: 2]
* **Interfaz Gráfica:** JavaFX (FXML)[cite: 2, 3]
* **Persistencia / ORM:** Hibernate[cite: 2]
* **Base de Datos:** PostgreSQL (Producción) / H2 (Desarrollo/Pruebas)
* **Seguridad:** BCrypt Hashing[cite: 2, 3]
* **Reportes:** OpenPDF[cite: 2]

---

## 📐 Arquitectura del Sistema

El proyecto implementa una arquitectura **multi-capas** para asegurar la mantenibilidad y escalabilidad del código[cite: 2, 3]:

* `com.cafeuniv.ppi.model`: Entidades de datos mapeadas con Hibernate[cite: 2].
* `com.cafeuniv.ppi.view`: Archivos FXML descriptivos de la interfaz gráfica[cite: 2, 3].
* `com.cafeuniv.ppi.controller`: Lógica de control que gestiona los eventos de la UI[cite: 2, 3].
* `com.cafeuniv.ppi.service`: Capa de negocio donde se gestiona la atomicidad de las transacciones (ACID).

---

## 💻 Instalación y Configuración Previa

### Requisitos locales
1. **JDK 21** o superior instalado.
2. **PostgreSQL** activo (en caso de usar el entorno de producción).

### Pasos para ejecución
1. Clona este repositorio:
```bash
   git clone [https://github.com/tu-usuario/jazzcoffee-pos.git](https://github.com/tu-usuario/jazzcoffee-pos.git)
