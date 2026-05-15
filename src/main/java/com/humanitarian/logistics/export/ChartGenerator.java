package com.humanitarian.logistics.export;

import com.humanitarian.logistics.model.AnalysisResult;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.general.DefaultPieDataset;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ChartGenerator {
    private final String outputDir;

    public ChartGenerator(String outputDir) {
        this.outputDir = outputDir;
        new File(outputDir).mkdirs();
    }

    public ChartGenerator() {
        this("data/charts");
    }

    public JFreeChart createBarChart(AnalysisResult result, String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (AnalysisResult.ChartDataPoint point : result.getChartData()) {
            dataset.addValue(point.getValue(),
                point.getSeries() != null ? point.getSeries() : "Default",
                point.getLabel());
        }

        JFreeChart chart = ChartFactory.createBarChart(
            title, "Category", "Count", dataset,
            PlotOrientation.VERTICAL, true, true, false);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        return chart;
    }

    public JFreeChart createLineChart(AnalysisResult result, String title) {
        Map<String, XYSeries> seriesMap = new LinkedHashMap<>();

        for (AnalysisResult.ChartDataPoint point : result.getChartData()) {
            String seriesName = point.getSeries() != null ? point.getSeries() : "Default";
            seriesMap.putIfAbsent(seriesName, new XYSeries(seriesName));
            try {
                double x = Double.parseDouble(point.getLabel().replace("-", ""));
                seriesMap.get(seriesName).add(x, point.getValue());
            } catch (NumberFormatException e) {
                seriesMap.get(seriesName).add(
                    seriesMap.get(seriesName).getItemCount(), point.getValue());
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        for (XYSeries series : seriesMap.values()) {
            dataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
            title, "Time", "Count", dataset,
            PlotOrientation.VERTICAL, true, true, false);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setDefaultShapesVisible(true);
        plot.setRenderer(renderer);

        return chart;
    }

    public JFreeChart createPieChart(AnalysisResult result, String title) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        Map<String, Double> aggregated = new LinkedHashMap<>();

        for (AnalysisResult.ChartDataPoint point : result.getChartData()) {
            String key = point.getSeries() != null ? point.getSeries() : point.getLabel();
            aggregated.merge(key, point.getValue(), Double::sum);
        }

        for (Map.Entry<String, Double> entry : aggregated.entrySet()) {
            if (entry.getValue() > 0) {
                dataset.setValue(entry.getKey(), entry.getValue());
            }
        }

        JFreeChart chart = ChartFactory.createPieChart(
            title, dataset, true, true, false);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setSectionOutlinesVisible(false);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}"));
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 13));
        plot.setSimpleLabels(true);

        return chart;
    }

    public String saveChartToImage(JFreeChart chart, int width, int height, String filename) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filePath = outputDir + "/" + filename + "_" + timestamp + ".png";
            BufferedImage image = chart.createBufferedImage(width, height);
            ImageIO.write(image, "png", new File(filePath));
            return filePath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JFreeChart createPieChart(Map<String, Integer> data, String title) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            if (entry.getValue() > 0) {
                dataset.setValue(entry.getKey(), entry.getValue());
            }
        }

        JFreeChart chart = ChartFactory.createPieChart(
            title, dataset, true, true, false);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setSectionOutlinesVisible(false);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}"));
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 13));
        plot.setSimpleLabels(true);

        return chart;
    }
}
