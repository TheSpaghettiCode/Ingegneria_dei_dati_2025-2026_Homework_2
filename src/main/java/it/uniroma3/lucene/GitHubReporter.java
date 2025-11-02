package it.uniroma3.lucene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Classe per l'integrazione con GitHub e il salvataggio dei report
 */
public class GitHubReporter {
    private String repoOwner;
    private String repoName;
    private String branchName;
    private String githubToken;
    private String reportPath;
    
    /**
     * Costruttore per GitHubReporter
     * 
     * @param repoOwner Nome del proprietario del repository
     * @param repoName Nome del repository
     * @param branchName Nome del branch
     * @param githubToken Token di accesso GitHub
     * @param reportPath Percorso dove salvare i report localmente
     */
    public GitHubReporter(String repoOwner, String repoName, String branchName, String githubToken, String reportPath) {
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.branchName = branchName;
        this.githubToken = githubToken;
        this.reportPath = reportPath;
        
        // Crea la directory per i report se non esiste
        try {
            Files.createDirectories(Paths.get(reportPath));
        } catch (IOException e) {
            System.err.println("Errore nella creazione della directory per i report: " + e.getMessage());
        }
    }
    
    /**
     * Salva un report su GitHub
     * 
     * @param metrics Le metriche di indicizzazione
     * @return true se il salvataggio è avvenuto con successo, false altrimenti
     */
    public boolean saveReportToGitHub(IndexingMetrics metrics) {
        try {
            // Genera il nome del file con timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "indexing_report_" + timestamp + ".json";
            
            // Salva il report localmente
            String localFilePath = reportPath + "/" + fileName;
            metrics.saveJsonReport(localFilePath);
            
            // Carica il file su GitHub
            return uploadFileToGitHub(localFilePath, "reports/" + fileName, 
                    "Aggiunto report di indicizzazione " + timestamp);
            
        } catch (Exception e) {
            System.err.println("Errore nel salvataggio del report su GitHub: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Carica un file su GitHub
     * 
     * @param localFilePath Percorso locale del file
     * @param githubPath Percorso su GitHub
     * @param commitMessage Messaggio di commit
     * @return true se il caricamento è avvenuto con successo, false altrimenti
     */
    private boolean uploadFileToGitHub(String localFilePath, String githubPath, String commitMessage) {
        try {
            // Leggi il contenuto del file
            String content = new String(Files.readAllBytes(Paths.get(localFilePath)), StandardCharsets.UTF_8);
            
            // Codifica il contenuto in Base64
            String encodedContent = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
            
            // Crea il payload JSON per la richiesta
            String jsonPayload = String.format(
                "{\"message\":\"%s\",\"content\":\"%s\",\"branch\":\"%s\"}",
                commitMessage, encodedContent, branchName
            );
            
            // Crea la connessione HTTP
            URL url = new URL(String.format("https://api.github.com/repos/%s/%s/contents/%s", 
                    repoOwner, repoName, githubPath));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Authorization", "token " + githubToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            // Invia la richiesta
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Leggi la risposta
            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                System.out.println("Report caricato con successo su GitHub: " + githubPath);
                return true;
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                System.err.println("Errore nel caricamento del report su GitHub. Codice: " + responseCode + 
                        ", Risposta: " + response.toString());
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Errore nell'upload del file su GitHub: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Aggiorna la scheda tecnica del repository con le metriche di indicizzazione
     * 
     * @param metrics Le metriche di indicizzazione
     * @return true se l'aggiornamento è avvenuto con successo, false altrimenti
     */
    public boolean updateTechnicalSheet(IndexingMetrics metrics) {
        try {
            // Genera il contenuto della scheda tecnica
            StringBuilder content = new StringBuilder();
            content.append("# Scheda Tecnica - Metriche di Indicizzazione\n\n");
            content.append("## Ultimo aggiornamento: ")
                  .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                  .append("\n\n");
            
            content.append("## Statistiche di indicizzazione\n\n");
            content.append("| Metrica | Valore |\n");
            content.append("|---------|--------|\n");
            content.append("| File totali elaborati | ").append(metrics.getTotalFiles()).append(" |\n");
            content.append("| File indicizzati con successo | ").append(metrics.getSuccessfulFiles()).append(" |\n");
            content.append("| File con errori | ").append(metrics.getFailedFiles()).append(" |\n");
            content.append("| Tempo totale di indicizzazione | ").append(metrics.getTotalIndexingTime()).append(" ms |\n");
            content.append("| Tempo medio per file | ").append(String.format("%.2f", metrics.getAverageFileProcessingTime())).append(" ms |\n");
            content.append("| Tempo massimo per file | ").append(metrics.getMaxFileProcessingTime()).append(" ms |\n");
            content.append("| Tempo minimo per file | ").append(metrics.getMinFileProcessingTime()).append(" ms |\n");
            
            // Salva la scheda tecnica localmente
            String localFilePath = reportPath + "/technical_sheet.md";
            Files.write(Paths.get(localFilePath), content.toString().getBytes(StandardCharsets.UTF_8));
            
            // Carica la scheda tecnica su GitHub
            return uploadFileToGitHub(localFilePath, "TECHNICAL_SHEET.md", 
                    "Aggiornamento scheda tecnica con metriche di indicizzazione");
            
        } catch (Exception e) {
            System.err.println("Errore nell'aggiornamento della scheda tecnica: " + e.getMessage());
            return false;
        }
    }
}