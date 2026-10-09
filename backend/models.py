"""
PathPilot AI - NEXORA TECH
SQLite Database Schema & Initialization (pathpilot.db)
"""

import sqlite3
import os

DB_NAME = "pathpilot.db"

def get_db_connection():
    conn = sqlite3.connect(DB_NAME)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    conn = get_db_connection()
    cursor = conn.cursor()

    # 1. Students Table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS students (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        course_branch TEXT NOT NULL,
        year TEXT NOT NULL,
        career_goal TEXT NOT NULL,
        current_skills TEXT,
        daily_available_hours REAL DEFAULT 3.0,
        preferred_study_hours TEXT,
        target_daily_minutes INTEGER DEFAULT 180,
        bio TEXT
    );
    """)

    # 2. Tasks Table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS tasks (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        task_name TEXT NOT NULL,
        category TEXT NOT NULL,
        deadline_timestamp INTEGER NOT NULL,
        deadline_desc TEXT,
        importance INTEGER DEFAULT 3,
        duration_minutes INTEGER DEFAULT 60,
        career_value INTEGER DEFAULT 2,
        can_split INTEGER DEFAULT 1,
        priority_score REAL DEFAULT 50.0,
        status TEXT DEFAULT 'Pending',
        completed_at INTEGER,
        notes TEXT
    );
    """)

    # 3. Skills Roadmap Table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS skills (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        category TEXT NOT NULL,
        career_goal TEXT NOT NULL,
        step_order INTEGER DEFAULT 1,
        status TEXT DEFAULT 'Recommended',
        progress_percent INTEGER DEFAULT 0,
        description TEXT
    );
    """)

    # 4. Study Sessions Table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS study_sessions (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        task_id INTEGER,
        task_name TEXT NOT NULL,
        category TEXT NOT NULL,
        duration_minutes INTEGER NOT NULL,
        timestamp INTEGER NOT NULL,
        completed INTEGER DEFAULT 1,
        notes TEXT
    );
    """)

    # 5. Progress History Table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS progress (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        date_string TEXT NOT NULL,
        completed_count INTEGER DEFAULT 0,
        pending_count INTEGER DEFAULT 0,
        missed_count INTEGER DEFAULT 0,
        total_minutes INTEGER DEFAULT 0,
        productivity_score INTEGER DEFAULT 0
    );
    """)

    conn.commit()

    # Seed initial data if student table empty
    cursor.execute("SELECT COUNT(*) FROM students;")
    if cursor.fetchone()[0] == 0:
        cursor.execute("""
        INSERT INTO students (name, course_branch, year, career_goal, current_skills, daily_available_hours, preferred_study_hours)
        VALUES ('Aryan Sharma', 'B.Tech CSE', '2nd Year', 'Software Engineer', 'Python, Git, Basic C++', 3.0, 'Morning (9:00 AM - 12:00 PM)');
        """)

        # Sample initial tasks
        import time
        now_ms = int(time.time() * 1000)
        hour_ms = 3600 * 1000

        cursor.execute("""
        INSERT INTO tasks (task_name, category, deadline_timestamp, deadline_desc, importance, duration_minutes, career_value, can_split, priority_score, status)
        VALUES ('Complete Python assignment', 'Assignment', ?, 'Tomorrow 11:59 PM', 4, 120, 3, 1, 85.0, 'Pending'),
               ('Operating Systems exam preparation', 'Exam Prep', ?, 'In 2 days', 5, 60, 3, 1, 78.0, 'Pending'),
               ('Python practice & algorithms', 'Skill Practice', ?, 'This weekend', 3, 40, 4, 1, 58.0, 'Pending');
        """, (now_ms + 24 * hour_ms, now_ms + 48 * hour_ms, now_ms + 7 * 24 * hour_ms))

        # Sample initial skills
        skills_data = [
            ("Python Programming", "Core Language", "Software Engineer", 1, "Mastered", 100, "Core syntax & OOP"),
            ("Data Structures & Algorithms", "Core CS", "Software Engineer", 2, "In Progress", 65, "Trees, graphs, dynamic programming"),
            ("SQL & Relational Databases", "Backend", "Software Engineer", 3, "In Progress", 40, "Queries, schema design, joins"),
            ("Git & GitHub Collaboration", "Dev Tools", "Software Engineer", 4, "Mastered", 90, "Branching, PRs, version control"),
            ("Web Development Fundamentals", "Web", "Software Engineer", 5, "In Progress", 50, "HTTP protocols, REST APIs"),
            ("APIs & Microservices", "Architecture", "Software Engineer", 6, "Recommended", 15, "FastAPI/Flask API development"),
            ("AI / ML Basics & LLM APIs", "AI", "Software Engineer", 7, "In Progress", 35, "Gemini API integration"),
            ("Full-Stack Projects", "Practical", "Software Engineer", 8, "Recommended", 20, "End-to-end applications"),
            ("Technical Interview Preparation", "Career", "Software Engineer", 9, "Recommended", 10, "Mock rounds & STAR technique")
        ]
        cursor.executemany("""
        INSERT INTO skills (name, category, career_goal, step_order, status, progress_percent, description)
        VALUES (?, ?, ?, ?, ?, ?, ?);
        """, skills_data)

        conn.commit()

    conn.close()

if __name__ == "__main__":
    init_db()
    print("Database pathpilot.db initialized successfully!")
