package it.uniroma1.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe per l'indicizzazione dei file di testo.
 * Utilizza SimpleAnalyzer per i nomi dei file e StandardAnalyzer per il contenuto.
 */
public class Indexer {
    private final Path indexPath;
    private final Analyzer filenameAnalyzer;
    private final Analyzer contentAnalyzer;

    /**
     * Costruttore dell'Indexer.
     * @param indexDirectoryPath percorso della directory dove salvare l'indice
     */
    public Indexer(String indexDirectoryPath) {
        this.indexPath = Paths.get(indexDirectoryPath);
        this.filenameAnalyzer = new SimpleAnalyzer();
        this.contentAnalyzer = new StandardAnalyzer();
    }

    /**
     * Crea l'indice a partire da una directory contenente file di testo.
     * @param dataDirectoryPath percorso della directory contenente i file da indicizzare
     * @return numero di file indicizzati
     * @throws IOException in caso di errori di I/O
     */
    public int createIndex(String dataDirectoryPath) throws IOException {
        // Crea la directory dell'indice se non esiste
        if (!Files.exists(indexPath)) {
            Files.createDirectories(indexPath);
        }

        Directory indexDirectory = FSDirectory.open(indexPath);
        
        // Configurazione per l'indice dei nomi dei file
        IndexWriterConfig filenameConfig = new IndexWriterConfig(filenameAnalyzer);
        filenameConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        
        // Configurazione per l'indice del contenuto
        IndexWriterConfig contentConfig = new IndexWriterConfig(contentAnalyzer);
        contentConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        
        // Utilizziamo un unico IndexWriter con l'analyzer per il contenuto
        // e gestiamo l'analyzer per i nomi file a livello di campo
        try (IndexWriter writer = new IndexWriter(indexDirectory, contentConfig)) {
            File dataDir = new File(dataDirectoryPath);
            if (!dataDir.exists() || !dataDir.isDirectory()) {
                throw new IOException("La directory dei dati non esiste: " + dataDirectoryPath);
            }
            
            return indexDirectory(writer, dataDir);
        }
    }

    /**
     * Indicizza ricorsivamente tutti i file .txt in una directory.
     * @param writer IndexWriter per scrivere l'indice
     * @param directory directory da indicizzare
     * @return numero di file indicizzati
     * @throws IOException in caso di errori di I/O
     */
    private int indexDirectory(IndexWriter writer, File directory) throws IOException {
        File[] files = directory.listFiles();
        int numIndexed = 0;
        
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    numIndexed += indexDirectory(writer, file);
                } else if (file.getName().endsWith(".txt")) {
                    numIndexed += indexFile(writer, file);
                }
            }
        }
        
        return numIndexed;
    }

    /**
     * Indicizza un singolo file di testo.
     * @param writer IndexWriter per scrivere l'indice
     * @param file file da indicizzare
     * @return 1 se il file è stato indicizzato, 0 altrimenti
     * @throws IOException in caso di errori di I/O
     */
    private int indexFile(IndexWriter writer, File file) throws IOException {
        Document document = new Document();
        
        // Aggiungi il nome del file come TextField (tokenizzato) per supportare query di frase
        document.add(new TextField("filename", file.getName(), Field.Store.YES));
        
        // Leggi e aggiungi il contenuto del file come TextField (tokenizzato)
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            document.add(new TextField("content", content.toString(), Field.Store.YES));
        }
        
        writer.addDocument(document);
        return 1;
    }

    /**
     * Chiude gli analyzer.
     */
    public void close() {
        filenameAnalyzer.close();
        contentAnalyzer.close();
    }
}