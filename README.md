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

Clean architecture style:
