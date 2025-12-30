package com.example.mydailymoney

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.NumberFormat
import java.util.*
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

class MainActivity : AppCompatActivity() {

    // ===== UI =====
    private lateinit var vfSaldo: ViewFlipper
    private lateinit var tvSaldo1: TextView
    private lateinit var tvSaldo2: TextView
    private lateinit var tvMasuk: TextView
    private lateinit var tvKeluar: TextView
    private lateinit var btnTambah: View
    private lateinit var btnReset: Button
    private lateinit var navPengaturan: LinearLayout
    private lateinit var navRiwayat: LinearLayout
    private lateinit var navLaporan: LinearLayout
    private lateinit var pieChartSummary: PieChart
    
    // ===== TABUNGAN =====
    private lateinit var rvTabungan: RecyclerView
    private lateinit var btnTambahTabungan: Button
    private val listTabungan = mutableListOf<Tabungan>()
    private lateinit var tabunganAdapter: TabunganAdapter

    // ===== DATA =====
    private val listTransaksi = mutableListOf<Transaksi>()

    private var totalMasuk = 0L
    private var totalKeluar = 0L
    private var lastSaldo = 0L

    private val PREF_NAME = "mydailymoney_pref"

    private val tambahTransaksiLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadData()
            updateUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        setupPieChart()
        loadData()
        loadTabungan()
        setupTabunganRecycler()
        setupAction()
        updateUI()
    }

    private fun initView() {
        vfSaldo = findViewById(R.id.vfSaldo)
        tvSaldo1 = findViewById(R.id.tvSaldo1)
        tvSaldo2 = findViewById(R.id.tvSaldo2)
        tvMasuk = findViewById(R.id.tvMasuk)
        tvKeluar = findViewById(R.id.tvKeluar)
        btnTambah = findViewById(R.id.btnTambah)
        btnReset = findViewById(R.id.btnReset)
        navPengaturan = findViewById(R.id.navPengaturan)
        navRiwayat = findViewById(R.id.navRiwayat)
        navLaporan = findViewById(R.id.navLaporan)
        pieChartSummary = findViewById(R.id.pieChartSummary)
        
        rvTabungan = findViewById(R.id.rvTabungan)
        btnTambahTabungan = findViewById(R.id.btnTambahTabungan)
    }

    private fun setupTabunganRecycler() {
        tabunganAdapter = TabunganAdapter(listTabungan) { selectedTabungan ->
            showEditTabunganDialog(selectedTabungan)
        }
        rvTabungan.layoutManager = LinearLayoutManager(this)
        rvTabungan.adapter = tabunganAdapter
    }
    
    private fun loadTabungan() {
        listTabungan.clear()
        val pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = pref.getString("listTabungan", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<Tabungan>>() {}.type
            listTabungan.addAll(Gson().fromJson(json, type))
        } else {
            // Default example if none exists
            if (listTabungan.isEmpty()) {
                 listTabungan.add(Tabungan(nama = "Liburan Akhir Tahun", target = 5000000, terkumpul = 1500000))
            }
        }
    }
    
    private fun saveTabungan() {
        val pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().putString("listTabungan", Gson().toJson(listTabungan)).apply()
        tabunganAdapter.notifyDataSetChanged()
    }

    private fun setupAction() {
        btnTambah.setOnClickListener {
            val intent = Intent(this, TambahTransaksiActivity::class.java)
            tambahTransaksiLauncher.launch(intent)
        }

        navPengaturan.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        navRiwayat.setOnClickListener {
            startActivity(Intent(this, RiwayatActivity::class.java))
        }

        navLaporan.setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }

        btnReset.setOnClickListener { showResetDialog() }
        
        btnTambahTabungan.setOnClickListener {
            showAddTabunganDialog()
        }
    }
    
    // ===== FORMATTER HELPER =====
    private fun setupNominalFormatter(editText: TextInputEditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    editText.removeTextChangedListener(this)
                    val cleanString = s.toString().replace("[Rp,.\\s]".toRegex(), "")
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        val formatted = NumberFormat.getNumberInstance(Locale("in", "ID")).format(parsed)
                        current = formatted
                        editText.setText(formatted)
                        editText.setSelection(formatted.length)
                    } else {
                        current = ""
                        editText.setText("")
                    }
                    editText.addTextChangedListener(this)
                }
            }
        })
    }
    
    // ===== CRUD TABUNGAN =====
    private fun showAddTabunganDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_tabungan, null)
        val etNama = dialogView.findViewById<TextInputEditText>(R.id.etNamaTabungan)
        val etTarget = dialogView.findViewById<TextInputEditText>(R.id.etTargetDana)
        val etTerkumpul = dialogView.findViewById<TextInputEditText>(R.id.etTerkumpul)
        val btnSimpan = dialogView.findViewById<Button>(R.id.btnSimpan)
        val btnBatal = dialogView.findViewById<Button>(R.id.btnBatal)
        val btnHapus = dialogView.findViewById<Button>(R.id.btnHapus)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)

        // Setup formatters
        setupNominalFormatter(etTarget)
        setupNominalFormatter(etTerkumpul)

        tvTitle.text = "Tambah Target Tabungan"
        btnHapus.visibility = View.GONE
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
            
        // Make background transparent for rounded corners effect if needed, but handled by card
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString()
            val targetStr = etTarget.text.toString().replace("[Rp,.\\s]".toRegex(), "")
            val terkumpulStr = etTerkumpul.text.toString().replace("[Rp,.\\s]".toRegex(), "")

            if (nama.isNotEmpty() && targetStr.isNotEmpty()) {
                val target = targetStr.toLongOrNull() ?: 0
                val terkumpul = terkumpulStr.toLongOrNull() ?: 0
                
                val newTabungan = Tabungan(nama = nama, target = target, terkumpul = terkumpul)
                listTabungan.add(newTabungan)
                saveTabungan()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Nama dan Target harus diisi", Toast.LENGTH_SHORT).show()
            }
        }
        
        btnBatal.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showEditTabunganDialog(tabungan: Tabungan) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_tabungan, null)
        val etNama = dialogView.findViewById<TextInputEditText>(R.id.etNamaTabungan)
        val etTarget = dialogView.findViewById<TextInputEditText>(R.id.etTargetDana)
        val etTerkumpul = dialogView.findViewById<TextInputEditText>(R.id.etTerkumpul)
        val btnSimpan = dialogView.findViewById<Button>(R.id.btnSimpan)
        val btnBatal = dialogView.findViewById<Button>(R.id.btnBatal)
        val btnHapus = dialogView.findViewById<Button>(R.id.btnHapus)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)

        // Setup formatters
        setupNominalFormatter(etTarget)
        setupNominalFormatter(etTerkumpul)

        tvTitle.text = "Edit Tabungan"
        etNama.setText(tabungan.nama)
        etTarget.setText(NumberFormat.getNumberInstance(Locale("in", "ID")).format(tabungan.target))
        etTerkumpul.setText(NumberFormat.getNumberInstance(Locale("in", "ID")).format(tabungan.terkumpul))
        
        btnHapus.visibility = View.VISIBLE
        btnSimpan.text = "Update"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString()
            val targetStr = etTarget.text.toString().replace("[Rp,.\\s]".toRegex(), "")
            val terkumpulStr = etTerkumpul.text.toString().replace("[Rp,.\\s]".toRegex(), "")

            if (nama.isNotEmpty() && targetStr.isNotEmpty()) {
                tabungan.nama = nama
                tabungan.target = targetStr.toLongOrNull() ?: 0
                tabungan.terkumpul = terkumpulStr.toLongOrNull() ?: 0
                
                saveTabungan()
                dialog.dismiss()
            }
        }
        
        btnHapus.setOnClickListener {
            dialog.dismiss()
            confirmDeleteTabungan(tabungan)
        }
        
        btnBatal.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun confirmDeleteTabungan(tabungan: Tabungan) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Tabungan")
            .setMessage("Yakin ingin menghapus target '${tabungan.nama}'?")
            .setPositiveButton("Hapus") { _, _ ->
                listTabungan.remove(tabungan)
                saveTabungan()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupPieChart() {
        pieChartSummary.setUsePercentValues(true)
        pieChartSummary.description.isEnabled = false
        pieChartSummary.legend.isEnabled = false
        pieChartSummary.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChartSummary.dragDecelerationFrictionCoef = 0.95f
        pieChartSummary.isDrawHoleEnabled = true
        pieChartSummary.setHoleColor(Color.WHITE)
        pieChartSummary.transparentCircleRadius = 61f
        pieChartSummary.animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
    }

    private fun updatePieChart() {
        if (totalMasuk == 0L && totalKeluar == 0L) {
            pieChartSummary.clear()
            pieChartSummary.setNoDataText("Belum ada data")
            pieChartSummary.setNoDataTextColor(Color.LTGRAY)
            pieChartSummary.invalidate()
            return
        }

        val entries = ArrayList<PieEntry>()
        // Cek agar tidak error jika 0
        if (totalMasuk > 0) entries.add(PieEntry(totalMasuk.toFloat(), "Pemasukan"))
        if (totalKeluar > 0) entries.add(PieEntry(totalKeluar.toFloat(), "Pengeluaran"))

        val dataSet = PieDataSet(entries, "Ringkasan")
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        
        val colors = ArrayList<Int>()
        if (totalMasuk > 0) colors.add(Color.parseColor("#4CAF50")) // Green
        if (totalKeluar > 0) colors.add(Color.parseColor("#EF5350")) // Red
        
        dataSet.colors = colors

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        
        pieChartSummary.data = data
        pieChartSummary.highlightValues(null)
        pieChartSummary.invalidate()
    }

    private fun updateUI() {
        // ===== SALDO =====
        val saldo = totalMasuk - totalKeluar
        animateSaldo(saldo)
        lastSaldo = saldo

        tvMasuk.text = formatRupiah(totalMasuk)
        tvKeluar.text = formatRupiah(totalKeluar)

        // ===== GRAFIK RINGKASAN =====
        updatePieChart()

        // ===== RESET BUTTON (INI KUNCI UTAMA) =====
        if (listTransaksi.isNotEmpty()) {
            btnReset.visibility = View.VISIBLE
            btnReset.isEnabled = true
            btnReset.alpha = 1f
        } else {
            btnReset.visibility = View.GONE
        }
    }

    private fun animateSaldo(newSaldo: Long) {
        val oldSaldo = lastSaldo

        val nextView = vfSaldo.currentView as? TextView
        val currentView = if (vfSaldo.currentView == tvSaldo1) tvSaldo2 else tvSaldo1
        
        // Set saldo baru ke view berikutnya
        currentView.text = formatRupiah(newSaldo)
        
        // Tentukan animasi berdasarkan perubahan saldo
        if (newSaldo > oldSaldo) {
            vfSaldo.setInAnimation(this, R.anim.slide_in_up)
            vfSaldo.setOutAnimation(this, R.anim.slide_out_up)
        } else {
            vfSaldo.setInAnimation(this, R.anim.slide_in_down) // Anda perlu membuat slide_in_down
            vfSaldo.setOutAnimation(this, R.anim.slide_out_down) // Anda perlu membuat slide_out_down
        }
        
        vfSaldo.showNext()
        lastSaldo = newSaldo
    }

    private fun loadData() {
        val pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        totalMasuk = pref.getLong("totalMasuk", 0)
        totalKeluar = pref.getLong("totalKeluar", 0)

        listTransaksi.clear()
        pref.getString("listTransaksi", null)?.let {
            val type = object : TypeToken<MutableList<Transaksi>>() {}.type
            listTransaksi.addAll(Gson().fromJson(it, type))
        }
    }

    private fun showResetDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reset Data")
            .setMessage("Hapus semua data keuangan?")
            .setPositiveButton("Hapus") { _, _ -> resetData() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun resetData() {
        getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        // Keep first run true so onboarding doesn't show again
        getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean("isFirstRun", true).apply()
        
        totalMasuk = 0
        totalKeluar = 0
        listTransaksi.clear()
        listTabungan.clear()
        tabunganAdapter.notifyDataSetChanged()
        updateUI()
    }

    private fun formatRupiah(v: Long): String =
        NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            .format(v)
            .replace(",00", "")
}