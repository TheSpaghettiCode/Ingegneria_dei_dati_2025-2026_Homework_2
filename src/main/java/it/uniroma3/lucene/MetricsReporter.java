package it.uniroma3.lucene;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Classe per la visualizzazione e il reporting delle metriche di indicizzazione.
 */
public class MetricsReporter {
    private final IndexingMetrics metrics;
    
    /**
     * Costruttore della classe MetricsReporter.
     * 
     * @param metrics Le metriche da visualizzare
     */
    public MetricsReporter(IndexingMetrics metrics) {
        this.metrics = metrics;
    }
    
    /**
     * Mostra un report grafico delle metriche.
     */
    public void showGraphicalReport() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Report Metriche di Indicizzazione");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLayout(new BorderLayout());
            
            // Pannello superiore con informazioni di riepilogo
            JPanel summaryPanel = new JPanel();
            summaryPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            summaryPanel.setBorder(BorderFactory.createTitledBorder("Riepilogo"));
            
            JPanel statsPanel = new JPanel();
            statsPanel.setLayout(new BorderLayout());
            
            JPanel leftStats = new JPanel();
            leftStats.setLayout(new FlowLayout(FlowLayout.LEFT));
            leftStats.add(new JLabel("<html><b>File totali:</b> " + metrics.getTotalFiles() + "</html>"));
            leftStats.add(Box.createHorizontalStrut(20));
            leftStats.add(new JLabel("<html><b>File indicizzati:</b> " + metrics.getSuccessfulFiles() + "</html>"));
            leftStats.add(Box.createHorizontalStrut(20));
            leftStats.add(new JLabel("<html><b>File con errori:</b> " + metrics.getFailedFiles() + "</html>"));
            
            JPanel rightStats = new JPanel();
            rightStats.setLayout(new FlowLayout(FlowLayout.LEFT));
            rightStats.add(new JLabel("<html><b>Tempo totale:</b> " + metrics.getTotalIndexingTime() + " ms</html>"));
            rightStats.add(Box.createHorizontalStrut(20));
            rightStats.add(new JLabel("<html><b>Tempo medio:</b> " + String.format("%.2f", metrics.getAverageFileProcessingTime()) + " ms</html>"));
            rightStats.add(Box.createHorizontalStrut(20));
            rightStats.add(new JLabel("<html><b>Tempo min:</b> " + metrics.getMinFileProcessingTime() + " ms</html>"));
            rightStats.add(Box.createHorizontalStrut(20));
            rightStats.add(new JLabel("<html><b>Tempo max:</b> " + metrics.getMaxFileProcessingTime() + " ms</html>"));
            
            statsPanel.add(leftStats, BorderLayout.NORTH);
            statsPanel.add(rightStats, BorderLayout.SOUTH);
            
            summaryPanel.add(statsPanel);
            
            // Pannello centrale con il report dettagliato
            JTextArea reportArea = new JTextArea();
            reportArea.setEditable(false);
            reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            reportArea.setText(metrics.generateSummaryReport());
            JScrollPane scrollPane = new JScrollPane(reportArea);
            scrollPane.setBorder(BorderFactory.createTitledBorder("Report Dettagliato"));
            
            // Pannello inferiore con pulsanti
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            
            JButton saveJsonButton = new JButton("Salva Report JSON");
            saveJsonButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String filePath = "reports/report_" + System.currentTimeMillis() + ".json";
                    metrics.saveJsonReport(filePath);
                    JOptionPane.showMessageDialog(frame, 
                            "Report JSON salvato in: " + filePath, 
                            "Salvataggio completato", 
                            JOptionPane.INFORMATION_MESSAGE);
                }
            });
            
            JButton saveCsvButton = new JButton("Salva Report CSV");
            saveCsvButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String filePath = "reports/report_" + System.currentTimeMillis() + ".csv";
                    metrics.saveCsvReport(filePath);
                    JOptionPane.showMessageDialog(frame, 
                            "Report CSV salvato in: " + filePath, 
                            "Salvataggio Completato", 
                            JOptionPane.INFORMATION_MESSAGE);
                }
            });
            
            JButton closeButton = new JButton("Chiudi");
            closeButton.addActionListener(e -> frame.dispose());
            
            buttonPanel.add(saveJsonButton);
            buttonPanel.add(saveCsvButton);
            buttonPanel.add(closeButton);
            
            // Aggiunta dei pannelli al frame
            frame.add(summaryPanel, BorderLayout.NORTH);
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.add(buttonPanel, BorderLayout.SOUTH);
            
            // Visualizzazione del frame
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
    
    /**
     * Salva i report in formato JSON e CSV
     */
    public void saveReports() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String jsonFilePath = "reports/report_" + timestamp + ".json";
        String csvFilePath = "reports/report_" + timestamp + ".csv";
        
        metrics.saveJsonReport(jsonFilePath);
        metrics.saveCsvReport(csvFilePath);
        
        System.out.println("Report JSON salvato in: " + jsonFilePath);
        System.out.println("Report CSV salvato in: " + csvFilePath);
    }
}