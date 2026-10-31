# AYSHA AI — Termux Android starter

এই ZIP-এ একটি পূর্ণ Android project structure-সহ কার্যকর starter/MVP দেওয়া হয়েছে।
এতে আছে Compose chat UI, Gemini REST API, API key encryption using Android Keystore,
SpeechRecognizer intent, Android Text-to-Speech, basic memory storage এবং কিছু Android intents।

## এখনো সম্পূর্ণ করা হয়নি
Room migration, সম্পূর্ণ Coding Studio editor, wake-word detection, screen OCR/analysis,
touch automation, full accessibility workflows, personality editor এবং extensive app launching
এই MVP-তে নেই। এগুলোকে fake functionality হিসেবে দেখানো হয়নি।

## Build requirements
- JDK 17
- Gradle 8.13
- Android SDK Platform 35
- Android Build Tools 35.x

## Build command
Project root folder-এ:
    gradle --version
    gradle :app:assembleDebug

APK:
    app/build/outputs/apk/debug/app-debug.apk

## Gemini setup
App খুলে Settings-এ Gemini API key দিন। Model: gemini-2.5-flash।
Test API চাপুন, তারপর Save করুন।

## Termux
Termux-এ চেষ্টা করুন:
    pkg update && pkg upgrade
    pkg install openjdk-17 gradle unzip

তারপর ZIP extract করে project folder-এ:
    gradle :app:assembleDebug

সতর্কতা: Termux-এ Android SDK platform/build-tools setup সব ডিভাইসে সহজ বা নিশ্চিত নয়।
SDK tools compatible ভাবে install না হলে একই project Linux PC বা cloud build runner-এ build করুন।
এই ZIP-এ Gradle wrapper JAR নেই; Termux-এ system Gradle command ব্যবহার করার জন্যই রাখা হয়েছে।

## Security
API key Android Keystore-backed AES-GCM encryption দিয়ে locally encrypt করা হয়।
তবে client-side APK-তে ব্যবহৃত API key extraction থেকে পুরোপুরি নিরাপদ নয়।
Public release-এর আগে backend proxy ব্যবহার করুন এবং API key-তে quota/restrictions দিন।
