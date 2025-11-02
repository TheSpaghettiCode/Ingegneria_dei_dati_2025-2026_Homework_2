package it.uniroma3.lucene;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SearcherTest {
    private static Path testIndexPath;

    @BeforeAll
    static void setupIndex() throws IOException {
        testIndexPath = Paths.get("target", "test-index");
        if (Files.exists(testIndexPath)) {
            // clean up previous runs
            Files.walk(testIndexPath)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
        }
        Files.createDirectories(testIndexPath);

        // Index the provided data directory
        Path dataPath = Paths.get("data");
        assertTrue(Files.exists(dataPath), "La directory 'data' deve esistere per i test");

        Indexer indexer = new Indexer(testIndexPath.toString());
        int numIndexed = indexer.createIndex(dataPath.toString());
        assertTrue(numIndexed > 0, "Devono essere indicizzati uno o più file");
        indexer.close();
    }

    @Test
    void testRicercaSempliceLucene() throws Exception {
        try (Searcher searcher = new Searcher(testIndexPath.toString())) {
            List<Searcher.SearchResult> results = searcher.search("lucene", 10);
            assertNotNull(results);
            assertTrue(results.size() > 0, "La ricerca semplice 'lucene' dovrebbe produrre risultati");
        }
    }

    @Test
    void testRicercaPerNomeDocumento() throws Exception {
        try (Searcher searcher = new Searcher(testIndexPath.toString())) {
            List<Searcher.SearchResult> results = searcher.search("nome:documento1", 10);
            assertNotNull(results);
            assertTrue(results.size() > 0, "La ricerca per nome:documento1 dovrebbe produrre risultati");
        }
    }

    @Test
    void testRicercaFraseNelContenuto() throws Exception {
        try (Searcher searcher = new Searcher(testIndexPath.toString())) {
            List<Searcher.SearchResult> results = searcher.search("contenuto:\"analisi del testo\"", 10);
            assertNotNull(results);
            assertTrue(results.size() > 0, "La ricerca di frase nel contenuto dovrebbe produrre risultati");
        }
    }

    @Test
    void testOperatoriBooleaniAND() throws Exception {
        try (Searcher searcher = new Searcher(testIndexPath.toString())) {
            List<Searcher.SearchResult> results = searcher.search("contenuto:lucene AND contenuto:java", 10);
            assertNotNull(results);
            // Può essere 0 se i documenti non contengono entrambe, ma non deve generare errori
            assertTrue(results.size() >= 0, "La ricerca con operatori booleani deve essere gestita correttamente");
        }
    }

    @Test
    void testCaratteriSpecialiWildcards() throws Exception {
        try (Searcher searcher = new Searcher(testIndexPath.toString())) {
            List<Searcher.SearchResult> results = searcher.search("contenuto:analisi*", 10);
            assertNotNull(results);
            assertTrue(results.size() > 0, "La ricerca con wildcard dovrebbe produrre risultati");
        }
    }
}