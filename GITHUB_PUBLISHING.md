# GitHub Publishing & Public Sharing Guide 🐙

This guide explains how to publish **PathPilot AI** publicly on **GitHub** so that anyone in the world can see the project, explore the source code, visit the live website via GitHub Pages, and download the Android APK.

---

## Method 1: One-Click Publish from Google AI Studio (Recommended)

Google AI Studio Build has a built-in GitHub integration that connects directly to your GitHub account:

1. **Open AI Studio Settings Menu**:
   - In the top-right corner of the AI Studio Build editor, look for the settings menu (`⋮` or project options).
2. **Click "Push to GitHub"** (or **"Export to GitHub"**):
   - A modal will appear asking you to authorize or select your GitHub account.
3. **Configure Repository Details**:
   - **Repository Name**: `pathpilot-ai` (or your preferred name)
   - **Visibility**: Select **Public** 🌐 *(Ensure Public is selected so anyone can view it)*
4. **Confirm & Push**:
   - Click **Push** / **Create Repository**.
   - AI Studio will create the repository on your GitHub profile and push all files and commits!

---

## Method 2: Command Line (Git Terminal)

If you have Git installed on your computer, you can clone or push directly:

```bash
# 1. Create a new public repository on GitHub named 'pathpilot-ai' (do not initialize with README)
# 2. Add the remote URL:
git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/pathpilot-ai.git

# 3. Rename branch to main (if not already):
git branch -M main

# 4. Push all code to GitHub:
git push -u origin main
```

---

## 🌐 Enabling the Public Website via GitHub Pages

This repository includes a standalone web app in the `/docs` folder and an automated workflow (`.github/workflows/deploy-pages.yml`).

Once the repository is pushed to GitHub:

1. Navigate to your repository on GitHub: `https://github.com/<YOUR_GITHUB_USERNAME>/pathpilot-ai`
2. Go to **Settings** ⚙️ -> **Pages** (in the left sidebar).
3. Under **Build and deployment**:
   - **Source**: Select **GitHub Actions** (recommended) OR **Deploy from a branch**.
   - If using "Deploy from a branch":
     - Branch: `main`
     - Folder: `/docs`
     - Click **Save**.
4. Within 1-2 minutes, GitHub will publish your site at:
   ```
   https://<YOUR_GITHUB_USERNAME>.github.io/pathpilot-ai/
   ```
5. Anyone in the world can visit this link in any web browser without needing to install anything!

---

## 📱 Automated Android APK Releases (GitHub Actions)

This repository includes `.github/workflows/android-build.yml`.

Whenever you push to the `main` branch:
1. GitHub Actions automatically spins up an Ubuntu runner.
2. It compiles the Android app using Gradle (`assembleDebug`).
3. The ready-to-install `app-debug.apk` is generated and attached to the GitHub Action run artifacts for anyone to download and test on their Android device.

---

## 🔗 Public URLs Summary

| Resource | URL |
|---|---|
| **Live Interactive Web App (Active Now)** | [https://ais-pre-map4oaarqgiyf63awjlugz-270839861885.asia-east1.run.app](https://ais-pre-map4oaarqgiyf63awjlugz-270839861885.asia-east1.run.app) |
| **Direct APK Download** | [https://ais-pre-map4oaarqgiyf63awjlugz-270839861885.asia-east1.run.app/download/app-debug.apk](https://ais-pre-map4oaarqgiyf63awjlugz-270839861885.asia-east1.run.app/download/app-debug.apk) |
| **GitHub Pages Site (After Push)** | `https://<YOUR_GITHUB_USERNAME>.github.io/pathpilot-ai/` |
| **GitHub Repository** | `https://github.com/<YOUR_GITHUB_USERNAME>/pathpilot-ai` |
