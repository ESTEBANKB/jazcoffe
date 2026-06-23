# Script de Monitoreo de Rendimiento para JazzCoffee POS
# Uso: .\monitorear-rendimiento.ps1 -NombreProceso "java" -DuracionSegundos 300 -IntervaloSegundos 5

param(
    [Parameter(Mandatory=$false)]
    [string]$NombreProceso = "java",
    
    [Parameter(Mandatory=$false)]
    [int]$DuracionSegundos = 300,
    
    [Parameter(Mandatory=$false)]
    [int]$IntervaloSegundos = 5,
    
    [Parameter(Mandatory=$false)]
    [string]$ArchivoSalida = "rendimiento_report.csv"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Monitor de Rendimiento - JazzCoffee POS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar si el proceso existe
$proceso = Get-Process -Name $NombreProceso -ErrorAction SilentlyContinue
if (-not $proceso) {
    Write-Host "ERROR: No se encontró el proceso '$NombreProceso'" -ForegroundColor Red
    Write-Host "Asegúrate de que la aplicación esté ejecutándose." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Procesos Java encontrados:" -ForegroundColor Yellow
    Get-Process | Where-Object {$_.ProcessName -like "*java*"} | Select-Object ProcessName, Id, CPU, WorkingSet
    Write-Host ""
    Write-Host "TIP: Si tu aplicación usa 'javaw.exe', ejecuta:" -ForegroundColor Cyan
    Write-Host "  .\monitorear-rendimiento.ps1 -NombreProceso 'javaw'" -ForegroundColor Cyan
    exit 1
}

# Si hay múltiples procesos, usar el más reciente o el que más memoria usa
if ($proceso.Count -gt 1) {
    Write-Host "ADVERTENCIA: Se encontraron $($proceso.Count) procesos '$NombreProceso'" -ForegroundColor Yellow
    Write-Host "Usando el proceso con más memoria (probablemente tu aplicación):" -ForegroundColor Yellow
    $proceso = $proceso | Sort-Object WorkingSet64 -Descending | Select-Object -First 1
    Write-Host "  PID: $($proceso.Id) | Memoria: $([math]::Round($proceso.WorkingSet64 / 1MB, 2)) MB" -ForegroundColor Yellow
    Write-Host ""
}

Write-Host "Proceso encontrado: $($proceso.ProcessName) (PID: $($proceso.Id))" -ForegroundColor Green
Write-Host "Duración: $DuracionSegundos segundos" -ForegroundColor Green
Write-Host "Intervalo: $IntervaloSegundos segundos" -ForegroundColor Green
Write-Host "Archivo de salida: $ArchivoSalida" -ForegroundColor Green
Write-Host ""
Write-Host "Iniciando monitoreo en 3 segundos..." -ForegroundColor Yellow
Write-Host "NOTA: Los contadores de rendimiento pueden tardar unos segundos en inicializarse." -ForegroundColor Cyan
Write-Host "      Los primeros valores pueden estar en 0 hasta que Windows active los contadores." -ForegroundColor Cyan
Start-Sleep -Seconds 3
Write-Host ""

# Preparar archivo CSV
$csvHeader = "Timestamp,CPU_Porcentaje,Memoria_MB,DiscoLectura_MB_s,DiscoEscritura_MB_s,CPU_Total,Memoria_Total_MB"
$csvHeader | Out-File -FilePath $ArchivoSalida -Encoding UTF8

# Contador de iteraciones
$iteraciones = [math]::Floor($DuracionSegundos / $IntervaloSegundos)
$contador = 0

# Contadores para promedios
$cpuSum = 0
$memoriaSum = 0
$discoLecturaSum = 0
$discoEscrituraSum = 0
$muestras = 0

Write-Host "Monitoreando..." -ForegroundColor Cyan
Write-Host ""

$inicio = Get-Date

# Variables para cálculo de CPU (necesitamos dos mediciones)
$cpuAnterior = $null
$tiempoAnterior = $null

while ($contador -lt $iteraciones) {
    try {
        # Obtener el proceso actual (puede cambiar el PID)
        $procesoActual = Get-Process -Id $proceso.Id -ErrorAction SilentlyContinue
        if (-not $procesoActual) {
            Write-Host "ADVERTENCIA: El proceso terminó. Deteniendo monitoreo." -ForegroundColor Yellow
            break
        }
        
        $tiempoActual = Get-Date
        
        # Memoria en MB
        $memoriaMB = [math]::Round($procesoActual.WorkingSet64 / 1MB, 2)
        
        # CPU - Método 1: Intentar con Get-Counter (más preciso)
        $cpuCounter = $null
        try {
            $counter = Get-Counter "\Process($($procesoActual.ProcessName))\% Processor Time" -ErrorAction Stop
            $cpuCounter = $counter.CounterSamples.CookedValue
        } catch {
            # Método 2: Calcular CPU manualmente usando tiempo de CPU
            if ($cpuAnterior -ne $null -and $tiempoAnterior -ne $null) {
                $tiempoTranscurrido = ($tiempoActual - $tiempoAnterior).TotalSeconds
                if ($tiempoTranscurrido -gt 0) {
                    $cpuUsado = ($procesoActual.CPU - $cpuAnterior) / $tiempoTranscurrido
                    # Convertir a porcentaje (asumiendo 1 core, ajustar si es multi-core)
                    $cores = (Get-WmiObject Win32_ComputerSystem).NumberOfLogicalProcessors
                    $cpuCounter = ($cpuUsado / $cores) * 100
                } else {
                    $cpuCounter = 0
                }
            } else {
                $cpuCounter = 0
            }
        }
        
        # Guardar valores para próxima iteración
        $cpuAnterior = $procesoActual.CPU
        $tiempoAnterior = $tiempoActual
        
        if (-not $cpuCounter) { $cpuCounter = 0 }
        $cpuCounter = [math]::Round($cpuCounter, 2)
        
        # Disco I/O - Intentar múltiples métodos
        $ioReadMB = 0
        $ioWriteMB = 0
        
        try {
            # Método 1: Get-Counter (más preciso)
            $ioReadCounter = Get-Counter "\Process($($procesoActual.ProcessName))\IO Read Bytes/sec" -ErrorAction Stop
            $ioWriteCounter = Get-Counter "\Process($($procesoActual.ProcessName))\IO Write Bytes/sec" -ErrorAction Stop
            
            $ioRead = $ioReadCounter.CounterSamples.CookedValue
            $ioWrite = $ioWriteCounter.CounterSamples.CookedValue
            
            if ($ioRead) { $ioReadMB = [math]::Round($ioRead / 1MB, 4) }
            if ($ioWrite) { $ioWriteMB = [math]::Round($ioWrite / 1MB, 4) }
        } catch {
            # Método 2: Usar WMI (menos preciso pero más confiable)
            try {
                $wmiProcess = Get-WmiObject Win32_Process -Filter "ProcessId = $($procesoActual.Id)" -ErrorAction Stop
                if ($wmiProcess) {
                    # WMI no proporciona I/O directamente, pero podemos intentar con Get-Counter usando PID
                    $processNameWithPid = "$($procesoActual.ProcessName)#$($procesoActual.Id)"
                    $ioReadCounter = Get-Counter "\Process($processNameWithPid)\IO Read Bytes/sec" -ErrorAction SilentlyContinue
                    $ioWriteCounter = Get-Counter "\Process($processNameWithPid)\IO Write Bytes/sec" -ErrorAction SilentlyContinue
                    
                    if ($ioReadCounter) { 
                        $ioRead = $ioReadCounter.CounterSamples.CookedValue
                        if ($ioRead) { $ioReadMB = [math]::Round($ioRead / 1MB, 4) }
                    }
                    if ($ioWriteCounter) { 
                        $ioWrite = $ioWriteCounter.CounterSamples.CookedValue
                        if ($ioWrite) { $ioWriteMB = [math]::Round($ioWrite / 1MB, 4) }
                    }
                }
            } catch {
                # Si todo falla, dejar en 0
                $ioReadMB = 0
                $ioWriteMB = 0
            }
        }
        
        # Obtener CPU total del sistema
        $cpuTotal = 0
        try {
            $cpuTotalCounter = Get-Counter "\Processor(_Total)\% Processor Time" -ErrorAction Stop
            $cpuTotal = $cpuTotalCounter.CounterSamples.CookedValue
        } catch {
            $cpuTotal = 0
        }
        if (-not $cpuTotal) { $cpuTotal = 0 }
        
        # Memoria total del sistema
        $memoriaTotal = (Get-CimInstance Win32_ComputerSystem).TotalPhysicalMemory / 1MB
        
        # Timestamp
        $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        
        # Mostrar en consola con colores según valores
        $colorCPU = if ($cpuCounter -gt 0) { "Green" } else { "DarkGray" }
        $colorIO = if ($ioReadMB -gt 0 -or $ioWriteMB -gt 0) { "Green" } else { "DarkGray" }
        
        $mensaje = "[$timestamp] CPU: $cpuCounter% | Memoria: $memoriaMB MB | Disco R: $ioReadMB MB/s | Disco W: $ioWriteMB MB/s"
        
        # Mostrar con colores
        Write-Host "[$timestamp] " -NoNewline
        Write-Host "CPU: $cpuCounter% " -ForegroundColor $colorCPU -NoNewline
        Write-Host "| Memoria: $memoriaMB MB " -NoNewline
        Write-Host "| Disco R: $ioReadMB MB/s " -ForegroundColor $colorIO -NoNewline
        Write-Host "| Disco W: $ioWriteMB MB/s" -ForegroundColor $colorIO
        
        # Advertencia si todos los valores están en 0 después de varias iteraciones
        if ($contador -gt 3 -and $cpuCounter -eq 0 -and $ioReadMB -eq 0 -and $ioWriteMB -eq 0) {
            if ($contador -eq 4) {
                Write-Host "ADVERTENCIA: Los contadores aún muestran 0. Esto puede ser normal si la aplicación está inactiva." -ForegroundColor Yellow
                Write-Host "             Intenta realizar alguna acción en la aplicación para generar actividad." -ForegroundColor Yellow
            }
        }
        
        # Guardar en CSV
        $csvLine = "$timestamp,$([math]::Round($cpuCounter, 2)),$memoriaMB,$ioReadMB,$ioWriteMB,$([math]::Round($cpuTotal, 2)),$([math]::Round($memoriaTotal, 2))"
        $csvLine | Out-File -FilePath $ArchivoSalida -Append -Encoding UTF8
        
        # Acumular para promedios
        $cpuSum += $cpuCounter
        $memoriaSum += $memoriaMB
        $discoLecturaSum += $ioReadMB
        $discoEscrituraSum += $ioWriteMB
        $muestras++
        
        $contador++
        
        # Esperar intervalo
        Start-Sleep -Seconds $IntervaloSegundos
        
    } catch {
        Write-Host "Error al obtener métricas: $_" -ForegroundColor Red
        $contador++
        Start-Sleep -Seconds $IntervaloSegundos
    }
}

$fin = Get-Date
$duracionReal = ($fin - $inicio).TotalSeconds

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Monitoreo completado" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($muestras -gt 0) {
    # Calcular promedios
    $cpuPromedio = [math]::Round($cpuSum / $muestras, 2)
    $memoriaPromedio = [math]::Round($memoriaSum / $muestras, 2)
    $discoLecturaPromedio = [math]::Round($discoLecturaSum / $muestras, 4)
    $discoEscrituraPromedio = [math]::Round($discoEscrituraSum / $muestras, 4)
    
    Write-Host "RESUMEN DE MÉTRICAS:" -ForegroundColor Yellow
    Write-Host "-------------------" -ForegroundColor Yellow
    Write-Host "Duración total: $([math]::Round($duracionReal, 2)) segundos" -ForegroundColor White
    Write-Host "Muestras capturadas: $muestras" -ForegroundColor White
    Write-Host ""
    Write-Host "PROMEDIOS:" -ForegroundColor Cyan
    Write-Host "  CPU: $cpuPromedio %" -ForegroundColor White
    Write-Host "  Memoria RAM: $memoriaPromedio MB" -ForegroundColor White
    Write-Host "  Disco Lectura: $discoLecturaPromedio MB/s" -ForegroundColor White
    Write-Host "  Disco Escritura: $discoEscrituraPromedio MB/s" -ForegroundColor White
    Write-Host ""
    
    # Guardar resumen en archivo
    $resumenArchivo = $ArchivoSalida -replace "\.csv$", "_resumen.txt"
    $resumen = @"
========================================
RESUMEN DE RENDIMIENTO - JazzCoffee POS
========================================
Fecha: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
Duración: $([math]::Round($duracionReal, 2)) segundos
Muestras: $muestras

PROMEDIOS:
----------
CPU: $cpuPromedio %
Memoria RAM: $memoriaPromedio MB
Disco Lectura: $discoLecturaPromedio MB/s
Disco Escritura: $discoEscrituraPromedio MB/s

Archivo completo: $ArchivoSalida
"@
    $resumen | Out-File -FilePath $resumenArchivo -Encoding UTF8
    Write-Host "Resumen guardado en: $resumenArchivo" -ForegroundColor Green
}

Write-Host "Datos completos guardados en: $ArchivoSalida" -ForegroundColor Green
Write-Host ""
Write-Host "Puedes abrir el archivo CSV en Excel para análisis detallado." -ForegroundColor Yellow

