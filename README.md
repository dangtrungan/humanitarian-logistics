# Humanitarian Logistics - Social Media Analyzer

![Version](https://img.shields.io/github/v/release/dangtrungan/humanitarian-logistics)

A dual-language (Java + Python) system that analyzes social media sentiment during natural disasters to inform and improve humanitarian response efforts.

Built around a **case study of Typhoon Yagi** (Bão Yagi / Bão số 3), which struck Northern Vietnam from September 6–30, 2024.

## Overview

The application ingests social media posts (currently via mock data), preprocesses text with Vietnamese-language awareness, runs four distinct analyses, and visualizes results through a Swing desktop GUI or headless batch mode. A companion Flask API can optionally replace the built-in keyword-based sentiment engine.

### Pipeline

```
Data Collection → Preprocessing → Analysis → Storage / Export / Visualization
```

| Stage | Components |
|---|---|
| **Collection** | Mock data for Twitter, Facebook, TikTok, YouTube |
| **Preprocessing** | Basic (URL/mention/hashtag removal) and Advanced (Vietnamese stopwords, accent normalization) |
| **Analysis** | Four analysis modules (see below) |
| **Storage** | CSV and JSON output to `data/output/` |
| **Export** | PNG charts via JFreeChart, TXT/HTML reports |
| **UI** | Swing desktop GUI with 8 tabbed panels |

### The Four Analyses

| # | Analysis | Question |
|---|---|---|
| 1 | **Sentiment Timeline** | How does public sentiment evolve over the course of the disaster? |
| 2 | **Damage Classification** | What types of damage (housing, infrastructure, etc.) are most discussed? |
| 3 | **Relief Satisfaction** | Which relief efforts satisfy or dissatisfy the public? |
| 4 | **Relief Timeline** | How does sentiment for each relief category change over time? |

## Project Structure

```
src/main/java/com/humanitarian/logistics/
├── Main.java                        # Entry point (GUI or batch)
├── config/
│   └── AppConfig.java               # Singleton configuration manager
├── model/
│   ├── Post.java                    # Social media post data model
│   ├── SentimentResult.java         # Sentiment scores and labels
│   ├── DamageReport.java            # Damage classification data
│   ├── ReliefFeedback.java          # Relief satisfaction data
│   ├── AnalysisResult.java          # Generic analysis result
│   └── AnalysisConfig.java          # Full analysis configuration
├── collector/
│   ├── DataCollector.java           # Collection interface
│   ├── DataCollectorFactory.java    # Factory for collectors
│   └── MockDataCollector.java       # Mock implementation (34 templates)
├── preprocessor/
│   ├── TextPreprocessor.java        # Preprocessing interface
│   ├── BasicTextPreprocessor.java   # URL/mention/hashtag removal
│   ├── AdvancedTextPreprocessor.java # Vietnamese stopwords, accent normalization
│   └── PreprocessorPipeline.java    # Chains multiple preprocessors
├── analyzer/
│   ├── Analyzer.java                # Analysis interface
│   ├── SentimentAnalyzer.java       # Sentiment analysis interface
│   ├── LocalSentimentAnalyzer.java  # Keyword-based sentiment (Vietnamese)
│   ├── SentimentTimelineAnalyzer.java   # Problem 1
│   ├── DamageAnalyzer.java              # Problem 2
│   ├── ReliefSatisfactionAnalyzer.java  # Problem 3
│   ├── ReliefTimelineAnalyzer.java      # Problem 4
│   ├── AnalyzerFactory.java         # Factory registering all analyzers
│   └── AnalysisEngine.java          # Orchestrates the full pipeline
├── storage/
│   ├── DataStore.java               # Persistence interface
│   ├── CsvDataStore.java            # CSV output
│   └── JsonDataStore.java           # JSON output (Gson)
├── apiclient/
│   ├── SentimentApiClient.java      # HTTP client for Python API
│   └── PythonSentimentAdapter.java  # Adapts Python API to SentimentAnalyzer
├── export/
│   ├── ChartGenerator.java          # JFreeChart PNG export
│   └── ReportExporter.java          # TXT/HTML report export
└── ui/
    ├── MainFrame.java               # Main window with tabs
    ├── DashboardPanel.java          # Overview stats
    ├── ConfigPanel.java             # Configuration editor
    ├── CollectorControlPanel.java   # Pipeline controls + progress
    ├── SentimentTimelinePanel.java  # Problem 1 view
    ├── DamageAnalysisPanel.java     # Problem 2 view
    ├── ReliefSatisfactionPanel.java # Problem 3 view
    ├── ReliefTimelinePanel.java     # Problem 4 view
    └── LogPanel.java                # Log viewer

python-api/
├── app.py                           # Flask REST API server
├── sentiment_model.py               # Sentiment/damage/relief analysis
└── requirements.txt                 # Python dependencies

data/
├── sample/yagi_sample_posts.json    # 16 hand-written sample posts
├── charts/                          # Generated PNG charts
├── output/                          # Generated CSV/JSON results
└── reports/                         # Generated TXT/HTML reports
```

## Quick Start

### Prerequisites

- Java 17+ (OpenJDK recommended)
- Maven 3.6+
- Python 3.9+ (optional, for Python API)

### Build and Run (GUI)

```bash
mvn clean package
java -jar target/humanitarian-logistics-1.0.0.jar
```

### Run (Batch Mode)

```bash
java -jar target/humanitarian-logistics-1.0.0.jar --batch
```

### Run with Python Sentiment API

```bash
# Terminal 1: Start the Flask API
cd python-api
pip install -r requirements.txt
python app.py

# Terminal 2: Run Java with Python integration
java -jar target/humanitarian-logistics-1.0.0.jar --use-python-api
```

### Custom Configuration

```bash
java -jar target/humanitarian-logistics-1.0.0.jar --config=path/to/config.json
```

## Configuration

Default configuration is for Typhoon Yagi (Vietnam, Sep 2024) and includes:

- **Damage categories**: people affected, economic disruption, housing, personal property, infrastructure, environment
- **Relief categories**: housing, transportation, food, medical, cash
- **Keywords**: bão yagi, siêu bão, bão số 3, lũ lụt, sạt lở, cứu trợ, thiệt hại, etc.
- **Data sources**: Twitter, Facebook, TikTok, YouTube (all mocked)

Config can be edited at runtime via the `ConfigPanel` tab or loaded from a JSON file.

## Tech Stack

### Java (core engine)

| Dependency | Purpose |
|---|---|
| Gson 2.10.1 | JSON serialization |
| JFreeChart 1.5.4 | Chart generation (bar, line, pie) |
| Commons CSV 1.10.0 | CSV output |

### Python (optional API)

Flask, TextBlob, NLTK, VADER, Transformers, scikit-learn, pandas, numpy

*Note: the current Python implementation uses keyword-based logic matching the Java `LocalSentimentAnalyzer`. The heavier ML dependencies are available for extension.*

## Architecture & Design Patterns

- **Singleton**: `AppConfig`, `DataCollectorFactory`, `AnalyzerFactory`
- **Factory**: Collector and analyzer registration/retrieval by name
- **Strategy/Adapter**: `SentimentAnalyzer` interface with local or Python backends
- **Pipeline**: `PreprocessorPipeline` chains preprocessing steps
- **Template Method**: `AnalysisEngine` orchestrates the full collect → preprocess → analyze → export flow

## Data

The `MockDataCollector` generates reproducible posts from 34 Vietnamese-language templates. 16 hand-written sample posts are also available in `data/sample/yagi_sample_posts.json`. All mock data uses seed 42 for deterministic output.

## Output

All generated artifacts go to `data/`:

- `data/output/` — CSV and JSON analysis results
- `data/charts/` — PNG bar charts, line charts, and pie charts
- `data/reports/` — TXT and HTML formatted reports

## License

MIT — see [LICENSE](LICENSE).

## Author

Dang Trung An
