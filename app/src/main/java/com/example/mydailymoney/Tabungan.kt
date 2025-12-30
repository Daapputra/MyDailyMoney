package com.example.mydailymoney

data class Tabungan(
    val id: String = java.util.UUID.randomUUID().toString(),
    var nama: String,
    var target: Long,
    var terkumpul: Long
)