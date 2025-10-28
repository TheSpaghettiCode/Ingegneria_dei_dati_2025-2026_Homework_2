package it.uniroma1.lucene;

import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.List;

/**
 * Classe per testare il sistema di ricerca con diverse query.
 */
public class TestQueries {
    private static final String INDEX_DIR = "index";
    private static final int MAX_RESULTS = 10;

    public static void main(String[] args) {
        try {
            // Inizializza il searcher
            Searcher searcher = new Searcher(INDEX_DIR);
            
            // Array di query di test
            String[] queries = {
                "Lucene",                          // Termine singolo
                "Java programmazione",             // Più termini
                "\"information retrieval\"",       // Phrase query
                "nome:documento",                  // Ricerca per nome file
                "nome:analisi",                    // Ricerca per nome file
                "contenuto:indici",                // Ricerca per contenuto
                "contenuto:\"struttura dati\"",    // Phrase query nel contenuto
                "nome:ricerca contenuto:sistema",  // Combinazione nome+contenuto
                "analyzer",                        // Termine tecnico
                "Java Lucene"                      // Combinazione di termini
            };
            
            // Esegui le query di test
            for (int i = 0; i < queries.length; i++) {
                String query = queries[i];
                System.out.println("\n===== TEST QUERY " + (i + 1) + " =====");
                System.out.println("Query: " + query);
                
                try {
                    // Esegui la ricerca
                    List<Searcher.SearchResult> results = searcher.search(query, MAX_RESULTS);
                    
                    // Mostra i risultati
                    System.out.println("Risultati trovati: " + results.size());
                    for (int j = 0; j < results.size(); j++) {
                        System.out.println("\nRisultato " + (j + 1) + ":");
                        System.out.println(results.get(j));
                    }
                } catch (ParseException e) {
                    System.out.println("Errore nel parsing della query: " + e.getMessage());
                }
            }
            
            // Chiudi il searcher
            searcher.close();
            
            System.out.println("\nTest completati con successo!");
            
        } catch (IOException e) {
            System.err.println("Errore di I/O: " + e.getMessage());
            e.printStackTrace();
        }
    }
}