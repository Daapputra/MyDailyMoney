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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    // ===== UI =====
    private lateinit var vfSaldo: ViewFlipper
    private lateinit var tvSaldo1: TextView
    private lateinit var tvSaldo2: TextView
    private lateinit var tvMasuk: TextView
    private lateinit var tvKeluar: TextView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var btnReset: Button
    private lateinit var btnToggleSaldo: ImageView
    private lateinit var btnHeaderHistory: ImageView
    private lateinit var navSettings: LinearLayout
    private lateinit var navRiwayat: LinearLayout
    private lateinit var navLaporan: LinearLayout
    private lateinit var navHome: LinearLayout
    private lateinit var barChartSummary: BarChart

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

    private var isSaldoHidden = false

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
        setupBarChart()
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
        fabAdd = findViewById(R.id.fabAdd)
        btnReset = findViewById(R.id.btnReset)
        btnToggleSaldo = findViewById(R.id.btnToggleSaldo)
        btnHeaderHistory = findViewById(R.id.btnHeaderHistory)
        navSettings = findViewById(R.id.navSettings)
        navRiwayat = findViewById(R.id.navRiwayat)
        navLaporan = findViewById(R.id.navLaporan)
        navHome = findViewById(R.id.navHome)
        barChartSummary = findViewById(R.id.barChartSummary)

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
        fabAdd.setOnClickListener {
            val intent = Intent(this, TambahTransaksiActivity::class.java)
            tambahTransaksiLauncher.launch(intent)
        }

        navSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        navRiwayat.setOnClickListener {
            startActivity(Intent(this, RiwayatActivity::class.java))
        }

        navLaporan.setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }

        navHome.setOnClickListener {
            // Already on home, maybe refresh or scroll to top
        }

        btnReset.setOnClickListener { showResetDialog() }

        btnTambahTabungan.setOnClickListener {
            showAddTabunganDialog()
        }

        btnToggleSaldo.setOnClickListener {
            toggleSaldoVisibility()
        }

        btnHeaderHistory.setOnClickListener {
            startActivity(Intent(this, RiwayatActivity::class.java))
        }
    }

    private fun toggleSaldoVisibility() {
        isSaldoHidden = !isSaldoHidden

        // Update icon based on state (assuming we have visibility_off too, using same for now or tint)
        btnToggleSaldo.alpha = if (isSaldoHidden) 0.5f else 1.0f

        val saldo = totalMasuk - totalKeluar
        val displaySaldo = if (isSaldoHidden) "Rp *****" else formatRupiah(saldo)

        // Update current view text directly or animate if you want
        // For simplicity, just update the text of current visible view or force re-animation
        val currentView = vfSaldo.currentView as TextView
        currentView.text = displaySaldo

        // Also update tvSaldo1/2 so next animation is correct
        tvSaldo1.text = displaySaldo
        tvSaldo2.text = displaySaldo
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

    private fun setupBarChart() {
        barChartSummary.description.isEnabled = false
        barChartSummary.setDrawGridBackground(false)
        barChartSummary.setDrawBarShadow(false)
        barChartSummary.legend.isEnabled = false // Custom legend used in XML

        // Interaction
        barChartSummary.setTouchEnabled(true)
        barChartSummary.isDragEnabled = true
        barChartSummary.setScaleEnabled(false)
        barChartSummary.setPinchZoom(false)

        // Axis styling
        val xAxis = barChartSummary.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.parseColor("#757575")
        xAxis.textSize = 12f
        xAxis.granularity = 1f
        xAxis.setCenterAxisLabels(true) // Center labels for groups

        val leftAxis = barChartSummary.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.parseColor("#E0E0E0")
        leftAxis.gridLineWidth = 0.5f
        leftAxis.textColor = Color.parseColor("#9E9E9E")
        leftAxis.textSize = 10f
        leftAxis.setDrawAxisLine(false)
        leftAxis.axisMinimum = 0f // Start from 0

        barChartSummary.axisRight.isEnabled = false

        barChartSummary.animateY(1000)
    }

    private fun updateBarChart() {
        if (listTransaksi.isEmpty()) {
            barChartSummary.clear()
            barChartSummary.invalidate()
            return
        }

        // Group data by Date (dd/MM) or Month depending on range. For now, daily/recent.
        // Let's group by DAY for the last 5-7 days or transactions

        // Simplified approach: Group by date string "dd MMM"
        val incomeMap = mutableMapOf<String, Float>()
        val expenseMap = mutableMapOf<String, Float>()
        val allDates = sortedSetOf<String>()
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

        // Sort transactions by time
        val sortedList = listTransaksi.sortedBy { it.timestamp }

        // Process data
        for (t in sortedList) {
            val dateKey = dateFormat.format(Date(t.timestamp))
            allDates.add(dateKey)

            val nominal = t.nominal.toFloat()
            if (t.jenis.equals("Pemasukan", ignoreCase = true)) {
                incomeMap[dateKey] = (incomeMap[dateKey] ?: 0f) + nominal
            } else {
                expenseMap[dateKey] = (expenseMap[dateKey] ?: 0f) + nominal
            }
        }

        // Take last 5 days/entries to fit chart nicely
        val recentDates = allDates.toList().takeLast(5)

        val entriesIncome = ArrayList<BarEntry>()
        val entriesExpense = ArrayList<BarEntry>()

        recentDates.forEachIndexed { index, date ->
            entriesIncome.add(BarEntry(index.toFloat(), incomeMap[date] ?: 0f))
            entriesExpense.add(BarEntry(index.toFloat(), expenseMap[date] ?: 0f))
        }

        // Colors
        val colorIncome = Color.parseColor("#4CAF50") // Green
        val colorExpense = Color.parseColor("#EF5350") // Red

        val set1 = BarDataSet(entriesIncome, "Income")
        set1.color = colorIncome
        set1.setDrawValues(false) // Hide values on top of bars

        val set2 = BarDataSet(entriesExpense, "Expense")
        set2.color = colorExpense
        set2.setDrawValues(false)

        val groupSpace = 0.4f
        val barSpace = 0.05f // x2 dataset
        val barWidth = 0.25f // x2 dataset
        // (0.25 + 0.05) * 2 + 0.4 = 1.00 -> interval per "group"

        val data = BarData(set1, set2)
        data.barWidth = barWidth

        barChartSummary.data = data

        // Set X-Axis labels
        barChartSummary.xAxis.valueFormatter = IndexAxisValueFormatter(recentDates)
        // Restrict view to show groups correctly
        barChartSummary.xAxis.axisMinimum = 0f
        barChartSummary.xAxis.axisMaximum = recentDates.size.toFloat()

        barChartSummary.groupBars(0f, groupSpace, barSpace)
        barChartSummary.invalidate()
    }

    private fun updateUI() {
        // ===== SALDO =====
        val saldo = totalMasuk - totalKeluar
        animateSaldo(saldo)
        lastSaldo = saldo

        tvMasuk.text = formatRupiah(totalMasuk)
        tvKeluar.text = formatRupiah(totalKeluar)

        // ===== GRAFIK RINGKASAN =====
        updateBarChart()

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

        val displaySaldo = if (isSaldoHidden) "Rp *****" else formatRupiah(newSaldo)

        // Set saldo baru ke view berikutnya
        currentView.text = displaySaldo

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