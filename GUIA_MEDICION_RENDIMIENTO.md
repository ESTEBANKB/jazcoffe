# Guía de Medición de Rendimiento - JazzCoffee POS

## Método 1: Usando el Administrador de Tareas de Windows (Manual)

### Paso 1: Configurar el Administrador de Tareas

1. **Abrir el Administrador de Tareas:**
   - Presiona `Ctrl + Shift + Esc` o `Ctrl + Alt + Del` → "Administrador de tareas"
   - O clic derecho en la barra de tareas → "Administrador de tareas"

2. **Ir a la pestaña "Detalles":**
   - En la pestaña "Detalles" verás todos los procesos en ejecución

3. **Activar las columnas necesarias:**
   - Clic derecho en cualquier encabezado de columna → "Seleccionar columnas"
   - Activar las siguientes columnas:
     - ✅ **CPU** (ya está por defecto)
     - ✅ **Memoria (uso de memoria privada de trabajo)** o **Memoria (conjunto de trabajo privado)**
     - ✅ **E/S de lectura (bytes)** 
     - ✅ **E/S de escritura (bytes)**
     - ✅ **E/S de lectura (bytes/seg)**
     - ✅ **E/S de escritura (bytes/seg)**
   - Clic en "Aceptar"

4. **Buscar el proceso Java:**
   - Busca el proceso llamado `java.exe` o `javaw.exe` (para aplicaciones JavaFX)
   - Si hay varios procesos Java, identifica el correcto por:
     - El nombre de la ventana de la aplicación
     - El uso de memoria (tu aplicación debería tener un uso significativo)

### Paso 2: Capturar Métricas Durante el Uso

#### Escenario de Prueba Recomendado:

1. **Inicio de la aplicación:**
   - Ejecuta la aplicación
   - Captura pantalla inmediatamente después del inicio
   - Anota: CPU inicial, Memoria inicial, E/S inicial

2. **Login y navegación:**
   - Realiza login como administrador
   - Navega por diferentes módulos (Dashboard, Productos, POS, etc.)
   - Captura pantalla cada 30 segundos

3. **Operaciones intensivas:**
   - Carga de productos (si hay muchos)
   - Generación de reportes
   - Procesamiento de ventas
   - Generación de recibos PDF
   - Captura pantalla durante estas operaciones

4. **Uso normal:**
   - Simula uso normal durante 5-10 minutos
   - Captura pantalla cada minuto para obtener promedio

### Paso 3: Calcular Promedios

Para calcular el **CPU % promedio**:
- Suma todos los valores de CPU capturados
- Divide entre el número de capturas
- Ejemplo: (5% + 8% + 12% + 6% + 4%) / 5 = 7%

Para **Memoria RAM**:
- Toma el valor más alto durante el uso normal (pico de memoria)
- O calcula el promedio si prefieres

Para **Disco MB/s**:
- Suma "E/S de lectura (bytes/seg)" + "E/S de escritura (bytes/seg)"
- Convierte a MB/s: (bytes/seg) / (1024 * 1024)
- Calcula el promedio de todas las capturas

### Paso 4: Capturar Pantallas

**Métodos para capturar pantalla:**
- `Windows + Shift + S` - Herramienta de recorte de Windows
- `PrtScn` - Captura toda la pantalla
- `Alt + PrtScn` - Captura solo la ventana activa
- Herramienta de recorte (Snipping Tool) - Buscar en el menú inicio

**Recomendación:** Usa `Windows + Shift + S` para capturar solo el área del Administrador de Tareas.

---

## Método 2: Script Automatizado con PowerShell

Usa el script `monitorear-rendimiento.ps1` para automatizar la captura de métricas.

### Uso del Script:

```powershell
# Ejecutar el script (debe ejecutarse como administrador)
.\monitorear-rendimiento.ps1 -NombreProceso "java" -DuracionSegundos 300 -IntervaloSegundos 5
```

El script:
- Monitorea el proceso Java automáticamente
- Captura métricas cada X segundos
- Genera un reporte en CSV
- Calcula promedios automáticamente

---

## Método 3: Usando el Monitor de Rendimiento (Performance Monitor)

### Configurar el Monitor de Rendimiento:

1. **Abrir el Monitor de Rendimiento:**
   - Presiona `Windows + R`
   - Escribe `perfmon` y presiona Enter

2. **Agregar contadores:**
   - Clic derecho en el gráfico → "Agregar contadores"
   - Buscar y agregar:
     - `Process` → `% Processor Time` → Instancia: `java`
     - `Process` → `Working Set` → Instancia: `java`
     - `Process` → `IO Data Bytes/sec` → Instancia: `java`

3. **Configurar para captura:**
   - Clic derecho en "Conjuntos de recopiladores de datos" → "Nuevo" → "Conjunto de recopiladores de datos"
   - Seguir el asistente para crear un registro de rendimiento

---

## Métricas a Documentar

### Tabla de Resultados Sugerida:

| Escenario | CPU % Promedio | Memoria RAM (MB) | Disco Lectura (MB/s) | Disco Escritura (MB/s) | Notas |
|----------|----------------|------------------|----------------------|------------------------|-------|
| Inicio de aplicación | | | | | |
| Login y carga inicial | | | | | |
| Navegación normal | | | | | |
| Carga de productos | | | | | |
| Procesamiento de venta | | | | | |
| Generación de recibo PDF | | | | | |
| Uso continuo (5 min) | | | | | |

---

## Consejos para Capturas de Pantalla

1. **Organización:**
   - Nombra las capturas con formato: `rendimiento_001_inicio.png`
   - Crea una carpeta `rendimiento_capturas/` para organizarlas

2. **Información a incluir:**
   - Fecha y hora en la captura
   - Escenario que se está probando
   - Valores numéricos visibles (CPU, Memoria, E/S)

3. **Múltiples ejecuciones:**
   - Realiza al menos 3 ejecuciones para obtener promedios confiables
   - Documenta variaciones entre ejecuciones

---

## Interpretación de Resultados

### CPU:
- **< 5%**: Excelente rendimiento
- **5-15%**: Buen rendimiento
- **15-30%**: Rendimiento aceptable
- **> 30%**: Puede necesitar optimización

### Memoria RAM:
- Depende del tamaño de la aplicación
- Para una aplicación JavaFX pequeña-mediana: 100-500 MB es normal
- > 1 GB puede indicar memory leaks

### Disco I/O:
- **< 1 MB/s**: Muy bajo, aplicación principalmente en memoria
- **1-10 MB/s**: Normal para operaciones de base de datos
- **> 10 MB/s**: Alto, puede indicar muchas operaciones de lectura/escritura

---

## Troubleshooting

**No encuentro el proceso Java:**
- Verifica que la aplicación esté ejecutándose
- Busca por `javaw.exe` en lugar de `java.exe`
- Revisa la pestaña "Procesos" en lugar de "Detalles"

**Los valores de E/S están en 0:**
- Espera unos segundos, puede tardar en actualizarse
- Verifica que las columnas de E/S estén activadas
- Algunas operaciones pueden no generar E/S inmediatamente

**CPU muy alto al inicio:**
- Es normal durante la inicialización (carga de clases, conexión a BD)
- Espera 30-60 segundos después del inicio para mediciones estables

