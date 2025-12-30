package com.example.mydailymoney

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.NumberFormat
import java.util.*
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

class MainActivity : AppCompatActivity() {

    // ===== FILTER JENIS =====
    private lateinit var btnFilterAll: Button
    private lateinit var btnFilterMasuk: Button
    private lateinit var btnFilterKeluar: Button
    private var currentFilter: String? = null

    // ===== FILTER WAKTU =====
    private lateinit var tvFilterWaktu: TextView
    private var filterWaktu = "BULAN"

    // ===== UI =====
    private lateinit var tvSaldo: TextView
    private lateinit var tvMasuk: TextView
    private lateinit var tvKeluar: TextView
    private lateinit var rvTransaksi: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var btnTambah: Button
    private lateinit var btnReset: Button
    private lateinit var btnWhatsapp: LinearLayout
    private lateinit var btnTelepon: LinearLayout

    // ===== GRAFIK =====
    private lateinit var barMasuk: View
    private lateinit var barKeluar: View

    // ===== DATA =====
    private val listTransaksi = mutableListOf<Transaksi>()
    private val listFiltered = mutableListOf<Transaksi>()
    private lateinit var adapter: TransaksiAdapter

    private var totalMasuk = 0L
    private var totalKeluar = 0L
    private var lastSaldo = 0L

    private val PREF_NAME = "mydailymoney_pref"
    private val WA_NUMBER = "6281221070986"
    private val PHONE_NUMBER = "081221070986"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        loadData()
        setupRecycler()
        setupAction()

        setActiveFilter(btnFilterAll)
        applyFilter(null, animate = false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadData()
            applyFilter(currentFilter, animate = false)
        }
    }

    private fun initView() {
        tvSaldo = findViewById(R.id.tvSaldo)
        tvMasuk = findViewById(R.id.tvMasuk)
        tvKeluar = findViewById(R.id.tvKeluar)
        rvTransaksi = findViewById(R.id.rvTransaksi)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        btnTambah = findViewById(R.id.btnTambah)
        btnReset = findViewById(R.id.btnReset)
        btnWhatsapp = findViewById(R.id.btnWhatsapp)
        btnTelepon = findViewById(R.id.btnTelepon)
        btnFilterAll = findViewById(R.id.btnFilterAll)
        btnFilterMasuk = findViewById(R.id.btnFilterMasuk)
        btnFilterKeluar = findViewById(R.id.btnFilterKeluar)
        tvFilterWaktu = findViewById(R.id.tvFilterWaktu)
        barMasuk = findViewById(R.id.barPemasukan)
        barKeluar = findViewById(R.id.barPengeluaran)
    }

    private fun setupRecycler() {
        adapter = TransaksiAdapter(listFiltered)
        rvTransaksi.layoutManager = LinearLayoutManager(this)
        rvTransaksi.adapter = adapter
    }

    private fun setupAction() {
        btnTambah.setOnClickListener {
            startActivityForResult(Intent(this, TambahTransaksiActivity::class.java), 100)
        }

        btnFilterAll.setOnClickListener {
            setActiveFilter(btnFilterAll)
            applyFilter(null, true)
        }

        btnFilterMasuk.setOnClickListener {
            setActiveFilter(btnFilterMasuk)
            applyFilter("Pemasukan", true)
        }

        btnFilterKeluar.setOnClickListener {
            setActiveFilter(btnFilterKeluar)
            applyFilter("Pengeluaran", true)
        }

        tvFilterWaktu.setOnClickListener { showFilterWaktuDialog() }
        btnReset.setOnClickListener { showResetDialog() }

        btnWhatsapp.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$WA_NUMBER")))
        }

        btnTelepon.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$PHONE_NUMBER")))
        }
    }

    private fun setActiveFilter(active: Button) {
        listOf(btnFilterAll, btnFilterMasuk, btnFilterKeluar).forEach {
            it.setBackgroundResource(R.drawable.bg_filter_default)
            it.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        }
        active.setBackgroundResource(R.drawable.bg_filter_active)
        active.setTextColor(ContextCompat.getColor(this, android.R.color.white))
    }

    private fun applyFilter(jenis: String?, animate: Boolean) {
        currentFilter = jenis
        if (animate) animateList()

        listFiltered.clear()

        val byJenis = if (jenis == null) listTransaksi else listTransaksi.filter { it.jenis == jenis }
        val result = when (filterWaktu) {
            "HARI" -> byJenis.filter { isToday(it.timestamp) }
            "MINGGU" -> byJenis.filter { isThisWeek(it.timestamp) }
            else -> byJenis.filter { isThisMonth(it.timestamp) }
        }

        listFiltered.addAll(result)
        adapter.notifyDataSetChanged()
        updateUI()
    }

    // ===== ANIMASI FILTER WAKTU =====
    private fun animateFilterWaktuChange() {
        rvTransaksi.animate()
            .alpha(0f)
            .translationY(20f)
            .setDuration(120)
            .withEndAction {
                rvTransaksi.translationY = -10f
                rvTransaksi.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(200)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
            .start()
    }

    private fun showFilterWaktuDialog() {
        val items = arrayOf("Hari Ini", "Minggu Ini", "Bulan Ini")
        AlertDialog.Builder(this)
            .setTitle("Filter Waktu")
            .setItems(items) { _, which ->
                filterWaktu = when (which) {
                    0 -> "HARI"
                    1 -> "MINGGU"
                    else -> "BULAN"
                }
                tvFilterWaktu.text = "📅 ${items[which]} ▾"
                animateFilterWaktuChange()
                applyFilter(currentFilter, false)
            }
            .show()
    }

    private fun animateList() {
        rvTransaksi.animate()
            .alpha(0f)
            .translationY(40f)
            .setDuration(150)
            .withEndAction {
                rvTransaksi.translationY = -20f
                rvTransaksi.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(220)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
            .start()
    }

    private fun updateUI() {
        // ===== SALDO =====
        val saldo = totalMasuk - totalKeluar
        animateSaldo(lastSaldo, saldo)
        lastSaldo = saldo

        tvMasuk.text = formatRupiah(totalMasuk)
        tvKeluar.text = formatRupiah(totalKeluar)

        // ===== LIST / EMPTY STATE =====
        if (listFiltered.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvTransaksi.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvTransaksi.visibility = View.VISIBLE
        }

        // ===== RESET BUTTON (INI KUNCI UTAMA) =====
        if (listTransaksi.isNotEmpty()) {
            btnReset.visibility = View.VISIBLE
            btnReset.isEnabled = true
            btnReset.alpha = 1f        // 🔑 WAJIB, biar tidak transparan
        } else {
            btnReset.visibility = View.GONE
        }

        // ===== GRAFIK =====
        updateGrafik()
    }

    private fun updateGrafik() {
        barMasuk.post {
            val width = (barMasuk.parent as View).width
            val max = maxOf(totalMasuk, totalKeluar, 1L)
            animateBar(barMasuk, (width * totalMasuk / max).toInt())
            animateBar(barKeluar, (width * totalKeluar / max).toInt())
        }
    }

    private fun animateBar(bar: View, target: Int) {
        val start = bar.layoutParams.width.toFloat()

        ValueAnimator.ofFloat(start, target.toFloat()).apply {
            duration = 900L
            startDelay = 100L
            interpolator = FastOutSlowInInterpolator()

            addUpdateListener { animator ->
                bar.layoutParams.width = (animator.animatedValue as Float).toInt()
                bar.requestLayout()
            }
            start()
        }
    }

    private fun animateSaldo(from: Long, to: Long) {
        ValueAnimator.ofFloat(from.toFloat(), to.toFloat()).apply {
            duration = 900L
            startDelay = 100L
            interpolator = FastOutSlowInInterpolator()

            addUpdateListener { animator ->
                val value = (animator.animatedValue as Float).toLong()
                tvSaldo.text = formatRupiah(value)
            }
            start()
        }
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
        totalMasuk = 0
        totalKeluar = 0
        listTransaksi.clear()
        listFiltered.clear()
        adapter.notifyDataSetChanged()
        updateUI()
    }

    private fun isToday(time: Long): Boolean {
        val now = Calendar.getInstance()
        val t = Calendar.getInstance().apply { timeInMillis = time }
        return now.get(Calendar.YEAR) == t.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == t.get(Calendar.DAY_OF_YEAR)
    }

    private fun isThisWeek(time: Long): Boolean {
        val now = Calendar.getInstance()
        val t = Calendar.getInstance().apply { timeInMillis = time }
        return now.get(Calendar.YEAR) == t.get(Calendar.YEAR) &&
                now.get(Calendar.WEEK_OF_YEAR) == t.get(Calendar.WEEK_OF_YEAR)
    }

    private fun isThisMonth(time: Long): Boolean {
        val now = Calendar.getInstance()
        val t = Calendar.getInstance().apply { timeInMillis = time }
        return now.get(Calendar.YEAR) == t.get(Calendar.YEAR) &&
                now.get(Calendar.MONTH) == t.get(Calendar.MONTH)
    }

    private fun formatRupiah(v: Long): String =
        NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            .format(v)
            .replace(",00", "")
}
