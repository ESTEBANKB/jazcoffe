package com.cafeuniv.ppi.service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Monitor de rendimiento integrado en la aplicación.
 * 
 * Captura métricas de CPU, Memoria RAM y Disco I/O durante la ejecución.
 * Genera un archivo CSV con todas las métricas capturadas.
 */
public class PerformanceMonitor {
    
    private static PerformanceMonitor instance;
    private Timer timer;
    private boolean isMonitoring = false;
    private PrintWriter csvWriter;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    // Métricas acumuladas para cálculo de promedios
    private final AtomicLong cpuTimeSum = new AtomicLong(0);
    private final AtomicLong memorySum = new AtomicLong(0);
    private final AtomicLong diskReadSum = new AtomicLong(0);
    private final AtomicLong diskWriteSum = new AtomicLong(0);
    private final AtomicLong sampleCount = new AtomicLong(0);
    
    // Referencias para cálculo de CPU
    private long previousCpuTime = 0;
    private long previousTimestamp = 0;
    
    // Referencias para cálculo de I/O de disco
    private long previousDiskRead = 0;
    private long previousDiskWrite = 0;
    
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final com.sun.management.OperatingSystemMXBean osBean = 
        (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    
    private PerformanceMonitor() {
        // Constructor privado para singleton
    }
    
    public static synchronized PerformanceMonitor getInstance() {
        if (instance == null) {
            instance = new PerformanceMonitor();
        }
        return instance;
    }
    
    /**
     * Inicia el monitoreo de rendimiento.
     * 
     * @param intervalSeconds Intervalo en segundos entre cada captura de métricas
     * @param outputFile Nombre del archivo CSV de salida
     */
    public void startMonitoring(int intervalSeconds, String outputFile) {
        if (isMonitoring) {
            System.out.println("El monitoreo ya está activo.");
            return;
        }
        
        try {
            // Crear archivo CSV
            csvWriter = new PrintWriter(new FileWriter(outputFile, false));
            csvWriter.println("Timestamp,CPU_Porcentaje,Memoria_MB,DiscoLectura_MB_s,DiscoEscritura_MB_s,CPU_Total_Sistema,Memoria_Total_MB");
            csvWriter.flush();
            
            // Inicializar valores de referencia
            previousCpuTime = osBean.getProcessCpuTime();
            previousTimestamp = System.currentTimeMillis();
            
            // Inicializar tamaño de base de datos como referencia
            try {
                Path dbPath = Path.of("data/cafe.mv.db");
                if (Files.exists(dbPath)) {
                    previousDiskRead = Files.size(dbPath);
                    previousDiskWrite = previousDiskRead;
                } else {
                    previousDiskRead = 0;
                    previousDiskWrite = 0;
                }
            } catch (Exception e) {
                previousDiskRead = 0;
                previousDiskWrite = 0;
            }
            
            isMonitoring = true;
            timer = new Timer("PerformanceMonitor", true);
            
            // Captura inicial
            captureMetrics();
            
            // Programar capturas periódicas
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    captureMetrics();
                }
            }, intervalSeconds * 1000L, intervalSeconds * 1000L);
            
            System.out.println("Monitor de rendimiento iniciado. Archivo: " + outputFile);
            System.out.println("Intervalo: " + intervalSeconds + " segundos");
            
        } catch (IOException e) {
            System.err.println("Error al iniciar el monitor de rendimiento: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Inicia el monitoreo con valores por defecto (5 segundos, rendimiento_report.csv)
     */
    public void startMonitoring() {
        startMonitoring(5, "rendimiento_report.csv");
    }
    
    /**
     * Detiene el monitoreo y genera un resumen.
     */
    public void stopMonitoring() {
        if (!isMonitoring) {
            return;
        }
        
        isMonitoring = false;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        
        if (csvWriter != null) {
            csvWriter.close();
            csvWriter = null;
        }
        
        // Generar resumen
        generateSummary();
        
        System.out.println("Monitor de rendimiento detenido.");
    }
    
    /**
     * Captura las métricas actuales y las guarda en el CSV.
     */
    private void captureMetrics() {
        try {
            long currentTimestamp = System.currentTimeMillis();
            long elapsedTime = currentTimestamp - previousTimestamp;
            
            // CPU %
            double cpuPercent = calculateCpuPercent(elapsedTime);
            
            // Memoria RAM (MB)
            long memoryUsed = memoryBean.getHeapMemoryUsage().getUsed();
            double memoryMB = memoryUsed / (1024.0 * 1024.0);
            
            // Disco I/O (MB/s) - pasar el tiempo transcurrido
            double[] diskIO = calculateDiskIO(elapsedTime);
            double diskReadMBs = diskIO[0];
            double diskWriteMBs = diskIO[1];
            
            // CPU total del sistema
            double systemCpuPercent = 0;
            try {
                // Usar el método no deprecado si está disponible (Java 14+)
                if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                    com.sun.management.OperatingSystemMXBean sunOsBean = 
                        (com.sun.management.OperatingSystemMXBean) osBean;
                    systemCpuPercent = sunOsBean.getCpuLoad() * 100.0;
                } else {
                    // Fallback para versiones anteriores
                    systemCpuPercent = osBean.getSystemCpuLoad() * 100.0;
                }
            } catch (Exception e) {
                // Si falla, usar 0
                systemCpuPercent = 0;
            }
            if (systemCpuPercent < 0 || Double.isNaN(systemCpuPercent)) {
                systemCpuPercent = 0;
            }
            
            // Memoria total del sistema (MB)
            long totalMemory = 0;
            try {
                // Usar el método no deprecado si está disponible (Java 14+)
                if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                    com.sun.management.OperatingSystemMXBean sunOsBean = 
                        (com.sun.management.OperatingSystemMXBean) osBean;
                    totalMemory = sunOsBean.getTotalMemorySize();
                } else {
                    // Fallback para versiones anteriores
                    totalMemory = osBean.getTotalPhysicalMemorySize();
                }
            } catch (Exception e) {
                // Si falla, usar 0
                totalMemory = 0;
            }
            double totalMemoryMB = totalMemory / (1024.0 * 1024.0);
            
            // Timestamp
            String timestamp = dateFormat.format(new Date());
            
            // Guardar en CSV
            String csvLine = String.format("%s,%.2f,%.2f,%.4f,%.4f,%.2f,%.2f",
                timestamp, cpuPercent, memoryMB, diskReadMBs, diskWriteMBs, 
                systemCpuPercent, totalMemoryMB);
            
            synchronized (this) {
                if (csvWriter != null) {
                    csvWriter.println(csvLine);
                    csvWriter.flush();
                }
            }
            
            // Acumular para promedios
            cpuTimeSum.addAndGet((long)(cpuPercent * 100));
            memorySum.addAndGet((long)(memoryMB * 100));
            diskReadSum.addAndGet((long)(diskReadMBs * 10000));
            diskWriteSum.addAndGet((long)(diskWriteMBs * 10000));
            sampleCount.incrementAndGet();
            
            // Mostrar en consola
            System.out.printf("[%s] CPU: %.2f%% | Memoria: %.2f MB | Disco R: %.4f MB/s | Disco W: %.4f MB/s%n",
                timestamp, cpuPercent, memoryMB, diskReadMBs, diskWriteMBs);
            
            // Actualizar referencias
            previousTimestamp = currentTimestamp;
            
        } catch (Exception e) {
            System.err.println("Error al capturar métricas: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Calcula el porcentaje de CPU usado por el proceso.
     */
    private double calculateCpuPercent(long elapsedTimeMs) {
        if (elapsedTimeMs <= 0) {
            return 0.0;
        }
        
        long currentCpuTime = osBean.getProcessCpuTime();
        long cpuTimeDelta = currentCpuTime - previousCpuTime;
        previousCpuTime = currentCpuTime;
        
        // CPU time está en nanosegundos, convertir a porcentaje
        // elapsedTimeMs está en milisegundos, convertir a nanosegundos
        long elapsedTimeNs = elapsedTimeMs * 1_000_000L;
        
        // Número de procesadores lógicos
        int processors = osBean.getAvailableProcessors();
        
        // Porcentaje de CPU = (tiempo de CPU usado / tiempo transcurrido) / número de procesadores * 100
        double cpuPercent = (cpuTimeDelta / (double) elapsedTimeNs) * processors * 100.0;
        
        // Limitar entre 0 y 100
        return Math.max(0.0, Math.min(100.0, cpuPercent));
    }
    
    /**
     * Calcula el I/O de disco en MB/s.
     * Retorna un array [lectura, escritura]
     * 
     * NOTA: Java no proporciona acceso directo a I/O de disco por proceso.
     * Esta implementación usa una aproximación basada en el tamaño de archivos
     * de la base de datos (data/cafe.mv.db) como proxy.
     * 
     * @param elapsedTimeMs Tiempo transcurrido desde la última captura en milisegundos
     */
    private double[] calculateDiskIO(long elapsedTimeMs) {
        try {
            // Obtener tamaño actual del archivo de base de datos como proxy de I/O
            Path dbPath = Path.of("data/cafe.mv.db");
            long currentDbSize = 0;
            if (Files.exists(dbPath)) {
                currentDbSize = Files.size(dbPath);
            }
            
            // Calcular delta desde la última medición
            long sizeDelta = currentDbSize - previousDiskRead;
            
            // Actualizar referencias
            previousDiskRead = currentDbSize;
            previousDiskWrite = currentDbSize;
            
            // Convertir tiempo a segundos
            double intervalSeconds = elapsedTimeMs / 1000.0;
            if (intervalSeconds <= 0) {
                intervalSeconds = 5.0; // Valor por defecto
            }
            
            // Aproximación: asumir que los cambios de tamaño son tanto lectura como escritura
            // En una base de datos, los cambios generalmente implican ambas operaciones
            long readDelta = Math.max(0, sizeDelta);
            long writeDelta = Math.max(0, sizeDelta);
            
            // Convertir bytes a MB/s
            double readMBs = (readDelta / (1024.0 * 1024.0)) / intervalSeconds;
            double writeMBs = (writeDelta / (1024.0 * 1024.0)) / intervalSeconds;
            
            return new double[]{Math.max(0, readMBs), Math.max(0, writeMBs)};
            
        } catch (Exception e) {
            // Si no se puede obtener I/O, retornar 0
            return new double[]{0.0, 0.0};
        }
    }
    
    /**
     * Genera un archivo de resumen con promedios.
     */
    private void generateSummary() {
        if (sampleCount.get() == 0) {
            return;
        }
        
        try {
            String summaryFile = "rendimiento_report_resumen.txt";
            PrintWriter summaryWriter = new PrintWriter(new FileWriter(summaryFile, false));
            
            double cpuPromedio = (cpuTimeSum.get() / 100.0) / sampleCount.get();
            double memoriaPromedio = (memorySum.get() / 100.0) / sampleCount.get();
            double discoLecturaPromedio = (diskReadSum.get() / 10000.0) / sampleCount.get();
            double discoEscrituraPromedio = (diskWriteSum.get() / 10000.0) / sampleCount.get();
            
            summaryWriter.println("========================================");
            summaryWriter.println("RESUMEN DE RENDIMIENTO - JazzCoffee POS");
            summaryWriter.println("========================================");
            summaryWriter.println("Fecha: " + dateFormat.format(new Date()));
            summaryWriter.println("Muestras capturadas: " + sampleCount.get());
            summaryWriter.println();
            summaryWriter.println("PROMEDIOS:");
            summaryWriter.println("----------");
            summaryWriter.printf("CPU: %.2f %%\n", cpuPromedio);
            summaryWriter.printf("Memoria RAM: %.2f MB\n", memoriaPromedio);
            summaryWriter.printf("Disco Lectura: %.4f MB/s\n", discoLecturaPromedio);
            summaryWriter.printf("Disco Escritura: %.4f MB/s\n", discoEscrituraPromedio);
            summaryWriter.println();
            summaryWriter.println("Archivo completo: rendimiento_report.csv");
            
            summaryWriter.close();
            
            System.out.println("\n========================================");
            System.out.println("RESUMEN DE MÉTRICAS:");
            System.out.println("========================================");
            System.out.printf("Muestras: %d\n", sampleCount.get());
            System.out.printf("CPU Promedio: %.2f %%\n", cpuPromedio);
            System.out.printf("Memoria Promedio: %.2f MB\n", memoriaPromedio);
            System.out.printf("Disco Lectura Promedio: %.4f MB/s\n", discoLecturaPromedio);
            System.out.printf("Disco Escritura Promedio: %.4f MB/s\n", discoEscrituraPromedio);
            System.out.println("Resumen guardado en: " + summaryFile);
            
        } catch (IOException e) {
            System.err.println("Error al generar resumen: " + e.getMessage());
        }
    }
    
    /**
     * Detiene el monitoreo cuando la aplicación se cierra.
     */
    public void shutdown() {
        stopMonitoring();
    }
}

