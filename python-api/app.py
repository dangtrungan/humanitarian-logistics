"""
Python REST API for Humanitarian Logistics Sentiment Analysis.
Provides endpoints that the Java desktop application calls.
To swap with a Java model, simply implement the same API contract.
"""

from flask import Flask, request, jsonify, make_response
from flask_cors import CORS
from sentiment_model import SentimentModel
import json

app = Flask(__name__)
CORS(app)

model = SentimentModel(model_type="vader")

DAMAGE_CATEGORIES = {
    "Nguoi bi anh huong": ["tu vong", "bi thuong", "mat tich", "so tan"],
    "Gian doan hoat dong kinh te": ["nha may", "nong nghiep", "kinh doanh", "san xuat"],
    "Nha cua/toa nha hu hong": ["sup nha", "toc mai", "ngap nha", "hu hai"],
    "Tai san ca nhan": ["mat tai san", "xe co", "do dac"],
    "Co so ha tang": ["duong sa", "cau", "dien", "nuoc", "vien thong"],
    "Moi truong": ["cay do", "o nhiem", "ngap ung"]
}

RELIEF_CATEGORIES = {
    "Nha o": ["nha tam", "sua nha", "cho o"],
    "Giao thong": ["duong sa", "cau", "di chuyen", "tac duong"],
    "Thuc pham": ["do an", "nuoc uong", "luong thuc", "thuc pham"],
    "Ho tro y te": ["benh vien", "thuoc", "cap cuu", "y te"],
    "Tien mat": ["ho tro tai chinh", "tien", "quyen gop", "boi thuong"]
}


@app.route("/api/health", methods=["GET"])
def health_check():
    return jsonify({"status": "ok", "model": model.model_type})


@app.route("/api/sentiment/analyze", methods=["POST"])
def analyze_sentiment():
    """
    Input:  {"text": "string"} or {"texts": ["string", ...]}
    Output: {
        "results": [{"positive": float, "negative": float, "neutral": float, "label": "POSITIVE|NEGATIVE|NEUTRAL"}],
        "model": "vader"
    }
    """
    data = request.get_json()
    if not data:
        return jsonify({"error": "No data provided"}), 400

    if "text" in data:
        result = model.analyze(data["text"])
        return jsonify({"results": [result], "model": model.model_type})
    elif "texts" in data:
        results = model.analyze_batch(data["texts"])
        return jsonify({"results": results, "model": model.model_type})
    else:
        return jsonify({"error": "Provide 'text' or 'texts' field"}), 400


@app.route("/api/sentiment/analyze-posts", methods=["POST"])
def analyze_posts():
    """
    Input:  {"posts": [{"id": "...", "content": "..."}, ...]}
    Output: {"results": [{"postId": "...", "positive": float, "negative": float, ...}], "model": "..."}
    """
    data = request.get_json()
    if not data or "posts" not in data:
        return jsonify({"error": "Provide 'posts' array"}), 400

    results = []
    texts = [p.get("content", "") for p in data["posts"]]
    sentiments = model.analyze_batch(texts)

    for i, post in enumerate(data["posts"]):
        results.append({
            "postId": post.get("id", str(i)),
            **sentiments[i]
        })

    return jsonify({"results": results, "model": model.model_type})


@app.route("/api/damage/classify", methods=["POST"])
def classify_damage():
    """
    Input:  {"text": "string"} or {"texts": ["string", ...]}
            Optional: "categories" to override default damage categories
    Output: {"results": [{"text": "...", "damageCategories": {...}}, ...]}
    """
    data = request.get_json()
    if not data:
        return jsonify({"error": "No data provided"}), 400

    categories = data.get("categories", DAMAGE_CATEGORIES)

    if "text" in data:
        result = model.classify_damage(data["text"], categories)
        return jsonify({"results": [{"text": data["text"], "damageCategories": result}]})

    elif "texts" in data:
        results = []
        for text in data["texts"]:
            result = model.classify_damage(text, categories)
            results.append({"text": text, "damageCategories": result})
        return jsonify({"results": results})

    else:
        return jsonify({"error": "Provide 'text' or 'texts' field"}), 400


@app.route("/api/relief/analyze", methods=["POST"])
def analyze_relief():
    """
    Input:  {"text": "string"} or {"texts": ["string", ...]}
    Output: {"results": [{"sentiment": {...}, "matchedCategories": [...]}], ...}
    """
    data = request.get_json()
    if not data:
        return jsonify({"error": "No data provided"}), 400

    categories = data.get("categories", RELIEF_CATEGORIES)

    if "text" in data:
        result = model.analyze_relief_sentiment(data["text"], categories)
        return jsonify({"results": [result]})

    elif "texts" in data:
        results = []
        for text in data["texts"]:
            result = model.analyze_relief_sentiment(text, categories)
            results.append(result)
        return jsonify({"results": results})

    else:
        return jsonify({"error": "Provide 'text' or 'texts' field"}), 400


@app.route("/api/model/switch", methods=["POST"])
def switch_model():
    """Switch model type at runtime - demonstrates easy model swapping."""
    data = request.get_json()
    if not data or "model_type" not in data:
        return jsonify({"error": "Provide 'model_type'"}), 400

    global model
    new_type = data["model_type"]
    model = SentimentModel(model_type=new_type)
    return jsonify({"status": "ok", "model": new_type})


@app.route("/api/config/categories", methods=["GET"])
def get_categories():
    return jsonify({
        "damageCategories": DAMAGE_CATEGORIES,
        "reliefCategories": RELIEF_CATEGORIES
    })


@app.errorhandler(404)
def not_found(e):
    return jsonify({"error": "Not found"}), 404


@app.errorhandler(500)
def server_error(e):
    return jsonify({"error": "Internal server error"}), 500


if __name__ == "__main__":
    print("Humanitarian Logistics Sentiment API starting...")
    print("Endpoints:")
    print("  GET  /api/health")
    print("  POST /api/sentiment/analyze")
    print("  POST /api/sentiment/analyze-posts")
    print("  POST /api/damage/classify")
    print("  POST /api/relief/analyze")
    print("  POST /api/model/switch")
    print("  GET  /api/config/categories")
    app.run(host="0.0.0.0", port=5000, debug=True)
