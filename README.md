# Sistema di Ricerca basato su Lucene

Questo progetto implementa un sistema di ricerca basato su Apache Lucene per l'indicizzazione e la ricerca di file di testo, con un'interfaccia grafica intuitiva.

## Requisiti di Sistema

- Java 11 o superiore
- Apache Lucene 9.7.0 (incluso nella directory `lib/`)

## Struttura del Progetto

- `src/main/java/it/uniroma3/lucene/`: Contiene i file sorgente Java
  - `Indexer.java`: Classe per l'indicizzazione dei file di testo
  - `Searcher.java`: Classe per la ricerca nell'indice
  - `Main.java`: Classe principale per l'esecuzione del programma da linea di comando
  - `LuceneGUI.java`: Classe per l'interfaccia grafica utente
  - `TestQueries.java`: Classe per testare automaticamente diverse query

- `data/`: Contiene i file di testo di esempio
- `index/`: Directory dove viene salvato l'indice creato
- `lib/`: Contiene le librerie Lucene necessarie
- `logs/`: Contiene i file di log dell'applicazione
- `src/main/resources/`: Contiene i file di localizzazione per l'interfaccia grafica

## Utilizzo

### Compilazione

```bash
# Su Windows
javac -cp "lib/*" -d target/classes src/main/java/it/uniroma3/lucene/*.java

# Su Linux/Mac
javac -cp "lib/*" -d target/classes src/main/java/it/uniroma3/lucene/*.java
```

### Esecuzione

#### Interfaccia a Linea di Comando

```bash
# Su Windows
java -cp "target/classes;lib/*" it.uniroma3.lucene.Main

# Su Linux/Mac
java -cp "target/classes:lib/*" it.uniroma3.lucene.Main
```

#### Interfaccia Grafica (GUI)

```bash
# Su Windows
java -cp "target/classes;lib/*" it.uniroma3.lucene.LuceneGUI

# Su Linux/Mac
java -cp "target/classes:lib/*" it.uniroma3.lucene.LuceneGUI
```

### Indicizzazione

Il programma indicizzerà automaticamente i file nella directory `data/` all'avvio. Se la directory `data/` non esiste, verrà creata automaticamente. Se la directory `index/` non esiste, verrà creata automaticamente.

### Ricerca

#### Tramite Linea di Comando
Dopo l'avvio, è possibile inserire query di ricerca. Il programma supporta:

- Ricerca semplice: `lucene` o `java`
- Ricerca in campi specifici: `nome:documento1` o `contenuto:java`
- Ricerca di frasi: `"informationi retrieval"` o `"analisi del testo"`
- Ricerca combinata: `nome:documento contenuto:java`

Per uscire, digitare `exit`.

#### Tramite Interfaccia Grafica
L'interfaccia grafica offre:

- Campo di ricerca con validazione in tempo reale
- Opzioni per filtrare la ricerca per nome file e/o contenuto
- Visualizzazione dei risultati in una tabella con nome file, snippet e punteggio
- Sezione per salvare e riutilizzare query preimpostate
- Scorciatoie da tastiera per le operazioni comuni

## Esempi di Query

- `lucene`: Cerca la parola "lucene" in tutti i campi
- `nome:documento`: Cerca file con "documento" nel nome
- `contenuto:java`: Cerca file con "java" nel contenuto
- `"information retrieval"`: Cerca la frase esatta "information retrieval"
- `nome:documento contenuto:java`: Cerca file con "documento" nel nome e "java" nel contenuto

## Test Automatico

Per eseguire il test automatico con query predefinite:

```bash
# Su Windows
java -cp "target/classes;lib/*" it.uniroma3.lucene.TestQueries

# Su Linux/Mac
java -cp "target/classes:lib/*" it.uniroma3.lucene.TestQueries
```

## Dettagli Implementativi

### Indexer

L'indicizzatore utilizza:
- `SimpleAnalyzer` per i nomi dei file
- `StandardAnalyzer` per il contenuto dei file

### Searcher

Il sistema di ricerca supporta:
- Prefissi `nome:` e `contenuto:` per specificare il campo di ricerca
- Query di frase utilizzando le virgolette
- Ricerca in tutti i campi se non viene specificato un prefisso

### Campi Indicizzati

- `filename`: Nome del file (indicizzato come TextField)
- `content`: Contenuto del file (indicizzato come TextField)

## Funzionalità dell'Interfaccia Grafica


### Opzioni di Ricerca Avanzate
- Checkbox interattivi per filtrare la ricerca per nome file e/o contenuto
- Feedback visivo immediato sullo stato delle opzioni selezionate
- Tooltip informativi per guidare l'utente nell'utilizzo

### Query Preimpostate
- Pannello dedicato per salvare e gestire query frequentemente utilizzate
- Funzionalità per aggiungere, modificare e rimuovere query personalizzate
- Selezione rapida per inserire automaticamente le query nel campo di ricerca
- Salvataggio automatico delle query tra diverse sessioni

### Altre Caratteristiche
- Supporto multilingua (italiano e inglese)
- Logging automatico delle operazioni
- Scorciatoie da tastiera per le operazioni comuni
- Visualizzazione dettagliata dei risultati con snippet contestuali

## Risoluzione Problemi

### Errore "Cannot parse '': Encountered "<EOF>""
Questo errore si verifica quando si preme invio senza inserire alcun testo nella query. Assicurarsi di digitare una query valida prima di premere invio.

### Errore "field was indexed without position data; cannot run PhraseQuery"
Se si verifica questo errore, è necessario ricreare l'indice. Questo può accadere se il campo è stato indicizzato come StringField invece che come TextField. Il programma è stato corretto per utilizzare TextField per tutti i campi, quindi questo errore non dovrebbe più verificarsi.

## Licenza

Questo progetto è distribuito con licenza MIT. Vedere il file LICENSE per i dettagli.