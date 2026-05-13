package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.analyzer.AnalysisEngine;
import com.humanitarian.logistics.model.AnalysisResult;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {
    private final AnalysisEngine engine;
    private final JLabel totalPostsLabel;
    private final JLabel totalPositiveLabel;
    private final JLabel totalNegativeLabel;
    private final JLabel totalNeutralLabel;
    private final JTextArea summaryArea;

    public DashboardPanel(AnalysisEngine engine) {
        this.engine = engine;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Tổng quan phân tích", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Thống kê nhanh"));

        totalPostsLabel = createStatBox(statsPanel, "Tổng bài đăng", "0", new Color(52, 152, 219));
        totalPositiveLabel = createStatBox(statsPanel, "Tích cực", "0", new Color(46, 204, 113));
        totalNegativeLabel = createStatBox(statsPanel, "Tiêu cực", "0", new Color(231, 76, 60));
        totalNeutralLabel = createStatBox(statsPanel, "Trung tính", "0", new Color(149, 165, 166));

        add(statsPanel, BorderLayout.CENTER);

        summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        summaryArea.setBorder(BorderFactory.createTitledBorder("Kết quả phân tích"));
        summaryArea.setText("Chưa có dữ liệu. Vào tab 'Điều khiển' để chạy pipeline phân tích.");

        JScrollPane scrollPane = new JScrollPane(summaryArea);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        add(scrollPane, BorderLayout.SOUTH);
    }

    private JLabel createStatBox(JPanel panel, String title, String value, Color bgColor) {
        JLabel label = new JLabel("<html><div style='text-align:center;background:" + toHex(bgColor)
            + ";color:white;padding:15px;border-radius:8px;font-size:24px'>"
            + value + "<br><span style='font-size:12px'>" + title + "</span></div></html>",
            SwingConstants.CENTER);
        panel.add(label);
        return label;
    }

    public void refresh() {
        AnalysisResult sentimentResult = engine.getResultByType("sentiment_timeline");
        if (sentimentResult != null) {
            long total = ((Number) sentimentResult.getData().getOrDefault("totalPosts", 0)).longValue();
            long pos = ((Number) sentimentResult.getData().getOrDefault("totalPositive", 0)).longValue();
            long neg = ((Number) sentimentResult.getData().getOrDefault("totalNegative", 0)).longValue();
            long neu = ((Number) sentimentResult.getData().getOrDefault("totalNeutral", 0)).longValue();

            totalPostsLabel.setText(createStatHtml(toHex(new Color(52, 152, 219)), String.valueOf(total), "Tổng bài đăng"));
            totalPositiveLabel.setText(createStatHtml(toHex(new Color(46, 204, 113)), String.valueOf(pos), "Tích cực"));
            totalNegativeLabel.setText(createStatHtml(toHex(new Color(231, 76, 60)), String.valueOf(neg), "Tiêu cực"));
            totalNeutralLabel.setText(createStatHtml(toHex(new Color(149, 165, 166)), String.valueOf(neu), "Trung tính"));
        }

        StringBuilder sb = new StringBuilder();
        for (AnalysisResult result : engine.getResults()) {
            sb.append("=== ").append(result.getAnalysisName()).append(" ===\n");
            sb.append(result.getSummary()).append("\n\n");
        }
        if (sb.isEmpty()) sb.append("Chưa có dữ liệu. Vào tab 'Điều khiển' để chạy pipeline phân tích.");
        summaryArea.setText(sb.toString());
        summaryArea.setCaretPosition(0);
    }

    private String createStatHtml(String bgColor, String value, String title) {
        return "<html><div style='text-align:center;background:" + bgColor
            + ";color:white;padding:15px;border-radius:8px;font-size:24px'>"
            + value + "<br><span style='font-size:12px'>" + title + "</span></div></html>";
    }

    private String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
