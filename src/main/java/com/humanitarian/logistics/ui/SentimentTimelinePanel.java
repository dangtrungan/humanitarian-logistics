package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.analyzer.AnalysisEngine;
import com.humanitarian.logistics.model.AnalysisResult;
import com.humanitarian.logistics.export.ChartGenerator;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.*;

public class SentimentTimelinePanel extends JPanel {
    private final AnalysisEngine engine;
    private final JTextArea summaryArea;
    private ChartPanel chartPanel;
    private ChartGenerator chartGenerator;

    public SentimentTimelinePanel(AnalysisEngine engine) {
        this.engine = engine;
        this.chartGenerator = new ChartGenerator();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Bài toán 1: Theo dõi tâm lý công chúng theo thời gian");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        JLabel descLabel = new JLabel("<html>Phân tích số lượng post tích cực/tiêu cực theo ngày. "
            + "Ví dụ: Bão Yagi (06-10/09/2024) - giai đoạn đầu tâm lý tiêu cực chiếm ưu thế, "
            + "sau đó tâm lý tích cực tăng dần.</html>");
        descLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        descLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        headerPanel.add(descLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        chartPanel = new ChartPanel(null);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        chartPanel.setBorder(BorderFactory.createTitledBorder("Sentiment Timeline"));
        add(chartPanel, BorderLayout.CENTER);

        summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        summaryArea.setBorder(BorderFactory.createTitledBorder("Kết quả"));
        JScrollPane scrollPane = new JScrollPane(summaryArea);
        scrollPane.setPreferredSize(new Dimension(800, 150));
        add(scrollPane, BorderLayout.SOUTH);
    }

    public void refresh() {
        AnalysisResult result = engine.getResultByType("sentiment_timeline");
        if (result != null) {
            try {
                JFreeChart chart = chartGenerator.createLineChart(result,
                    "Sentiment Timeline - " + engine.getCollectedPosts().size() + " posts");
                chartPanel.setChart(chart);
            } catch (Exception e) {
                JFreeChart barChart = chartGenerator.createBarChart(result,
                    "Sentiment by Date - " + engine.getCollectedPosts().size() + " posts");
                chartPanel.setChart(barChart);
            }
            summaryArea.setText(result.getSummary() + "\n\n");
            summaryArea.append("Chi tiết:\n");
            result.getData().forEach((k, v) -> summaryArea.append("  " + k + ": " + v + "\n"));
            summaryArea.setCaretPosition(0);
        } else {
            summaryArea.setText("Chưa có kết quả. Chạy pipeline phân tích trước.");
        }
    }
}
