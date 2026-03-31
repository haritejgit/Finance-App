# Project Context: Village Finance Management App (Native Android)

## Project Overview
A specialized Ledger and Loan Management application for field employees to track micro-loans across villages. Designed for offline-first reliability and easy ledger tracking.

## Tech Stack
- **Frontend:** Kotlin with Jetpack Compose (Modern UI)
- **Local Database:** Room (for offline data persistence)
- **Remote Backend:** Supabase (Auth, PostgreSQL for sync, Edge Functions)
- **Dependency Injection:** Hilt
- **Excel Export:** Apache POI or Simple-Excel-Kotlin
- **Architecture:** MVVM (Model-View-ViewModel)

---

## Database Schema (Local Room & Remote Supabase)

### 1. Villages
- `id`: UUID (PK)
- `name`: String
- `employee_id`: UUID (FK to auth.users)
- `day_of_week`: Enum (Monday, Tuesday, etc.)

### 2. Customers
- `id`: UUID (PK)
- `numerical_id`: Int (Reusable "Book Number")
- `name`: String
- `phone`: String
- `aadhar`: String
- `location_desc`: String
- `co_name`: String (Optional)
- `village_id`: UUID (FK)
- `is_active`: Boolean (Default: true)

### 3. Loans
- `id`: UUID (PK)
- `customer_id`: UUID (FK)
- `principal_amount`: Double (e.g., 5000)
- `interest_amount`: Double (Default 20% = 1000)
- `total_payable`: Double (6000)
- `balance_amount`: Double
- `start_date`: Long (Timestamp)
- `status`: String ("ACTIVE", "CLOSED", "RENEWED")

### 4. Payments (Ledger Entries)
- `id`: UUID (PK)
- `loan_id`: UUID (FK)
- `amount_paid`: Double
- `payment_date`: Long
- `week_number`: Int (Calculated relative to loan start)

---

## Core Logic Implementation

### 1. The Flexible Ledger (Logic)
- **View:** A vertical list representing weeks.
- **Color Coding:** 
    - If `sum(payments)` for a week >= target: **Green**
    - If `sum(payments)` for a week < target and date passed: **Red**
- **Flexibility:** Users can tap any week to add any amount. The `balance_amount` in the `Loans` table is updated reactively.

### 2. Numerical ID Management
- When a loan is "CLOSED" (Balance = 0) and the user marks the customer as inactive, that `numerical_id` is added to a "Available IDs" pool.
- New customers pull the smallest available integer.

### 3. Loan Renewal
- User selects "Renew".
- Current `balance_amount` is fetched.
- New `principal` is added. 
- New `total_payable` = `old_balance` + (`new_principal` * 1.2).
- The old loan is marked "RENEWED" and linked to the new loan ID for history tracking.

---

## Android Project Structure
- `data/`: Room entities, DAOs, Supabase repositories.
- `domain/`: UseCases (CalculateInterest, RenewLoan, ExportExcel).
- `ui/`: 
    - `auth/`: Login/Register screens.
    - `dashboard/`: Day/Shift/Village selection.
    - `village/`: Customer list and Village management.
    - `customer/`: Profile, Ledger view, Payment entry.
    - `reports/`: Date picker and Excel generation logic.

## Reports (Excel)
- Uses a `ContentProvider` to save the generated `.xlsx` file to the device's "Downloads" folder.
- Filter: `WHERE payment_date BETWEEN start_date AND end_date`.
