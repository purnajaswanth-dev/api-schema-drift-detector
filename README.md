# API Schema Drift Detector

A system that detects breaking changes in API response structures to prevent silent failures in dependent services.

## 🚨 Problem

In microservices architectures, services depend on external APIs.  
If an API changes its response (fields removed, types changed), it can break systems without immediate errors.

## 💡 Solution

This application:
- Monitors API responses
- Extracts schema dynamically
- Compares with previous versions
- Detects schema drift

## 🔍 Drift Types Detected

- FIELD_ADDED → new field introduced  
- FIELD_REMOVED → field missing  
- TYPE_CHANGED → data type changed  
- FIELD_MOVED → structure changed  

## ⚙️ How It Works

1. Register an API endpoint  
2. System captures baseline schema  
3. Periodically polls API  
4. Compares new schema with baseline  
5. Logs drift events  

## 🛠 Tech Stack

- Spring Boot  
- MySQL  
- REST APIs  
- Jackson (JSON processing)  

## 📌 Use Cases

- Prevent breaking API integrations  
- Monitor third-party API changes  
- Improve system reliability  

## ▶️ How to Run

1. Configure database in `application.properties`
2. Run Spring Boot application
3. Open `http://localhost:8080`

## ⚠️ Note

Sensitive configs like API keys and DB credentials are not included.
Use your own values in `application.properties`.
