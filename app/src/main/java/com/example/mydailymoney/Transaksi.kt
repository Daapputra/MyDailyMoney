package com.example.mydailymoney

data class Transaksi(
    val kategori: String,
    val jenis: String,
    val nominal: Long,
    val catatan: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
