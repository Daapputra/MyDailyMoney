# 💰 MyDailyMoney

**MyDailyMoney** adalah aplikasi manajemen keuangan pribadi yang dirancang untuk membantu pengguna mencatat, memantau, dan menganalisis pendapatan serta pengeluaran harian dengan mudah. Proyek ini dibangun untuk memberikan visibilitas penuh terhadap kondisi finansial secara *real-time* dan membantu pengguna mengambil keputusan keuangan yang lebih cerdas.

---

## ✨ Fitur Utama

*   **Pencatatan Transaksi Cepat:** Catat pemasukan (income) dan pengeluaran (expense) hanya dalam beberapa klik.
*   **Kategorisasi Pintar:** Klasifikasikan transaksi berdasarkan kategori (makanan, transportasi, tagihan, investasi, dll.).
*   **Dasbor Analitik (Dashboard):** Visualisasi data keuangan berupa grafik dan ringkasan total saldo untuk memantau sirkulasi uang.
*   **Riwayat Transaksi:** Riwayat lengkap yang dapat difilter berdasarkan tanggal, bulan, atau kategori tertentu.
*   **Arsitektur Modern:** Kode yang bersih, modular, dan siap untuk dikembangkan lebih lanjut.

---

## 🚀 Teknologi yang Digunakan

Aplikasi ini dibangun menggunakan *tech stack* modern:

### **Frontend / Backend**
> *Catatan: Silakan sesuaikan daftar ini dengan teknologi aktual yang kamu gunakan di proyek ini (contoh di bawah menggunakan Vue/Nest/Node).*
*   **Frontend:** [Vue.js 3](https://vuejs.org/) / [Tailwind CSS](https://tailwindcss.com/)
*   **Backend:** [NestJS](https://nestjs.com/) / [Node.js](https://nodejs.org/)
*   **Database & ORM:** PostgreSQL / MySQL dengan [Prisma ORM](https://www.prisma.io/)
*   **Containerization:** [Docker](https://www.docker.com/)

---

## 🛠️ Cara Menjalankan Proyek di Lokal

Ikuti langkah-langkah berikut untuk menjalankan proyek ini di komputer lokal kamu:

### **Prasyarat**
Pastikan kamu sudah menginstal perangkat lunak berikut:
*   [Node.js](https://nodejs.org/) (versi LTS direkomendasikan)
*   [Docker](https://www.docker.com/) (jika menggunakan database/container)
*   Package Manager (`npm`, `yarn`, atau `pnpm`)

### **Langkah-Langkah Instalasi**

1.  **Clone Repositori**
```bash
    git clone [https://github.com/Daapputra/MyDailyMoney.git](https://github.com/Daapputra/MyDailyMoney.git)
    cd MyDailyMoney
    ```

2.  **Instalasi Dependensi**
    Jika proyek ini menggunakan arsitektur monorepo atau terpisah, masuk ke folder masing-masing (`frontend`/`backend`) atau jalankan di *root*:
```bash
    npm install
    # atau
    yarn install
    ```

3.  **Konfigurasi Environment Variables**
    Salin file `.env.example` menjadi `.env` dan sesuaikan konfigurasinya (Database URL, Port, dll.).
```bash
    cp .env.example .env
    ```

4.  **Menjalankan Database (Jika menggunakan Docker)**
```bash
    docker-compose up -d
    ```

5.  **Menjalankan Migrasi Database**
```bash
    npx prisma migrate dev
    ```

6.  **Menjalankan Aplikasi (Mode Pengembangan)**
```bash
    npm run dev
    # atau jika menggunakan NestJS/Backend
    npm run start:dev
    ```

Aplikasi sekarang dapat diakses melalui browser di `http://localhost:3000` (atau port yang disesuaikan).

---

## 📸 Tangkapan Layar (Screenshots)

| Dashboard Utama | Riwayat Transaksi |
| :---: | :---: |
| _[Tambahkan gambar/screenshot di sini]_ | _[Tambahkan gambar/screenshot di sini]_ |

---

## 👤 Kontributor

*   **Muhammad Nur Daffa Naufal Putra** - [*Daapputra*](https://github.com/Daapputra)

---

## 📄 Lisensi

Proyek ini dilisensikan di bawah **MIT License** - lihat file [LICENSE](LICENSE) untuk detail lebih lanjut.
