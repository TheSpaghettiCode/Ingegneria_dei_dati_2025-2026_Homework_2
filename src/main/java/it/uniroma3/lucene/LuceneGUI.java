package it.uniroma3.lucene;

import org.apache.lucene.queryparser.classic.ParseException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.Preferences;

/**
 * Interfaccia grafica per il sistema di ricerca Lucene
 */
public class LuceneGUI extends JFrame {
    
    // Componenti dell'interfaccia
    private JTextField searchField;
    private JTable resultsTable;
    private JLabel statusLabel;
    
    // Modello della tabella
    private DefaultTableModel tableModel;
    
    // Gestione dello stato
    private Indexer indexer;
    private Searcher searcher;
    private String indexPath = "index";
    private String dataPath = "data";
    private ResourceBundle messages;
    private List<String> presetQueries = new java.util.ArrayList<>();
    private JList<String> presetQueryList;
    
    // Logging
    private static final Logger logger = Logger.getLogger(LuceneGUI.class.getName());
    
    // Preferenze utente
    private Preferences prefs;
    
    /**
     * Costruttore
     */
    public LuceneGUI() {
        // Inizializzazione
        setupLogging();
        prefs = Preferences.userNodeForPackage(LuceneGUI.class);
        loadPreferences();
        initializeLocalization();
        
        try {
            // Inizializzazione del sistema Lucene
            initializeLuceneSystem();
            
            // Configurazione della finestra
            setTitle(messages.getString("app.title"));
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(900, 600);
            setLocationRelativeTo(null);
            
            // Creazione dell'interfaccia
            createUI();
            
            // Applicazione del tema scuro
            setupDarkTheme();
            
            // Registrazione degli shortcut globali
            setupKeyboardShortcuts();
            
            logger.info("Applicazione avviata con successo");
            updateStatus(messages.getString("status.ready"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Errore durante l'avvio dell'applicazione", e);
            showError(messages.getString("error.startup"), e.getMessage());
        }
    }
    
    /**
     * Inizializza il sistema di logging
     */
    private void setupLogging() {
        try {
            Path logDir = Paths.get("logs");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            
            FileHandler fileHandler = new FileHandler("logs/lucene-gui.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("Impossibile inizializzare il sistema di logging: " + e.getMessage());
        }
    }
    
    /**
     * Carica le preferenze dell'utente
     */
    private void loadPreferences() {
        indexPath = prefs.get("indexPath", "index");
        dataPath = prefs.get("dataPath", "data");
    }
    
    /**
     * Salva le preferenze dell'utente
     */
    private void savePreferences() {
        prefs.put("indexPath", indexPath);
        prefs.put("dataPath", dataPath);
    }
    
    /**
     * Inizializza la localizzazione
     */
    private void initializeLocalization() {
        try {
            messages = ResourceBundle.getBundle("messages", Locale.getDefault());
        } catch (Exception e) {
            // Fallback alla lingua di default
            messages = ResourceBundle.getBundle("messages", Locale.ENGLISH);
            logger.warning("Impossibile caricare le risorse per la localizzazione: " + e.getMessage());
        }
    }
    
    /**
     * Inizializza il sistema Lucene
     */
    private void initializeLuceneSystem() throws IOException {
        // Creazione delle directory se non esistono
        Path indexDir = Paths.get(indexPath);
        Path dataDir = Paths.get(dataPath);
        
        if (!Files.exists(indexDir)) {
            Files.createDirectories(indexDir);
        }
        
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }
        
        // Inizializzazione di Indexer e Searcher
        indexer = new Indexer(indexPath);
        searcher = new Searcher(indexPath);
        
        // Indicizzazione dei file esistenti
        if (Files.list(dataDir).count() > 0) {
            int numIndexed = indexer.createIndex(dataPath);
            logger.info("Indicizzati " + numIndexed + " file");
        }
    }
    
    /**
     * Crea l'interfaccia utente
     */
    private void createUI() {
        // Layout principale
        setLayout(new BorderLayout());
        
        // Menu
        setJMenuBar(createMenuBar());
        
        // Pannello principale
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Pannello di ricerca
        mainPanel.add(createSearchPanel(), BorderLayout.NORTH);
        
        // Tabella dei risultati
        mainPanel.add(createResultsPanel(), BorderLayout.CENTER);
        
        // Barra di stato
        mainPanel.add(createStatusBar(), BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Crea la barra dei menu
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // Menu File
        JMenu fileMenu = new JMenu(messages.getString("menu.file"));
        JMenuItem indexItem = new JMenuItem(messages.getString("menu.file.index"));
        indexItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
        indexItem.addActionListener(e -> reindexFiles());
        
        JMenuItem selectDataDirItem = new JMenuItem(messages.getString("menu.file.selectDataDir"));
        selectDataDirItem.addActionListener(e -> selectDataDirectory());
        
        JMenuItem selectIndexDirItem = new JMenuItem(messages.getString("menu.file.selectIndexDir"));
        selectIndexDirItem.addActionListener(e -> selectIndexDirectory());
        
        JMenuItem exitItem = new JMenuItem(messages.getString("menu.file.exit"));
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        exitItem.addActionListener(e -> {
            savePreferences();
            System.exit(0);
        });
        
        fileMenu.add(indexItem);
        fileMenu.addSeparator();
        fileMenu.add(selectDataDirItem);
        fileMenu.add(selectIndexDirItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Menu Visualizza
        JMenu viewMenu = new JMenu(messages.getString("menu.view"));
        
        // Menu Aiuto
        JMenu helpMenu = new JMenu(messages.getString("menu.help"));
        JMenuItem helpItem = new JMenuItem(messages.getString("menu.help.help"));
        helpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        helpItem.addActionListener(e -> showHelp());
        
        JMenuItem aboutItem = new JMenuItem(messages.getString("menu.help.about"));
        aboutItem.addActionListener(e -> showAbout());
        
        helpMenu.add(helpItem);
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    /**
     * Crea il pannello di ricerca
     */
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(10, 10, 10, 10)));
        
        // Etichetta
        JLabel searchLabel = new JLabel(messages.getString("search.label"));
        searchLabel.setFont(searchLabel.getFont().deriveFont(Font.BOLD));
        searchPanel.add(searchLabel, BorderLayout.NORTH);
        
        // Campo di ricerca e pulsante
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        
        searchField = new JTextField();
        searchField.setToolTipText(messages.getString("search.tooltip"));
        
        // Validazione in tempo reale
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validateQuery();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validateQuery();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validateQuery();
            }
            
            private void validateQuery() {
                String query = searchField.getText().trim();
                if (query.isEmpty()) {
                    searchField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                } else {
                    try {
                        // Tentiamo di eseguire una ricerca di prova per validare la query
                        searcher.search(query, 1);
                        searchField.setBorder(BorderFactory.createLineBorder(Color.GREEN));
                    } catch (Exception ex) {
                        searchField.setBorder(BorderFactory.createLineBorder(Color.RED));
                    }
                }
            }
        });
        
        JButton searchButton = new JButton(messages.getString("search.button"));
        searchButton.addActionListener(e -> performSearch());
        getRootPane().setDefaultButton(searchButton);
        
        inputPanel.add(searchField, BorderLayout.CENTER);
        inputPanel.add(searchButton, BorderLayout.EAST);
        
        searchPanel.add(inputPanel, BorderLayout.CENTER);
        
        // Opzioni di ricerca
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        
        // Checkbox per la ricerca nel nome del file con tooltip migliorato
        JCheckBox filenameCheckBox = new JCheckBox(messages.getString("search.option.filename"));
        filenameCheckBox.setSelected(true);
        filenameCheckBox.setToolTipText("Attiva questa opzione per cercare nei nomi dei file. Disattivala per escludere i nomi dei file dalla ricerca.");
        filenameCheckBox.setBackground(new Color(60, 63, 65));
        filenameCheckBox.setForeground(Color.WHITE);
        filenameCheckBox.addActionListener(e -> {
            // Feedback visivo
            if (filenameCheckBox.isSelected()) {
                filenameCheckBox.setForeground(Color.GREEN);
            } else {
                filenameCheckBox.setForeground(Color.LIGHT_GRAY);
            }
        });
        
        // Checkbox per la ricerca nel contenuto con tooltip migliorato
        JCheckBox contentCheckBox = new JCheckBox(messages.getString("search.option.content"));
        contentCheckBox.setSelected(true);
        contentCheckBox.setToolTipText("Attiva questa opzione per cercare nel contenuto dei file. Disattivala per escludere il contenuto dalla ricerca.");
        contentCheckBox.setBackground(new Color(60, 63, 65));
        contentCheckBox.setForeground(Color.WHITE);
        contentCheckBox.addActionListener(e -> {
            // Feedback visivo
            if (contentCheckBox.isSelected()) {
                contentCheckBox.setForeground(Color.GREEN);
            } else {
                contentCheckBox.setForeground(Color.LIGHT_GRAY);
            }
        });
        
        JLabel maxResultsLabel = new JLabel(messages.getString("search.option.maxResults"));
        JComboBox<Integer> maxResultsComboBox = new JComboBox<>(
                new Integer[] {5, 10, 20, 50, 100});
        maxResultsComboBox.setSelectedItem(10);
        
        optionsPanel.add(filenameCheckBox);
        optionsPanel.add(contentCheckBox);
        optionsPanel.add(new JSeparator(JSeparator.VERTICAL));
        optionsPanel.add(maxResultsLabel);
        optionsPanel.add(maxResultsComboBox);
        
        searchPanel.add(optionsPanel, BorderLayout.SOUTH);
        
        return searchPanel;
    }
    
    /**
     * Crea il pannello dei risultati
     */
    private JPanel createResultsPanel() {
        JPanel resultsPanel = new JPanel(new BorderLayout());
        
        // Modello della tabella
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableModel.addColumn(messages.getString("results.column.filename"));
        tableModel.addColumn(messages.getString("results.column.score"));
        tableModel.addColumn(messages.getString("results.column.snippet"));
        
        // Tabella
        resultsTable = new JTable(tableModel);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setRowHeight(25);
        
        // Impostazione delle larghezze delle colonne
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(500);
        
        // Gestione del doppio click
        resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = resultsTable.getSelectedRow();
                    if (row >= 0) {
                        String filename = (String) tableModel.getValueAt(row, 0);
                        openFile(filename);
                    }
                }
            }
        });
        
        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        resultsPanel.add(scrollPane, BorderLayout.CENTER);
        
        return resultsPanel;
    }
    
    /**
     * Crea la barra di stato
     */
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout(10, 0));
        statusBar.setBorder(new EmptyBorder(5, 5, 5, 5));
        statusBar.setBackground(Color.LIGHT_GRAY);
        
        statusLabel = new JLabel(messages.getString("status.ready"));
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        
        statusBar.add(statusLabel, BorderLayout.CENTER);
        statusBar.add(rightPanel, BorderLayout.EAST);
        
        return statusBar;
    }
    
    /**
     * Configura il tema scuro
     */
    private void setupDarkTheme() {
        try {
            // Tema scuro fisso
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("control", new Color(43, 43, 43));
            UIManager.put("info", new Color(43, 43, 43));
            UIManager.put("nimbusBase", new Color(0, 0, 0));
            UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
            UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
            UIManager.put("nimbusFocus", new Color(115, 164, 209));
            UIManager.put("nimbusGreen", new Color(176, 179, 50));
            UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
            UIManager.put("nimbusLightBackground", new Color(18, 30, 49));
            UIManager.put("nimbusOrange", new Color(191, 98, 4));
            UIManager.put("nimbusRed", new Color(169, 46, 34));
            UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
            UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));
            UIManager.put("text", new Color(230, 230, 230));
            
            // Colori aggiuntivi per migliorare il tema scuro
            UIManager.put("Table.background", new Color(50, 50, 50));
            UIManager.put("Table.foreground", new Color(220, 220, 220));
            UIManager.put("Table.selectionBackground", new Color(80, 80, 80));
            UIManager.put("Table.selectionForeground", new Color(255, 255, 255));
            UIManager.put("Table.gridColor", new Color(70, 70, 70));
            
            UIManager.put("TextField.background", new Color(60, 60, 60));
            UIManager.put("TextField.foreground", new Color(220, 220, 220));
            UIManager.put("TextField.caretForeground", new Color(220, 220, 220));
            
            UIManager.put("Button.background", new Color(60, 60, 60));
            UIManager.put("Button.foreground", new Color(220, 220, 220));
            
            UIManager.put("Panel.background", new Color(50, 50, 50));
            UIManager.put("Panel.foreground", new Color(220, 220, 220));
            
            UIManager.put("Label.foreground", new Color(220, 220, 220));
            
            UIManager.put("List.background", new Color(60, 60, 60));
            UIManager.put("List.foreground", new Color(220, 220, 220));
            UIManager.put("List.selectionBackground", new Color(80, 80, 80));
            UIManager.put("List.selectionForeground", new Color(255, 255, 255));
            
            UIManager.put("TitledBorder.titleColor", new Color(220, 220, 220));
            UIManager.put("MenuBar.background", new Color(60, 60, 60));
            UIManager.put("MenuBar.foreground", Color.WHITE);
            UIManager.put("Menu.background", new Color(60, 60, 60));
            UIManager.put("Menu.foreground", Color.WHITE);
            UIManager.put("MenuItem.background", new Color(60, 60, 60));
            UIManager.put("MenuItem.foreground", Color.WHITE);
            
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Errore nell'applicazione del tema", e);
        }
    }
    
    /**
     * Configura gli shortcut da tastiera
     */
    private void setupKeyboardShortcuts() {
        // Ctrl+F per focus sul campo di ricerca
        getRootPane().registerKeyboardAction(
                e -> searchField.requestFocus(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        // F5 per reindicizzare
        getRootPane().registerKeyboardAction(
                e -> reindexFiles(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
    
    /**
     * Esegue la ricerca
     */
    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            updateStatus(messages.getString("status.emptyQuery"));
            return;
        }
        
        try {
            logger.info("Esecuzione query: " + query);
            updateStatus(messages.getString("status.searching"));
            
            // Pulisci la tabella
            while (tableModel.getRowCount() > 0) {
                tableModel.removeRow(0);
            }
            
            // Esegui la ricerca
            List<Searcher.SearchResult> results = searcher.search(query, 10);
            
            // Aggiungi i risultati alla tabella
            for (Searcher.SearchResult result : results) {
                tableModel.addRow(new Object[] {
                        result.getFilename(),
                        String.format("%.4f", result.getScore()),
                        result.getSnippet()
                });
            }
            
            updateStatus(String.format(messages.getString("status.resultsFound"), results.size()));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Errore durante la ricerca", e);
            updateStatus(messages.getString("status.searchError") + ": " + e.getMessage());
            showError(messages.getString("error.search"), e.getMessage());
        }
    }
    
    /**
     * Reindicizza i file
     */
    private void reindexFiles() {
        try {
            updateStatus(messages.getString("status.indexing"));
            int numIndexed = indexer.createIndex(dataPath);
            updateStatus(String.format(messages.getString("status.indexed"), numIndexed));
            logger.info("Indicizzati " + numIndexed + " file");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Errore durante l'indicizzazione", e);
            updateStatus(messages.getString("status.indexError") + ": " + e.getMessage());
            showError(messages.getString("error.index"), e.getMessage());
        }
    }
    
    /**
     * Seleziona la directory dei dati
     */
    private void selectDataDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(messages.getString("dialog.selectDataDir"));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new File(dataPath));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            dataPath = selectedFile.getAbsolutePath();
            updateStatus(String.format(messages.getString("status.dataPathChanged"), dataPath));
            logger.info("Directory dei dati cambiata: " + dataPath);
        }
    }
    
    /**
     * Seleziona la directory dell'indice
     */
    private void selectIndexDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(messages.getString("dialog.selectIndexDir"));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new File(indexPath));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            indexPath = selectedFile.getAbsolutePath();
            try {
                searcher.close();
                searcher = new Searcher(indexPath);
                updateStatus(String.format(messages.getString("status.indexPathChanged"), indexPath));
                logger.info("Directory dell'indice cambiata: " + indexPath);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Errore durante il cambio della directory dell'indice", e);
                showError(messages.getString("error.indexPath"), e.getMessage());
            }
        }
    }
    
    /**
     * Apre un file
     */
    private void openFile(String filename) {
        try {
            File file = new File(dataPath, filename);
            if (file.exists()) {
                // Su Windows
                new ProcessBuilder("cmd", "/c", "start", "", file.getAbsolutePath()).start();
                updateStatus(String.format(messages.getString("status.fileOpened"), filename));
            } else {
                updateStatus(String.format(messages.getString("status.fileNotFound"), filename));
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Errore durante l'apertura del file", e);
            showError(messages.getString("error.openFile"), e.getMessage());
        }
    }
    

    
    /**
     * Mostra la finestra di aiuto
     */
    private void showHelp() {
        JTextArea textArea = new JTextArea(messages.getString("dialog.help.content"));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        
        JOptionPane.showMessageDialog(this,
                scrollPane,
                messages.getString("dialog.help.title"),
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Mostra la finestra di informazioni
     */
    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                messages.getString("dialog.about.content"),
                messages.getString("dialog.about.title"),
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Mostra un messaggio di errore
     */
    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this,
                message,
                title,
                JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Aggiorna la barra di stato
     */
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
    
    /**
     * Metodo principale
     */
    public static void main(String[] args) {
        // Imposta il look and feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Avvia l'applicazione
        SwingUtilities.invokeLater(() -> {
            LuceneGUI gui = new LuceneGUI();
            gui.setVisible(true);
        });
    }
}