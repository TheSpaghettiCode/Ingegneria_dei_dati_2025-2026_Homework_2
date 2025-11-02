package it.uniroma3.lucene;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Classe per la raccolta e l'analisi delle metriche di indicizzazione.
 * Fornisce funzionalità per misurare i tempi di indicizzazione, contare i file
 * e rilevare errori durante il processo.
 */
public class IndexingMetrics {
    private int totalFiles;
    private int successfulFiles;
    private int failedFiles;
    private long startTime;
    private long endTime;
    private final List<FileMetric> fileMetrics;
    private final List<String> errors;
    private final Path metricsDirectory;

    /**
     * Classe interna per memorizzare le metriche di un singolo file.
     */
    public static class FileMetric {
        private final String fileName;
        private final long processingTime;
        private final boolean successful;
        private final String errorMessage;

        public FileMetric(String fileName, long processingTime, boolean successful, String errorMessage) {
            this.fileName = fileName;
            this.processingTime = processingTime;
            this.successful = successful;
            this.errorMessage = errorMessage;
        }

        public String getFileName() {
            return fileName;
        }

        public long getProcessingTime() {
            return processingTime;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Costruttore della classe IndexingMetrics.
     */
    public IndexingMetrics() {
        this.totalFiles = 0;
        this.successfulFiles = 0;
        this.failedFiles = 0;
        this.fileMetrics = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.metricsDirectory = Paths.get("metrics");
        
        // Crea la directory delle metriche se non esiste
        try {
            if (!Files.exists(metricsDirectory)) {
                Files.createDirectories(metricsDirectory);
            }
        } catch (IOException e) {
            System.err.println("Impossibile creare la directory delle metriche: " + e.getMessage());
        }
    }

    /**
     * Inizia la misurazione del tempo di indicizzazione.
     */
    public void startIndexing() {
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Termina la misurazione del tempo di indicizzazione.
     */
    public void endIndexing() {
        this.endTime = System.currentTimeMillis();
    }

    /**
     * Registra le metriche per un singolo file.
     * 
     * @param fileName Nome del file
     * @param processingTime Tempo di elaborazione in millisecondi
     * @param successful Indica se l'indicizzazione è avvenuta con successo
     * @param errorMessage Messaggio di errore (se presente)
     */
    public void recordFileMetric(String fileName, long processingTime, boolean successful, String errorMessage) {
        FileMetric metric = new FileMetric(fileName, processingTime, successful, errorMessage);
        fileMetrics.add(metric);
        
        totalFiles++;
        if (successful) {
            successfulFiles++;
        } else {
            failedFiles++;
            errors.add("Errore nell'indicizzazione di " + fileName + ": " + errorMessage);
        }
    }

    /**
     * Restituisce il numero totale di file elaborati.
     * 
     * @return Numero totale di file
     */
    public int getTotalFiles() {
        return totalFiles;
    }

    /**
     * Restituisce il numero di file indicizzati con successo.
     * 
     * @return Numero di file indicizzati con successo
     */
    public int getSuccessfulFiles() {
        return successfulFiles;
    }

    /**
     * Restituisce il numero di file che hanno generato errori.
     * 
     * @return Numero di file con errori
     */
    public int getFailedFiles() {
        return failedFiles;
    }

    /**
     * Restituisce il tempo totale di indicizzazione in millisecondi.
     * 
     * @return Tempo totale di indicizzazione
     */
    public long getTotalIndexingTime() {
        return endTime - startTime;
    }

    /**
     * Restituisce il tempo medio di indicizzazione per file in millisecondi.
     * 
     * @return Tempo medio di indicizzazione per file
     */
    public double getAverageFileProcessingTime() {
        OptionalDouble average = fileMetrics.stream()
                .filter(FileMetric::isSuccessful)
                .mapToLong(FileMetric::getProcessingTime)
                .average();
        return average.orElse(0.0);
    }

    /**
     * Restituisce il tempo massimo di indicizzazione per un singolo file.
     * 
     * @return Tempo massimo di indicizzazione
     */
    public long getMaxFileProcessingTime() {
        return fileMetrics.stream()
                .filter(FileMetric::isSuccessful)
                .mapToLong(FileMetric::getProcessingTime)
                .max()
                .orElse(0);
    }

    /**
     * Restituisce il tempo minimo di indicizzazione per un singolo file.
     * 
     * @return Tempo minimo di indicizzazione
     */
    public long getMinFileProcessingTime() {
        return fileMetrics.stream()
                .filter(FileMetric::isSuccessful)
                .mapToLong(FileMetric::getProcessingTime)
                .min()
                .orElse(0);
    }

    /**
     * Restituisce la lista degli errori verificatisi durante l'indicizzazione.
     * 
     * @return Lista degli errori
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * Genera un report delle metriche in formato JSON.
     * 
     * @return Map contenente il report
     */
    public Map<String, Object> generateJsonReport() {
        Map<String, Object> report = new HashMap<>();
        
        // Informazioni generali
        report.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        report.put("totalFiles", totalFiles);
        report.put("successfulFiles", successfulFiles);
        report.put("failedFiles", failedFiles);
        report.put("totalIndexingTimeMs", getTotalIndexingTime());
        report.put("averageFileProcessingTimeMs", getAverageFileProcessingTime());
        report.put("maxFileProcessingTimeMs", getMaxFileProcessingTime());
        report.put("minFileProcessingTimeMs", getMinFileProcessingTime());
        
        // Errori
        report.put("errors", errors);
        
        // Metriche dettagliate per file
        List<Map<String, Object>> fileMetricsArray = new ArrayList<>();
        for (FileMetric metric : fileMetrics) {
            Map<String, Object> fileMetricObj = new HashMap<>();
            fileMetricObj.put("fileName", metric.getFileName());
            fileMetricObj.put("processingTimeMs", metric.getProcessingTime());
            fileMetricObj.put("successful", metric.isSuccessful());
            if (!metric.isSuccessful()) {
                fileMetricObj.put("errorMessage", metric.getErrorMessage());
            }
            fileMetricsArray.add(fileMetricObj);
        }
        report.put("fileMetrics", fileMetricsArray);
        
        // Analisi delle anomalie
        report.put("anomalies", detectAnomalies());
        
        return report;
    }

    /**
     * Genera e salva un report in formato JSON
     * 
     * @param filePath Il percorso del file dove salvare il report
     */
    public void saveJsonReport(String filePath) {
        try {
            Map<String, Object> report = generateJsonReport();
            String jsonString = mapToJsonString(report);
            
            // Crea la directory se non esiste
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            
            // Salva il report
            Files.write(path, jsonString.getBytes(StandardCharsets.UTF_8));
            System.out.println("Report JSON salvato in: " + filePath);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio del report JSON: " + e.getMessage());
        }
    }
    
    /**
     * Converte una Map in una stringa JSON.
     * 
     * @param map Map da convertire
     * @return Stringa JSON
     */
    private String mapToJsonString(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{\n");
        int i = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (i > 0) {
                json.append(",\n");
            }
            json.append("  \"").append(entry.getKey()).append("\": ");
            json.append(objectToJsonString(entry.getValue()));
            i++;
        }
        json.append("\n}");
        return json.toString();
    }
    
    /**
     * Converte un oggetto in una stringa JSON.
     * 
     * @param obj Oggetto da convertire
     * @return Stringa JSON
     */
    private String objectToJsonString(Object obj) {
        if (obj == null) {
            return "null";
        } else if (obj instanceof String) {
            return "\"" + ((String) obj).replace("\"", "\\\"") + "\"";
        } else if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        } else if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            StringBuilder json = new StringBuilder("[\n");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    json.append(",\n");
                }
                json.append("    ").append(objectToJsonString(list.get(i)));
            }
            json.append("\n  ]");
            return json.toString();
        } else if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder json = new StringBuilder("{\n");
            int i = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (i > 0) {
                    json.append(",\n");
                }
                json.append("    \"").append(entry.getKey()).append("\": ");
                json.append(objectToJsonString(entry.getValue()));
                i++;
            }
            json.append("\n  }");
            return json.toString();
        } else {
            return "\"" + obj.toString() + "\"";
        }
    }

    /**
     * Genera e salva un report in formato CSV
     * 
     * @param filePath Il percorso del file dove salvare il report
     */
    public void saveCsvReport(String filePath) {
        try {
            StringBuilder csv = new StringBuilder();
            
            // Intestazione
            csv.append("fileName,successful,processingTime,errorMessage\n");
            
            // Dati dei file
            for (FileMetric metric : fileMetrics) {
                csv.append(metric.getFileName()).append(",")
                   .append(metric.isSuccessful()).append(",")
                   .append(metric.getProcessingTime()).append(",")
                   .append(metric.getErrorMessage() != null ? "\"" + metric.getErrorMessage().replace("\"", "\"\"") + "\"" : "")
                   .append("\n");
            }
            
            // Crea la directory se non esiste
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            
            // Salva il report
            Files.write(path, csv.toString().getBytes(StandardCharsets.UTF_8));
            System.out.println("Report CSV salvato in: " + filePath);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio del report CSV: " + e.getMessage());
        }
    }

    /**
     * Rileva anomalie nelle metriche di indicizzazione.
     * 
     * @return Lista contenente le anomalie rilevate
     */
    private List<Map<String, Object>> detectAnomalies() {
        List<Map<String, Object>> anomalies = new ArrayList<>();
        
        // Calcola la media e la deviazione standard
        double mean = getAverageFileProcessingTime();
        double stdDev = calculateStandardDeviation();
        
        // Identifica i file con tempi di elaborazione anomali (oltre 2 deviazioni standard)
        for (FileMetric metric : fileMetrics) {
            if (metric.isSuccessful()) {
                double zScore = (metric.getProcessingTime() - mean) / stdDev;
                if (Math.abs(zScore) > 2.0) {
                    Map<String, Object> anomaly = new HashMap<>();
                    anomaly.put("fileName", metric.getFileName());
                    anomaly.put("processingTimeMs", metric.getProcessingTime());
                    anomaly.put("zScore", zScore);
                    anomaly.put("type", zScore > 0 ? "slow" : "fast");
                    anomalies.add(anomaly);
                }
            }
        }
        
        return anomalies;
    }

    /**
     * Calcola la deviazione standard dei tempi di elaborazione.
     * 
     * @return Deviazione standard
     */
    private double calculateStandardDeviation() {
        double mean = getAverageFileProcessingTime();
        double sumSquaredDiff = fileMetrics.stream()
                .filter(FileMetric::isSuccessful)
                .mapToDouble(m -> Math.pow(m.getProcessingTime() - mean, 2))
                .sum();
        
        int successfulCount = (int) fileMetrics.stream()
                .filter(FileMetric::isSuccessful)
                .count();
        
        return Math.sqrt(sumSquaredDiff / successfulCount);
    }

    /**
     * Genera un report di riepilogo delle metriche.
     * 
     * @return Stringa contenente il report di riepilogo
     */
    public String generateSummaryReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== REPORT DI INDICIZZAZIONE ===\n");
        report.append(String.format("Data: %s\n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
        report.append(String.format("File totali elaborati: %d\n", totalFiles));
        report.append(String.format("File indicizzati con successo: %d\n", successfulFiles));
        report.append(String.format("File con errori: %d\n", failedFiles));
        report.append(String.format("Tempo totale di indicizzazione: %d ms (%.2f secondi)\n", 
                getTotalIndexingTime(), getTotalIndexingTime() / 1000.0));
        report.append(String.format("Tempo medio per file: %.2f ms\n", getAverageFileProcessingTime()));
        report.append(String.format("Tempo massimo per file: %d ms\n", getMaxFileProcessingTime()));
        report.append(String.format("Tempo minimo per file: %d ms\n", getMinFileProcessingTime()));
        
        if (!errors.isEmpty()) {
            report.append("\nErrori rilevati:\n");
            for (String error : errors) {
                report.append("- ").append(error).append("\n");
            }
        }
        
        return report.toString();
    }
}