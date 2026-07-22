# Local Setup — H-Phsar API

How to get the API running on your own machine without ever committing a real
secret. If you only want the quick path, jump to [Quick start](#quick-start).

---

## 1. Required software

| Tool | Version | Notes |
|---|---|---|
| JDK | 17 (project currently targets Java 17 — see `pom.xml`) | `java -version` to check |
| PostgreSQL | 13+ | Local install, Docker container, or a cloud instance you control |
| Git | any recent version | |
| Maven | none required | this repo ships the Maven Wrapper (`mvnw` / `mvnw.cmd`) — don't need Maven installed globally |

---

## 2. PostgreSQL setup

Create an empty local database for the app to use. Example (`psql`):

```sql
CREATE DATABASE h_phsar;
CREATE USER h_phsar_dev WITH PASSWORD 'choose-your-own-local-password';
GRANT ALL PRIVILEGES ON DATABASE h_phsar TO h_phsar_dev;
```

Table creation itself is **not** handled by a migration tool yet — on startup,
`config/DatabaseInitializer.java` runs the DDL needed to bring the schema up to
date (this is a known, pre-existing limitation, not something this guide
changes). Nothing further to run manually beyond having an empty database the
app user can connect to.

Use your own username/password here — never the ones from any shared example
or from another developer's message.

---

## 3. Environment-variable setup

The app reads all secrets and per-environment values from environment
variables — nothing is hardcoded in `application.yaml` (on purpose; a missing
value now means the app **fails to start** with a clear error instead of
silently using a guessable default).

Required variables (see `.env.example` for the full list with placeholders):

| Variable | Purpose |
|---|---|
| `DB_URL` | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/h_phsar` |
| `DB_USERNAME` | Database user |
| `DB_PASSWORD` | Database password |
| `MAIL_HOST` | SMTP host (e.g. `smtp.gmail.com`) |
| `MAIL_PORT` | SMTP port (defaults to `587` if unset) |
| `MAIL_USERNAME` | SMTP account username |
| `MAIL_PASSWORD` | SMTP account password (for Gmail: an **app password**, not your login password) |
| `JWT_SECRET` | Signing key for issued JWTs — long, random, at least 64 characters |
| `FILE_UPLOAD_DIR` | Where uploaded files land locally (defaults to `uploads` if unset) |
| `SPRING_PROFILES_ACTIVE` | Which profile to run: `local`, `test`, or `prod` |

## Quick start

```powershell
# 1. Copy the example files
Copy-Item .env.example .env
Copy-Item src\main\resources\application-local.yaml.example src\main\resources\application-local.yaml

# 2. Edit both copies with your own local DB/mail/JWT values (see sections 4-8 below)
notepad .env
notepad src\main\resources\application-local.yaml

# 3. Load the .env values into your PowerShell session (no dotenv tool needed)
Get-Content .env | Where-Object { $_ -match '^\s*[^#].*=' } | ForEach-Object {
    $name, $value = $_.Split('=', 2)
    Set-Item -Path "Env:$($name.Trim())" -Value $value.Trim()
}

# 4. Run with the local profile
.\mvnw.cmd spring-boot:run
```

Bash / macOS / Linux equivalent for step 3: `set -a; source .env; set +a`.

`application-local.yaml` is optional — everything it contains can also be
supplied purely through the environment variables in `.env`. Use whichever is
more convenient; `application.yaml` already defaults to the `local` profile
when none is specified, so `application-local.yaml` (if present) is picked up
automatically.

---

## 4. How to copy `.env.example`

```powershell
Copy-Item .env.example .env
```
```bash
cp .env.example .env
```

Then edit `.env` with a real editor and fill in your own values. `.env` is
gitignored — `git status` should never show it as a tracked change.

---

## 5. How to start with the local profile

`spring.profiles.default: local` in `application.yaml` means the `local`
profile is used automatically when `SPRING_PROFILES_ACTIVE` isn't set. To be
explicit (or to run a different profile), set it yourself:

```powershell
$env:SPRING_PROFILES_ACTIVE = "local"
.\mvnw.cmd spring-boot:run
```

---

## 6. How to avoid committing secrets

- Never put a real password, API key, or JWT secret directly into
  `application.yaml`, `application-test.yaml`, or `application-prod.yaml` —
  those three files are tracked in Git and shared with everyone who clones
  the repo. They should only ever contain `${ENV_VAR}` placeholders (or, for
  `application-test.yaml`, values that are obviously fake/test-only).
- Real values belong in `.env` (gitignored) or `application-local.yaml`
  (gitignored) — never in the `.example` counterparts.
- Before committing, run `git status` and `git diff --staged` and look twice
  at anything that isn't source code you meant to change.
- If you ever commit a real secret by accident: treat it as compromised,
  rotate it immediately (see the rotation checklist from the security-hardening
  step), and only then worry about cleaning history — a rotated secret makes
  history-scrubbing far less urgent.

---

## 7. How to configure mail safely

- For Gmail: enable 2-Step Verification on the account, then generate an
  **App Password** (Google Account → Security → App passwords). Use that
  16-character app password as `MAIL_PASSWORD` — never your real account
  password.
- For any other SMTP provider, use their equivalent of an app-specific or API
  password, not a personal login password.
- Never share a real `MAIL_PASSWORD` in chat, a ticket, or a commit message.

---

## 8. How to create a local JWT secret

Any sufficiently long random string works. Two options:

```bash
openssl rand -base64 64
```
```powershell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))
```

Paste the result as `JWT_SECRET` in your `.env` (or `jwt.secret` in your local
`application-local.yaml`). Use a different value per environment — never reuse
your local secret in a shared or production deployment.

---

## 9. Files that must never be committed

| File | Why |
|---|---|
| `.env` | Contains your real local secrets |
| `application-local.yaml` | Contains your real local secrets |
| `application-secrets.yaml` / `.yml` | Reserved name for any future secrets file — never commit one under this name |
| `*.jks`, `*.p12`, `*.pfx`, `*.pem`, `*.key` | Keystores/certificates/private keys |

All of the above are already excluded in `.gitignore`. If `git status` ever
shows one of them as trackable, stop and double-check before staging.

---

## 10. Troubleshooting missing environment variables

**Symptom:** the app fails immediately on startup with an error like
`Could not resolve placeholder 'DB_URL' in value "${DB_URL}"`.

This is intentional — it means a required environment variable isn't set,
rather than the app silently falling back to a weak default. Fix:

1. Confirm `.env` exists and has a real value for the variable named in the
   error (not still `replace_me`).
2. Confirm your shell actually loaded `.env` into the environment this
   session (see the Quick start step 3 command) — copying the file is not
   enough by itself.
3. If running from an IDE, check its run/debug configuration has the same
   environment variables set (IDEs don't read `.env` automatically).
4. Re-run `.\mvnw.cmd spring-boot:run` (or your IDE run configuration) after
   confirming the variable is set: `echo $env:DB_URL` (PowerShell) or
   `echo $DB_URL` (bash).

**Symptom:** `jwt.secret` placeholder error even though `.env` looks right.

Check for a stray `application-local.yaml` that still has the placeholder
text from the `.example` file (`replace_with_a_long_random_local_secret...`)
— a leftover placeholder there overrides your `.env` value for that key.
