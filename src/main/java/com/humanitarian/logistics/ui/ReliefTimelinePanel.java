package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.analyzer.AnalysisEngine;
import com.humanitarian.logistics.model.AnalysisResult;
import com.humanitarian.logistics.export.ChartGenerator;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ReliefTimelinePanel extends JPanel {
    private final AnalysisEngine engine;
    private final JTextArea summaryArea;
    private ChartPanel chartPanel;
    private ChartGenerator chartGenerator;

    public ReliefTimelinePanel(AnalysisEngine engine) {
        this.engine = engine;
        this.chartGenerator = new ChartGenerator();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Bài toán 4: Theo dõi tâm lý theo từng loại hàng cứu trợ");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        JLabel descLabel = new JLabel("<html>Quan sát tâm lý thay đổi theo thời gian cho từng danh mục cứu trợ."
            + "<br>VD: Tiền mặt/thực phẩm có xu hướng tích cực; Giao thông/nhà ở có xu hướng tiêu cực."
            + "<br>Xác định hiệu quả nỗ lực cứu trợ theo lĩnh vực.</html>");
        descLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        descLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        headerPanel.add(descLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        chartPanel = new ChartPanel(null);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        chartPanel.setBorder(BorderFactory.createTitledBorder("Relief Sentiment Timeline"));
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
        AnalysisResult result = engine.getResultByType("relief_timeline");
        if (result != null) {
            try {
                JFreeChart chart = chartGenerator.createLineChart(result,
                    "Relief Sentiment Over Time - " + engine.getCollectedPosts().size() + " posts");
                chartPanel.setChart(chart);
            } catch (Exception e) {
                JFreeChart barChart = chartGenerator.createBarChart(result,
                    "Relief Sentiment by Category - " + engine.getCollectedPosts().size() + " posts");
                chartPanel.setChart(barChart);
            }
            summaryArea.setText(result.getSummary() + "\n\n");
            summaryArea.append("Xu hướng theo từng loại:\n");

            Object trendDescs = result.getData().get("trendDescriptions");
            if (trendDescs instanceof List) {
                for (Object desc : (List<Object>) trendDescs) {
                    summaryArea.append("  • " + desc.toString() + "\n");
                }
            }

            summaryArea.append("\nÝ nghĩa: Các lĩnh vực có tâm lý tiêu cực cần ưu tiên nguồn lực.\n");
            summaryArea.append("Các lĩnh vực tích cực cho thấy hiệu quả phân phối tốt.\n");
            summaryArea.setCaretPosition(0);
        } else {
            summaryArea.setText("Chưa có kết quả. Chạy pipeline phân tích trước.");
        }
    }
}
