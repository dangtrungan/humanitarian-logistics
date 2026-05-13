package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.config.AppConfig;
import com.humanitarian.logistics.model.AnalysisConfig;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class ConfigPanel extends JPanel {
    private final AppConfig appConfig;
    private final AnalysisConfig config;

    private final JTextField disasterNameField;
    private final JTextField startDateField;
    private final JTextField endDateField;
    private final JTextField languageField;
    private final DefaultTableModel keywordTableModel;
    private final DefaultTableModel hashtagTableModel;
    private final DefaultTableModel sourceTableModel;
    private final DefaultTableModel analyzerTableModel;
    private final JTextArea damageCategoryArea;
    private final JTextArea reliefCategoryArea;

    public ConfigPanel(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.config = appConfig.getAnalysisConfig();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel headerLabel = new JLabel("Cấu hình phân tích", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        mainPanel.add(headerLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        mainPanel.add(new JLabel("Disaster Name:"), gbc);
        gbc.gridx = 1;
        disasterNameField = new JTextField(config.getDisasterName(), 20);
        mainPanel.add(disasterNameField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        mainPanel.add(new JLabel("Start Date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        startDateField = new JTextField(config.getStartDate(), 20);
        mainPanel.add(startDateField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        mainPanel.add(new JLabel("End Date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        endDateField = new JTextField(config.getEndDate(), 20);
        mainPanel.add(endDateField, gbc);

        gbc.gridy = 4; gbc.gridx = 0;
        mainPanel.add(new JLabel("Language:"), gbc);
        gbc.gridx = 1;
        languageField = new JTextField(config.getLanguage(), 10);
        mainPanel.add(languageField, gbc);

        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2;
        JTabbedPane configTabs = new JTabbedPane();

        keywordTableModel = new DefaultTableModel(new String[]{"Keywords"}, 0);
        for (String kw : config.getKeywords()) keywordTableModel.addRow(new Object[]{kw});
        JTable keywordTable = new JTable(keywordTableModel);
        configTabs.addTab("Keywords", new JScrollPane(keywordTable));

        hashtagTableModel = new DefaultTableModel(new String[]{"Hashtags"}, 0);
        for (String ht : config.getHashtags()) hashtagTableModel.addRow(new Object[]{ht});
        JTable hashtagTable = new JTable(hashtagTableModel);
        configTabs.addTab("Hashtags", new JScrollPane(hashtagTable));

        sourceTableModel = new DefaultTableModel(new String[]{"Data Sources"}, 0);
        for (String src : config.getDataSources()) sourceTableModel.addRow(new Object[]{src});
        JTable sourceTable = new JTable(sourceTableModel);
        configTabs.addTab("Sources", new JScrollPane(sourceTable));

        analyzerTableModel = new DefaultTableModel(new String[]{"Analyzers"}, 0);
        for (String an : config.getSelectedAnalyzers()) analyzerTableModel.addRow(new Object[]{an});
        JTable analyzerTable = new JTable(analyzerTableModel);
        configTabs.addTab("Analyzers", new JScrollPane(analyzerTable));

        damageCategoryArea = new JTextArea(8, 30);
        damageCategoryArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        StringBuilder dcText = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : config.getDamageCategories().entrySet()) {
            dcText.append(entry.getKey()).append(": ").append(String.join(", ", entry.getValue())).append("\n");
        }
        damageCategoryArea.setText(dcText.toString());
        configTabs.addTab("Damage Categories", new JScrollPane(damageCategoryArea));

        reliefCategoryArea = new JTextArea(8, 30);
        reliefCategoryArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        StringBuilder rcText = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : config.getReliefCategories().entrySet()) {
            rcText.append(entry.getKey()).append(": ").append(String.join(", ", entry.getValue())).append("\n");
        }
        reliefCategoryArea.setText(rcText.toString());
        configTabs.addTab("Relief Categories", new JScrollPane(reliefCategoryArea));

        mainPanel.add(configTabs, gbc);

        gbc.gridy = 6;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("💾 Lưu cấu hình");
        saveButton.addActionListener(e -> saveConfig());
        buttonPanel.add(saveButton);

        JButton resetButton = new JButton("Mặc định");
        resetButton.addActionListener(e -> loadDefaults());
        buttonPanel.add(resetButton);

        mainPanel.add(buttonPanel, gbc);

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
    }

    private void saveConfig() {
        config.setDisasterName(disasterNameField.getText());
        config.setStartDate(startDateField.getText());
        config.setEndDate(endDateField.getText());
        config.setLanguage(languageField.getText());

        List<String> keywords = new ArrayList<>();
        for (int i = 0; i < keywordTableModel.getRowCount(); i++) {
            keywords.add((String) keywordTableModel.getValueAt(i, 0));
        }
        config.setKeywords(keywords);

        List<String> hashtags = new ArrayList<>();
        for (int i = 0; i < hashtagTableModel.getRowCount(); i++) {
            hashtags.add((String) hashtagTableModel.getValueAt(i, 0));
        }
        config.setHashtags(hashtags);

        List<String> sources = new ArrayList<>();
        for (int i = 0; i < sourceTableModel.getRowCount(); i++) {
            sources.add((String) sourceTableModel.getValueAt(i, 0));
        }
        config.setDataSources(sources);

        List<String> analyzers = new ArrayList<>();
        for (int i = 0; i < analyzerTableModel.getRowCount(); i++) {
            analyzers.add((String) analyzerTableModel.getValueAt(i, 0));
        }
        config.setSelectedAnalyzers(analyzers);

        JOptionPane.showMessageDialog(this, "Cấu hình đã được lưu!");
    }

    private void loadDefaults() {
        disasterNameField.setText("Bão Yagi");
        startDateField.setText("2024-09-06");
        endDateField.setText("2024-09-30");
        languageField.setText("vi");

        keywordTableModel.setRowCount(0);
        for (String kw : List.of("bão Yagi", "bão số 3", "cứu trợ", "thiên tai", "lũ lụt")) {
            keywordTableModel.addRow(new Object[]{kw});
        }

        hashtagTableModel.setRowCount(0);
        for (String ht : List.of("#baoyagi", "#baoso3", "#cuutro", "#thientai")) {
            hashtagTableModel.addRow(new Object[]{ht});
        }

        sourceTableModel.setRowCount(0);
        for (String src : List.of("twitter", "facebook", "tiktok", "youtube")) {
            sourceTableModel.addRow(new Object[]{src});
        }

        JOptionPane.showMessageDialog(this, "Đã nạp cấu hình mặc định.");
    }

    public void refresh() {
        disasterNameField.setText(config.getDisasterName());
        startDateField.setText(config.getStartDate());
        endDateField.setText(config.getEndDate());
        languageField.setText(config.getLanguage());
    }
}
