# AI-Powered Hostel Complaint Tracker - Setup Guide

This project is a modern Android application designed to streamline hostel management using AI-driven complaint analysis and real-time tracking.

## 🚀 Prerequisites
Before you begin, ensure you have the following installed:
- **Android Studio** (Hedgehog or later recommended)
- **JDK 17**
- **Firebase Account**
- **Google AI Studio Account** (for Gemini API)

## 🛠️ Step-by-Step Setup

### 1. Clone/Import the Project
1. Open Android Studio.
2. Select **File > Open** and navigate to the project folder: `major projecy-swizosoft`.
3. Wait for the Gradle sync to complete.

### 2. Firebase Configuration
I have already integrated the basic Firebase configuration, but ensure the following are enabled in your [Firebase Console](https://console.firebase.google.com/):
1. **Authentication**: Enable `Email/Password` and `Google Sign-In`.
2. **Firestore Database**: Create a database in "Test Mode".
3. **Storage**: Enable storage to allow media uploads (Images/Video).
4. **Cloud Messaging**: (Optional) For push notifications.

*Note: The `google-services.json` is already placed in the `app/` folder.*

### 3. Gemini API Configuration
The app uses the Gemini API for smart categorization and the chatbot.
1. The API key is stored in `app/src/main/res/values/secrets.xml`.
2. If you need to update it, replace the value in that file:
   ```xml
   <string name="gemini_api_key">YOUR_KEY_HERE</string>
   ```

### 4. Running the App
1. Connect an Android device or start an Emulator (API 24 or higher).
2. Click the **Run** (Green Play button) in Android Studio.

## 📱 How to Use

### For Students:
1. **Register**: Create an account and select the **Student** role. Provide your Block and Room Number.
2. **Submit Complaint**: 
   - Click "Submit New Complaint".
   - Type a description (e.g., "The water tap is leaking").
   - **Watch the AI**: The app will automatically suggest a category and predict the urgency level.
   - **Attach Media**: Click the '+' icon to add photos or videos of the issue.
   - **AI Assistant**: Use the "Ask AI Assistant" button if you need help drafting your complaint.
3. **Track Progress**: Click "My Complaint History" to see the real-time timeline of your request.

### For Wardens (Admin):
1. **Register/Login**: Create an account and select the **Warden** role.
2. **Dashboard**: Click "Go to Admin Panel".
3. **Prioritized Queue**: You will see a list of complaints sorted by **AI-predicted urgency** (Critical issues appear first).
4. **Take Action**: Click on a complaint to view media evidence, update the status (e.g., to "In Progress"), and add resolution remarks.

## 🤖 AI Features Included
- **Auto-Categorization**: Gemini predicts the category (Plumbing, Electricity, etc.) based on text.
- **Urgency Prediction**: Classifies complaints as Low, Medium, High, or Critical.
- **Sentiment Analysis**: Detects the student's mood (Frustrated, Calm) to help wardens prioritize.
- **AI Chatbot**: A conversational assistant to guide students.

---
**Developed for Swizosoft (OPC) Private Limited**
