# PathPilot AI
**Organization:** NEXORA TECH  
**Tagline:** Your AI-powered guide to better learning and better careers.

---

## 1. Problem Statement
College students struggle to juggle multiple overlapping commitments:
- Academic deadlines & assignments
- Exam preparation & revision
- Daily routines & personal tasks
- Technical skill development & coding practice (DSA, Web, AI)
- Long-term career goal alignment

Standard calendar and todo applications are static—they record dates, but do not understand workload trade-offs, priority calculations, or how to continuously adapt when deadlines change or tasks are missed.

## 2. Solution: PathPilot AI
PathPilot AI introduces an **Adaptive AI Planner** that combines:
1. **Deterministic Priority & Scheduling Engine**: Reliable calculations using deadlines, importance, career value, and splittability.
2. **Automatic Rescheduler**: Instantly detects missed or early-finished tasks, calculates urgency deltas, and re-balances the timetable.
3. **AI Student Guidance (Gemini)**: Explains the rationale behind schedule decisions and mentors the student on career milestones.
4. **Career Skill Roadmaps**: Progress tracking towards roles like Software Engineer, AI/ML Engineer, etc.

---

## 3. Core Deterministic Priority Formula

$$\text{Priority} = \text{Urgency} + \text{Importance} + \text{Career Value} + \text{Remaining Work}$$

- **Urgency (0–50 pts):**
  - Overdue: 50 pts
  - < 12 hours: 45 pts
  - < 24 hours: 35 pts
  - < 48 hours: 25 pts
  - < 72 hours: 18 pts
  - < 1 week: 10 pts
- **Importance (5–25 pts):** $1\text{ to }5 \times 5\text{ pts}$
- **Career Value (5–20 pts):** $1\text{ to }4 \times 5\text{ pts}$
- **Remaining Work (6–12 pts):** Rewards manageable chunks and tasks marked "can be split"
- **Missed Task Penalty:** Boosted with $+55\text{ pts}$ to recover lost momentum immediately.

---

## 4. Technology Stack

### Android App (Primary Interactive System)
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material Design 3
- **Local Persistence:** Room SQLite (`pathpilot.db`)
- **Architecture:** MVVM + Clean Architecture with Coroutines & StateFlow
- **AI Integration:** Google Gemini API (`gemini-3.5-flash`) with heuristic fallback

### Python Backend (REST API Service)
- **Framework:** Flask 3.0
- **Database:** SQLite (`pathpilot.db`)
- **AI:** Google Generative AI SDK (`gemini-1.5-flash`)
- **Endpoints:**
  - `GET/POST /api/student`: Student profile & available study time
  - `GET/POST /api/tasks`: Smart tasks CRUD with priority scoring
  - `POST /api/tasks/<id>/complete`: Complete task & log study session
  - `POST /api/tasks/<id>/missed`: Auto-rescheduling trigger
  - `GET /api/schedule`: Adaptive daily timetable
  - `GET/POST /api/skills`: Career skill roadmap & progress
  - `POST /api/ai-guidance`: Gemini student guidance & explanations

---

## 5. How to Run

### Running the Android App
The Android application builds with standard Gradle:
```bash
# In the project root:
gradle :app:assembleDebug
```

### Running the Python Backend
```bash
cd backend
pip install -r requirements.txt
python app.py
```
Server runs on `http://127.0.0.1:5000`.
