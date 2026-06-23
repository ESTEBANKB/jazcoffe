@echo off
REM Script para ejecutar la aplicación y monitorear rendimiento simultáneamente
REM Uso: ejecutar-con-monitoreo.bat

echo ========================================
echo JazzCoffee POS - Ejecutar con Monitoreo
echo ========================================
echo.

REM Verificar si PowerShell está disponible
powershell -Command "Get-Host" >nul 2>&1
if errorlevel 1 (
    echo ERROR: PowerShell no está disponible
    pause
    exit /b 1
)

echo Iniciando aplicación en segundo plano...
echo.

REM Compilar y ejecutar la aplicación (ajustar según tu método de ejecución)
REM Opción 1: Si usas Maven
start "JazzCoffee POS" cmd /c "mvn javafx:run"

REM Opción 2: Si tienes un JAR ejecutable
REM start "JazzCoffee POS" cmd /c "java -jar target\ppi-cafe-0.1.0.jar"

REM Esperar a que la aplicación inicie
echo Esperando 10 segundos para que la aplicación inicie...
timeout /t 10 /nobreak >nul

echo.
echo Iniciando monitoreo de rendimiento...
echo Presiona Ctrl+C para detener el monitoreo (la aplicación seguirá ejecutándose)
echo.

REM Ejecutar el script de monitoreo
powershell -ExecutionPolicy Bypass -File "monitorear-rendimiento.ps1" -NombreProceso "java" -DuracionSegundos 600 -IntervaloSegundos 5

echo.
echo Monitoreo completado.
pause

