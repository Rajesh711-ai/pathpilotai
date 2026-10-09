# PathPilot AI 🚀
**Personalized Student Life & Career Guidance Platform with Adaptive AI Planner**  
*by NEXORA TECH*

[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Jetpack%20Compose-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)
[![AI](https://img.shields.io/badge/AI-Google%20Gemini-orange.svg)](https://ai.google.dev/)

---

## 🌟 Overview

**PathPilot AI** is an intelligent student productivity and career guidance platform designed specifically for college students balancing coursework, coding practice, exam preparation, and personal growth.

Unlike traditional static todo lists or calendar apps, PathPilot AI features a **dynamic, mathematically grounded adaptive scheduling engine** paired with **Google Gemini AI**. It continuously monitors academic deadlines, available daily study hours, career aspirations, and task completion states to answer the fundamental student question:

> **"What should I do right now, and why?"**

---

## 📱 Live Web Preview & Public Access

- **Public Shared App URL:**  
  [https://ais-pre-map4oaarqgiyf63awjlugz-270839861885.asia-east1.run.app](https://ais-pre-map4oaarqgiyf63awjlugz-270839861885.asia-east1.run.app)

*(Open the link above in any browser to test the interactive streaming Android emulator.)*

---

## ✨ Key Features

### 1. 🎯 Adaptive AI Schedule Engine
- **Deterministic Priority Formula**: Weights tasks based on urgency, academic importance, long-term career value, and remaining workload chunks.
- **Dynamic Time Budgeting**: Allocate daily study hours (e.g., 2–8 hrs/day). The engine fills available time blocks with high-priority tasks and marks low-priority tasks for tomorrow when time runs out.
- **One-Tap Quick Actions**:
  - **Finished Early**: Frees up time and pulls the next scheduled block forward.
  - **Mark Missed**: Automatically recalculates urgency and re-schedules the task into the next viable slot with priority boost.
  - **Mark Done**: Logs study minutes, updates daily streak, and recalculates real-time schedule.

### 2. 🔔 Android Status Bar & Notification Shade Integration
- **Live Focus Block**: Pins the currently active study block directly into the Android notification bar.
- **Deep Explanations**: Displays the AI rationale ("Why this?") and remaining time directly in the expandable notification.
- **Direct Notification Actions**: Tap "Mark Done" directly from the notification shade without needing to switch back into the app.

### 3. 🤖 AI Student Guidance (Powered by Google Gemini)
- **Schedule Explanations**: Plain-English breakdowns of why tasks were scheduled in a specific order.
- **Career Strategy Mentor**: Guidance on technical milestones (DSA, Web Dev, System Design, AI/ML) tailored to target roles (e.g., Full Stack Engineer, Cloud Architect, Data Scientist).
- **Graceful Fallbacks**: Intelligent heuristic fallback engine ensures reliable planning even when offline.

### 4. 🗺️ Career Skill Roadmaps & Analytics
- Progress tracking across essential skills (LeetCode/DSA, System Design, Cloud & DevOps, Projects).
- Track daily study streaks, total study minutes, completed deadlines, and overall career readiness.

---

## 🏗️ Architecture & Tech Stack

### Android Client
- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **Architecture**: MVVM + Clean Architecture with Coroutines & StateFlow
- **Local Persistence**: Room SQLite (`pathpilot.db`) with reactive DAOs
- **System Integration**: Android Notification Manager (`NotificationCompat.BigTextStyle`), BroadcastReceivers for background actions
- **AI Integration**: Google Gemini API via Kotlin Coroutines & REST client with heuristic backup

### Backend Service (Optional REST API)
- **Framework**: Python Flask 3.0
- **Database**: SQLite
- **Endpoints**: Student profiles, task CRUD, adaptive schedule generation, Gemini AI guidance

---

## 🚀 How to Build & Run

### Android App
```bash
# Clone the repository
git clone https://github.com/<your-username>/pathpilot-ai.git
cd pathpilot-ai

# Build the debug APK
gradle :app:assembleDebug

# Run unit tests
gradle :app:testDebugUnitTest
```

### Python Backend
```bash
cd backend
pip install -r requirements.txt
python app.py
```

---

## 🔒 Security & Privacy
- Zero tracking or unnecessary permissions.
- Local-first architecture using Room database ensures student schedule data remains completely private on-device.

---

## 📄 License
This project is open-source under the [MIT License](LICENSE).  
Developed with ❤️ by **NEXORA TECH**.
