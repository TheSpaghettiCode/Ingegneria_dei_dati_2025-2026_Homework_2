package it.uniroma3.lucene;

import java.io.IOException;
import java.util.List;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 * Classe per testare il sistema di ricerca con un set di query rappresentative.
 */
public class QueryTester {
    private final Searcher searcher;
    private final String[] testQueries = {
        "lucene",
        "java",
        "nome:documento",
        "contenuto:java",
        "nome:documento contenuto:java",
        "\"information retrieval\"",
        "contenuto:\"analisi del testo\"",
        "nome:documento1",
        "nome:analisi"
    };

    public QueryTester(String indexPath) throws IOException {
        this.searcher = new Searcher(indexPath);
    }

    public void runTests() {
        System.out.println("Esecuzione test di ricerca con query rappresentative...");
        System.out.println("=======================================================");
        
        int successCount = 0;
        
        for (String query : testQueries) {
            try {
                System.out.println("\nTest query: " + query);
                List<Searcher.SearchResult> results = searcher.search(query, 10);
                
                System.out.println("Risultati trovati: " + results.size());
                if (!results.isEmpty()) {
                    System.out.println("Primo risultato: " + results.get(0).getFilename());
                    successCount++;
                } else {
                    System.out.println("ATTENZIONE: Nessun risultato trovato");
                }
            } catch (Exception e) {
                System.out.println("ERRORE durante l'esecuzione della query: " + e.getMessage());
            }
        }
        
        System.out.println("\n=======================================================");
        System.out.println("Riepilogo test: " + successCount + "/" + testQueries.length + " query hanno prodotto risultati");
        
        try {
            searcher.close();
        } catch (IOException e) {
            System.out.println("Errore durante la chiusura del searcher: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Utilizzo: java QueryTester <percorso_indice>");
            System.exit(1);
        }
        
        try {
            QueryTester tester = new QueryTester(args[0]);
            tester.runTests();
        } catch (IOException e) {
            System.out.println("Errore durante l'inizializzazione del tester: " + e.getMessage());
        }
    }
}