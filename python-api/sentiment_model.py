"""
Sentiment Analysis Model - Python module for humanitarian logistics.
Can be easily swapped with Java-based implementation via API abstraction.
"""

import re
import math
from typing import Dict, List, Tuple


class SentimentModel:
    def __init__(self, model_type="vader"):
        self.model_type = model_type
        self.positive_words = self._load_positive_words()
        self.negative_words = self._load_negative_words()

    def analyze(self, text: str) -> Dict:
        text_lower = text.lower()
        pos_score = self._calc_score(text_lower, self.positive_words)
        neg_score = self._calc_score(text_lower, self.negative_words)

        total = pos_score + neg_score

        if total == 0:
            return {"positive": 0.0, "negative": 0.0, "neutral": 1.0, "label": "NEUTRAL"}

        pos = pos_score / total
        neg = neg_score / total

        if pos > neg + 0.1:
            label = "POSITIVE"
        elif neg > pos + 0.1:
            label = "NEGATIVE"
        else:
            label = "NEUTRAL"

        return {
            "positive": round(pos, 4),
            "negative": round(neg, 4),
            "neutral": round(1.0 - min(total, 1.0), 4),
            "label": label
        }

    def analyze_batch(self, texts: List[str]) -> List[Dict]:
        return [self.analyze(t) for t in texts]

    def classify_damage(self, text: str, categories: Dict[str, List[str]]) -> Dict:
        text_lower = text.lower()
        results = {}
        for category, keywords in categories.items():
            for kw in keywords:
                if kw.lower() in text_lower:
                    results[category] = results.get(category, 0) + 1
                    break
        return results

    def analyze_relief_sentiment(self, text: str, relief_categories: Dict[str, List[str]]) -> Dict:
        text_lower = text.lower()
        sentiment = self.analyze(text)
        matched_categories = []

        for category, keywords in relief_categories.items():
            for kw in keywords:
                if kw.lower() in text_lower:
                    matched_categories.append(category)
                    break

        return {
            "sentiment": sentiment,
            "matched_categories": matched_categories
        }

    def _calc_score(self, text: str, word_dict: Dict[str, float]) -> float:
        score = 0.0
        for word, weight in word_dict.items():
            if word in text:
                score += weight
        return min(score, 1.0)

    def _load_positive_words(self) -> Dict[str, float]:
        return {
            "cam on": 0.8, "tuyet voi": 0.9, "doan ket": 0.7,
            "giup do": 0.6, "kip thoi": 0.7, "kham phuc": 0.8,
            "hao tam": 0.7, "khan truong": 0.5, "khen ngoi": 0.8,
            "ho tro": 0.5, "hy vong": 0.6, "phuc hoi": 0.5,
            "tot": 0.6, "hieu qua": 0.7, "thanh cong": 0.8,
            "hai long": 0.7, "an toan": 0.6, "vui mung": 0.8,
            "biet on": 0.9, "tich cuc": 0.6, "on dinh": 0.5,
            "du": 0.4, "good": 0.6, "thank": 0.8, "great": 0.8,
            "help": 0.5, "hope": 0.6, "grateful": 0.8, "excellent": 0.9
        }

    def _load_negative_words(self) -> Dict[str, float]:
        return {
            "sap": 0.8, "pha huy": 0.9, "thiet hai": 0.7,
            "ngap": 0.6, "sat lo": 0.8, "te liet": 0.7,
            "kho khan": 0.6, "chet": 0.9, "tu vong": 0.9,
            "thuong": 0.7, "mat tich": 0.8, "cham": 0.5,
            "khong cong bang": 0.7, "thieu": 0.6, "nghiem trong": 0.6,
            "lo lang": 0.6, "so": 0.7, "buc xuc": 0.8,
            "that vong": 0.8, "khung khiep": 0.9, "co lap": 0.6,
            "mat": 0.5, "nguy hiem": 0.7, "dau": 0.7,
            "bad": 0.6, "terrible": 0.8, "awful": 0.8,
            "damage": 0.6, "destroy": 0.8, "failure": 0.7
        }
