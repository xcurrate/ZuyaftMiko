# Root Suspender (Android 10, Root)

Aplikasi Android untuk:
- suspend beberapa aplikasi (manual & periodik)
- force stop beberapa aplikasi (manual & periodik)
- suspend beberapa aplikasi saat aplikasi trigger terbuka
- force stop beberapa aplikasi saat aplikasi trigger terbuka
- otomatis **unsuspend** beberapa aplikasi saat trigger app ditutup / tidak lagi aktif

## Cara pakai singkat
1. Install APK.
2. Buka app `Root Suspender`.
3. Isi daftar package name (satu per baris).
4. Beri izin **Usage Access** (tombol di app).
5. Tap **Start Watcher**.

> Aplikasi menjalankan command root `pm suspend <package>` dan `am force-stop <package>` via `su`.

## Build APK di cloud (GitHub Actions)

### 1) Push source ke GitHub
```bash
git init
git add .
git commit -m "init root suspender"
git branch -M main
git remote add origin https://github.com/USERNAME/REPO.git
git push -u origin main
```

### 2) Tambahkan workflow
Buat file `.github/workflows/android.yml`:

```yaml
name: Android CI

on:
  workflow_dispatch:
  push:
    branches: [ "main" ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup JDK
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Setup Android SDK
        uses: android-actions/setup-android@v3

      - name: Grant gradle execute
        run: chmod +x gradlew

      - name: Build debug APK
        run: ./gradlew :app:assembleDebug

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk
```

### 3) Tambahkan Gradle Wrapper (sekali)
Di komputer mana saja yang ada gradle:
```bash
gradle wrapper
```
Lalu commit file wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/*`).

### 4) Build dari web GitHub
- Buka tab **Actions**
- Jalankan workflow **Android CI**
- Download artifact `app-debug` -> `app-debug.apk`

## Catatan penting
- Pastikan package yang disuspend bukan aplikasi sistem kritikal.
- Beberapa ROM butuh Magisk + grant root manual ke app.
- Jika command gagal, cek root shell: `su -c id`.


## Konsep Trigger Otomatis
- Saat app trigger terdeteksi berjalan di foreground: daftar `Suspend saat trigger aktif` + `Force-stop saat trigger aktif` dieksekusi.
- Saat app trigger ditutup / pindah ke app lain: daftar `Auto unsuspend saat trigger ditutup` dieksekusi otomatis dengan `pm unsuspend`.
