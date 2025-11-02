package it.uniroma3.lucene;

import org.apache.lucene.queryparser.classic.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Classe principale per l'esecuzione del sistema di indicizzazione e ricerca.
 */
public class Main {
    private static final String INDEX_DIR = "index";
    private static final String DATA_DIR = "data";
    private static final int MAX_RESULTS = 10;

    public static void main(String[] args) {
        try {
            // Crea le directory se non esistono
            createDirectories();

            // Indicizza i file
            System.out.println("Indicizzazione dei file in corso...");
            Indexer indexer = new Indexer(INDEX_DIR);
            int numIndexed = indexer.createIndex(DATA_DIR);
            
            // Visualizza il report delle metriche
            MetricsReporter reporter = new MetricsReporter(indexer.getMetrics());
            reporter.showGraphicalReport();
            
            // Salva i report localmente
            String reportDir = "reports";
            indexer.getMetrics().saveJsonReport(reportDir + "/latest_report.json");
            indexer.getMetrics().saveCsvReport(reportDir + "/latest_report.csv");
            
            // Configurazione per GitHub (da personalizzare con i propri dati)
            // Per utilizzare l'integrazione con GitHub, decommentare il codice seguente
            // e inserire i propri dati di accesso
            /*
            String repoOwner = "username"; // Inserire il proprio username GitHub
            String repoName = "repo-name"; // Inserire il nome del repository
            String branchName = "main";    // Inserire il nome del branch
            String githubToken = "";       // Inserire il token di accesso GitHub
            
            GitHubReporter githubReporter = new GitHubReporter(
                repoOwner, repoName, branchName, githubToken, reportDir);
            githubReporter.saveReportToGitHub(indexer.getMetrics());
            githubReporter.updateTechnicalSheet(indexer.getMetrics());
            */
            
            indexer.close();
            System.out.println("Indicizzazione completata. " + numIndexed + " file indicizzati.");

            // Inizializza il searcher
            Searcher searcher = new Searcher(INDEX_DIR);

            // Interfaccia utente per la ricerca
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.println("\nInserisci una query di ricerca (o 'exit' per uscire):");
                System.out.println("Puoi usare i prefissi 'nome:' e 'contenuto:' per specificare il campo di ricerca.");
                System.out.println("Esempio: nome:documento contenuto:\"esempio di frase\"");
                
                String queryString = reader.readLine();
                if ("exit".equalsIgnoreCase(queryString)) {
                    break;
                }

                try {
                    // Esegui la ricerca
                    List<Searcher.SearchResult> results = searcher.search(queryString, MAX_RESULTS);
                    
                    // Mostra i risultati
                    System.out.println("\nRisultati della ricerca per: " + queryString);
                    if (results.isEmpty()) {
                        System.out.println("Nessun risultato trovato.");
                    } else {
                        for (int i = 0; i < results.size(); i++) {
                            System.out.println("\nRisultato " + (i + 1) + ":");
                            System.out.println(results.get(i));
                        }
                    }
                } catch (ParseException e) {
                    System.out.println("Errore nel parsing della query: " + e.getMessage());
                }
            }

            // Chiudi il searcher
            searcher.close();
            
        } catch (IOException e) {
            System.err.println("Errore di I/O: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crea le directory necessarie se non esistono.
     * @throws IOException in caso di errori di I/O
     */
    private static void createDirectories() throws IOException {
        Path indexPath = Paths.get(INDEX_DIR);
        Path dataPath = Paths.get(DATA_DIR);
        
        if (!Files.exists(indexPath)) {
            Files.createDirectories(indexPath);
        }
        
        if (!Files.exists(dataPath)) {
            Files.createDirectories(dataPath);
            System.out.println("Directory 'data' creata. Inserisci i file .txt da indicizzare in questa directory.");
            System.out.println("Riavvia l'applicazione dopo aver inserito i file.");
            System.exit(0);
        }
    }
}