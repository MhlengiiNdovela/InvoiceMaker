# Invoice Maker — Android Application

A native Android application developed using Kotlin and Jetpack Compose for creating invoices, converting totals between multiple currencies, and sending invoices directly to clients via email.

This repository contains the full source code and configuration for the Invoice Maker prototype.

## Project Structure

The project is structured with a root repository folder and the Android project contained within the `InvoiceMaker` subdirectory:

* **`InvoiceMaker/`**: The core Android Studio project.
  * **`app/src/main/java/com/example/invoicemaker/`**
    * `data/model/`: Client, Invoice, and LineItem data classes.
    * `data/repository/`: AuthRepository (Firebase Auth) and InvoiceRepository (Cloud Firestore).
    * `network/`: Retrofit interfaces and data models for ExchangeRate-API and SendGrid.
    * `ui/auth/`: Login and Registration screens along with the AuthViewModel.
    * `ui/invoice/`: Invoice listing and creation screens along with the InvoiceViewModel.
    * `ui/navigation/`: Compose navigation graphs and destination routing.
    * `MainActivity.kt`: The main entry point of the application.

## Features Implemented

- **Authentication:** Secure user registration and login handled entirely via Firebase Authentication.
- **Invoice Management:** Create complete invoices detailing client info and multiple custom line items.
- **Currency Conversion:** Integrated with [ExchangeRate-API](https://www.exchangerate-api.com/) using Retrofit to dynamically convert invoice totals into any selected display currency.
- **Database Storage:** Real-time persistence and querying hosted via Google Cloud Firestore.
- **Email Delivery:** Direct delivery of invoices to clients using the [SendGrid](https://sendgrid.com/) email delivery platform via Retrofit.
- **Input Validation:** Safe parsing and validation for text and numeric inputs to prevent app crashes.
- **Testing:** Unit tests verifying core invoice math calculations (`app/src/test/...`).
- **CI/CD:** Automated GitHub Actions workflow (`.github/workflows/build.yml`) to test and build a debug APK on every push.

## Getting Started & Setup

To run this project locally, you must complete the following configuration steps:

### 1. Firebase Configuration
1. Create a new project in the [Firebase Console](https://console.firebase.google.com/).
2. Add an Android App with the package name `com.example.invoicemaker`.
3. Enable **Email/Password** authentication under *Authentication -> Sign-in method*.
4. Enable **Cloud Firestore** in test mode or with the following recommended rules:
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /invoices/{invoiceId} {
         allow read, update, delete: if request.auth != null && resource.data.ownerUid == request.auth.uid;
         allow create: if request.auth != null && request.resource.data.ownerUid == request.auth.uid;
       }
     }
   }
   ```
5. Download your custom `google-services.json` and place it in the `InvoiceMaker/app/` directory.

### 2. External API Keys Setup
* **ExchangeRate-API:** Sign up at [ExchangeRate-API](https://www.exchangerate-api.com/) and replace `YOUR_EXCHANGERATE_API_KEY` in `InvoiceMaker/app/build.gradle.kts` with your real key.
* **SendGrid:** Sign up at [SendGrid](https://sendgrid.com/), create an API key with Mail Send permissions, and replace `YOUR_SENDGRID_API_KEY` in `InvoiceMaker/app/build.gradle.kts`. Ensure the `fromEmail` used matches your verified sender address.

### 3. Open and Build
1. Open Android Studio and choose **Open an existing project**, targeting the `InvoiceMaker/` subdirectory.
2. Let Gradle sync and download all dependencies automatically.
3. Build and execute on an Android Emulator or physical device.

---
*Note: Do not commit real API keys or your `google-services.json` file to public repositories. Ensure they remain git-ignored or injected securely via environment/CI variables.*
