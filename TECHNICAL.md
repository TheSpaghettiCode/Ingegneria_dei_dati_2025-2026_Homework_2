# Documentazione Tecnica

## Architettura del Sistema

Il sistema di ricerca è composto da tre componenti principali:

1. **Indexer**: Responsabile dell'indicizzazione dei file di testo
2. **Searcher**: Gestisce le query di ricerca e restituisce i risultati
3. **Main**: Interfaccia utente a riga di comando

## Dettagli Implementativi

### Indexer

La classe `Indexer` si occupa di:
- Creare un indice Lucene nella directory specificata
- Indicizzare ricorsivamente tutti i file .txt in una directory
- Utilizzare analyzer diversi per i campi "filename" e "content"

```java
// Analyzer utilizzati
this.filenameAnalyzer = new SimpleAnalyzer();
this.contentAnalyzer = new StandardAnalyzer();

// Campi indicizzati
document.add(new TextField("filename", file.getName(), Field.Store.YES));
document.add(new TextField("content", content.toString(), Field.Store.YES));
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

### Main

La classe `Main` fornisce:
- Creazione delle directory necessarie se non esistono
- Indicizzazione automatica dei file nella directory "data"
- Interfaccia utente a riga di comando per la ricerca
- Gestione degli errori e visualizzazione dei risultati

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

## Limitazioni e Possibili Miglioramenti

- Supporto per altri tipi di file oltre ai .txt
- Implementazione di filtri e facet per raffinare i risultati
- Interfaccia grafica per una migliore esperienza utente
- Supporto per l'indicizzazione incrementale