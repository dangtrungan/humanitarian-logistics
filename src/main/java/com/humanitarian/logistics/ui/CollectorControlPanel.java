package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.analyzer.AnalysisEngine;
import com.humanitarian.logistics.preprocessor.PreprocessorPipeline;

import javax.swing.*;
import java.awt.*;

public class CollectorControlPanel extends JPanel {
    private final AnalysisEngine engine;
    private final MainFrame mainFrame;
    private final JButton runFullButton;
    private final JButton collectButton;
    private final JButton preprocessButton;
    private final JButton analyzeButton;
    private final JButton saveButton;
    private final JComboBox<String> pipelineCombo;
    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private SwingWorker<Void, String> currentWorker;

    public CollectorControlPanel(AnalysisEngine engine, MainFrame mainFrame) {
        this.engine = engine;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Điều khiển Pipeline"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Preprocessing Pipeline:"), gbc);
        gbc.gridx = 1;
        pipelineCombo = new JComboBox<>(new String[]{"Default (Basic)", "Advanced (Vietnamese-aware)"});
        controlPanel.add(pipelineCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        runFullButton = new JButton("▶ Chạy Full Pipeline");
        runFullButton.setBackground(new Color(46, 204, 113));
        runFullButton.setForeground(Color.WHITE);
        runFullButton.setFont(new Font("Dialog", Font.BOLD, 12));
        runFullButton.addActionListener(e -> runPipeline());
        buttonPanel.add(runFullButton);

        collectButton = new JButton("1. Thu thập dữ liệu");
        collectButton.addActionListener(e -> runCollection());
        buttonPanel.add(collectButton);

        preprocessButton = new JButton("2. Tiền xử lý");
        preprocessButton.setEnabled(false);
        buttonPanel.add(preprocessButton);

        analyzeButton = new JButton("3. Phân tích");
        analyzeButton.setEnabled(false);
        buttonPanel.add(analyzeButton);

        saveButton = new JButton("💾 Lưu kết quả");
        saveButton.setEnabled(false);
        buttonPanel.add(saveButton);

        controlPanel.add(buttonPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        controlPanel.add(progressBar, gbc);

        gbc.gridy = 3;
        statusLabel = new JLabel("Sẵn sàng");
        statusLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        controlPanel.add(statusLabel, gbc);

        add(controlPanel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Hướng dẫn"));
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setText(
            "1. Chọn preprocessing pipeline phù hợp.\n" +
            "2. Nhấn 'Chạy Full Pipeline' để thực hiện toàn bộ quy trình.\n" +
            "   - Bước 1: Thu thập dữ liệu từ các nguồn mạng xã hội\n" +
            "   - Bước 2: Tiền xử lý dữ liệu\n" +
            "   - Bước 3: Phân tích dữ liệu (4 bài toán)\n" +
            "   - Bước 4: Lưu và xuất kết quả\n\n" +
            "3. Xem kết quả trong các tab tương ứng.\n" +
            "4. Cấu hình từ khóa, nguồn dữ liệu, categories trong tab 'Cấu hình'.\n\n" +
            "Mẫu dữ liệu: Bão Yagi (06/09/2024 - 30/09/2024)\n" +
            "Nguồn: Twitter, Facebook, TikTok, YouTube"
        );
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        infoPanel.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        add(infoPanel, BorderLayout.CENTER);
    }

    public void runPipeline() {
        if (currentWorker != null && !currentWorker.isDone()) {
            JOptionPane.showMessageDialog(this, "Pipeline đang chạy.");
            return;
        }

        selectPipeline();
        setButtonsEnabled(false);

        currentWorker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() {
                publish("Bắt đầu pipeline...");
                publish("Thu thập dữ liệu...");
                setProgress(10);
                engine.collectData();

                publish("Tiền xử lý dữ liệu...");
                setProgress(40);
                engine.preprocessData();

                publish("Phân tích dữ liệu...");
                setProgress(70);
                engine.runAnalysis();

                publish("Lưu kết quả...");
                setProgress(90);
                engine.saveResults();

                publish("Hoàn tất!");
                setProgress(100);
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                String last = chunks.get(chunks.size() - 1);
                statusLabel.setText(last);
            }

            @Override
            protected void done() {
                setButtonsEnabled(true);
                mainFrame.refreshAllPanels();
                JOptionPane.showMessageDialog(CollectorControlPanel.this,
                    "Pipeline hoàn tất! Xem kết quả trong các tab.", "Hoàn tất",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        };
        currentWorker.execute();
    }

    public void runCollection() {
        selectPipeline();
        setButtonsEnabled(false);

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() {
                publish("Thu thập dữ liệu...");
                setProgress(10);
                engine.collectData();
                publish("Hoàn tất thu thập: " + engine.getCollectedPosts().size() + " posts");
                setProgress(100);
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                statusLabel.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                setButtonsEnabled(true);
            }
        };
        worker.execute();
    }

    private void selectPipeline() {
        String selected = (String) pipelineCombo.getSelectedItem();
        if (selected != null && selected.contains("Advanced")) {
            engine.setPreprocessor(PreprocessorPipeline.createAdvanced());
        } else {
            engine.setPreprocessor(PreprocessorPipeline.createDefault());
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        runFullButton.setEnabled(enabled);
        collectButton.setEnabled(enabled);
        preprocessButton.setEnabled(enabled);
        analyzeButton.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        pipelineCombo.setEnabled(enabled);
    }
}
