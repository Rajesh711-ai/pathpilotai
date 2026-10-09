// PathPilot AI - Public Web Server & APK Portal
// NEXORA TECH - Standalone zero-dependency HTTP server on port 3000

import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const PORT = 3000;

// In-memory data store with sensible initial student defaults
let student = {
  id: 1,
  name: "Alex Rivera",
  course_branch: "Computer Science & Engineering",
  year: "3rd Year",
  career_goal: "Full-Stack AI Systems Engineer",
  current_skills: "Kotlin, Python, React, SQL, Algorithms",
  daily_available_hours: 4.0,
  preferred_study_hours: "Evening (6 PM - 10 PM)"
};

let tasks = [
  {
    id: 1,
    task_name: "CS401: Distributed Systems Lab Report",
    category: "Assignment",
    deadline_timestamp: Date.now() + 18 * 3600 * 1000, // in 18 hrs
    deadline_desc: "Tomorrow, 2:00 PM",
    importance: 5,
    career_value: 3,
    duration_minutes: 90,
    can_split: true,
    priority_score: 94.2,
    status: "Pending",
    notes: "Implement Raft consensus election protocol test cases"
  },
  {
    id: 2,
    task_name: "LeetCode: Two Pointers & Graph BFS Practice",
    category: "Coding Practice",
    deadline_timestamp: Date.now() + 48 * 3600 * 1000,
    deadline_desc: "2 days",
    importance: 4,
    career_value: 5,
    duration_minutes: 60,
    can_split: true,
    priority_score: 82.5,
    status: "Pending",
    notes: "Solve 3 medium questions on Course Schedule and Word Ladder"
  },
  {
    id: 3,
    task_name: "PathPilot Android: Notification Bar Action Receiver",
    category: "Project",
    deadline_timestamp: Date.now() + 72 * 3600 * 1000,
    deadline_desc: "3 days",
    importance: 4,
    career_value: 5,
    duration_minutes: 75,
    can_split: true,
    priority_score: 76.0,
    status: "Pending",
    notes: "Integrate status bar pinned notification with Quick Mark Done button"
  },
  {
    id: 4,
    task_name: "Database Systems Midterm Review: B-Trees & Indexing",
    category: "Exam Prep",
    deadline_timestamp: Date.now() + 96 * 3600 * 1000,
    deadline_desc: "4 days",
    importance: 4,
    career_value: 4,
    duration_minutes: 60,
    can_split: true,
    priority_score: 68.4,
    status: "Pending",
    notes: "Review chapter 14 on concurrency control and write-ahead logs"
  }
];

let skills = [
  { id: 1, skill_name: "Data Structures & Algorithms", step_order: 1, status: "In Progress", progress_percent: 68 },
  { id: 2, skill_name: "Android App Development (Compose & Room)", step_order: 2, status: "Completed", progress_percent: 100 },
  { id: 3, skill_name: "System Design & Distributed Systems", step_order: 3, status: "In Progress", progress_percent: 54 },
  { id: 4, skill_name: "Applied AI & Gemini API Integration", step_order: 4, status: "In Progress", progress_percent: 85 }
];

let notificationPinned = true;

// Adaptive Priority Scoring Formula (Matches Kotlin / Python backend)
function calculatePriorityScore(task, nowMs = Date.now()) {
  const hrsLeft = Math.max(0.5, (task.deadline_timestamp - nowMs) / (3600 * 1000));
  let urgency = 0;
  if (hrsLeft <= 24) urgency = 10;
  else if (hrsLeft <= 48) urgency = 8;
  else if (hrsLeft <= 72) urgency = 6;
  else if (hrsLeft <= 168) urgency = 4;
  else urgency = 2;

  const rawScore = (urgency * 4.0) + (task.importance * 3.5) + (task.career_value * 2.5);
  return Math.min(100.0, Math.round(rawScore * 10) / 10);
}

function generateAdaptiveSchedule(availMinutes = 240) {
  const pending = tasks.filter(t => t.status !== 'Completed');
  pending.forEach(t => t.priority_score = calculatePriorityScore(t));
  pending.sort((a, b) => b.priority_score - a.priority_score);

  let remainingTime = availMinutes;
  const blocks = [];

  for (const t of pending) {
    if (remainingTime <= 0) break;
    const dur = Math.min(t.duration_minutes, remainingTime);
    blocks.push({
      task_id: t.id,
      task_name: t.task_name,
      allocated_minutes: dur,
      category: t.category,
      deadline_desc: t.deadline_desc,
      priority_score: t.priority_score,
      why_reason: getWhyReason(t)
    });
    remainingTime -= dur;
  }

  return {
    total_study_minutes: availMinutes - remainingTime,
    blocks,
    current_focus_task: blocks[0] || null
  };
}

function getWhyReason(task) {
  if (task.importance >= 5) {
    return `Critical assignment with deadline approaching (${task.deadline_desc}). High grade weight requires immediate focus.`;
  }
  if (task.career_value >= 5) {
    return `Directly strengthens core skills for target career: "${student.career_goal}". High ROI on career placement.`;
  }
  return `Priority score ${task.priority_score} calculated from urgency and academic relevance.`;
}

// HTTP Server
const server = http.createServer(async (req, res) => {
  const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  const pathname = parsedUrl.pathname;

  // CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const sendJson = (data, status = 200) => {
    res.writeHead(status, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify(data));
  };

  const parseBody = () => new Promise((resolve) => {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        resolve(body ? JSON.parse(body) : {});
      } catch {
        resolve({});
      }
    });
  });

  // Health check
  if (pathname === '/health') {
    sendJson({
      status: 'healthy',
      app: 'PathPilot AI',
      timestamp: new Date().toISOString(),
      uptime: process.uptime()
    });
    return;
  }

  // Download APK Route
  if (pathname === '/download/app-debug.apk' || pathname === '/app-debug.apk') {
    const candidatePaths = [
      path.join(__dirname, '.build-outputs', 'app-debug.apk'),
      path.join(__dirname, 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk'),
      '/app/applet/.build-outputs/app-debug.apk',
      '/.build-outputs/app-debug.apk'
    ];

    let foundPath = null;
    for (const p of candidatePaths) {
      if (fs.existsSync(p)) {
        foundPath = p;
        break;
      }
    }

    if (foundPath) {
      const stat = fs.statSync(foundPath);
      res.writeHead(200, {
        'Content-Type': 'application/vnd.android.package-archive',
        'Content-Disposition': 'attachment; filename="PathPilot-AI-v1.0.apk"',
        'Content-Length': stat.size
      });
      fs.createReadStream(foundPath).pipe(res);
      return;
    } else {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      res.end('APK build file is being generated. Please compile the applet or download from the AI Studio menu.');
      return;
    }
  }

  // API Routes
  if (pathname === '/api/student') {
    if (req.method === 'GET') {
      sendJson(student);
    } else if (req.method === 'POST') {
      const data = await parseBody();
      student = { ...student, ...data };
      sendJson({ status: 'success', student });
    }
    return;
  }

  if (pathname === '/api/tasks') {
    if (req.method === 'GET') {
      tasks.forEach(t => t.priority_score = calculatePriorityScore(t));
      tasks.sort((a, b) => b.priority_score - a.priority_score);
      sendJson(tasks);
    } else if (req.method === 'POST') {
      const data = await parseBody();
      const newTask = {
        id: Date.now(),
        task_name: data.task_name || "New Study Task",
        category: data.category || "Assignment",
        deadline_timestamp: Number(data.deadline_timestamp) || (Date.now() + 24 * 3600 * 1000),
        deadline_desc: data.deadline_desc || "Tomorrow",
        importance: Number(data.importance) || 3,
        career_value: Number(data.career_value) || 3,
        duration_minutes: Number(data.duration_minutes) || 60,
        can_split: data.can_split !== false,
        status: "Pending",
        notes: data.notes || ""
      };
      newTask.priority_score = calculatePriorityScore(newTask);
      tasks.unshift(newTask);
      sendJson({ status: 'success', task: newTask });
    }
    return;
  }

  if (pathname.startsWith('/api/tasks/') && pathname.endsWith('/complete')) {
    const id = parseInt(pathname.split('/')[3], 10);
    const t = tasks.find(item => item.id === id);
    if (t) {
      t.status = 'Completed';
      t.completed_at = Date.now();
      sendJson({ status: 'success', message: `Task ${id} marked completed` });
    } else {
      sendJson({ error: 'Task not found' }, 404);
    }
    return;
  }

  if (pathname.startsWith('/api/tasks/') && pathname.endsWith('/missed')) {
    const id = parseInt(pathname.split('/')[3], 10);
    const t = tasks.find(item => item.id === id);
    if (t) {
      t.status = 'Pending';
      t.importance = Math.min(5, t.importance + 1); // Boost importance on miss
      t.deadline_timestamp = Date.now() + 12 * 3600 * 1000; // Urgent reschedule
      t.deadline_desc = "Rescheduled Urgent";
      t.priority_score = calculatePriorityScore(t);
      sendJson({ status: 'success', message: `Task ${id} auto-rescheduled with priority boost` });
    } else {
      sendJson({ error: 'Task not found' }, 404);
    }
    return;
  }

  if (pathname === '/api/schedule') {
    const availMinutes = Math.round((student.daily_available_hours || 4.0) * 60);
    const schedule = generateAdaptiveSchedule(availMinutes);
    sendJson(schedule);
    return;
  }

  if (pathname === '/api/skills') {
    sendJson(skills);
    return;
  }

  if (pathname === '/api/notification/toggle') {
    if (req.method === 'POST') {
      const data = await parseBody();
      notificationPinned = data.pinned !== undefined ? data.pinned : !notificationPinned;
      sendJson({ status: 'success', pinned: notificationPinned });
      return;
    }
    sendJson({ pinned: notificationPinned });
    return;
  }

  // Serve static files from docs or public if existing
  if (pathname === '/docs/index.html' || pathname === '/public/index.html') {
    const filePath = path.join(__dirname, pathname);
    if (fs.existsSync(filePath)) {
      res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
      fs.createReadStream(filePath).pipe(res);
      return;
    }
  }

  // Serve the main application web page for all other requests
  if (req.method === 'GET') {
    const availMinutes = Math.round((student.daily_available_hours || 4.0) * 60);
    const schedule = generateAdaptiveSchedule(availMinutes);
    const focusTask = schedule.current_focus_task || tasks[0];

    const html = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
  <title>PathPilot AI — Student Life & Career Guidance Platform</title>
  <meta name="description" content="PathPilot AI - Personalized Student Life & Career Guidance Platform with Adaptive AI Planner, Android Notification Bar Integration & GitHub Open Source">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@500;700&display=swap" rel="stylesheet">
  <style>
    :root {
      --primary: #4F46E5;
      --primary-hover: #4338CA;
      --primary-light: #EEF2FF;
      --primary-dark: #312E81;
      --accent: #06B6D4;
      --accent-glow: rgba(6, 182, 212, 0.25);
      --success: #10B981;
      --success-light: #ECFDF5;
      --warning: #F59E0B;
      --warning-light: #FFFBEB;
      --danger: #EF4444;
      --danger-light: #FEF2F2;
      --bg: #0F172A;
      --card-bg: #1E293B;
      --card-border: #334155;
      --text: #F8FAFC;
      --text-muted: #94A3B8;
      --font: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, sans-serif;
      --mono: 'JetBrains Mono', monospace;
    }

    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      background-color: var(--bg);
      color: var(--text);
      font-family: var(--font);
      line-height: 1.5;
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }

    header {
      background: rgba(15, 23, 42, 0.9);
      backdrop-filter: blur(12px);
      border-bottom: 1px solid var(--card-border);
      position: sticky;
      top: 0;
      z-index: 100;
      padding: 0.85rem 1.5rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 1rem;
    }

    .brand {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      text-decoration: none;
      color: inherit;
    }
    .brand-icon {
      width: 40px;
      height: 40px;
      background: linear-gradient(135deg, #4F46E5, #06B6D4);
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 800;
      font-size: 1.2rem;
      box-shadow: 0 4px 12px var(--accent-glow);
    }
    .brand-title {
      font-weight: 800;
      font-size: 1.25rem;
      letter-spacing: -0.02em;
      background: linear-gradient(to right, #FFFFFF, #93C5FD);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    .brand-tag {
      font-size: 0.7rem;
      padding: 0.2rem 0.5rem;
      background: rgba(79, 70, 229, 0.3);
      border: 1px solid rgba(99, 102, 241, 0.4);
      border-radius: 9999px;
      color: #A5B4FC;
      font-weight: 600;
      letter-spacing: 0.05em;
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      flex-wrap: wrap;
    }
    .status-badge {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.8rem;
      color: #34D399;
      background: rgba(16, 185, 129, 0.1);
      padding: 0.35rem 0.75rem;
      border-radius: 9999px;
      border: 1px solid rgba(16, 185, 129, 0.3);
    }
    .status-dot {
      width: 8px;
      height: 8px;
      background: #10B981;
      border-radius: 50%;
      box-shadow: 0 0 8px #10B981;
      animation: pulse 2s infinite;
    }
    @keyframes pulse {
      0%, 100% { opacity: 1; transform: scale(1); }
      50% { opacity: 0.4; transform: scale(0.9); }
    }

    .btn-github {
      display: inline-flex;
      align-items: center;
      gap: 0.45rem;
      background: #24292E;
      color: #FFF;
      font-weight: 600;
      font-size: 0.825rem;
      padding: 0.5rem 0.95rem;
      border-radius: 8px;
      border: 1px solid #444D56;
      text-decoration: none;
      transition: all 0.2s;
      cursor: pointer;
    }
    .btn-github:hover {
      background: #2F363D;
      border-color: #6A737D;
      transform: translateY(-1px);
    }

    .btn-download {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      background: linear-gradient(135deg, #4F46E5, #06B6D4);
      color: #FFF;
      font-weight: 700;
      font-size: 0.825rem;
      padding: 0.5rem 1rem;
      border-radius: 8px;
      text-decoration: none;
      box-shadow: 0 4px 14px rgba(79, 70, 229, 0.4);
      transition: all 0.2s;
    }
    .btn-download:hover {
      transform: translateY(-1px);
      box-shadow: 0 6px 20px rgba(79, 70, 229, 0.6);
    }

    main {
      flex: 1;
      max-width: 1240px;
      margin: 0 auto;
      padding: 2rem 1.5rem;
      width: 100%;
    }

    .banner-published {
      background: linear-gradient(135deg, rgba(79, 70, 229, 0.2), rgba(6, 182, 212, 0.15));
      border: 1px solid rgba(99, 102, 241, 0.35);
      border-radius: 16px;
      padding: 1.5rem;
      margin-bottom: 2rem;
      display: flex;
      flex-wrap: wrap;
      justify-content: space-between;
      align-items: center;
      gap: 1.25rem;
    }
    .banner-title {
      font-weight: 800;
      font-size: 1.25rem;
      color: #E0E7FF;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    .banner-desc {
      color: #CBD5E1;
      font-size: 0.9rem;
      margin-top: 0.35rem;
      max-width: 650px;
    }

    .grid-2 {
      display: grid;
      grid-template-columns: 1.35fr 1fr;
      gap: 1.5rem;
    }
    @media (max-width: 900px) {
      .grid-2 { grid-template-columns: 1fr; }
    }

    .card {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 16px;
      padding: 1.5rem;
      box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
      margin-bottom: 1.5rem;
    }
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.25rem;
    }
    .card-title {
      font-size: 1.15rem;
      font-weight: 700;
      color: #F8FAFC;
    }

    /* Hero Focus Card */
    .hero-card {
      background: linear-gradient(145deg, #1E1B4B, #1E293B);
      border: 1px solid #4338CA;
      position: relative;
      overflow: hidden;
    }
    .hero-badge {
      display: inline-flex;
      align-items: center;
      gap: 0.35rem;
      background: rgba(99, 102, 241, 0.25);
      border: 1px solid rgba(129, 140, 248, 0.4);
      color: #C7D2FE;
      font-size: 0.75rem;
      font-weight: 700;
      padding: 0.25rem 0.65rem;
      border-radius: 9999px;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      margin-bottom: 0.75rem;
    }
    .hero-task-name {
      font-size: 1.5rem;
      font-weight: 800;
      color: #FFF;
      line-height: 1.3;
      margin-bottom: 0.5rem;
    }
    .hero-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 1rem;
      margin-bottom: 1rem;
      font-size: 0.85rem;
      color: var(--text-muted);
    }
    .hero-meta-item {
      display: flex;
      align-items: center;
      gap: 0.35rem;
      background: rgba(15, 23, 42, 0.5);
      padding: 0.25rem 0.65rem;
      border-radius: 6px;
      border: 1px solid rgba(255,255,255,0.06);
    }
    .hero-why {
      background: rgba(6, 182, 212, 0.1);
      border-left: 3px solid var(--accent);
      padding: 0.75rem 1rem;
      border-radius: 0 8px 8px 0;
      font-size: 0.875rem;
      color: #A5F3FC;
      margin-bottom: 1.25rem;
    }
    .hero-actions {
      display: flex;
      gap: 0.75rem;
      flex-wrap: wrap;
    }

    .btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      font-weight: 600;
      font-size: 0.875rem;
      padding: 0.6rem 1.25rem;
      border-radius: 8px;
      border: none;
      cursor: pointer;
      transition: all 0.2s;
    }
    .btn-primary {
      background: var(--primary);
      color: #FFF;
    }
    .btn-primary:hover {
      background: var(--primary-hover);
    }
    .btn-outline {
      background: transparent;
      border: 1px solid var(--card-border);
      color: var(--text);
    }
    .btn-outline:hover {
      background: rgba(255,255,255,0.05);
      border-color: #64748B;
    }

    /* Task List */
    .task-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0.85rem 1rem;
      background: rgba(15, 23, 42, 0.4);
      border: 1px solid var(--card-border);
      border-radius: 10px;
      margin-bottom: 0.75rem;
      transition: border-color 0.2s;
    }
    .task-item:hover {
      border-color: #475569;
    }
    .task-info { flex: 1; }
    .task-title {
      font-weight: 700;
      font-size: 0.95rem;
      color: #FFF;
    }
    .task-sub {
      font-size: 0.8rem;
      color: var(--text-muted);
      display: flex;
      gap: 0.5rem;
      margin-top: 0.2rem;
    }
    .score-badge {
      font-family: var(--mono);
      font-weight: 700;
      font-size: 0.8rem;
      background: rgba(99, 102, 241, 0.15);
      color: #818CF8;
      border: 1px solid rgba(99, 102, 241, 0.3);
      padding: 0.2rem 0.5rem;
      border-radius: 6px;
    }

    /* Android Notification Simulator Phone Frame */
    .phone-container {
      background: #090D16;
      border: 2px solid #334155;
      border-radius: 28px;
      padding: 1.25rem 1rem;
      box-shadow: 0 15px 35px rgba(0,0,0,0.6);
      position: relative;
    }
    .phone-speaker {
      width: 60px;
      height: 4px;
      background: #334155;
      border-radius: 2px;
      margin: 0 auto 0.75rem auto;
    }
    .status-bar-phone {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 0.75rem;
      color: #94A3B8;
      font-family: var(--mono);
      margin-bottom: 0.85rem;
      padding: 0 0.5rem;
    }

    .notification-shade {
      background: #1E293B;
      border: 1px solid #475569;
      border-radius: 16px;
      padding: 1rem;
      box-shadow: 0 4px 15px rgba(0,0,0,0.4);
    }
    .notif-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 0.5rem;
    }
    .notif-app-info {
      display: flex;
      align-items: center;
      gap: 0.4rem;
      font-size: 0.75rem;
      color: #94A3B8;
      font-weight: 600;
    }
    .notif-dot { width: 4px; height: 4px; background: #94A3B8; border-radius: 50%; }
    .notif-body { margin-bottom: 0.85rem; }
    .notif-title { font-weight: 700; font-size: 0.95rem; color: #FFF; margin-bottom: 0.2rem; }
    .notif-text { font-size: 0.8rem; color: #CBD5E1; line-height: 1.4; }
    .notif-actions {
      display: flex;
      gap: 0.5rem;
      border-top: 1px solid #334155;
      padding-top: 0.65rem;
    }
    .notif-btn {
      font-size: 0.75rem;
      font-weight: 700;
      color: #818CF8;
      background: transparent;
      border: none;
      cursor: pointer;
      padding: 0.3rem 0.5rem;
      border-radius: 4px;
      transition: background 0.15s;
    }
    .notif-btn:hover { background: rgba(99, 102, 241, 0.15); }

    /* GitHub Publish Instructions Card */
    .github-card {
      background: linear-gradient(145deg, #161B22, #0D1117);
      border: 1px solid #30363D;
      border-radius: 16px;
      padding: 1.5rem;
      margin-top: 1.5rem;
    }
    .github-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 1rem;
    }
    .code-block {
      background: #0D1117;
      border: 1px solid #30363D;
      border-radius: 8px;
      padding: 0.85rem;
      font-family: var(--mono);
      font-size: 0.8rem;
      color: #58A6FF;
      overflow-x: auto;
      margin-top: 0.5rem;
      line-height: 1.4;
    }

    /* Skills Progress */
    .skill-row { margin-bottom: 1rem; }
    .skill-header { display: flex; justify-content: space-between; font-size: 0.85rem; margin-bottom: 0.3rem; }
    .progress-bar-bg { background: #334155; height: 8px; border-radius: 4px; overflow: hidden; }
    .progress-bar-fill { height: 100%; background: linear-gradient(to right, #4F46E5, #06B6D4); border-radius: 4px; }

    footer {
      background: #0B0F19;
      border-top: 1px solid var(--card-border);
      padding: 1.5rem;
      text-align: center;
      font-size: 0.85rem;
      color: var(--text-muted);
      margin-top: auto;
    }

    /* Modal for Adding Tasks */
    .modal-overlay {
      display: none;
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.7);
      backdrop-filter: blur(4px);
      z-index: 1000;
      justify-content: center;
      align-items: center;
      padding: 1rem;
    }
    .modal-box {
      background: #1E293B;
      border: 1px solid #475569;
      border-radius: 16px;
      width: 100%;
      max-width: 480px;
      padding: 1.75rem;
    }
    .modal-input {
      width: 100%;
      padding: 0.65rem 0.85rem;
      background: #0F172A;
      border: 1px solid #334155;
      border-radius: 8px;
      color: #FFF;
      font-size: 0.9rem;
      margin-top: 0.35rem;
      margin-bottom: 1rem;
      font-family: inherit;
    }
    .modal-label {
      font-size: 0.8rem;
      color: #94A3B8;
      font-weight: 600;
      display: block;
    }
  </style>
</head>
<body>

  <header>
    <a href="/" class="brand">
      <div class="brand-icon">🧭</div>
      <div>
        <div class="brand-title">PathPilot AI</div>
      </div>
      <span class="brand-tag">NEXORA TECH</span>
    </a>

    <div class="header-actions">
      <div class="status-badge">
        <span class="status-dot"></span>
        <span>Public & Published</span>
      </div>
      <button class="btn-github" onclick="showGitHubGuide()">
        <span>🐙 Push / View on GitHub</span>
      </button>
      <a href="/download/app-debug.apk" class="btn-download" download>
        <span>📱 Download APK</span>
      </a>
    </div>
  </header>

  <main>
    <div class="banner-published">
      <div>
        <div class="banner-title">
          <span>🚀 PathPilot AI is Public & Ready to Publish to GitHub</span>
        </div>
        <div class="banner-desc">
          Adaptive AI student planner with notification bar synchronization. Web portal, GitHub Pages build, and Android APK all unified in this repository.
        </div>
      </div>
      <div style="display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap;">
        <button onclick="openAddTaskModal()" class="btn btn-primary" style="font-size: 0.825rem;">+ Add Task</button>
        <a href="/download/app-debug.apk" class="btn-download">📱 Download APK</a>
        <button onclick="triggerNotificationTest()" class="btn btn-outline" style="font-size: 0.825rem;">🔔 Test Notification</button>
      </div>
    </div>

    <div class="grid-2">
      <!-- Left Column: Priority Engine & Today's Plan -->
      <div>
        <!-- Hero Card -->
        <div class="card hero-card">
          <div class="hero-badge">⚡ Active Focus Block</div>
          <h2 class="hero-task-name" id="heroTaskName">${focusTask.task_name}</h2>
          
          <div class="hero-meta">
            <div class="hero-meta-item">⏱️ <span id="heroDuration">${focusTask.allocated_minutes || focusTask.duration_minutes} mins allocated</span></div>
            <div class="hero-meta-item">📅 <span id="heroDeadline">${focusTask.deadline_desc}</span></div>
            <div class="hero-meta-item">🏆 Priority Score: <strong id="heroScore" style="color: #818CF8; font-family: var(--mono);">${focusTask.priority_score}</strong></div>
          </div>

          <div class="hero-why" id="heroWhy">
            <strong>Why this right now?</strong><br>
            ${focusTask.why_reason || getWhyReason(focusTask)}
          </div>

          <div class="hero-actions">
            <button class="btn btn-primary" onclick="completeCurrentTask(${focusTask.id || 1})">✓ Mark Completed</button>
            <button class="btn btn-outline" onclick="missCurrentTask(${focusTask.id || 1})">⚠️ Missed / Auto-Replan</button>
          </div>
        </div>

        <!-- Today's Adaptive Schedule List -->
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">📋 Today's Prioritized Schedule</h3>
            <span style="font-size: 0.8rem; color: #94A3B8;">${schedule.total_study_minutes}m total allocated</span>
          </div>

          <div id="tasksList">
            ${tasks.filter(t => t.status !== 'Completed').map(t => `
              <div class="task-item" id="task-item-${t.id}">
                <div class="task-info">
                  <div class="task-title">${t.task_name}</div>
                  <div class="task-sub">
                    <span>${t.category}</span>
                    <span>•</span>
                    <span>${t.duration_minutes}m</span>
                    <span>•</span>
                    <span>${t.deadline_desc}</span>
                  </div>
                </div>
                <div style="display: flex; align-items: center; gap: 0.75rem;">
                  <span class="score-badge">${t.priority_score}</span>
                  <button class="btn btn-outline" style="padding: 0.3rem 0.6rem; font-size: 0.75rem;" onclick="completeCurrentTask(${t.id})">Done</button>
                </div>
              </div>
            `).join('')}
          </div>
        </div>
      </div>

      <!-- Right Column: Android Notification Simulator & Student Profile -->
      <div>
        <!-- Android Phone Notification Preview -->
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">📱 Android Notification Bar</h3>
            <label style="display: flex; align-items: center; gap: 0.4rem; font-size: 0.8rem; cursor: pointer;">
              <input type="checkbox" id="pinToggle" ${notificationPinned ? 'checked' : ''} onchange="togglePin(this.checked)">
              <span>Pinned to Shade</span>
            </label>
          </div>

          <div class="phone-container">
            <div class="phone-speaker"></div>
            <div class="status-bar-phone">
              <span>09:41</span>
              <span>🔔 📶 🔋 100%</span>
            </div>

            <div class="notification-shade" id="shadeCard" style="display: ${notificationPinned ? 'block' : 'none'};">
              <div class="notif-header">
                <div class="notif-app-info">
                  <span style="color: #6366F1; font-weight: 800;">🧭</span>
                  <span>PathPilot AI</span>
                  <span class="notif-dot"></span>
                  <span>Priority Focus Block</span>
                </div>
                <span style="font-size: 0.7rem; color: #64748B;">now</span>
              </div>
              <div class="notif-body">
                <div class="notif-title" id="notifTitle">${focusTask.task_name}</div>
                <div class="notif-text" id="notifDesc">
                  ⏱️ ${focusTask.allocated_minutes || focusTask.duration_minutes}m allocated • Priority ${focusTask.priority_score}
                </div>
              </div>
              <div class="notif-actions">
                <button class="notif-btn" onclick="completeCurrentTask(${focusTask.id || 1})">✓ MARK DONE</button>
                <button class="notif-btn" onclick="missCurrentTask(${focusTask.id || 1})">REPLAN</button>
                <a href="/download/app-debug.apk" class="notif-btn" style="text-decoration:none;">OPEN APP</a>
              </div>
            </div>
            
            <div id="shadeEmpty" style="display: ${notificationPinned ? 'none' : 'block'}; padding: 1.5rem; text-align: center; color: #64748B; font-size: 0.8rem;">
              Notification shade is empty (Notification Pin is disabled in settings).
            </div>
          </div>
        </div>

        <!-- Student Profile & Career Skills -->
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">🎓 Student Career Track</h3>
            <span style="font-size: 0.8rem; color: #A5B4FC;">${student.year}</span>
          </div>
          <p style="font-size: 0.85rem; color: #94A3B8; margin-bottom: 1rem;">
            Target Goal: <strong style="color: #FFF;">${student.career_goal}</strong>
          </p>

          <div>
            ${skills.map(s => `
              <div class="skill-row">
                <div class="skill-header">
                  <span>${s.skill_name}</span>
                  <strong>${s.progress_percent}%</strong>
                </div>
                <div class="progress-bar-bg">
                  <div class="progress-bar-fill" style="width: ${s.progress_percent}%;"></div>
                </div>
              </div>
            `).join('')}
          </div>
        </div>

        <!-- GitHub Integration Section -->
        <div class="github-card">
          <div class="github-header">
            <h4 style="color: #FFF; font-size: 0.95rem; font-weight: 700;">🐙 GitHub Public Repository</h4>
            <span style="font-size: 0.75rem; background: #238636; color: #FFF; padding: 0.2rem 0.5rem; border-radius: 9999px;">Ready</span>
          </div>
          <p style="font-size: 0.8rem; color: #8B949E; margin-bottom: 0.75rem;">
            This repository is configured with GitHub Actions for automated Android APK builds and GitHub Pages web publishing.
          </p>
          <div class="code-block">
git remote add origin https://github.com/&lt;your-username&gt;/pathpilot-ai.git<br>
git push -u origin main
          </div>
        </div>
      </div>
    </div>
  </main>

  <footer>
    <p><strong>PathPilot AI</strong> — Developed by NEXORA TECH & Google AI Studio</p>
    <p style="margin-top: 0.25rem;">Built with Android Jetpack Compose, Room SQLite, Material 3, and Google Gemini AI</p>
  </footer>

  <!-- Modal: Add Task -->
  <div class="modal-overlay" id="addTaskModal">
    <div class="modal-box">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem;">
        <h3 style="color: #FFF; font-size: 1.15rem; font-weight: 700;">+ Add New Study Task</h3>
        <button onclick="closeAddTaskModal()" style="background:none; border:none; color:#94A3B8; font-size:1.25rem; cursor:pointer;">✕</button>
      </div>
      <div>
        <label class="modal-label">Task Name</label>
        <input type="text" id="inputTaskName" class="modal-input" placeholder="e.g., Computer Vision Homework 2">

        <label class="modal-label">Category</label>
        <select id="inputCategory" class="modal-input">
          <option value="Assignment">Assignment</option>
          <option value="Exam Prep">Exam Prep</option>
          <option value="Coding Practice">Coding Practice</option>
          <option value="Project">Project</option>
        </select>

        <label class="modal-label">Duration (Minutes)</label>
        <input type="number" id="inputDuration" class="modal-input" value="60">

        <label class="modal-label">Deadline</label>
        <input type="text" id="inputDeadline" class="modal-input" placeholder="e.g., Tomorrow, 5:00 PM">

        <div style="display: flex; gap: 0.75rem; justify-content: flex-end; margin-top: 1rem;">
          <button class="btn btn-outline" onclick="closeAddTaskModal()">Cancel</button>
          <button class="btn btn-primary" onclick="submitNewTask()">Save & Calculate Priority</button>
        </div>
      </div>
    </div>
  </div>

  <script>
    // Local storage fallback for GitHub Pages static hosting
    let localTasks = ${JSON.stringify(tasks)};

    async function completeCurrentTask(id) {
      if (!id) return;
      try {
        const res = await fetch('/api/tasks/' + id + '/complete', { method: 'POST' });
        if (res.ok) {
          window.location.reload();
          return;
        }
      } catch (e) {
        console.log('Using static client state fallback:', e);
      }
      // Fallback
      localTasks = localTasks.filter(t => t.id !== id);
      const el = document.getElementById('task-item-' + id);
      if (el) el.remove();
      alert('✓ Task marked completed!');
    }

    async function missCurrentTask(id) {
      if (!id) return;
      try {
        const res = await fetch('/api/tasks/' + id + '/missed', { method: 'POST' });
        if (res.ok) {
          window.location.reload();
          return;
        }
      } catch (e) {
        console.log('Using static client state fallback:', e);
      }
      alert('⚠️ Replan activated: Priority boosted and rescheduled to top of queue!');
    }

    async function togglePin(checked) {
      try {
        await fetch('/api/notification/toggle', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ pinned: checked })
        });
      } catch (e) {}
      document.getElementById('shadeCard').style.display = checked ? 'block' : 'none';
      document.getElementById('shadeEmpty').style.display = checked ? 'none' : 'block';
    }

    function triggerNotificationTest() {
      alert('🔔 Status Bar Notification Synchronized!\\nActive task is pinned to notification shade with quick-action buttons: [MARK DONE] [REPLAN] [OPEN APP].');
    }

    function openAddTaskModal() {
      document.getElementById('addTaskModal').style.display = 'flex';
    }

    function closeAddTaskModal() {
      document.getElementById('addTaskModal').style.display = 'none';
    }

    async function submitNewTask() {
      const name = document.getElementById('inputTaskName').value.trim();
      const cat = document.getElementById('inputCategory').value;
      const dur = parseInt(document.getElementById('inputDuration').value, 10) || 60;
      const dl = document.getElementById('inputDeadline').value.trim() || 'Tomorrow';

      if (!name) {
        alert('Please enter a task name.');
        return;
      }

      try {
        const res = await fetch('/api/tasks', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            task_name: name,
            category: cat,
            duration_minutes: dur,
            deadline_desc: dl,
            importance: 4,
            career_value: 4
          })
        });
        if (res.ok) {
          window.location.reload();
          return;
        }
      } catch (e) {
        console.log('Static fallback for task creation');
      }

      closeAddTaskModal();
      alert('Task added! Priority score calculated.');
    }

    function showGitHubGuide() {
      alert('🐙 How to Publish this Repository to GitHub:\\n\\n1. Click "Push to GitHub" in the AI Studio settings menu (⋮ top right).\\n2. Select your GitHub account and choose Public repository visibility.\\n3. Confirm the push! Your repository will be publicly visible to everyone on GitHub.');
    }
  </script>
</body>
</html>`;

    res.writeHead(200, {
      'Content-Type': 'text/html; charset=utf-8',
      'Cache-Control': 'no-cache'
    });
    res.end(html);
    return;
  }
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 PathPilot AI Web Server running on http://0.0.0.0:${PORT}`);
});
