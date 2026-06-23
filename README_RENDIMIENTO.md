# 📊 Medición de Rendimiento - JazzCoffee POS

## Inicio Rápido

### Opción 1: Método Manual (Recomendado para principiantes)

1. **Abre el Administrador de Tareas:**
   - Presiona `Ctrl + Shift + Esc`
   - Ve a la pestaña **"Detalles"**

2. **Configura las columnas:**
   - Clic derecho en cualquier encabezado → **"Seleccionar columnas"**
   - Activa: CPU, Memoria, E/S de lectura (bytes/seg), E/S de escritura (bytes/seg)
   - Busca el proceso `java.exe` o `javaw.exe`

3. **Ejecuta tu aplicación y captura pantallas:**
   - Usa `Windows + Shift + S` para capturar
   - Guarda las capturas en una carpeta `rendimiento_capturas/`

4. **Consulta la guía completa:** `GUIA_MEDICION_RENDIMIENTO.md`

---

### Opción 2: Script Automatizado (Recomendado para mediciones precisas)

1. **Ejecuta la aplicación primero:**
   ```bash
   mvn javafx:run
   ```

2. **Espera 10-30 segundos** para que los contadores de rendimiento se inicialicen

3. **En otra ventana de PowerShell, ejecuta el monitor:**
   ```powershell
   .\monitorear-rendimiento.ps1
   ```

4. **Si los valores están en 0, prueba la versión simple:**
   ```powershell
   .\monitorear-rendimiento-simple.ps1
   ```

5. **O diagnostica el problema primero:**
   ```powershell
   .\diagnosticar-contadores.ps1
   ```

6. **Revisa los resultados:**
   - `rendimiento_report.csv` - Datos completos
   - `rendimiento_report_resumen.txt` - Resumen con promedios

---

## Archivos Incluidos

- 📖 `GUIA_MEDICION_RENDIMIENTO.md` - Guía completa paso a paso
- 🔧 `monitorear-rendimiento.ps1` - Script PowerShell automatizado (versión completa)
- 🔧 `monitorear-rendimiento-simple.ps1` - Script simplificado (sin contadores de rendimiento)
- 🔍 `diagnosticar-contadores.ps1` - Script de diagnóstico para problemas
- 🚀 `ejecutar-con-monitoreo.bat` - Script para ejecutar todo junto
- 📋 `EJEMPLO_REPORTE_RENDIMIENTO.md` - Plantilla para documentar resultados

---

## Métricas que se Capturan

✅ **CPU % Promedio** - Uso del procesador  
✅ **Memoria RAM (MB)** - Uso de memoria  
✅ **Disco Lectura (MB/s)** - Velocidad de lectura  
✅ **Disco Escritura (MB/s)** - Velocidad de escritura  

---

## Requisitos

- Windows 10/11
- PowerShell 5.1 o superior
- Permisos de administrador (para el script automatizado)

---

## Ejemplo de Uso del Script

```powershell
# Monitoreo básico (5 minutos, cada 5 segundos)
.\monitorear-rendimiento.ps1

# Monitoreo personalizado
.\monitorear-rendimiento.ps1 -DuracionSegundos 600 -IntervaloSegundos 3

# Especificar nombre de proceso diferente
.\monitorear-rendimiento.ps1 -NombreProceso "javaw"
```

---

## Solución de Problemas

**Error: "No se encontró el proceso 'java'"**
- Asegúrate de que la aplicación esté ejecutándose
- Verifica el nombre del proceso: puede ser `javaw.exe` en lugar de `java.exe`
- Ejecuta: `.\diagnosticar-contadores.ps1` para ver qué procesos están disponibles

**Error: "No se puede ejecutar scripts"**
- Ejecuta PowerShell como administrador
- O ejecuta: `Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser`

**Los valores de CPU y E/S están en 0**
- **Causa común:** Los contadores de rendimiento de Windows tardan en inicializarse
- **Solución 1:** Espera 10-30 segundos después de iniciar la aplicación antes de monitorear
- **Solución 2:** Realiza acciones en la aplicación (navegar, hacer clic, etc.) para generar actividad
- **Solución 3:** Ejecuta `.\diagnosticar-contadores.ps1` para diagnosticar el problema
- **Solución 4:** Usa la versión simple: `.\monitorear-rendimiento-simple.ps1` (no requiere contadores)
- **Solución 5:** Usa el Administrador de Tareas manualmente (método más confiable)

**El script se detiene después de pocas muestras**
- Verifica que la aplicación siga ejecutándose
- Revisa si hay errores en la consola de PowerShell
- Intenta ejecutar como Administrador

---

## Próximos Pasos

1. Ejecuta las pruebas según los escenarios en `GUIA_MEDICION_RENDIMIENTO.md`
2. Captura pantallas durante cada escenario
3. Completa la plantilla en `EJEMPLO_REPORTE_RENDIMIENTO.md`
4. Analiza los resultados y documenta conclusiones

---

## Contacto y Soporte

Para dudas sobre el proceso de medición, consulta la guía completa o revisa los ejemplos incluidos.

