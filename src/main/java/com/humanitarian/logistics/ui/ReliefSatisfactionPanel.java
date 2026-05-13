package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.analyzer.AnalysisEngine;
import com.humanitarian.logistics.model.AnalysisResult;
import com.humanitarian.logistics.export.ChartGenerator;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.*;

public class ReliefSatisfactionPanel extends JPanel {
    private final AnalysisEngine engine;
    private final JTextArea summaryArea;
    private ChartPanel chartPanel;
    private ChartGenerator chartGenerator;

    public ReliefSatisfactionPanel(AnalysisEngine engine) {
        this.engine = engine;
        this.chartGenerator = new ChartGenerator();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Bài toán 3: Mức độ hài lòng với hàng cứu trợ");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        JLabel descLabel = new JLabel("<html>Đánh giá tâm lý tích cực/tiêu cực liên quan đến các loại hàng cứu trợ: "
            + "Nhà ở, Giao thông, Thực phẩm, Y tế, Tiền mặt."
            + "<br>Hỗ trợ ưu tiên phân bổ nguồn lực cho các lĩnh vực cấp bách.</html>");
        descLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        descLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        headerPanel.add(descLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        chartPanel = new ChartPanel(null);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        chartPanel.setBorder(BorderFactory.createTitledBorder("Relief Satisfaction by Category"));
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
        AnalysisResult result = engine.getResultByType("relief_satisfaction");
        if (result != null) {
            try {
                JFreeChart chart = chartGenerator.createBarChart(result,
                    "Relief Satisfaction - " + engine.getCollectedPosts().size() + " posts");
                chartPanel.setChart(chart);
            } catch (Exception e) {
                e.printStackTrace();
            }
            summaryArea.setText(result.getSummary() + "\n\n");
            summaryArea.append("Chi tiết hài lòng theo từng loại hàng cứu trợ:\n");

            Object reliefStats = result.getData().get("reliefStats");
            if (reliefStats != null) {
                summaryArea.append("  " + reliefStats.toString() + "\n");
            }

            summaryArea.append("\nHài lòng nhất: " + result.getData().getOrDefault("mostSatisfied", "N/A") + "\n");
            summaryArea.append("Không hài lòng nhất: " + result.getData().getOrDefault("mostDissatisfied", "N/A") + "\n");
            summaryArea.setCaretPosition(0);
        } else {
            summaryArea.setText("Chưa có kết quả. Chạy pipeline phân tích trước.");
        }
    }
}
