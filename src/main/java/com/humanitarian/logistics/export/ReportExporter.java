package com.humanitarian.logistics.export;

import com.humanitarian.logistics.model.AnalysisResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ReportExporter {
    private final String outputDir;

    public ReportExporter(String outputDir) {
        this.outputDir = outputDir;
        new File(outputDir).mkdirs();
    }

    public ReportExporter() {
        this("data/reports");
    }

    public void exportTextReport(AnalysisResult result) {
        String filePath = outputDir + "/report_" + result.getAnalysisType() + "_"
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            writer.println("=" .repeat(70));
            writer.println("HUMANITARIAN LOGISTICS ANALYSIS REPORT");
            writer.println("=" .repeat(70));
            writer.println("Analysis: " + result.getAnalysisName());
            writer.println("Type: " + result.getAnalysisType());
            writer.println("-".repeat(70));
            writer.println("SUMMARY:");
            writer.println(result.getSummary());
            writer.println("-".repeat(70));
            writer.println("DETAILED DATA:");

            for (Map.Entry<String, Object> entry : result.getData().entrySet()) {
                writer.println("  " + entry.getKey() + ": " + entry.getValue());
            }

            writer.println("-".repeat(70));
            writer.println("CHART DATA (" + result.getChartData().size() + " points):");
            for (AnalysisResult.ChartDataPoint point : result.getChartData()) {
                writer.printf("  Label: %s | Value: %.2f | Category: %s | Series: %s%n",
                    point.getLabel(), point.getValue(),
                    point.getCategory() != null ? point.getCategory() : "",
                    point.getSeries() != null ? point.getSeries() : "");
            }

            writer.println("=" .repeat(70));
            writer.println("Generated: " + LocalDateTime.now());
            writer.println("=" .repeat(70));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportHtmlReport(AnalysisResult result) {
        String filePath = outputDir + "/report_" + result.getAnalysisType() + "_"
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".html";

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            writer.println("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
            writer.println("<title>Humanitarian Logistics Report</title>");
            writer.println("<style>body{font-family:Arial;margin:20px}");
            writer.println("h1{color:#2c3e50}.summary{background:#ecf0f1;padding:15px;border-radius:5px}");
            writer.println("table{border-collapse:collapse;width:100%}");
            writer.println("th,td{border:1px solid #ddd;padding:8px;text-align:left}");
            writer.println("th{background:#3498db;color:white}</style></head><body>");
            writer.println("<h1>Humanitarian Logistics Analysis Report</h1>");
            writer.println("<h2>" + result.getAnalysisName() + "</h2>");
            writer.println("<div class='summary'><strong>Summary:</strong> " + result.getSummary() + "</div>");
            writer.println("<h3>Detailed Data</h3><table><tr><th>Key</th><th>Value</th></tr>");
            for (Map.Entry<String, Object> entry : result.getData().entrySet()) {
                writer.println("<tr><td>" + entry.getKey() + "</td><td>" + entry.getValue() + "</td></tr>");
            }
            writer.println("</table></body></html>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
