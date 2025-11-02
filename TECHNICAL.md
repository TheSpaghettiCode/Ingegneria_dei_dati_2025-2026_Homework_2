# Documentazione Tecnica

## Architettura del Sistema

Il sistema di ricerca è composto da cinque componenti principali:

1. **Indexer**: Responsabile dell'indicizzazione dei file di testo
2. **Searcher**: Gestisce le query di ricerca e restituisce i risultati
3. **Main**: Interfaccia utente a riga di comando
4. **IndexingMetrics**: Sistema di misurazione e analisi delle prestazioni
5. **MetricsReporter**: Visualizzazione e reporting delle metriche

## Dettagli Implementativi

### Indexer

La classe `Indexer` si occupa di:
- Creare un indice Lucene nella directory specificata
- Indicizzare ricorsivamente tutti i file .txt in una directory
- Utilizzare analyzer diversi per i campi "filename" e "content"
- Raccogliere metriche di indicizzazione tramite la classe `IndexingMetrics`

```java
// Analyzer utilizzati
this.filenameAnalyzer = new SimpleAnalyzer();
this.contentAnalyzer = new StandardAnalyzer();
this.metrics = new IndexingMetrics();

// Campi indicizzati
document.add(new TextField("filename", file.getName(), Field.Store.YES));
document.add(new TextField("content", content.toString(), Field.Store.YES));

// Misurazione delle prestazioni
long startTime = System.currentTimeMillis();
// ... operazioni di indicizzazione ...
long endTime = System.currentTimeMillis();
metrics.recordFileMetric(file.getName(), endTime - startTime, true, null);
```

### Searcher

La classe `Searcher` implementa:
- Parsing delle query con supporto per prefissi "nome:" e "contenuto:"
- Ricerca in tutti i campi se non viene specificato un prefisso
- Supporto per query di frase utilizzando le virgolette
- Boost dei campi (il campo "filename" ha un peso maggiore)

```java
// Configurazione dei boost per i campi
Map<String, Float> boosts = new HashMap<>();
boosts.put("filename", 2.0f);
boosts.put("content", 1.0f);

// Parser per query multi-campo
this.multiFieldParser = new MultiFieldQueryParser(
        new String[] {"filename", "content"},
        contentAnalyzer,
        boosts
);
```

### IndexingMetrics

La classe `IndexingMetrics` fornisce:
- Raccolta di metriche dettagliate sul processo di indicizzazione
- Calcolo di statistiche (media, massimo, minimo) sui tempi di elaborazione
- Rilevamento automatico di anomalie nei tempi di elaborazione
- Generazione di report in formato JSON e CSV
- Salvataggio dei report su file

```java
// Registrazione delle metriche per un file
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

// Rilevamento delle anomalie
public List<Map<String, Object>> detectAnomalies() {
    List<Map<String, Object>> anomalies = new ArrayList<>();
    double mean = getAverageFileProcessingTime();
    double stdDev = calculateStandardDeviation();
    double threshold = mean + (2 * stdDev);
    
    for (FileMetric metric : fileMetrics) {
        if (metric.isSuccessful() && metric.getProcessingTime() > threshold) {
            Map<String, Object> anomaly = new HashMap<>();
            anomaly.put("fileName", metric.getFileName());
            anomaly.put("processingTime", metric.getProcessingTime());
            anomaly.put("threshold", threshold);
            anomaly.put("percentageAboveAverage", 
                    ((metric.getProcessingTime() - mean) / mean) * 100);
            anomalies.add(anomaly);
        }
    }
    
    return anomalies;
}
```

### MetricsReporter

La classe `MetricsReporter` implementa:
- Visualizzazione grafica delle metriche di indicizzazione
- Generazione e salvataggio di report in formato JSON e CSV
- Interfaccia utente per l'analisi delle prestazioni

```java
// Visualizzazione grafica delle metriche
public void showGraphicalReport() {
    SwingUtilities.invokeLater(() -> {
        JFrame frame = new JFrame("Report Metriche di Indicizzazione");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        
        // Componenti per la visualizzazione delle metriche
        // ...
        
        // Pulsanti per il salvataggio dei report
        JButton saveJsonButton = new JButton("Salva Report JSON");
        saveJsonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String filePath = "reports/report_" + timestamp + ".json";
                metrics.saveJsonReport(filePath);
                JOptionPane.showMessageDialog(frame, "Report JSON salvato in: " + filePath);
            }
        });
        
        // ...
    });
}
```




```

## Algoritmi e Strutture Dati

### Indice Invertito

Il sistema utilizza l'indice invertito di Lucene, che mappa termini a documenti:
- Ogni termine è associato a una lista di documenti in cui appare
- Per ogni documento, vengono memorizzate informazioni come la frequenza e la posizione del termine

### Scoring dei Risultati

Il ranking dei risultati si basa su:
- TF-IDF (Term Frequency-Inverse Document Frequency)
- Boost dei campi (il campo "filename" ha un peso maggiore)
- Prossimità dei termini nelle query di frase

### Rilevamento Anomalie

Il sistema implementa un algoritmo di rilevamento anomalie basato su:
- Calcolo della media e deviazione standard dei tempi di elaborazione
- Identificazione di outlier utilizzando una soglia di 2 deviazioni standard
- Calcolo della percentuale di scostamento dalla media per ogni anomalia

```java
// Calcolo della deviazione standard
private double calculateStandardDeviation() {
    double mean = getAverageFileProcessingTime();
    double sumSquaredDiff = fileMetrics.stream()
            .filter(FileMetric::isSuccessful)
            .mapToDouble(metric -> {
                double diff = metric.getProcessingTime() - mean;
                return diff * diff;
            })
            .sum();
    
    int count = (int) fileMetrics.stream()
            .filter(FileMetric::isSuccessful)
            .count();
    
    return Math.sqrt(sumSquaredDiff / count);
}
```

## Flusso di Lavoro del Sistema

1. **Inizializzazione**:
   - Creazione delle directory necessarie (index, data, reports)
   - Inizializzazione del sistema di metriche

2. **Indicizzazione**:
   - Scansione ricorsiva della directory dei dati
   - Indicizzazione di ogni file con misurazione dei tempi
   - Raccolta delle metriche per ogni file elaborato

3. **Analisi delle Metriche**:
   - Calcolo delle statistiche (media, massimo, minimo)
   - Rilevamento delle anomalie nei tempi di elaborazione
   - Generazione dei report in formato JSON e CSV

4. **Visualizzazione e Reporting**:
   - Visualizzazione grafica delle metriche raccolte
   - Salvataggio dei report su file locale
   - Opzionale: integrazione con GitHub per il salvataggio remoto

5. **Ricerca**:
   - Parsing delle query dell'utente
   - Esecuzione della ricerca sull'indice
   - Visualizzazione dei risultati

## Considerazioni sulle Prestazioni

### Ottimizzazioni Implementate

- **Misurazione Granulare**: Misurazione dei tempi a livello di singolo file per identificare colli di bottiglia
- **Analisi Statistica**: Calcolo di metriche statistiche per valutare le prestazioni complessive
- **Rilevamento Anomalie**: Identificazione automatica di file che richiedono tempi di elaborazione anomali
- **Reporting Completo**: Generazione di report dettagliati per analisi successive

### Possibili Miglioramenti Futuri

- **Indicizzazione Parallela**: Implementazione di tecniche di parallelizzazione per migliorare le prestazioni
- **Ottimizzazione della Memoria**: Riduzione dell'utilizzo di memoria durante l'indicizzazione di file di grandi dimensioni
- **Compressione dell'Indice**: Implementazione di tecniche di compressione per ridurre le dimensioni dell'indice
- **Indicizzazione Incrementale**: Supporto per l'aggiornamento incrementale dell'indice senza ricostruirlo completamente

## Limitazioni e Possibili Miglioramenti

- Supporto per altri tipi di file oltre ai .txt
- Implementazione di filtri e facet per raffinare i risultati
- Interfaccia grafica per una migliore esperienza utente
- Supporto per l'indicizzazione incrementale
- Integrazione con sistemi di continuous integration per il monitoraggio delle prestazioni nel tempo
- Implementazione di dashboard interattive per l'analisi delle metriche