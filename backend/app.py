"""
PathPilot AI - NEXORA TECH
Flask REST API Server
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import sqlite3
import time
from models import init_db, get_db_connection
from planner import calculate_priority_score, generate_adaptive_schedule
from ai_guidance import get_student_guidance

app = Flask(__name__)
CORS(app)

# Initialize database on startup
init_db()

@app.route('/api/student', methods=['GET', 'POST'])
def handle_student():
    conn = get_db_connection()
    cursor = conn.cursor()

    if request.method == 'GET':
        cursor.execute("SELECT * FROM students LIMIT 1;")
        row = cursor.fetchone()
        conn.close()
        return jsonify(dict(row) if row else {})

    elif request.method == 'POST':
        data = request.json
        cursor.execute("""
        UPDATE students SET 
            name = ?, course_branch = ?, year = ?, career_goal = ?, 
            current_skills = ?, daily_available_hours = ?, preferred_study_hours = ?
        WHERE id = 1;
        """, (
            data.get('name'), data.get('course_branch'), data.get('year'),
            data.get('career_goal'), data.get('current_skills'),
            data.get('daily_available_hours', 3.0), data.get('preferred_study_hours')
        ))
        conn.commit()
        conn.close()
        return jsonify({"status": "success", "message": "Profile updated"})

@app.route('/api/tasks', methods=['GET', 'POST'])
def handle_tasks():
    conn = get_db_connection()
    cursor = conn.cursor()

    if request.method == 'GET':
        cursor.execute("SELECT * FROM tasks ORDER BY priority_score DESC;")
        rows = cursor.fetchall()
        tasks = [dict(r) for r in rows]
        conn.close()
        return jsonify(tasks)

    elif request.method == 'POST':
        data = request.json
        now_ms = int(time.time() * 1000)
        deadline_ms = data.get('deadline_timestamp', now_ms + 86400 * 1000)

        task_dict = {
            'deadline_timestamp': deadline_ms,
            'importance': data.get('importance', 3),
            'career_value': data.get('career_value', 2),
            'can_split': data.get('can_split', True),
            'duration_minutes': data.get('duration_minutes', 60),
            'status': 'Pending'
        }
        score = calculate_priority_score(task_dict, now_ms)

        cursor.execute("""
        INSERT INTO tasks (task_name, category, deadline_timestamp, deadline_desc, importance, duration_minutes, career_value, can_split, priority_score, status, notes)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Pending', ?);
        """, (
            data.get('task_name'), data.get('category', 'Assignment'),
            deadline_ms, data.get('deadline_desc', 'Tomorrow'),
            data.get('importance', 3), data.get('duration_minutes', 60),
            data.get('career_value', 2), 1 if data.get('can_split', True) else 0,
            score, data.get('notes', '')
        ))
        conn.commit()
        task_id = cursor.lastrowid
        conn.close()
        return jsonify({"status": "success", "task_id": task_id, "priority_score": score})

@app.route('/api/tasks/<int:task_id>/complete', methods=['POST'])
def complete_task(task_id):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("UPDATE tasks SET status = 'Completed', completed_at = ? WHERE id = ?;", (int(time.time() * 1000), task_id))
    conn.commit()
    conn.close()
    return jsonify({"status": "success", "message": f"Task {task_id} marked completed"})

@app.route('/api/tasks/<int:task_id>/missed', methods=['POST'])
def mark_missed(task_id):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("UPDATE tasks SET status = 'Missed' WHERE id = ?;", (task_id,))
    conn.commit()
    conn.close()
    return jsonify({"status": "success", "message": f"Task {task_id} marked missed - Automatic Rescheduling triggered"})

@app.route('/api/schedule', methods=['GET'])
def get_schedule():
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("SELECT * FROM students LIMIT 1;")
    student = dict(cursor.fetchone())

    cursor.execute("SELECT * FROM tasks WHERE status != 'Completed';")
    tasks = [dict(r) for r in cursor.fetchall()]
    conn.close()

    available_mins = int(student.get('daily_available_hours', 3.0) * 60)
    schedule = generate_adaptive_schedule(tasks, available_minutes=available_mins)
    return jsonify(schedule)

@app.route('/api/skills', methods=['GET', 'POST'])
def handle_skills():
    conn = get_db_connection()
    cursor = conn.cursor()
    if request.method == 'GET':
        cursor.execute("SELECT * FROM skills ORDER BY step_order ASC;")
        skills = [dict(r) for r in cursor.fetchall()]
        conn.close()
        return jsonify(skills)
    elif request.method == 'POST':
        data = request.json
        cursor.execute("UPDATE skills SET status = ?, progress_percent = ? WHERE id = ?;",
                       (data.get('status'), data.get('progress_percent'), data.get('id')))
        conn.commit()
        conn.close()
        return jsonify({"status": "success"})

@app.route('/api/ai-guidance', methods=['POST'])
def ai_guidance():
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM students LIMIT 1;")
    student = dict(cursor.fetchone())
    cursor.execute("SELECT * FROM tasks WHERE status != 'Completed' ORDER BY priority_score DESC;")
    tasks = [dict(r) for r in cursor.fetchall()]
    cursor.execute("SELECT * FROM skills;")
    skills = [dict(r) for r in cursor.fetchall()]
    conn.close()

    user_q = request.json.get('question') if request.json else None
    guidance = get_student_guidance(student, tasks, skills, user_q)
    return jsonify({"guidance": guidance})

if __name__ == '__main__':
    print("🚀 PathPilot AI Backend running on http://127.0.0.1:5000")
    app.run(debug=True, port=5000)
