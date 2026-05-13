package com.humanitarian.logistics.ui;

import com.humanitarian.logistics.analyzer.AnalysisEngine;
import com.humanitarian.logistics.config.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainFrame extends JFrame {
    private final AnalysisEngine engine;
    private final AppConfig config;
    private final JTabbedPane tabbedPane;
    private final DashboardPanel dashboardPanel;
    private final SentimentTimelinePanel sentimentTimelinePanel;
    private final DamageAnalysisPanel damageAnalysisPanel;
    private final ReliefSatisfactionPanel reliefSatisfactionPanel;
    private final ReliefTimelinePanel reliefTimelinePanel;
    private final LogPanel logPanel;
    private final ConfigPanel configPanel;
    private final CollectorControlPanel controlPanel;

    public MainFrame() {
        super("Humanitarian Logistics - Social Media Analyzer");
        this.engine = new AnalysisEngine();
        this.config = AppConfig.getInstance();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Dialog", Font.BOLD, 12));

        dashboardPanel = new DashboardPanel(engine);
        sentimentTimelinePanel = new SentimentTimelinePanel(engine);
        damageAnalysisPanel = new DamageAnalysisPanel(engine);
        reliefSatisfactionPanel = new ReliefSatisfactionPanel(engine);
        reliefTimelinePanel = new ReliefTimelinePanel(engine);
        logPanel = new LogPanel(engine);
        configPanel = new ConfigPanel(config);
        controlPanel = new CollectorControlPanel(engine, this);

        tabbedPane.addTab("Tổng quan", createIcon("📊"), dashboardPanel, "Dashboard overview");
        tabbedPane.addTab("Điều khiển", createIcon("▶"), controlPanel, "Run analysis pipeline");
        tabbedPane.addTab("Tâm lý theo thời gian", createIcon("📈"), sentimentTimelinePanel, "Bài toán 1");
        tabbedPane.addTab("Phân loại thiệt hại", createIcon("🏚"), damageAnalysisPanel, "Bài toán 2");
        tabbedPane.addTab("Hài lòng hàng cứu trợ", createIcon("📦"), reliefSatisfactionPanel, "Bài toán 3");
        tabbedPane.addTab("Tâm lý cứu trợ theo thời gian", createIcon("⏱"), reliefTimelinePanel, "Bài toán 4");
        tabbedPane.addTab("Log", createIcon("📋"), logPanel, "System logs");
        tabbedPane.addTab("Cấu hình", createIcon("⚙"), configPanel, "Configuration");

        add(tabbedPane, BorderLayout.CENTER);

        JMenuBar menuBar = createMenuBar();
        setJMenuBar(menuBar);

        JLabel statusBar = new JLabel(" Sẵn sàng | Bão Yagi (06/09/2024 - 30/09/2024)");
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        statusBar.setFont(new Font("Dialog", Font.PLAIN, 11));
        add(statusBar, BorderLayout.SOUTH);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem loadConfig = new JMenuItem("Load Configuration");
        loadConfig.addActionListener(e -> loadConfigFromFile());
        JMenuItem saveConfig = new JMenuItem("Save Configuration");
        saveConfig.addActionListener(e -> saveConfigToFile());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(loadConfig);
        fileMenu.add(saveConfig);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu runMenu = new JMenu("Run");
        JMenuItem runFull = new JMenuItem("Run Full Pipeline");
        runFull.addActionListener(e -> controlPanel.runPipeline());
        JMenuItem runCollect = new JMenuItem("Collect Data Only");
        runCollect.addActionListener(e -> controlPanel.runCollection());
        runMenu.add(runFull);
        runMenu.add(runCollect);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Humanitarian Logistics Social Media Analyzer\n" +
            "Phân tích dữ liệu mạng xã hội cho logistics nhân đạo\n" +
            "Version 1.0.0", "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(runMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private void loadConfigFromFile() {
        JFileChooser chooser = new JFileChooser("config");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                config.loadFromJson(chooser.getSelectedFile().getAbsolutePath());
                configPanel.refresh();
                JOptionPane.showMessageDialog(this, "Configuration loaded.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveConfigToFile() {
        JFileChooser chooser = new JFileChooser("config");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                config.saveToJson(chooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Configuration saved.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Icon createIcon(String emoji) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                g.drawString(emoji, x, y + 14);
            }
            @Override
            public int getIconWidth() { return 20; }
            @Override
            public int getIconHeight() { return 16; }
        };
    }

    public void refreshAllPanels() {
        dashboardPanel.refresh();
        sentimentTimelinePanel.refresh();
        damageAnalysisPanel.refresh();
        reliefSatisfactionPanel.refresh();
        reliefTimelinePanel.refresh();
        logPanel.refresh();
    }
}
