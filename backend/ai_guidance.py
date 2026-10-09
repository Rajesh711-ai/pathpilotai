"""
PathPilot AI - NEXORA TECH
AI Student Guidance Engine (Python)
Uses Gemini API or Contextual Heuristic Coach
"""

import os

def get_student_guidance(student, tasks, skills, user_question=None):
    api_key = os.environ.get("GEMINI_API_KEY", "")

    if api_key and api_key != "MY_GEMINI_API_KEY":
        try:
            import google.generativeai as genai
            genai.configure(api_key=api_key)
            model = genai.GenerativeModel("gemini-1.5-flash")

            prompt = f"""
            You are PathPilot AI, an expert academic and career mentor from NEXORA TECH.
            Student: {student.get('name')}, {student.get('course_branch')}, {student.get('year')}.
            Career Goal: {student.get('career_goal')}.
            Current Skills: {student.get('current_skills')}.
            Daily Study Budget: {student.get('daily_available_hours')} hours.
            Active Tasks: {[t['task_name'] + ' (due ' + t.get('deadline_desc', '') + ')' for t in tasks]}.

            Question: {user_question or "Provide today's personalized learning and priority strategy."}
            Provide concise, encouraging, and actionable guidance explaining why urgent deadlines precede optional career practice today.
            """
            response = model.generate_content(prompt)
            if response.text:
                return response.text
        except Exception:
            pass

    # Heuristic Coach Fallback
    top_task = tasks[0] if tasks else None
    return f"""🎯 **PathPilot AI Daily Strategy for {student.get('name', 'Student')}**

1. **Top Priority:** Complete **{top_task['task_name'] if top_task else 'your main assignment'}** first. Coursework deadlines carry continuous evaluation weight that protects your GPA.
2. **Career Skill Milestone:** Once your priority assignment block is cleared, invest 30-45 minutes in **Data Structures & Algorithms** towards your goal as a **{student.get('career_goal', 'Software Engineer')}**.
3. **Adaptive Notice:** If your routine changes, tap 'Adjust Time' or 'Missed'—the Adaptive Planner will balance your timetable instantly.
"""
