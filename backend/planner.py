"""
PathPilot AI - NEXORA TECH
Adaptive AI Planner Engine (Python / Flask Backend)

Deterministic Priority Formula:
Priority = Urgency + Importance + Career Value + Remaining Work
"""

import time
from datetime import datetime, timedelta

def calculate_priority_score(task, current_time_ms=None):
    if current_time_ms is None:
        current_time_ms = int(time.time() * 1000)

    # 1. Urgency (0-50 pts)
    diff_hours = (task['deadline_timestamp'] - current_time_ms) / (1000.0 * 3600)

    if task.get('status') == 'Missed':
        urgency = 55.0  # Immediate boost to recover lost time
    elif diff_hours <= 0:
        urgency = 50.0  # Overdue
    elif diff_hours <= 12:
        urgency = 45.0
    elif diff_hours <= 24:
        urgency = 35.0
    elif diff_hours <= 48:
        urgency = 25.0
    elif diff_hours <= 72:
        urgency = 18.0
    elif diff_hours <= 168:
        urgency = 10.0
    else:
        urgency = 5.0

    # 2. Importance (5-25 pts)
    importance = min(5, max(1, task.get('importance', 3))) * 5.0

    # 3. Career Value (5-20 pts)
    career_val = min(4, max(1, task.get('career_value', 2))) * 5.0

    # 4. Remaining Work / Splittability (6-12 pts)
    if task.get('can_split', True):
        work_score = 12.0
    elif task.get('duration_minutes', 60) <= 45:
        work_score = 10.0
    else:
        work_score = 6.0

    return urgency + importance + career_val + work_score

def generate_adaptive_schedule(tasks, available_minutes=180, start_hour=9, start_minute=0):
    """
    Generates a deterministic daily timetable fitting the student's available hours.
    """
    active_tasks = [t for t in tasks if t.get('status') != 'Completed']
    for t in active_tasks:
        t['priority_score'] = calculate_priority_score(t)

    active_tasks.sort(key=lambda x: x['priority_score'], reverse=True)

    schedule = []
    remaining_minutes = available_minutes
    current_dt = datetime.now().replace(hour=start_hour, minute=start_minute, second=0, microsecond=0)

    for task in active_tasks:
        if remaining_minutes <= 0:
            break

        duration = max(10, task.get('duration_minutes', 60))

        if duration <= remaining_minutes:
            start_str = current_dt.strftime("%I:%M %p")
            current_dt += timedelta(minutes=duration)
            end_str = current_dt.strftime("%I:%M %p")

            schedule.append({
                "slot_id": f"slot_{task['id']}_full",
                "task_id": task['id'],
                "task_name": task['task_name'],
                "category": task['category'],
                "start_time": start_str,
                "end_time": end_str,
                "duration_minutes": duration,
                "priority_score": task['priority_score'],
                "is_split": False,
                "is_missed": task.get('status') == 'Missed',
                "reason": f"Ranked high with {int(task['priority_score'])} pts. Fits dedicated focus block."
            })
            remaining_minutes -= duration

        elif task.get('can_split') and remaining_minutes >= 15:
            # Split task into today's final available block
            chunk = remaining_minutes
            start_str = current_dt.strftime("%I:%M %p")
            current_dt += timedelta(minutes=chunk)
            end_str = current_dt.strftime("%I:%M %p")

            schedule.append({
                "slot_id": f"slot_{task['id']}_part1",
                "task_id": task['id'],
                "task_name": task['task_name'],
                "category": task['category'],
                "start_time": start_str,
                "end_time": end_str,
                "duration_minutes": chunk,
                "priority_score": task['priority_score'],
                "is_split": True,
                "split_info": f"Part 1 ({chunk} min of {duration} min)",
                "is_missed": task.get('status') == 'Missed',
                "reason": "Split to maximize today's study capacity. Part 2 deferred to tomorrow."
            })
            remaining_minutes = 0

    return schedule
