package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.analyzer.AnalysisEngine;
import com.humanitarian.logistics.model.AnalysisResult;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ReliefSatisfactionPanel extends JPanel {
    private final AnalysisEngine engine;
    private final JTextArea summaryArea;
    private final JLabel imageLabel;
    private final JButton prevBtn;
    private final JButton nextBtn;
    private final JLabel counterLabel;
    private List<File> savedImages;
    private int currentImageIndex;

    public ReliefSatisfactionPanel(AnalysisEngine engine) {
        this.engine = engine;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JPanel titleRow = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Bài toán 3: Mức độ hài lòng với hàng cứu trợ");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        titleRow.add(titleLabel, BorderLayout.WEST);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        prevBtn = new JButton("\u25C0");
        prevBtn.setEnabled(false);
        prevBtn.addActionListener(e -> navigate(-1));
        nextBtn = new JButton("\u25B6");
        nextBtn.setEnabled(false);
        nextBtn.addActionListener(e -> navigate(1));
        counterLabel = new JLabel("0/0");
        counterLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        navPanel.add(prevBtn);
        navPanel.add(counterLabel);
        navPanel.add(nextBtn);
        titleRow.add(navPanel, BorderLayout.EAST);
        headerPanel.add(titleRow, BorderLayout.NORTH);

        JLabel descLabel = new JLabel("<html>Đánh giá tâm lý tích cực/tiêu cực liên quan đến các loại hàng cứu trợ: "
            + "Nhà ở, Giao thông, Thực phẩm, Y tế, Tiền mặt."
            + "<br>Hỗ trợ ưu tiên phân bổ nguồn lực cho các lĩnh vực cấp bách.</html>");
        descLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        descLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        headerPanel.add(descLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        imageLabel = new JLabel("Chưa có ảnh nào. Chạy pipeline phân tích trước.", SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.TOP);
        JScrollPane imageScrollPane = new JScrollPane(imageLabel);
        imageScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        add(imageScrollPane, BorderLayout.CENTER);

        summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        summaryArea.setBorder(BorderFactory.createTitledBorder("Kết quả"));
        JScrollPane scrollPane = new JScrollPane(summaryArea);
        scrollPane.setPreferredSize(new Dimension(800, 150));
        add(scrollPane, BorderLayout.SOUTH);

        savedImages = new ArrayList<>();
        currentImageIndex = 0;
    }

    private void navigate(int direction) {
        int newIndex = currentImageIndex + direction;
        if (newIndex >= 0 && newIndex < savedImages.size()) {
            currentImageIndex = newIndex;
            showCurrentImage();
        }
    }

    private void loadSavedImages(String typePrefix) {
        savedImages.clear();
        File chartsDir = new File("data/charts");
        File[] files = chartsDir.listFiles((dir, name) ->
            name.startsWith(typePrefix) && name.endsWith(".png"));
        if (files != null) {
            Arrays.sort(files, Comparator.<File, Integer>comparing(f -> {
                String n = f.getName();
                if (n.contains("_bar_")) return 0;
                if (n.contains("_line_")) return 1;
                if (n.contains("_pie_")) return 2;
                return 3;
            }).thenComparing(Comparator.comparingLong(File::lastModified).reversed()));
            savedImages.addAll(Arrays.asList(files));
        }
        currentImageIndex = 0;
        showCurrentImage();
    }

    private void showCurrentImage() {
        if (!savedImages.isEmpty() && currentImageIndex < savedImages.size()) {
            File file = savedImages.get(currentImageIndex);
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            if (w > 0) {
                double scale = Math.min(780.0 / w, 500.0 / h);
                int newW = (int) (w * scale);
                int newH = (int) (h * scale);
                Image scaled = icon.getImage().getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
                imageLabel.setText(null);
            } else {
                imageLabel.setIcon(null);
                imageLabel.setText(file.getName());
            }
            counterLabel.setText((currentImageIndex + 1) + "/" + savedImages.size());
            prevBtn.setEnabled(currentImageIndex > 0);
            nextBtn.setEnabled(currentImageIndex < savedImages.size() - 1);
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("Chưa có ảnh nào. Chạy pipeline phân tích trước.");
            counterLabel.setText("0/0");
            prevBtn.setEnabled(false);
            nextBtn.setEnabled(false);
        }
    }

    public void refresh() {
        AnalysisResult result = engine.getResultByType("relief_satisfaction");
        if (result != null) {
            summaryArea.setText(result.getSummary() + "\n\n");
            summaryArea.append("Chi tiết hài lòng theo từng loại hàng cứu trợ:\n");

            Object reliefStats = result.getData().get("reliefStats");
            if (reliefStats != null) {
                summaryArea.append("  " + reliefStats.toString() + "\n");
            }

            summaryArea.append("\nHài lòng nhất: " + result.getData().getOrDefault("mostSatisfied", "N/A") + "\n");
            summaryArea.append("Không hài lòng nhất: " + result.getData().getOrDefault("mostDissatisfied", "N/A") + "\n");
            summaryArea.setCaretPosition(0);
            loadSavedImages("relief_satisfaction");
        } else {
            summaryArea.setText("Chưa có kết quả. Chạy pipeline phân tích trước.");
            savedImages.clear();
            showCurrentImage();
        }
    }
}
