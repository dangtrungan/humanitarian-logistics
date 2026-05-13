package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.analyzer.AnalysisEngine;
import com.humanitarian.logistics.model.AnalysisResult;
import com.humanitarian.logistics.export.ChartGenerator;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class DamageAnalysisPanel extends JPanel {
    private final AnalysisEngine engine;
    private final JTextArea summaryArea;
    private ChartPanel chartPanel;
    private ChartGenerator chartGenerator;

    public DamageAnalysisPanel(AnalysisEngine engine) {
        this.engine = engine;
        this.chartGenerator = new ChartGenerator();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Bài toán 2: Xác định mức độ và loại thiệt hại phổ biến");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        JLabel descLabel = new JLabel("<html>Phân loại bài đăng thành các danh mục thiệt hại: "
            + "Người bị ảnh hưởng, Gián đoạn KT, Nhà cửa hư hỏng, Tài sản cá nhân, Cơ sở hạ tầng, Môi trường"
            + "<br>Xác định loại thiệt hại được công chúng quan tâm nhất.</html>");
        descLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        descLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        headerPanel.add(descLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        chartPanel = new ChartPanel(null);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        chartPanel.setBorder(BorderFactory.createTitledBorder("Damage Categories Distribution"));
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
        AnalysisResult result = engine.getResultByType("damage_analysis");
        if (result != null) {
            try {
                JFreeChart chart = chartGenerator.createBarChart(result,
                    "Damage Categories - " + engine.getCollectedPosts().size() + " posts");
                chartPanel.setChart(chart);
            } catch (Exception e) {
                e.printStackTrace();
            }
            summaryArea.setText(result.getSummary() + "\n\n");
            summaryArea.append("Chi tiết các loại thiệt hại:\n");

            Object damageDetails = result.getData().get("damageDetails");
            if (damageDetails instanceof Map) {
                Map<String, Map<String, Object>> details = (Map<String, Map<String, Object>>) damageDetails;
                for (Map.Entry<String, Map<String, Object>> entry : details.entrySet()) {
                    summaryArea.append("  " + entry.getKey() + ": " + entry.getValue().getOrDefault("total", 0) + "\n");
                }
            }

            summaryArea.append("\nLoại thiệt hại được đề cập nhiều nhất: "
                + result.getData().getOrDefault("mostMentioned", "N/A"));
            summaryArea.setCaretPosition(0);
        } else {
            summaryArea.setText("Chưa có kết quả. Chạy pipeline phân tích trước.");
        }
    }
}
