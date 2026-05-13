package com.humanitarian.logistics.collector;

import java.util.HashMap;
import java.util.Map;

public class DataCollectorFactory {
    private static DataCollectorFactory instance;
    private final Map<String, DataCollector> collectors;

    private DataCollectorFactory() {
        collectors = new HashMap<>();
        registerDefaults();
    }

    public static synchronized DataCollectorFactory getInstance() {
        if (instance == null) {
            instance = new DataCollectorFactory();
        }
        return instance;
    }

    private void registerDefaults() {
        registerCollector("twitter", new MockDataCollector("Twitter"));
        registerCollector("facebook", new MockDataCollector("Facebook"));
        registerCollector("tiktok", new MockDataCollector("TikTok"));
        registerCollector("youtube", new MockDataCollector("YouTube"));
        registerCollector("mock", new MockDataCollector("Mock"));
    }

    public void registerCollector(String name, DataCollector collector) {
        collectors.put(name.toLowerCase(), collector);
    }

    public DataCollector getCollector(String name) {
        DataCollector collector = collectors.get(name.toLowerCase());
        if (collector == null) {
            throw new IllegalArgumentException("Unknown data source: " + name);
        }
        return collector;
    }

    public Map<String, DataCollector> getAllCollectors() {
        return new HashMap<>(collectors);
    }

    public void removeCollector(String name) {
        collectors.remove(name.toLowerCase());
    }
}
