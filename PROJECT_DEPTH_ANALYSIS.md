# 🚀 Project Depth Analysis: Hostel Complaint Tracker

This document provides a comprehensive technical breakdown of the **AI-Powered Hostel Complaint Tracker** (Nano Banana Edition).

---

## 📂 1. Folder Structure & Architecture

The project follows the **MVVM (Model-View-ViewModel)** architectural pattern combined with a Repository-based data layer for clean separation of concerns.

### `app/src/main/java/com/swizosoft/hostelcomplaints/`
*   **`auth/`**: Contains `LoginActivity` and `RegisterActivity`. Handles Firebase Authentication.
*   **`complaints/`**: Core student features.
    *   `SubmitComplaintActivity`: The form where students raise issues.
    *   `ComplaintHistoryActivity`: List of past complaints.
    *   `ComplaintStatusActivity`: Real-time timeline tracker.
    *   `ChatActivity`: Gemini-powered AI Assistant.
*   **`warden/`**: Admin/Warden features.
    *   `WardenDashboardActivity`: The main panel for managing complaints.
    *   `ComplaintDetailActivity`: Deep dive into a single complaint.
*   **`models/`**: Data classes (`User`, `Complaint`, `ChatMessage`).
*   **`data/`**: Repositories (`AuthRepository`, `ComplaintRepository`) handling Firestore and Storage logic.
*   **`ai/`**: `GeminiService` - The engine for AI analysis and chatbot.

---

## 📱 2. Page-by-Page Logic

### 🔐 Authentication (Login & Register)
*   **Logic**: Uses `FirebaseAuth` for secure entry.
*   **Role-Based Access**: During registration, users are tagged as `STUDENT` or `WARDEN`. This determines which dashboard they see after login.
*   **UI**: Features the **Nano Banana** branding for a premium feel.

### 📝 Submit Complaint
*   **Logic**: 
    *   **Real-time AI**: As the student types, `GeminiService` analyzes the text to predict **Category** and **Urgency**.
    *   **Parallel Uploads**: Media attachments (images/video) are uploaded to Firebase Storage in parallel threads for maximum speed.
    *   **Firestore Entry**: Saves a structured JSON object to the `complaints` collection.

### 📊 Warden Dashboard
*   **Logic**:
    *   **Real-time Listening**: Uses `addSnapshotListener` to receive new complaints instantly without refreshing.
    *   **AI Prioritization**: Automatically sorts the list so "Critical" and "High" urgency issues appear at the very top.
    *   **Filtering**: Tabs separate "Active" (Submitted, Assigned, In Progress) from "Resolved" issues.

### 🤖 AI Chatbot Assistant (`ChatActivity`)
*   **Logic**: A conversational interface where students can ask for help drafting complaints.
*   **Integration**: Directly calls the Gemini Pro model using the `generativeai` SDK.

### ⏳ Real-Time Status Tracker
*   **Logic**: A visual timeline (`Submitted` -> `Assigned` -> `In Progress` -> `Resolved`).
*   **Sync**: Listens to changes in the `status` field in Firestore and updates the UI live.

---

## 🤖 3. AI Features In Detail

| Feature | Tech Used | Logic Description |
| :--- | :--- | :--- |
| **Smart Categorization** | Gemini API | Analyzes description text to suggest tags like "Plumbing" or "Electricity". |
| **Urgency Prediction** | Gemini API | Evaluates the severity of the issue based on keywords (e.g., "fire", "leak", "broken"). |
| **Sentiment Analysis** | Gemini API | Detects student frustration to help Wardens prioritize emotional urgency. |
| **Interactive Assistant** | Gemini Pro | A contextual chatbot that understands hostel-specific rules and drafting. |

---

## 🛠️ 4. Technology Stack

*   **Language**: Kotlin
*   **Database**: Firebase Firestore (NoSQL, Real-time)
*   **Storage**: Firebase Cloud Storage (Media files)
*   **Auth**: Firebase Authentication (Email/Password)
*   **AI**: Google Gemini Pro (LLM Integration)
*   **UI Components**: Material Design 3 (M3)
*   **Image Loading**: Glide

---

## 📈 5. Key Design Decisions

1.  **Nano Banana Branding**: Custom-generated AI assets used for a unique, state-of-the-art visual identity.
2.  **Parallel Repository Pattern**: Used `async/awaitAll` in the data layer to ensure that media-heavy complaints don't feel slow to the user.
3.  **Adaptive Status Logic**: Instead of a simple text status, we used a **Timeline View** to increase transparency and trust for students.

---
**Document Status**: Final Version (Nano Banana v1.0)
