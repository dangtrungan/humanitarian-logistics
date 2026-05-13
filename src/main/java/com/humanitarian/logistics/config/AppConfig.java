package com.humanitarian.logistics.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.humanitarian.logistics.model.AnalysisConfig;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AppConfig {
    private static AppConfig instance;
    private AnalysisConfig analysisConfig;
    private Map<String, String> apiKeys;
    private Map<String, String> apiEndpoints;
    private Properties properties;
    private static final String CONFIG_DIR = "src/main/resources/config/";

    private AppConfig() {
        this.analysisConfig = new AnalysisConfig();
        this.apiKeys = new HashMap<>();
        this.apiEndpoints = new HashMap<>();
        this.properties = new Properties();
        loadDefaults();
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private void loadDefaults() {
        analysisConfig.setDisasterName("Bão Yagi");
        analysisConfig.setDisasterType("Typhoon");
        analysisConfig.setStartDate("2024-09-06");
        analysisConfig.setEndDate("2024-09-30");
        analysisConfig.setLanguage("vi");

        analysisConfig.getKeywords().addAll(Arrays.asList(
            "bão Yagi", "bão số 3", "Yagi", "typhoon Yagi",
            "cứu trợ", "thiên tai", "lũ lụt", "sạt lở",
            "khắc phục hậu quả", "cứu hộ", "cứu nạn"
        ));

        analysisConfig.getHashtags().addAll(Arrays.asList(
            "#baoyagi", "#baoso3", "#Yagi", "#typhoonYagi",
            "#cuutro", "#thientai", "#khacphuc"
        ));

        analysisConfig.getDataSources().addAll(Arrays.asList("twitter", "facebook", "tiktok", "youtube"));

        analysisConfig.getSelectedAnalyzers().addAll(Arrays.asList(
            "sentiment_timeline", "damage_analysis", "relief_satisfaction", "relief_timeline"
        ));

        Map<String, List<String>> damageCats = analysisConfig.getDamageCategories();
        damageCats.put("Người bị ảnh hưởng", Arrays.asList("tử vong", "bị thương", "mất tích", "sơ tán"));
        damageCats.put("Gián đoạn hoạt động kinh tế", Arrays.asList("nhà máy", "nông nghiệp", "kinh doanh", "sản xuất"));
        damageCats.put("Nhà cửa/tòa nhà hư hỏng", Arrays.asList("sập nhà", "tốc mái", "ngập nhà", "hư hại"));
        damageCats.put("Tài sản cá nhân", Arrays.asList("mất tài sản", "xe cộ", "đồ đạc"));
        damageCats.put("Cơ sở hạ tầng", Arrays.asList("đường sá", "cầu", "điện", "nước", "viễn thông"));
        damageCats.put("Môi trường", Arrays.asList("cây đổ", "ô nhiễm", "ngập úng"));

        Map<String, List<String>> reliefCats = analysisConfig.getReliefCategories();
        reliefCats.put("Nhà ở", Arrays.asList("nhà tạm", "sửa nhà", "chỗ ở"));
        reliefCats.put("Giao thông", Arrays.asList("đường sá", "cầu", "di chuyển", "tắc đường"));
        reliefCats.put("Thực phẩm", Arrays.asList("đồ ăn", "nước uống", "lương thực", "thực phẩm"));
        reliefCats.put("Hỗ trợ y tế", Arrays.asList("bệnh viện", "thuốc", "cấp cứu", "y tế"));
        reliefCats.put("Tiền mặt", Arrays.asList("hỗ trợ tài chính", "tiền", "quyên góp", "bồi thường"));
    }

    public AnalysisConfig getAnalysisConfig() { return analysisConfig; }

    public String getApiKey(String source) { return apiKeys.getOrDefault(source, ""); }
    public void setApiKey(String source, String key) { apiKeys.put(source, key); }

    public String getApiEndpoint(String service) { return apiEndpoints.getOrDefault(service, "http://localhost:5000"); }
    public void setApiEndpoint(String service, String endpoint) { apiEndpoints.put(service, endpoint); }

    public void loadFromJson(String filePath) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Reader reader = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8)) {
            AnalysisConfig loaded = gson.fromJson(reader, AnalysisConfig.class);
            if (loaded != null) this.analysisConfig = loaded;
        }
    }

    public void saveToJson(String filePath) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
            gson.toJson(analysisConfig, writer);
        }
    }

    public void loadProperties(String filePath) throws IOException {
        try (InputStream is = new FileInputStream(filePath)) {
            properties.load(is);
        }
    }
}
