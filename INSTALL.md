# Guida all'Installazione

Questa guida ti aiuterà a installare e configurare il sistema di ricerca basato su Lucene.

## Prerequisiti

- Java 11 o superiore
- Le librerie Lucene sono già incluse nella directory `lib/`

## Installazione

1. Clona il repository:
   ```
   git clone https://github.com/tuousername/lucene-search-system.git
   cd lucene-search-system
   ```

2. Compila il progetto:
   ```
   # Su Windows
   javac -cp "lib/*" -d target/classes src/main/java/it/uniroma3/lucene/*.java

   # Su Linux/Mac
   javac -cp "lib/*" -d target/classes src/main/java/it/uniroma3/lucene/*.java
   ```

3. Verifica che le directory necessarie esistano:
   - `data/` - per i file da indicizzare
   - `index/` - per l'indice creato da Lucene
   - `target/classes/` - per i file compilati

   Se non esistono, verranno create automaticamente all'avvio del programma.

## Esecuzione

Esegui il programma principale:
```
# Su Windows
java -cp "target/classes;lib/*" it.uniroma3.lucene.Main

# Su Linux/Mac
java -cp "target/classes:lib/*" it.uniroma3.lucene.Main
```

## Verifica dell'Installazione

Per verificare che tutto funzioni correttamente, esegui il test automatico:
```
# Su Windows
java -cp "target/classes;lib/*" it.uniroma3.lucene.TestQueries

# Su Linux/Mac
java -cp "target/classes:lib/*" it.uniroma3.lucene.TestQueries
```

Se vedi i risultati delle query di test, l'installazione è avvenuta con successo.