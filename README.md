# 💰 MyDailyMoney

<h3 align="center">MyDailyMoney</h3>

<p align="center">
  <b>Aplikasi Manajemen Keuangan Pribadi Harian Berbasis Android yang Modern, Ringan, dan Mudah Digunakan.</b>
</p>

<p align="center">
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin Badge" /></a>
  <a href="https://developer.android.com/"><img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android Badge" /></a>
  <img src="https://img.shields.io/badge/Min%20SDK-21-orange?style=for-the-badge" alt="Min SDK" />
  <img src="https://img.shields.io/badge/Target%20SDK-36-blue?style=for-the-badge" alt="Target SDK" />
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" alt="License" />
</p>

---

## 📖 Tentang MyDailyMoney

**MyDailyMoney** adalah asisten finansial pribadi cerdas berbasis Android yang dirancang untuk membantu Anda memantau arus kas secara harian. Mulai dari pencatatan pengeluaran (*Expense*) dan pemasukan (*Income*), pembuatan target tabungan (*Savings Goals*), hingga visualisasi statistik keuangan terperinci menggunakan grafik batang dinamis. 

Dengan UI modern yang rapi, ringan, serta aman (penyimpanan lokal penuh), aplikasi ini memudahkan siapa saja untuk mengontrol dan merencanakan masa depan keuangan mereka secara praktis.

---

## ✨ Fitur Utama

Aplikasi ini dilengkapi dengan fitur-fitur esensial untuk memaksimalkan pelacakan finansial Anda:

*   ⚡ **Pencatatan Transaksi Cepat & Instan**
    *   Mencatat arus uang masuk dan keluar hanya dalam hitungan detik.
    *   Pengelompokan otomatis berdasarkan kategori transaksi yang relevan (Belanja, Gaji, Kesehatan, Hiburan, dll.).
*   📊 **Visualisasi Data Interaktif (MPAndroidChart)**
    *   Diagram dan Grafik Batang (*Bar Chart*) dinamis untuk menganalisis perbandingan pengeluaran vs pemasukan harian Anda secara real-time.
    *   Membantu melihat tren finansial Anda secara instan.
*   🎯 **Target Tabungan Impian (Savings Goals)**
    *   Buat resolusi keuangan seperti membeli gadget, liburan, atau dana darurat.
    *   Pantau perkembangan tabungan dengan bilah kemajuan (*Progress Bar*) yang dinamis.
*   👁️ **Keamanan & Privasi Saldo**
    *   Sembunyikan nominal saldo utama Anda di Dashboard dengan sekali ketuk. Sangat berguna untuk menjaga privasi di tempat umum.
*   🔍 **Riwayat & Filter Pintar**
    *   Cari riwayat transaksi lama dengan pencarian instan dan filter berdasarkan rentang waktu atau kategori.
*   🔄 **Reset Data Aman**
    *   Mulai pencatatan dari awal kapan saja dengan fitur hapus data permanen dan aman dari SharedPreferences lokal.

---

## 🛠️ Tech Stack & Library

*   **Bahasa Utama:** [Kotlin](https://kotlinlang.org/) (100% Native Android development).
*   **Arsitektur & UI:**
    *   **Material 3 Design & Components**: Tombol, Cards, Input Field modern dengan UI premium.
    *   **ConstraintLayout**: Layout rendering cepat dan responsif di berbagai ukuran layar.
    *   **RecyclerView & ViewHolders**: Untuk performa list transaksi dan tabungan yang halus tanpa lag.
*   **Penyimpanan Data Lokal (Local Storage):**
    *   `SharedPreferences` & `Google Gson` v2.10.1: Penyimpanan lokal yang cepat tanpa memerlukan setup database yang rumit.
*   **Library Pihak Ketiga:**
    *   📈 [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) v3.1.0: Digunakan untuk memvisualisasikan data grafik keuangan secara interaktif.

---

## 📂 Struktur Folder Proyek

```text
app/src/main/
├── java/com/example/mydailymoney/
│   ├── MainActivity.kt            # Dashboard utama (Ringkasan Saldo, Chart, & List Target Tabungan)
│   ├── TambahTransaksiActivity.kt # Form penginputan transaksi pemasukan/pengeluaran baru
│   ├── RiwayatActivity.kt         # Halaman pencarian & filter riwayat transaksi lengkap
│   ├── LaporanActivity.kt         # Laporan detail keuangan berbasis grafik batang & kategori teratas
│   ├── SettingsActivity.kt        # Pengaturan aplikasi (Tentang, Reset Data, Bantuan)
│   ├── SplashScreenActivity.kt    # Animasi layar pembuka saat meluncurkan aplikasi
│   ├── Transaksi.kt               # Model data transaksi
│   ├── TransaksiAdapter.kt        # Pengelola rendering daftar riwayat transaksi
│   ├── Tabungan.kt                # Model data target tabungan
│   ├── TabunganAdapter.kt         # Pengelola rendering target tabungan
│   └── CategoryAdapter.kt         # Adapter untuk memilih ikon/kategori transaksi
├── res/                           # File resource XML layout, drawable, values, dan mipmap
└── AndroidManifest.xml            # Konfigurasi Activity dan permissions Android
```

---

## 🚀 Cara Menjalankan Proyek di Lokal

Silakan ikuti langkah-langkah di bawah ini untuk memasang dan menjalankan proyek di Android Studio:

### 1. Prasyarat System
*   **Android Studio** versi Koala / Ladybug atau yang terbaru.
*   **Java Development Kit (JDK) 11** atau lebih baru.
*   Perangkat Android fisik (aktifkan USB Debugging) atau Android Virtual Device (AVD / Emulator).

### 2. Kloning Repositori
Jalankan perintah berikut di terminal Anda:
```bash
git clone https://github.com/Daapputra/MyDailyMoney.git
cd MyDailyMoney
```

### 3. Buka di Android Studio
1. Pilih menu **Open An Existing Project** di Android Studio.
2. Arahkan dan pilih folder **MyDailyMoney**.
3. Tunggu proses **Gradle Sync & Build** selesai secara otomatis (membutuhkan koneksi internet untuk mengunduh dependencies).

### 4. Jalankan Aplikasi
*   Pilih perangkat target (Emulator atau Device Fisik).
*   Klik ikon **Run (tombol segitiga hijau)** atau gunakan shortcut `Shift + F10` untuk mengompilasi dan memasang aplikasi ke perangkat Anda.

---

## 👤 Kontributor

Kontributor utama proyek ini:

*   **Muhammad Nur Daffa Naufal Putra** - [@Daapputra](https://github.com/Daapputra)

---
