# Script de diagnóstico para verificar contadores de rendimiento
# Uso: .\diagnosticar-contadores.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Diagnóstico de Contadores de Rendimiento" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Buscar procesos Java
Write-Host "1. Buscando procesos Java..." -ForegroundColor Yellow
$procesosJava = Get-Process | Where-Object {$_.ProcessName -like "*java*"}
if ($procesosJava) {
    Write-Host "   Procesos Java encontrados:" -ForegroundColor Green
    $procesosJava | ForEach-Object {
        $memoriaMB = [math]::Round($_.WorkingSet64 / 1MB, 2)
        Write-Host "   - $($_.ProcessName) (PID: $($_.Id)) - Memoria: $memoriaMB MB" -ForegroundColor White
    }
} else {
    Write-Host "   No se encontraron procesos Java." -ForegroundColor Red
    Write-Host "   Ejecuta tu aplicación primero." -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Probar contadores de CPU
Write-Host "2. Probando contadores de CPU..." -ForegroundColor Yellow
$proceso = $procesosJava | Select-Object -First 1
$nombreProceso = $proceso.ProcessName

try {
    $cpuCounter = Get-Counter "\Process($nombreProceso)\% Processor Time" -ErrorAction Stop
    Write-Host "   ✓ Contador de CPU disponible" -ForegroundColor Green
    Write-Host "   Valor actual: $($cpuCounter.CounterSamples.CookedValue)%" -ForegroundColor White
} catch {
    Write-Host "   ✗ Error al acceder al contador de CPU: $_" -ForegroundColor Red
    Write-Host "   Intentando con PID..." -ForegroundColor Yellow
    try {
        $processNameWithPid = "$nombreProceso#$($proceso.Id)"
        $cpuCounter = Get-Counter "\Process($processNameWithPid)\% Processor Time" -ErrorAction Stop
        Write-Host "   ✓ Contador de CPU disponible (con PID)" -ForegroundColor Green
        Write-Host "   Valor actual: $($cpuCounter.CounterSamples.CookedValue)%" -ForegroundColor White
    } catch {
        Write-Host "   ✗ También falló con PID: $_" -ForegroundColor Red
    }
}

Write-Host ""

# Probar contadores de I/O
Write-Host "3. Probando contadores de I/O..." -ForegroundColor Yellow
try {
    $ioReadCounter = Get-Counter "\Process($nombreProceso)\IO Read Bytes/sec" -ErrorAction Stop
    Write-Host "   ✓ Contador de Lectura disponible" -ForegroundColor Green
    Write-Host "   Valor actual: $($ioReadCounter.CounterSamples.CookedValue) bytes/seg" -ForegroundColor White
} catch {
    Write-Host "   ✗ Error al acceder al contador de Lectura: $_" -ForegroundColor Red
}

try {
    $ioWriteCounter = Get-Counter "\Process($nombreProceso)\IO Write Bytes/sec" -ErrorAction Stop
    Write-Host "   ✓ Contador de Escritura disponible" -ForegroundColor Green
    Write-Host "   Valor actual: $($ioWriteCounter.CounterSamples.CookedValue) bytes/seg" -ForegroundColor White
} catch {
    Write-Host "   ✗ Error al acceder al contador de Escritura: $_" -ForegroundColor Red
}

Write-Host ""

# Verificar permisos
Write-Host "4. Verificando permisos..." -ForegroundColor Yellow
$currentPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
$isAdmin = $currentPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if ($isAdmin) {
    Write-Host "   ✓ Ejecutando como Administrador" -ForegroundColor Green
} else {
    Write-Host "   ⚠ No estás ejecutando como Administrador" -ForegroundColor Yellow
    Write-Host "   Algunos contadores pueden requerir permisos elevados." -ForegroundColor Yellow
}

Write-Host ""

# Verificar servicio de rendimiento
Write-Host "5. Verificando servicio de rendimiento..." -ForegroundColor Yellow
$perfService = Get-Service -Name "PerfHost" -ErrorAction SilentlyContinue
if ($perfService) {
    if ($perfService.Status -eq "Running") {
        Write-Host "   ✓ Servicio PerfHost está ejecutándose" -ForegroundColor Green
    } else {
        Write-Host "   ✗ Servicio PerfHost no está ejecutándose" -ForegroundColor Red
        Write-Host "   Estado: $($perfService.Status)" -ForegroundColor White
    }
} else {
    Write-Host "   ⚠ No se pudo verificar el servicio PerfHost" -ForegroundColor Yellow
}

Write-Host ""

# Recomendaciones
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "RECOMENDACIONES:" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($cpuCounter -eq $null) {
    Write-Host "1. Los contadores pueden tardar en inicializarse." -ForegroundColor White
    Write-Host "   Espera 10-30 segundos después de iniciar la aplicación." -ForegroundColor White
    Write-Host ""
}

Write-Host "2. Si los valores están en 0, puede ser porque:" -ForegroundColor White
Write-Host "   - La aplicación está inactiva (no está usando CPU/disco)" -ForegroundColor Gray
Write-Host "   - Los contadores aún no se han inicializado" -ForegroundColor Gray
Write-Host "   - El proceso Java no es el correcto" -ForegroundColor Gray
Write-Host ""

Write-Host "3. Para obtener mejores resultados:" -ForegroundColor White
Write-Host "   - Realiza acciones en la aplicación mientras se monitorea" -ForegroundColor Gray
Write-Host "   - Usa el nombre correcto del proceso (java o javaw)" -ForegroundColor Gray
Write-Host "   - Ejecuta el script como Administrador si es necesario" -ForegroundColor Gray
Write-Host ""

Write-Host "4. Método alternativo:" -ForegroundColor White
Write-Host "   Usa el Administrador de Tareas manualmente (Ctrl+Shift+Esc)" -ForegroundColor Gray
Write-Host "   Ve a la pestaña 'Detalles' y activa las columnas necesarias." -ForegroundColor Gray
Write-Host ""

