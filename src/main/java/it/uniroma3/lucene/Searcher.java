package it.uniroma3.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe per la ricerca nei file indicizzati.
 */
public class Searcher {
    private final IndexSearcher searcher;
    private final Analyzer filenameAnalyzer;
    private final Analyzer contentAnalyzer;
    private final QueryParser filenameParser;
    private final QueryParser contentParser;
    private final MultiFieldQueryParser multiFieldParser;

    /**
     * Costruttore del Searcher.
     * @param indexDirectoryPath percorso della directory contenente l'indice
     * @throws IOException in caso di errori di I/O
     */
    public Searcher(String indexDirectoryPath) throws IOException {
        Path indexPath = Paths.get(indexDirectoryPath);
        Directory indexDirectory = FSDirectory.open(indexPath);
        IndexReader reader = DirectoryReader.open(indexDirectory);
        this.searcher = new IndexSearcher(reader);
        
        this.filenameAnalyzer = new SimpleAnalyzer();
        this.contentAnalyzer = new StandardAnalyzer();
        
        this.filenameParser = new QueryParser("filename", filenameAnalyzer);
        this.contentParser = new QueryParser("content", contentAnalyzer);
        
        Map<String, Float> boosts = new HashMap<>();
        boosts.put("filename", 1.5f);  // Diamo un peso maggiore ai risultati che matchano il nome del file
        boosts.put("content", 1.0f);
        
        // Utilizziamo l'analyzer per il contenuto come default
        this.multiFieldParser = new MultiFieldQueryParser(
                new String[] {"filename", "content"},
                contentAnalyzer,
                boosts
        );
    }

    /**
     * Esegue una ricerca in base alla query fornita.
     * @param queryString stringa di query
     * @param maxResults numero massimo di risultati da restituire
     * @return lista di risultati della ricerca
     * @throws IOException in caso di errori di I/O
     * @throws ParseException in caso di errori nel parsing della query
     */
    public List<SearchResult> search(String queryString, int maxResults) throws IOException, ParseException {
        Query query = parseQuery(queryString);
        TopDocs topDocs = searcher.search(query, maxResults);
        
        List<SearchResult> results = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            String filename = doc.get("filename");
            String content = doc.get("content");
            
            // Estrai uno snippet rilevante dal contenuto
            String snippet = extractRelevantSnippet(content, queryString, 150);
            
            results.add(new SearchResult(filename, snippet, scoreDoc.score));
        }
        
        return results;
    }
    
    /**
     * Estrae uno snippet rilevante dal contenuto in base alla query.
     * @param content il contenuto completo del documento
     * @param queryString la query di ricerca
     * @param maxLength la lunghezza massima dello snippet
     * @return uno snippet rilevante
     */
    private String extractRelevantSnippet(String content, String queryString, int maxLength) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        // Estrai i termini dalla query (rimuovendo operatori e prefissi)
        String cleanQuery = queryString.replaceAll("nome:|contenuto:", "")
                                      .replaceAll("\"", "")
                                      .replaceAll("AND|OR|NOT", "")
                                      .trim();
        
        String[] queryTerms = cleanQuery.split("\\s+");
        
        // Cerca la prima occorrenza di uno dei termini della query
        int bestPosition = -1;
        int bestTermLength = 0;
        
        for (String term : queryTerms) {
            if (term.length() < 3) continue; // Ignora termini troppo brevi
            
            int pos = content.toLowerCase().indexOf(term.toLowerCase());
            if (pos >= 0 && (bestPosition == -1 || pos < bestPosition)) {
                bestPosition = pos;
                bestTermLength = term.length();
            }
        }
        
        // Se non è stato trovato nessun termine, prendi l'inizio del documento
        if (bestPosition == -1) {
            return content.length() > maxLength ? content.substring(0, maxLength) + "..." : content;
        }
        
        // Calcola l'inizio e la fine dello snippet
        int start = Math.max(0, bestPosition - 50);
        int end = Math.min(content.length(), bestPosition + bestTermLength + 100);
        
        // Aggiusta per non tagliare a metà parola
        if (start > 0) {
            int spacePos = content.lastIndexOf(" ", start);
            if (spacePos > start - 20) {
                start = spacePos + 1;
            }
        }
        
        if (end < content.length()) {
            int spacePos = content.indexOf(" ", end);
            if (spacePos > 0 && spacePos < end + 20) {
                end = spacePos;
            }
        }
        
        // Crea lo snippet
        String snippet = content.substring(start, end);
        
        // Aggiungi ... all'inizio e alla fine se necessario
        if (start > 0) {
            snippet = "..." + snippet;
        }
        
        if (end < content.length()) {
            snippet = snippet + "...";
        }
        
        return snippet;
    }

    /**
     * Analizza la query e la converte in un oggetto Query di Lucene.
     * Supporta prefissi "nome:" e "contenuto:" e phrase query tra virgolette.
     * @param queryString stringa di query
     * @return oggetto Query di Lucene
     * @throws ParseException in caso di errori nel parsing della query
     */
    private Query parseQuery(String queryString) throws ParseException {
        // Pattern per riconoscere i prefissi "nome:" e "contenuto:"
        Pattern pattern = Pattern.compile("(nome|contenuto):(\"[^\"]*\"|\\S+)");
        Matcher matcher = pattern.matcher(queryString);
        
        StringBuilder filenameQuery = new StringBuilder();
        StringBuilder contentQuery = new StringBuilder();
        StringBuilder generalQuery = new StringBuilder();
        
        int lastEnd = 0;
        
        while (matcher.find()) {
            String prefix = matcher.group(1);
            String term = matcher.group(2);
            
            // Aggiungi il testo tra i match al generalQuery
            if (matcher.start() > lastEnd) {
                generalQuery.append(queryString, lastEnd, matcher.start()).append(" ");
            }
            
            if ("nome".equals(prefix)) {
                filenameQuery.append(term).append(" ");
            } else if ("contenuto".equals(prefix)) {
                contentQuery.append(term).append(" ");
            }
            
            lastEnd = matcher.end();
        }
        
        // Aggiungi il resto della query al generalQuery
        if (lastEnd < queryString.length()) {
            generalQuery.append(queryString.substring(lastEnd));
        }
        
        // Se ci sono query specifiche per filename o content, usale
        if (filenameQuery.length() > 0 || contentQuery.length() > 0) {
            List<Query> queries = new ArrayList<>();
            
            if (filenameQuery.length() > 0) {
                queries.add(filenameParser.parse(filenameQuery.toString()));
            }
            
            if (contentQuery.length() > 0) {
                queries.add(contentParser.parse(contentQuery.toString()));
            }
            
            if (generalQuery.length() > 0) {
                queries.add(multiFieldParser.parse(generalQuery.toString()));
            }
            
            // Combina le query con OR
            org.apache.lucene.search.BooleanQuery.Builder builder = new org.apache.lucene.search.BooleanQuery.Builder();
            builder.add(queries.get(0), org.apache.lucene.search.BooleanClause.Occur.SHOULD);
            return builder.build();
        } else {
            // Usa il multiFieldParser per cercare in entrambi i campi
            return multiFieldParser.parse(queryString);
        }
    }

    /**
     * Chiude le risorse.
     * @throws IOException in caso di errori di I/O
     */
    public void close() throws IOException {
        searcher.getIndexReader().close();
        filenameAnalyzer.close();
        contentAnalyzer.close();
    }

    /**
     * Classe interna per rappresentare un risultato della ricerca.
     */
    public static class SearchResult {
        private final String filename;
        private final String snippet;
        private final float score;

        public SearchResult(String filename, String snippet, float score) {
            this.filename = filename;
            this.snippet = snippet;
            this.score = score;
        }

        public String getFilename() {
            return filename;
        }

        public String getSnippet() {
            return snippet;
        }

        public float getScore() {
            return score;
        }

        @Override
        public String toString() {
            return String.format("File: %s\nScore: %.4f\nSnippet: %s\n", filename, score, snippet);
        }
    }
}