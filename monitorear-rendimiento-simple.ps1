# Versión simplificada del monitor de rendimiento
# Usa métodos más básicos que no dependen de contadores de rendimiento
# Uso: .\monitorear-rendimiento-simple.ps1

param(
    [Parameter(Mandatory=$false)]
    [string]$NombreProceso = "java",
    
    [Parameter(Mandatory=$false)]
    [int]$DuracionSegundos = 300,
    
    [Parameter(Mandatory=$false)]
    [int]$IntervaloSegundos = 5,
    
    [Parameter(Mandatory=$false)]
    [string]$ArchivoSalida = "rendimiento_report_simple.csv"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Monitor de Rendimiento (Versión Simple)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Buscar proceso
$proceso = Get-Process -Name $NombreProceso -ErrorAction SilentlyContinue
if (-not $proceso) {
    Write-Host "ERROR: No se encontró el proceso '$NombreProceso'" -ForegroundColor Red
    Write-Host ""
    Write-Host "Procesos Java encontrados:" -ForegroundColor Yellow
    Get-Process | Where-Object {$_.ProcessName -like "*java*"} | Select-Object ProcessName, Id, CPU, WorkingSet
    exit 1
}

if ($proceso.Count -gt 1) {
    $proceso = $proceso | Sort-Object WorkingSet64 -Descending | Select-Object -First 1
}

Write-Host "Proceso: $($proceso.ProcessName) (PID: $($proceso.Id))" -ForegroundColor Green
Write-Host "Duración: $DuracionSegundos segundos" -ForegroundColor Green
Write-Host "Intervalo: $IntervaloSegundos segundos" -ForegroundColor Green
Write-Host ""

# Preparar CSV
$csvHeader = "Timestamp,CPU_Tiempo_Acumulado,Memoria_MB,CPU_Porcentaje_Aprox"
$csvHeader | Out-File -FilePath $ArchivoSalida -Encoding UTF8

$iteraciones = [math]::Floor($DuracionSegundos / $IntervaloSegundos)
$contador = 0
$cpuInicial = $proceso.CPU
$tiempoInicial = Get-Date

Write-Host "Monitoreando (versión simple - sin contadores de rendimiento)..." -ForegroundColor Cyan
Write-Host "NOTA: Esta versión calcula CPU de forma aproximada." -ForegroundColor Yellow
Write-Host ""

while ($contador -lt $iteraciones) {
    try {
        $procesoActual = Get-Process -Id $proceso.Id -ErrorAction SilentlyContinue
        if (-not $procesoActual) {
            Write-Host "ADVERTENCIA: El proceso terminó." -ForegroundColor Yellow
            break
        }
        
        $tiempoActual = Get-Date
        $tiempoTranscurrido = ($tiempoActual - $tiempoInicial).TotalSeconds
        
        # Memoria
        $memoriaMB = [math]::Round($procesoActual.WorkingSet64 / 1MB, 2)
        
        # CPU aproximado (tiempo de CPU acumulado)
        $cpuAcumulado = $procesoActual.CPU
        
        # CPU % aproximado (solo si ha pasado tiempo)
        $cpuPorcentaje = 0
        if ($tiempoTranscurrido -gt 0) {
            $cpuUsado = ($procesoActual.CPU - $cpuInicial)
            $cores = (Get-WmiObject Win32_ComputerSystem).NumberOfLogicalProcessors
            $cpuPorcentaje = [math]::Round(($cpuUsado / $tiempoTranscurrido / $cores) * 100, 2)
        }
        
        $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        
        # Mostrar
        Write-Host "[$timestamp] CPU: $cpuPorcentaje% | Memoria: $memoriaMB MB | CPU Acumulado: $cpuAcumulado s"
        
        # Guardar
        $csvLine = "$timestamp,$cpuAcumulado,$memoriaMB,$cpuPorcentaje"
        $csvLine | Out-File -FilePath $ArchivoSalida -Append -Encoding UTF8
        
        $contador++
        Start-Sleep -Seconds $IntervaloSegundos
        
    } catch {
        Write-Host "Error: $_" -ForegroundColor Red
        $contador++
        Start-Sleep -Seconds $IntervaloSegundos
    }
}

Write-Host ""
Write-Host "Monitoreo completado. Datos guardados en: $ArchivoSalida" -ForegroundColor Green
Write-Host ""
Write-Host "NOTA: Esta versión no captura I/O de disco." -ForegroundColor Yellow
Write-Host "      Para I/O de disco, usa el Administrador de Tareas manualmente" -ForegroundColor Yellow
Write-Host "      o ejecuta el script principal: monitorear-rendimiento.ps1" -ForegroundColor Yellow

