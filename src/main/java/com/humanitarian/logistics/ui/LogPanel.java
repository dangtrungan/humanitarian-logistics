package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.analyzer.AnalysisEngine;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LogPanel extends JPanel {
    private final AnalysisEngine engine;
    private final JTextArea logArea;

    public LogPanel(AnalysisEngine engine) {
        this.engine = engine;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("System Logs");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());
        headerPanel.add(refreshButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(0, 255, 0));
        logArea.setText("Logs will appear here after running the pipeline.\n");

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Pipeline Logs"));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refresh() {
        List<String> logs = engine.getLogs();
        if (logs.isEmpty()) {
            logArea.setText("No logs yet. Run the pipeline first.\n");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String log : logs) {
                sb.append(log).append("\n");
            }
            logArea.setText(sb.toString());
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }
}
