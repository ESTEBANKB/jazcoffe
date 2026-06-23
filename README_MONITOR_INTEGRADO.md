# 📊 Monitor de Rendimiento Integrado - JazzCoffee POS

## Descripción

El monitor de rendimiento está **integrado directamente en la aplicación Java**. Se inicia automáticamente cuando ejecutas la aplicación y captura métricas de:

- ✅ **CPU %** - Uso del procesador por el proceso Java
- ✅ **Memoria RAM (MB)** - Memoria heap utilizada
- ✅ **Disco Lectura (MB/s)** - Aproximación basada en cambios en la base de datos
- ✅ **Disco Escritura (MB/s)** - Aproximación basada en cambios en la base de datos

## Cómo Funciona

El monitor se inicia automáticamente cuando ejecutas la aplicación. Las métricas se capturan cada **5 segundos** y se guardan en:

- `rendimiento_report.csv` - Datos completos con todas las capturas
- `rendimiento_report_resumen.txt` - Resumen con promedios (se genera al cerrar la aplicación)

## Uso

### Ejecutar la aplicación normalmente

```bash
mvn javafx:run
```

El monitor se iniciará automáticamente y verás mensajes en la consola como:

```
Monitor de rendimiento iniciado. Archivo: rendimiento_report.csv
Intervalo: 5 segundos
[2025-11-20 17:30:00] CPU: 2.45% | Memoria: 342.18 MB | Disco R: 0.0000 MB/s | Disco W: 0.0000 MB/s
[2025-11-20 17:30:05] CPU: 1.23% | Memoria: 345.67 MB | Disco R: 0.0012 MB/s | Disco W: 0.0008 MB/s
...
```

### Desactivar el monitor (si no lo necesitas)

Si no quieres que el monitor se ejecute, edita `App.java` y comenta las líneas del monitor:

```java
// Comentar estas líneas para desactivar el monitor
/*
try {
    performanceMonitor = PerformanceMonitor.getInstance();
    performanceMonitor.startMonitoring(5, "rendimiento_report.csv");
    ...
} catch (Exception e) {
    ...
}
*/
```

### Cambiar el intervalo de captura

En `App.java`, cambia el parámetro del intervalo (en segundos):

```java
// Cambiar de 5 segundos a 10 segundos
performanceMonitor.startMonitoring(10, "rendimiento_report.csv");
```

## Archivos Generados

### rendimiento_report.csv

Contiene todas las capturas con el siguiente formato:

```csv
Timestamp,CPU_Porcentaje,Memoria_MB,DiscoLectura_MB_s,DiscoEscritura_MB_s,CPU_Total_Sistema,Memoria_Total_MB
2025-11-20 17:30:00,2.45,342.18,0.0000,0.0000,15.23,15720.25
2025-11-20 17:30:05,1.23,345.67,0.0012,0.0008,14.56,15720.25
...
```

### rendimiento_report_resumen.txt

Se genera automáticamente al cerrar la aplicación:

```
========================================
RESUMEN DE RENDIMIENTO - JazzCoffee POS
========================================
Fecha: 2025-11-20 17:35:00
Muestras capturadas: 60

PROMEDIOS:
----------
CPU: 2.15 %
Memoria RAM: 343.45 MB
Disco Lectura: 0.0012 MB/s
Disco Escritura: 0.0008 MB/s

Archivo completo: rendimiento_report.csv
```

## Ventajas del Monitor Integrado

✅ **No requiere herramientas externas** - Todo está en Java  
✅ **Se ejecuta automáticamente** - No necesitas scripts adicionales  
✅ **Métricas precisas** - Acceso directo a las APIs de Java  
✅ **Funciona en cualquier sistema** - Windows, Linux, macOS  
✅ **No requiere permisos especiales** - Funciona con permisos normales  

## Limitaciones

⚠️ **I/O de Disco**: Java no proporciona acceso directo a I/O de disco por proceso.  
   El monitor usa una aproximación basada en cambios en el archivo de base de datos.  
   Para métricas más precisas de I/O, usa el Administrador de Tareas (Windows) o herramientas del sistema.

⚠️ **CPU Total del Sistema**: Puede no estar disponible en todas las versiones de Java.

## Interpretación de Resultados

### CPU
- **< 5%**: Excelente - La aplicación es muy eficiente
- **5-15%**: Bueno - Uso normal
- **15-30%**: Aceptable - Puede optimizarse
- **> 30%**: Alto - Revisar posibles problemas de rendimiento

### Memoria RAM
- Depende del tamaño de la aplicación
- Para aplicaciones JavaFX pequeñas-medianas: **100-500 MB** es normal
- **> 1 GB**: Puede indicar memory leaks o datos excesivos en memoria

### Disco I/O
- **< 0.1 MB/s**: Muy bajo - Principalmente operaciones en memoria
- **0.1-1 MB/s**: Normal - Operaciones de base de datos ocasionales
- **> 1 MB/s**: Alto - Muchas operaciones de lectura/escritura

## Comparación con Scripts Externos

| Característica | Monitor Integrado | Scripts PowerShell |
|----------------|-------------------|-------------------|
| Requiere herramientas externas | ❌ No | ✅ Sí (PowerShell) |
| Funciona en todos los sistemas | ✅ Sí | ❌ Solo Windows |
| Requiere permisos especiales | ❌ No | ⚠️ A veces |
| Métricas de CPU | ✅ Precisas | ✅ Precisas |
| Métricas de Memoria | ✅ Precisas | ✅ Precisas |
| Métricas de I/O | ⚠️ Aproximadas | ✅ Precisas |
| Se ejecuta automáticamente | ✅ Sí | ❌ No |

## Solución de Problemas

**El monitor no se inicia:**
- Verifica que no haya errores en la consola al iniciar
- Revisa que el archivo `PerformanceMonitor.java` esté compilado correctamente

**Los valores están en 0:**
- Es normal si la aplicación está inactiva
- Realiza acciones en la aplicación (navegar, hacer clic, etc.) para generar actividad

**El archivo CSV no se genera:**
- Verifica permisos de escritura en el directorio de la aplicación
- Revisa la consola por errores

## Próximos Pasos

1. Ejecuta la aplicación normalmente
2. Usa la aplicación durante varios minutos
3. Cierra la aplicación
4. Revisa los archivos generados:
   - `rendimiento_report.csv` - Datos completos
   - `rendimiento_report_resumen.txt` - Resumen con promedios

---

**Nota**: El monitor se detiene automáticamente cuando cierras la aplicación y genera el resumen final.

