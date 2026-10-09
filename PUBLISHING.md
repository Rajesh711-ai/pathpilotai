# Publishing Guide — PathPilot AI 🚀

This guide explains all methods to publish and distribute **PathPilot AI** to users, testers, open-source communities, and the Google Play Store.

---

## 1. 🌐 Instant Public Web Sharing (Active Now)

PathPilot AI is deployed with live interactive streaming accessible to anyone with the link:

- **Public Streaming URL**:  
  **[https://ais-pre-map4oaarqgiyf63awjlugz-270839861885.asia-east1.run.app](https://ais-pre-map4oaarqgiyf63awjlugz-270839861885.asia-east1.run.app)**

### Managing Access in AI Studio:
1. In the upper-right corner of Google AI Studio, click the **Share** button.
2. Select **"Anyone with the link can view/run"**.
3. Share the generated link with peers, students, recruiters, or mentors.

---

## 2. 📱 Exporting APK / AAB for Android Phones

You can export standalone installable Android binaries directly from the AI Studio interface:

### Exporting via AI Studio UI:
1. Click the **Project Options menu (`⋮` / Settings)** in the top navigation bar.
2. Select **"Generate APK / AAB"** or **"Download APK"**.
3. Transfer the `.apk` file to any Android device running Android 7.0 (Nougat, API 24) or higher.
4. Tap the APK on your phone to install (enable "Install unknown apps" when prompted).

### Building Locally via Command Line:
```bash
# Debug APK for testing:
gradle :app:assembleDebug

# Output location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 3. 🏪 Publishing to Google Play Store

PathPilot AI is configured with modern Android standards ready for Google Play distribution:
- **Application ID**: `com.aistudio.pathpilot.nxrtch`
- **Target SDK**: `36` (Android 15 / 16 preview compliant)
- **Minimum SDK**: `24` (Supports 95%+ of active Android devices)
- **Architecture**: Jetpack Compose, Room SQLite, Material 3, Edge-to-Edge display

### Step-by-Step Play Store Submission:

1. **Google Play Console Account**:
   - Go to [Google Play Console](https://play.google.com/console) and log in with your developer account.

2. **Create New Application**:
   - Click **Create app**.
   - **App Name**: `PathPilot AI`
   - **Default language**: English (United States)
   - **App or game**: App
   - **Free or paid**: Free

3. **Generate Release Bundle (AAB)**:
   - In your release keystore configuration (`app/build.gradle.kts`):
     ```bash
     gradle :app:bundleRelease
     ```
   - The output `.aab` file will be generated in `app/build/outputs/bundle/release/app-release.aab`.

4. **Complete Store Listing Requirements**:
   - **Short description**: *Personalized Student Life & Career Guidance Platform with Adaptive AI Planner.*
   - **Full description**: Highlight the adaptive scheduling engine, Google Gemini AI guidance, and notification bar integration.
   - **App Icon**: 512x512 px 32-bit PNG.
   - **Feature Graphic**: 1024x500 px JPG or 24-bit PNG.
   - **Screenshots**: Upload phone screenshots (at least 2, 1080x2400 px recommended).

5. **App Content Declarations**:
   - **Target Age**: 13+ (High school & University students).
   - **Data Safety**: Declare that local tasks and notes are stored strictly on-device in Room database, with zero third-party tracking.
   - **Permissions**: Declare `POST_NOTIFICATIONS` for the status bar study block display.

6. **Submit for Review**:
   - Create a release in the **Internal Testing**, **Closed Testing**, or **Production** track.
   - Upload `app-release.aab`.
   - Submit for Google Play review.

---

## 4. 🐙 Publishing to GitHub (Open Source)

To publish the complete codebase to GitHub:

1. Click **Settings / ⋮** in AI Studio and select **"Push to GitHub"**.
2. Authorize your GitHub account.
3. Choose repository visibility (**Public** or **Private**) and repository name (e.g., `pathpilot-ai`).
4. AI Studio will automatically push the entire repository including the Android app, Python backend, `README.md`, and `LICENSE`.

---

## 5. ⚙️ App Identity & Package Metadata

| Property | Value |
|---|---|
| **App Name** | PathPilot AI |
| **Package Name** | `com.aistudio.pathpilot.nxrtch` |
| **Version Code** | 1 |
| **Version Name** | 1.0 |
| **License** | MIT License |
| **Primary Capabilities** | Gemini AI, Room DB, Android Notification Bar Integration |
