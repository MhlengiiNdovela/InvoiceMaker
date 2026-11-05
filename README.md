# Invoice Maker

Invoice Maker is an Android app for creating invoices, converting the total from South African rand (ZAR) into another currency, saving a PDF on the device, and emailing the invoice to the client.

The phone app is the whole client. Accounts and invoices live in one shared [Supabase](https://supabase.com/) project. Supabase hosts the database and turns those tables into a REST API. This repository does not contain a separate server to deploy.

Package name: `com.example.invoicemaker`

Shared project:

- Dashboard: https://supabase.com/dashboard/project/eerrwpllknozdfvhlzph
- API URL: `https://eerrwpllknozdfvhlzph.supabase.co`

## What a user can do

1. **Register** with an email address and a password of at least 6 characters. The confirm-password field must match. Supabase stores the password. The app never stores it.
2. **Log in** and stay signed in. The session is saved on the device and restored the next time the app opens.
3. **See only their own invoices** on the My Invoices screen. An empty list says to tap **+**.
4. **Create an invoice** with an invoice number, client name, client email, and one or more line items (description, quantity, unit price). Line items with a blank description are left out of the save.
5. **Watch the total convert** from ZAR into a 3-letter display currency such as USD, GBP, or EUR. The rate comes from ExchangeRate-API. If the currencies match, the rate is 1.
6. **Save the invoice** to Supabase. New invoices are stored as `DRAFT`.
7. **Open an invoice** and edit the number, client, currency, status (`DRAFT`, `SENT`, or `PAID`), notes, and line items, then tap **Update Invoice**.
8. **Save as PDF** after a successful create, or from the detail screen. Android opens the system save dialog. The PDF is one A4-sized page drawn on the device. It includes the number, date, client, line items, and total.
9. **Email the invoice** as plain text through SendGrid. The address in the app code must be a verified SendGrid sender. See [Email](#email).
10. **Delete an invoice.** The database deletes its line items as well.
11. **Sign out** from the list screen. The local session is cleared immediately, then the app tells Supabase to revoke the access token.

## Tech stack

| Piece | Choice |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose |
| HTTP | Retrofit 2.11, OkHttp, Gson |
| Async | Kotlin coroutines |
| Accounts and data | Supabase Auth and Supabase PostgREST |
| Currency | [ExchangeRate-API](https://www.exchangerate-api.com/) v6 |
| Email | [SendGrid](https://sendgrid.com/) Mail Send API |
| PDF | Android `PdfDocument` on the device |
| Min / target SDK | 24 / 34 |
| JDK | 17 |
| Android Gradle Plugin | 8.5.0 |
| Kotlin | 1.9.24 |
| Compose compiler | 1.5.14 |

## How the app is put together

```
UI screens and ViewModels
        │
        ▼
AuthRepository          InvoiceRepository
        │                       │
        ▼                       ▼
Supabase Auth REST      Supabase Data REST
/auth/v1/...            /rest/v1/invoices
                        /rest/v1/line_items
```

Exchange rates and email do not go through Supabase. `RetrofitClient` builds three clients:

- Supabase, base URL from `BuildConfig.SUPABASE_URL`
- `https://v6.exchangerate-api.com/`
- `https://api.sendgrid.com/`

### Auth

`AuthRepository` calls:

- `POST /auth/v1/signup` to register
- `POST /auth/v1/token?grant_type=password` to log in
- `POST /auth/v1/token?grant_type=refresh_token` when the access token is near expiry
- `POST /auth/v1/logout` on sign out

Every Supabase call sends the publishable key in the `apikey` header. Calls that read or change invoices also send `Authorization: Bearer <user access token>`.

`SessionStore` keeps the access token, refresh token, user id, email, and expiry in private app preferences named `supabase_session`. `InvoiceMakerApplication` loads that store when the process starts. `SupabaseGateway` refreshes the access token about 60 seconds before it expires, and once more if a call comes back `401`.

Signup only signs the user in when Supabase returns a session. That happens when **Confirm email** is off. If it is on, the account is created and the app shows “No session was returned”.

### Invoices

The phone model is `Invoice`, `Client`, and `LineItem`. The database uses snake_case columns. `InvoiceRepository` maps between them.

| App | Supabase |
| --- | --- |
| `ownerUid` | `invoices.owner_id` |
| `invoiceNumber` | `invoice_number` |
| `client.name` / `email` / `address` / `phone` | `client_name`, `client_email`, `client_address`, `client_phone` |
| `baseCurrency` | `base_currency` (the screens always save `ZAR`) |
| `displayCurrency` | `display_currency` |
| `exchangeRate` | `exchange_rate` (display total ÷ ZAR subtotal) |
| `createdAtMillis` / `dueDateMillis` | `created_at` / `due_at` as UTC timestamps |
| `status` | `status` |
| `notes` | `notes` |
| `LineItem.unitPrice` | `line_items.unit_price` |
| line order on screen | `line_items.position` |

`IsoTimes` converts epoch milliseconds to strings like `2026-11-05T13:24:00Z` and back.

Create sends the invoice, reads the new id from the response, then writes the line items. Update patches the invoice, deletes that invoice’s line items, and inserts the current list. Delete removes the invoice row. The foreign key deletes the lines.

List and get use `select=*,line_items(*)`, so each invoice comes back with its lines. The list is filtered with `owner_id=eq.<user id>` and ordered by `created_at.desc`. Row Level Security applies the same owner rule inside the database, even if a request forgets the filter.

The create screen collects client name and email only. Address and phone exist on the model and in the table, and they round-trip if they are already stored. Notes are edited on the detail screen.

### Screens

`AppNavigation` owns the routes:

| Route | Screen |
| --- | --- |
| `login` | `LoginScreen` |
| `register` | `RegisterScreen` |
| `invoice_list` | `InvoiceListScreen` |
| `create_invoice` | `CreateInvoiceScreen` |
| `invoice_detail` | `InvoiceDetailScreen` |

A saved session opens the list. Otherwise the app opens login.

## Repository layout

The Android project **is** the repository root. Open this folder in Android Studio. There is no nested `InvoiceMaker/` project.

```
app/src/main/java/com/example/invoicemaker/
  InvoiceMakerApplication.kt    starts SessionStore
  MainActivity.kt               Compose entry point
  data/model/                   Invoice, Client, LineItem
  data/SessionStore.kt          saved login session
  data/repository/              AuthRepository, InvoiceRepository
  network/                      Retrofit clients, Supabase models, IsoTimes
  ui/auth/                      login, register, AuthViewModel
  ui/invoice/                   list, create, detail, InvoiceViewModel
  ui/navigation/                routes
  ui/theme/                     Material theme
app/src/test/                   invoice math and timestamp tests
supabase/schema.sql             tables, indexes, row level security
.github/workflows/build.yml     unit tests and debug APK on every push to main
```

## Setup for a group member

You need Android Studio with Android SDK 34 and a JDK 17. Android Studio’s bundled JDK is enough. You also need an emulator or a phone with USB debugging. The app needs a network connection.

### 1. Clone and open the repo root

Open the folder that contains `settings.gradle.kts`. Let Gradle sync.

### 2. Add the Supabase values to `local.properties`

This file is created by Android Studio and is gitignored. It already has an `sdk.dir` line. **Add** the two lines below. Do not delete `sdk.dir`.

```properties
sdk.dir=C:\\Users\\You\\AppData\\Local\\Android\\Sdk
SUPABASE_URL=https://eerrwpllknozdfvhlzph.supabase.co
SUPABASE_ANON_KEY=sb_publishable_...
```

`SUPABASE_ANON_KEY` is the project **publishable** key (`sb_publishable_...`), from **Project Settings → API** in the dashboard. Ask the person who created the project if you cannot see that page. Do not put the secret key (`sb_secret_...`) in the app. Do not put the Postgres password in the app.

Gradle reads those two properties into `BuildConfig`. The same names can be set as environment variables, which is what CI can use. A missing key still compiles. Login and save then fail with “Add SUPABASE_URL and SUPABASE_ANON_KEY to local.properties”.

### 3. Run

Run the `app` configuration on an emulator or device. Register a new email. Each person should use their own account. Invoices are private to the signed-in user.

Command-line equivalent, after `JAVA_HOME` points at a JDK 17:

```bat
gradlew.bat installDebug
```

## Supabase project our group is using

The schema in `supabase/schema.sql` has already been applied to `eerrwpllknozdfvhlzph`. You do not need to run it again for this shared project.

These Auth settings have to stay like this, or the app breaks:

| Setting | Required value | What goes wrong |
| --- | --- | --- |
| Email provider | On | “Email signups are disabled” |
| Confirm email | Off | Account is created, but the app says no session was returned |
| Allow new users to sign up | On | “Signups not allowed for this instance” |

The page that holds those switches is **Authentication → Sign In / Providers → Email**. The menu item is named **Sign In / Providers**, not Providers on its own.

Direct link: https://supabase.com/dashboard/project/eerrwpllknozdfvhlzph/auth/providers

If Confirm email was on when someone registered, their account already exists. After the switch is off, they should use **Log in**, not Register again.

### What the schema creates

`public.invoices` and `public.line_items`.

- `invoices.owner_id` defaults to `auth.uid()` and points at `auth.users`. Deleting the auth user deletes their invoices.
- `line_items.invoice_id` points at `invoices`. Deleting an invoice deletes its lines.
- Row Level Security is on. The `authenticated` role can select, insert, update, and delete only rows owned by `auth.uid()`. Line items are allowed only when the parent invoice belongs to that user.
- The anon role is not granted access. A request with only the publishable key and no user token sees no rows.

### There is no separate API to deploy

After the tables exist, Supabase serves them at:

- `https://eerrwpllknozdfvhlzph.supabase.co/auth/v1/`
- `https://eerrwpllknozdfvhlzph.supabase.co/rest/v1/invoices`
- `https://eerrwpllknozdfvhlzph.supabase.co/rest/v1/line_items`

The Postgres connection string (`postgresql://postgres:...@db.eerrwpllknozdfvhlzph.supabase.co:5432/postgres`) is for the Supabase SQL editor, `psql`, or a desktop database tool. The Android app does not use it. Never commit the database password.

### Starting a brand-new Supabase project

Only do this if the group is moving off the shared project.

1. Create a project at https://supabase.com/dashboard.
2. Open **SQL Editor**, paste `supabase/schema.sql`, and run it once.
3. Open **Authentication → Sign In / Providers → Email**. Turn the Email provider **on**. Turn **Confirm email** **off**. Leave new signups allowed.
4. Copy the project URL and publishable key into `local.properties`.

## Currency and email keys

`EXCHANGE_RATE_API_KEY` and `SENDGRID_API_KEY` are `buildConfigField` values in `app/build.gradle.kts`. Replace them with keys from your own accounts before you rely on live conversion or real email. Do not commit a new private key if this repository is public.

- ExchangeRate-API: https://www.exchangerate-api.com/
- SendGrid: an API key with Mail Send, plus a verified sender address.

The sender address is hardcoded in `CreateInvoiceScreen` and `InvoiceDetailScreen` as `fromEmail`. It must match a verified sender in that SendGrid account.

`InvoiceViewModel.sendInvoiceEmail` treats HTTP 401 from SendGrid as success and shows “Invoice emailed to client!”. That path exists so a missing or rejected SendGrid key does not block the demo. A real send is only a 202 from SendGrid. Any other error code is shown as a failure.

## Tests and CI

Unit tests:

```bat
gradlew.bat testDebugUnitTest
```

- `InvoiceCalculationsTest` checks line totals, the subtotal, and the converted total.
- `IsoTimesTest` checks the UTC timestamp conversion used for Supabase.

GitHub Actions (`.github/workflows/build.yml`) runs those tests and builds a debug APK on every push and pull request to `main`. The workflow runner does not have `local.properties`, so that APK has an empty Supabase URL and cannot log in until `SUPABASE_URL` and `SUPABASE_ANON_KEY` are provided as environment variables or the APK is built on a machine that has `local.properties`.

## Troubleshooting

| What you see | Cause | Fix |
| --- | --- | --- |
| Email signups are disabled | Email provider is off | Turn Email on under Sign In / Providers |
| No session was returned | Confirm email is still on | Turn Confirm email off, then log in with the account you already created |
| Add SUPABASE_URL and SUPABASE_ANON_KEY to local.properties | Gradle did not see the keys | Add both lines to `local.properties` at the repo root and rebuild |
| SDK location not found | `local.properties` lost `sdk.dir` | Let Android Studio recreate it, then put the Supabase lines back |
| Session expired. Log in again | Refresh token was rejected | Sign in again |
| Save failed, permission or row-level security | The access token does not own that invoice | Stay on the account that created it |
| Total stays in ZAR or conversion shows nothing | ExchangeRate-API key or network failed | Check the key in `app/build.gradle.kts` and the device network |
| Email says it sent, but the client got nothing | SendGrid returned 401 and the app treated that as a demo success | Use a real Mail Send key and a verified `fromEmail` |

## What not to commit

`.gitignore` already excludes:

- `local.properties` (SDK path, Supabase URL, publishable key)
- `google-services.json` (left over from an earlier Firebase experiment; the app does not use Firebase)
- build outputs, `.gradle/`, and `.idea/`

The publishable key is safe to ship inside an APK because Row Level Security still limits each user to their own rows. It is still kept out of git so the key can be rotated without a code change. The secret key and the database password must never go in the app, the README, or git.
