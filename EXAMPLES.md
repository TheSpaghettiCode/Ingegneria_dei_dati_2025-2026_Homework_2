# Esempi di Query

Questo documento contiene esempi di query che puoi utilizzare con il sistema di ricerca Lucene.

## Query Semplici

### Ricerca di un singolo termine
```
lucene
```
Cerca il termine "lucene" in tutti i campi (nome file e contenuto).

### Ricerca di più termini
```
java programmazione
```
Cerca i documenti che contengono "java" O "programmazione" in qualsiasi campo.

## Query con Prefissi di Campo

### Ricerca nel nome del file
```
nome:documento
```
Cerca i documenti il cui nome contiene "documento".

### Ricerca nel contenuto
```
contenuto:java
```
Cerca i documenti il cui contenuto contiene "java".

### Ricerca combinata
```
nome:documento contenuto:java
```
Cerca i documenti il cui nome contiene "documento" E il cui contenuto contiene "java".

## Query di Frase

### Frase semplice
```
"information retrieval"
```
Cerca la frase esatta "information retrieval" in tutti i campi.

### Frase in un campo specifico
```
contenuto:"analisi del testo"
```
Cerca la frase esatta "analisi del testo" solo nel contenuto dei documenti.

## Query Complesse

### Combinazione di termini e frasi
```
nome:documento contenuto:"information retrieval"
```
Cerca i documenti il cui nome contiene "documento" E il cui contenuto contiene la frase esatta "information retrieval".

### Ricerca di più frasi
```
"information retrieval" "analisi del testo"
```
Cerca i documenti che contengono la frase "information retrieval" O la frase "analisi del testo".

## Esempi Pratici

### Trovare documenti su Lucene
```
lucene
```
o più specificamente:
```
contenuto:lucene
```

### Trovare documenti su Java
```
java
```
o più specificamente:
```
contenuto:java
```

### Trovare documenti su information retrieval
```
"information retrieval"
```
o più specificamente:
```
contenuto:"information retrieval"
```

### Trovare documenti specifici per nome
```
nome:documento1
```
o
```
nome:analisi
```