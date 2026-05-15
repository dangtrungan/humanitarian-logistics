package com.humanitarian.logistics.export;

import com.humanitarian.logistics.model.AnalysisResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReportExporter {
    private final String outputDir;

    public ReportExporter(String outputDir) {
        this.outputDir = outputDir;
        new File(outputDir).mkdirs();
    }

    public ReportExporter() {
        this("data/reports");
    }

    public String exportTextReport(AnalysisResult result, List<String> chartPaths) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filePath = outputDir + "/report_" + result.getAnalysisType() + "_" + timestamp + ".txt";

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            writer.println("=" .repeat(70));
            writer.println("BÃO YAGI - BÁO CÁO PHÂN TÍCH HÀNG CỨU TRỢ NHÂN ĐẠO");
            writer.println("=" .repeat(70));
            writer.println("  Phân tích: " + result.getAnalysisName());
            writer.println("  Ngày tạo: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            writer.println();

            writer.println("=" .repeat(70));
            writer.println("TÓM TẮT TỔNG QUAN");
            writer.println("=" .repeat(70));
            if (result.getNarrativeSummary() != null) {
                writer.println(wrapText(result.getNarrativeSummary(), 66));
            } else {
                writer.println(wrapText(result.getSummary(), 66));
            }
            writer.println();

            writer.println("=" .repeat(70));
            writer.println("THỐNG KÊ");
            writer.println("=" .repeat(70));
            Map<String, Object> data = result.getData();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                writer.println("  . " + formatKey(entry.getKey()) + ": " + formatValueForText(entry.getKey(), entry.getValue()));
            }
            writer.println();

            writer.println("=" .repeat(70));
            writer.println("DỮ LIỆU CHI TIẾT");
            writer.println("=" .repeat(70));
            String currentSeries = null;
            for (AnalysisResult.ChartDataPoint point : result.getChartData()) {
                String series = point.getSeries() != null ? point.getSeries() : "";
                if (!series.equals(currentSeries)) {
                    currentSeries = series;
                    if (!series.isEmpty()) {
                        writer.println("    --- " + series + " ---");
                    }
                }
                writer.printf("    %s: %.0f%n", point.getLabel(), point.getValue());
            }
            writer.println();

            writer.println("=" .repeat(70));
            writer.println("PHÂN TÍCH CHUYÊN SÂU");
            writer.println("=" .repeat(70));
            if (result.getInsights() != null && !result.getInsights().isEmpty()) {
                for (String insight : result.getInsights()) {
                    writer.println("  \u2022 " + wrapText(insight, 64));
                }
            } else {
                writer.println("  (Không có dữ liệu phân tích chuyên sâu)");
            }
            writer.println();

            writer.println("=" .repeat(70));
            writer.println("KẾT LUẬN");
            writer.println("=" .repeat(70));
            if (result.getConclusions() != null && !result.getConclusions().isEmpty()) {
                for (String c : result.getConclusions()) {
                    writer.println("  \u2022 " + wrapText(c, 64));
                }
            } else {
                writer.println("  (Không có kết luận)");
            }
            writer.println();

            writer.println("=" .repeat(70));
            writer.println("KHUYẾN NGHỊ");
            writer.println("=" .repeat(70));
            if (result.getRecommendations() != null && !result.getRecommendations().isEmpty()) {
                for (String rec : result.getRecommendations()) {
                    writer.println("  \u2022 " + wrapText(rec, 64));
                }
            } else {
                writer.println("  (Không có khuyến nghị)");
            }
            writer.println();

            if (chartPaths != null && !chartPaths.isEmpty()) {
                writer.println("=" .repeat(70));
                writer.println("BIỂU ĐỒ");
                writer.println("=" .repeat(70));
                for (String path : chartPaths) {
                    writer.println("  " + path);
                }
                writer.println();
            }

            writer.println("=" .repeat(70));
            writer.println("--- HẾT BÁO CÁO ---");
            writer.println("=" .repeat(70));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    public String exportTextReport(AnalysisResult result) {
        return exportTextReport(result, null);
    }

    public String exportHtmlReport(AnalysisResult result, List<String> chartPaths, String disasterName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filePath = outputDir + "/report_" + result.getAnalysisType() + "_" + timestamp + ".html";

        List<String> graphCharts = new ArrayList<>();
        List<String> pieCharts = new ArrayList<>();
        if (chartPaths != null) {
            for (String p : chartPaths) {
                String fn = p.substring(p.lastIndexOf('/') + 1);
                if (fn.contains("_pie_")) {
                    pieCharts.add(p);
                } else {
                    graphCharts.add(p);
                }
            }
        }

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            String safeName = result.getAnalysisName().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            String safeSummary = result.getNarrativeSummary() != null
                ? result.getNarrativeSummary().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                : (result.getSummary() != null ? result.getSummary().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") : "");
            String disaster = disasterName != null ? disasterName.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") : "";

            writer.println("<!DOCTYPE html>");
            writer.println("<html lang='vi'>");
            writer.println("<head>");
            writer.println("<meta charset='UTF-8'>");
            writer.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            writer.println("<title>" + safeName + " - " + disaster + "</title>");
            writer.println("<style>");
            writer.println("*{margin:0;padding:0;box-sizing:border-box}");
            writer.println("body{font-family:'Segoe UI',system-ui,-apple-ui,sans-serif;background:#f0f2f5;color:#1a1a2e;line-height:1.6}");
            writer.println(".container{max-width:1000px;margin:0 auto;padding:20px}");
            writer.println(".header{background:linear-gradient(135deg,#1a237e,#283593);color:#fff;padding:30px;border-radius:12px 12px 0 0}");
            writer.println(".header h1{font-size:24px;margin-bottom:6px}");
            writer.println(".header .meta{font-size:13px;opacity:.85}");
            writer.println(".section{background:#fff;padding:25px;border-bottom:1px solid #e8e8e8}");
            writer.println(".section:last-child{border-radius:0 0 12px 12px}");
            writer.println(".section h2{color:#1a237e;font-size:18px;margin-bottom:12px;padding-bottom:6px;border-bottom:2px solid #e8e8e8}");
            writer.println(".summary-text{font-size:15px;color:#333;line-height:1.8;text-align:justify}");

            writer.println(".tg{display:flex;gap:10px;margin-bottom:15px;flex-wrap:wrap}");
            writer.println(".tg button{padding:8px 20px;border:2px solid #1a237e;background:#fff;color:#1a237e;border-radius:6px;cursor:pointer;font-weight:600;font-size:14px;transition:all .2s}");
            writer.println(".tg button.sel{background:#1a237e;color:#fff}");
            writer.println(".tg button:hover:not(.sel){background:#e8eaf6}");

            writer.println(".stat-card{background:#f5f7ff;border:1px solid #e0e3f0;border-radius:8px;padding:14px;text-align:center}");
            writer.println(".stat-card .label{font-size:12px;text-transform:uppercase;color:#666;letter-spacing:.5px}");
            writer.println(".stat-card .value{font-size:20px;font-weight:700;color:#1a237e;margin-top:4px}");
            writer.println(".stats-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(220px,1fr));gap:12px}");

            writer.println(".search-bar{width:100%;padding:10px 14px;border:2px solid #ddd;border-radius:8px;font-size:14px;margin-bottom:12px;outline:none;transition:border-color .2s}");
            writer.println(".search-bar:focus{border-color:#1a237e}");
            writer.println(".detail-table{width:100%;border-collapse:collapse;font-size:14px}");
            writer.println(".detail-table th{background:#1a237e;color:#fff;padding:10px 12px;text-align:left;font-weight:600}");
            writer.println(".detail-table td{padding:8px 12px;border-bottom:1px solid #eee}");
            writer.println(".detail-table tr:hover{background:#f5f7ff}");
            writer.println(".detail-table .series-cell{color:#666;font-size:13px}");
            writer.println(".no-results{color:#999;text-align:center;padding:20px;display:none}");

            writer.println(".chart-container{text-align:center}");
            writer.println(".chart-container img{max-width:100%;height:auto;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,.08)}");

            writer.println(".insight-item{padding:10px 14px;margin-bottom:8px;background:#f5f7ff;border-left:3px solid #1a237e;border-radius:0 6px 6px 0;font-size:14px;line-height:1.6}");
            writer.println(".conclusion-item{padding:10px 14px;margin-bottom:8px;background:#fff8e1;border-left:3px solid #f9a825;border-radius:0 6px 6px 0;font-size:14px;line-height:1.6}");
            writer.println(".recommendation-item{padding:10px 14px;margin-bottom:8px;background:#e8f5e9;border-left:3px solid #43a047;border-radius:0 6px 6px 0;font-size:14px;line-height:1.6}");

            writer.println(".footer{background:#1a237e;color:rgba(255,255,255,.7);padding:16px 30px;border-radius:0 0 12px 12px;font-size:12px;text-align:center}");
            writer.println("@media(max-width:600px){.container{padding:10px}.header{padding:20px}.section{padding:16px}.stats-grid{grid-template-columns:1fr 1fr}}");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("<div class='container'>");

            writer.println("<div class='header'>");
            writer.println("<h1>" + safeName + "</h1>");
            writer.println("<div class='meta'>" + disaster + " &mdash; " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "</div>");
            writer.println("</div>");

            writer.println("<div class='section'>");
            writer.println("<h2>Tóm tắt tổng quan</h2>");
            writer.println("<div class='summary-text'>" + safeSummary + "</div>");
            writer.println("</div>");

            boolean hasGraphs = !graphCharts.isEmpty();
            boolean hasPies = !pieCharts.isEmpty();
            if (hasGraphs || hasPies) {
                writer.println("<div class='section'>");
                writer.println("<h2>Biểu đồ phân tích</h2>");
                writer.println("<div class='tg'>");
                if (hasGraphs) {
                    writer.println("<button id='btnGraph' class='sel' onclick='showChart(\"graph\")'>Biểu đồ cột / đường</button>");
                }
                if (hasPies) {
                    writer.println("<button id='btnPie' onclick='showChart(\"pie\")'>Biểu đồ tròn</button>");
                }
                writer.println("</div>");

                if (hasGraphs) {
                    writer.println("<div id='viewGraph' style='display:block'>");
                    writer.println("<div class='chart-container'>");
                    int graphIdx = 0;
                    for (String chartPath : graphCharts) {
                        String relPath = chartPath;
                        if (relPath.startsWith("data/charts/")) {
                            relPath = "../charts/" + relPath.substring("data/charts/".length());
                        }
                        writer.println(graphIdx == 0
                            ? "<img src='" + relPath + "' alt='Biểu đồ' style='max-width:100%;height:auto;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,.08)'>"
                            : "<img src='" + relPath + "' alt='Biểu đồ' style='max-width:100%;height:auto;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,.08);display:none' id='g" + graphIdx + "'>");
                        graphIdx++;
                    }
                    if (graphCharts.size() > 1) {
                        writer.println("<div style='margin-top:8px'>");
                        writer.println("<button onclick='pg(-1)' style='padding:4px 12px;margin-right:4px;border:1px solid #1a237e;background:#fff;color:#1a237e;border-radius:4px;cursor:pointer'>&#9664; Trước</button>");
                        writer.println("<span id='gc' style='font-size:13px;color:#666;margin:0 8px'>1 / " + graphCharts.size() + "</span>");
                        writer.println("<button onclick='pg(1)' style='padding:4px 12px;margin-left:4px;border:1px solid #1a237e;background:#fff;color:#1a237e;border-radius:4px;cursor:pointer'>Sau &#9654;</button>");
                        writer.println("</div>");
                    }
                    writer.println("</div>");
                    writer.println("</div>");
                }

                if (hasPies) {
                    writer.println("<div id='viewPie' style='display:none'>");
                    writer.println("<div class='chart-container'>");
                    int pieIdx = 0;
                    for (String chartPath : pieCharts) {
                        String relPath = chartPath;
                        if (relPath.startsWith("data/charts/")) {
                            relPath = "../charts/" + relPath.substring("data/charts/".length());
                        }
                        writer.println(pieIdx == 0
                            ? "<img src='" + relPath + "' alt='Biểu đồ tròn' style='max-width:100%;height:auto;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,.08)'>"
                            : "<img src='" + relPath + "' alt='Biểu đồ tròn' style='max-width:100%;height:auto;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,.08);display:none' id='p" + pieIdx + "'>");
                        pieIdx++;
                    }
                    writer.println("</div>");
                }

                writer.println("</div>");
            }

            writer.println("<div class='section'>");
            writer.println("<h2>Thống kê dữ liệu</h2>");
            writer.println("<div class='tg'>");
            writer.println("<button id='btnStatsQuick' class='sel' onclick='showStats(\"quick\")'>Xem nhanh</button>");
            writer.println("<button id='btnStatsDetail' onclick='showStats(\"detail\")'>Xem chi tiết</button>");
            writer.println("</div>");

            writer.println("<div id='viewStatsQuick' style='display:block'>");
            writer.println("<div class='stats-grid'>");
            Map<String, Object> data = result.getData();
            List<String> skipKeys = Arrays.asList("categoryTotals", "damageDetails", "reliefStats", "reliefSummaries", "trendDescriptions");
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (skipKeys.contains(entry.getKey())) continue;
                String val = formatValueForHtml(result.getAnalysisType(), entry.getKey(), entry.getValue());
                writer.println("<div class='stat-card'>");
                writer.println("<div class='label'>" + formatKey(entry.getKey()) + "</div>");
                writer.println("<div class='value'>" + val + "</div>");
                writer.println("</div>");
            }
            writer.println("</div>");
            writer.println("</div>");

            writer.println("<div id='viewStatsDetail' style='display:none'>");
            writer.println("<input id='searchInput' class='search-bar' type='text' placeholder='Tìm kiếm dữ liệu...' onkeyup='ft()'>");
            writer.println("<table class='detail-table'>");
            writer.println("<thead><tr><th>Nhãn</th><th>Giá trị</th><th>Loại</th><th>Nhóm</th></tr></thead>");
            writer.println("<tbody id='detailBody'>");
            if (result.getChartData().isEmpty()) {
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    if (skipKeys.contains(entry.getKey())) continue;
                    String val = formatValueForHtml(result.getAnalysisType(), entry.getKey(), entry.getValue());
                    writer.println("<tr><td>" + formatKey(entry.getKey()) + "</td><td>" + val + "</td><td>Thông số</td><td>-</td></tr>");
                }
            } else {
                for (AnalysisResult.ChartDataPoint point : result.getChartData()) {
                    String series = point.getSeries() != null ? point.getSeries() : "-";
                    String cat = point.getCategory() != null ? point.getCategory() : "-";
                    writer.println("<tr><td>" + point.getLabel() + "</td><td>" + String.format("%.0f", point.getValue()) + "</td><td>" + cat + "</td><td class='series-cell'>" + series + "</td></tr>");
                }
            }
            writer.println("</tbody></table>");
            writer.println("<div id='noResults' class='no-results'>Không tìm thấy dữ liệu phù hợp</div>");
            writer.println("</div>");
            writer.println("</div>");

            writer.println("<div class='section'>");
            writer.println("<h2>Phân tích chuyên sâu</h2>");
            if (result.getInsights() != null && !result.getInsights().isEmpty()) {
                for (String insight : result.getInsights()) {
                    writer.println("<div class='insight-item'>" + insight.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</div>");
                }
            } else {
                writer.println("<div class='insight-item' style='color:#999'>Chưa có dữ liệu phân tích chuyên sâu.</div>");
            }
            writer.println("</div>");

            writer.println("<div class='section'>");
            writer.println("<h2>Kết luận</h2>");
            if (result.getConclusions() != null && !result.getConclusions().isEmpty()) {
                for (String c : result.getConclusions()) {
                    writer.println("<div class='conclusion-item'>" + c.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</div>");
                }
            } else {
                writer.println("<div class='conclusion-item' style='color:#999'>Chưa có kết luận.</div>");
            }
            writer.println("</div>");

            writer.println("<div class='section'>");
            writer.println("<h2>Khuyến nghị</h2>");
            if (result.getRecommendations() != null && !result.getRecommendations().isEmpty()) {
                for (String rec : result.getRecommendations()) {
                    writer.println("<div class='recommendation-item'>" + rec.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</div>");
                }
            } else {
                writer.println("<div class='recommendation-item' style='color:#999'>Chưa có khuyến nghị.</div>");
            }
            writer.println("</div>");

            writer.println("<div class='footer'>");
            writer.println("Báo cáo được tạo tự động bởi Hệ thống Phân tích Hàng cứu trợ Nhân đạo &mdash; " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            writer.println("</div>");

            writer.println("</div>");

            writer.println("<script>");
            writer.println("var gi=0,gt=" + graphCharts.size() + ";");
            writer.println("function showChart(t){");
            if (hasGraphs) {
                writer.println("var g=document.getElementById('viewGraph');g?g.style.display=t==='graph'?'block':'none':null;");
                writer.println("var bg=document.getElementById('btnGraph');if(bg)bg.className=t==='graph'?'sel':'';");
            }
            if (hasPies) {
                writer.println("var p=document.getElementById('viewPie');p?p.style.display=t==='pie'?'block':'none':null;");
                writer.println("var bp=document.getElementById('btnPie');if(bp)bp.className=t==='pie'?'sel':'';");
            }
            writer.println("}");
            writer.println("function showStats(t){");
            writer.println("var sq=document.getElementById('viewStatsQuick');if(sq)sq.style.display=t==='quick'?'block':'none';");
            writer.println("var sd=document.getElementById('viewStatsDetail');if(sd)sd.style.display=t==='detail'?'block':'none';");
            writer.println("var bq=document.getElementById('btnStatsQuick');if(bq)bq.className=t==='quick'?'sel':'';");
            writer.println("var bd=document.getElementById('btnStatsDetail');if(bd)bd.className=t==='detail'?'sel':'';");
            writer.println("}");
            if (graphCharts.size() > 1) {
                writer.println("function pg(d){");
                writer.println("var e=document.getElementById('g'+gi);if(e)e.style.display='none';");
                writer.println("gi=(gi+d+gt)%gt;");
                writer.println("var e=document.getElementById('g'+gi);if(e)e.style.display='';");
                writer.println("var c=document.getElementById('gc');if(c)c.textContent=(gi+1)+'/'+gt;");
                writer.println("}");
            }
            writer.println("function ft(){");
            writer.println("var q=document.getElementById('searchInput');if(!q)return;");
            writer.println("var v=q.value.toLowerCase();");
            writer.println("var tb=document.getElementById('detailBody');if(!tb)return;");
            writer.println("var r=tb.getElementsByTagName('tr');");
            writer.println("var h=false;");
            writer.println("for(var i=0;i<r.length;i++){");
            writer.println("var m=r[i].textContent.toLowerCase().indexOf(v)>-1;");
            writer.println("r[i].style.display=m?'':'none';");
            writer.println("if(m)h=true;");
            writer.println("}");
            writer.println("var n=document.getElementById('noResults');if(n)n.style.display=h?'none':'block';");
            writer.println("}");
            writer.println("</script>");

            writer.println("</body>");
            writer.println("</html>");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    public String exportHtmlReport(AnalysisResult result) {
        return exportHtmlReport(result, null, null);
    }

    private String formatKey(String key) {
        switch (key) {
            case "totalPosts": return "Tổng số bài đăng";
            case "totalPositive": return "Bài đăng tích cực";
            case "totalNegative": return "Bài đăng tiêu cực";
            case "totalNeutral": return "Bài đăng trung tính";
            case "positiveRatio": return "Tỷ lệ tích cực";
            case "negativeRatio": return "Tỷ lệ tiêu cực";
            case "mostMentioned": return "Thiệt hại được nhắc đến nhiều nhất";
            case "totalMentions": return "Tổng lượt đề cập thiệt hại";
            case "mostSatisfied": return "Hài lòng nhất";
            case "mostDissatisfied": return "Không hài lòng nhất";
            default: {
                StringBuilder sb = new StringBuilder();
                boolean nextUpper = true;
                for (char c : key.toCharArray()) {
                    if (c == '_') { nextUpper = true; continue; }
                    if (nextUpper) { sb.append(Character.toUpperCase(c)); nextUpper = false; }
                    else sb.append(c);
                }
                return sb.toString();
            }
        }
    }

    private String formatValueForText(String key, Object value) {
        if (value == null) return "N/A";
        if ((key.endsWith("Ratio") || key.endsWith("ratio")) && value instanceof Number) {
            double pct = ((Number) value).doubleValue() * 100;
            return String.format("%.1f%%", pct);
        }
        if (value instanceof Double || value instanceof Float) {
            double d = ((Number) value).doubleValue();
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.format("%.0f", d);
            }
            return String.format("%.2f", d);
        }
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            return map.entrySet().stream()
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .collect(Collectors.joining(", "));
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return String.join("; ", list.stream().map(Object::toString).collect(Collectors.toList()));
        }
        return value.toString();
    }

    private String formatValueForHtml(String analysisType, String key, Object value) {
        if (value == null) return "N/A";
        if ((key.endsWith("Ratio") || key.endsWith("ratio")) && value instanceof Number) {
            double pct = ((Number) value).doubleValue() * 100;
            return String.format("%.1f%%", pct);
        }
        if (value instanceof Double || value instanceof Float) {
            double d = ((Number) value).doubleValue();
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.format("%.0f", d);
            }
            return String.format("%.2f", d);
        }
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            return map.entrySet().stream()
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .collect(Collectors.joining(", "));
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return String.join("; ", list.stream().map(Object::toString).collect(Collectors.toList()));
        }
        return value.toString();
    }

    private String wrapText(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) return text != null ? text : "";
        StringBuilder sb = new StringBuilder();
        String remaining = text;
        boolean first = true;
        while (remaining.length() > maxLen) {
            int breakPoint = maxLen;
            for (int i = maxLen; i > maxLen - 20 && i > 0; i--) {
                if (remaining.charAt(i) == ' ') { breakPoint = i; break; }
            }
            if (!first) sb.append("\n       ");
            else first = false;
            sb.append(remaining, 0, breakPoint);
            remaining = remaining.substring(breakPoint).trim();
        }
        if (!remaining.isEmpty()) {
            if (!first) sb.append("\n       ");
            sb.append(remaining);
        }
        return sb.toString();
    }
}
