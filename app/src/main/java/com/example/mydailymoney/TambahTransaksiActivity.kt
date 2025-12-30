package com.example.mydailymoney

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.NumberFormat
import java.util.Locale

class TambahTransaksiActivity : AppCompatActivity() {

    private lateinit var etNominal: EditText
    private lateinit var etKategori: EditText
    private lateinit var etCatatan: EditText
    private lateinit var rgJenis: RadioGroup
    private lateinit var btnSimpan: Button

    private var currentText = ""
    private val PREF_NAME = "mydailymoney_pref"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_transaksi)

        etNominal = findViewById(R.id.etNominal)
        etKategori = findViewById(R.id.etKategori)
        etCatatan = findViewById(R.id.etCatatan)
        rgJenis = findViewById(R.id.rgJenis)
        btnSimpan = findViewById(R.id.btnSimpan)

        setupNominalFormatter()
        setupAction()
    }

    private fun setupNominalFormatter() {
        etNominal.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == currentText) return
                etNominal.removeTextChangedListener(this)

                val clean = s.toString().replace("[Rp,.\\s]".toRegex(), "")
                if (clean.isNotEmpty()) {
                    val value = clean.toLong()
                    val formatted = formatRupiah(value)
                    currentText = formatted
                    etNominal.setText(formatted)
                    etNominal.setSelection(formatted.length)
                }

                etNominal.addTextChangedListener(this)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupAction() {
        btnSimpan.setOnClickListener {

            val nominal = etNominal.text.toString()
                .replace("[Rp,.\\s]".toRegex(), "")
                .toLongOrNull() ?: 0L

            val kategori = etKategori.text.toString().trim()
            val jenisId = rgJenis.checkedRadioButtonId

            // ===== VALIDASI =====
            if (nominal <= 0) {
                etNominal.error = "Nominal wajib diisi"
                etNominal.requestFocus()
                return@setOnClickListener
            }

            if (kategori.isEmpty()) {
                etKategori.error = "Kategori tidak boleh kosong"
                etKategori.requestFocus()
                return@setOnClickListener
            }

            if (jenisId == -1) {
                toast("Pilih jenis transaksi")
                return@setOnClickListener
            }

            val jenis = if (jenisId == R.id.rbMasuk) {
                "Pemasukan"
            } else {
                "Pengeluaran"
            }

            // ===== SIMPAN DATA =====
            val pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = pref.edit()

            val totalMasuk = pref.getLong("totalMasuk", 0L)
            val totalKeluar = pref.getLong("totalKeluar", 0L)

            val listType = object : TypeToken<MutableList<Transaksi>>() {}.type
            val listTransaksi: MutableList<Transaksi> =
                pref.getString("listTransaksi", null)?.let {
                    Gson().fromJson(it, listType)
                } ?: mutableListOf()

            val catatanInput = etCatatan.text.toString().trim()
            val catatan: String? = if (catatanInput.isBlank()) null else catatanInput

            listTransaksi.add(
                Transaksi(
                    kategori = kategori,
                    nominal = nominal,
                    jenis = jenis,
                    timestamp = System.currentTimeMillis(),
                    catatan = catatan
                )
            )

            if (jenis == "Pemasukan") {
                editor.putLong("totalMasuk", totalMasuk + nominal)
            } else {
                editor.putLong("totalKeluar", totalKeluar + nominal)
            }

            editor.putString("listTransaksi", Gson().toJson(listTransaksi))
            editor.apply()

            // ===== TOAST SUKSES =====
            toast("Catatan berhasil ditambahkan")

            setResult(RESULT_OK)
            finish()
        }
    }

    private fun formatRupiah(value: Long): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return formatter.format(value).replace(",00", "")
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
