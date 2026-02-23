
# 🔐 App Lock by AP

App Lock by AP is a modern Android application built with **Jetpack Compose** that allows users to protect selected apps using **Biometric authentication, PIN, or Password**.  
It works fully offline and focuses on privacy, speed, and tamper protection.

---

## ✨ Features

✅ Lock individual apps  
✅ Biometric unlock (Fingerprint / Face ID)  
✅ PIN & Password protection  
✅ Device lock support  
✅ Accessibility-based real-time app detection  
✅ Usage Stats detection (optional)  
✅ Foreground monitoring service  
✅ Tamper protection for critical system settings  
✅ Onboarding flow with permission guidance  
✅ Material 3 UI with dynamic theming  
✅ Room Database + DataStore  
✅ Works completely offline (no data collection)

---

## 📱 Screens & Flow

- Welcome & onboarding
- Required permissions setup
- App Lock authentication setup
- Lock type selection (4-digit / 6-digit / unlimited PIN / password)
- App list with search & sorting
- Individual app locking
- Secure unlock overlay
- Settings screen

---

## 🛡️ Security

- Lock values are encrypted before saving
- Uses Android Biometric API
- Supports Device Credential
- Protects access to:
  - Accessibility Settings
  - Usage Stats
  - Overlay Permission
  - Device Admin
- Prevents tampering using Accessibility monitoring

---

## 🧱 Architecture

data/
├── local (Room + DAO + Entities)
├── repository
└── preferences (DataStore)

domain/
├── models
└── usecases

ui/
├── onboarding
├── main
├── unlock
└── settings

Includes migrations and Flow support.

---

## 🔍 App Detection

Two methods:

### Accessibility Service (recommended)
Real-time foreground app detection.

### Usage Stats
Fallback method using usage events.

---

## 🔔 Foreground Service

A persistent foreground service keeps App Lock active after reboot.

Boot receiver automatically restarts protection.

---

## 🛠 Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Room Database
- DataStore Preferences
- Coroutines + Flow
- Accessibility Service
- Biometric API
- Foreground Service
- MVVM

---

## 📦 Permissions Used

- Accessibility Service  
- Usage Stats  
- Draw Over Other Apps  
- Device Admin  
- Notifications  
- Ignore Battery Optimizations  
- Receive Boot Completed  

(All required only for app-lock functionality.)

⚠️ Disclaimer

This project is for educational purposes.
Always respect user privacy and platform policies when distributing app-lock software.

<img width="1080" height="2266" alt="00001" src="https://github.com/user-attachments/assets/c5f6c1c1-6782-41b1-b11f-e4bf8c4631ae" />        


<img width="1080" height="2235" alt="00002" src="https://github.com/user-attachments/assets/964f7484-9130-4ffb-904c-9293006895c1" />



<img width="1080" height="2257" alt="00003" src="https://github.com/user-attachments/assets/ce382431-2e3f-4b24-964e-fa78f0454c85" />


<img width="1080" height="2252" alt="00004" src="https://github.com/user-attachments/assets/f5757dcc-6f8b-482b-b364-9fef0a6b7c0e" />


<img width="1080" height="2233" alt="00005" src="https://github.com/user-attachments/assets/c61e97f1-229f-448a-8f97-37aac362fb32" />


<img width="1080" height="3986" alt="00006" src="https://github.com/user-attachments/assets/dc9908a4-6af7-4464-8d14-404c54c93b7a" />



