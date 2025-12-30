package com.example.mydailymoney

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class LaporanActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var lineChart: LineChart
    private lateinit var tvFilterWaktu: TextView
    private lateinit var tvChartTitle: TextView
    private lateinit var btnSwitchPengeluaran: Button
    private lateinit var btnSwitchPemasukan: Button
    
    private val listTransaksi = mutableListOf<Transaksi>()
    private var filterWaktu = "BULAN"
    private var filterJenis = "Pengeluaran" // Default show expense
    
    private val PREF_NAME = "mydailymoney_pref"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        initView()
        setupCharts()
        loadData()
        updateCharts()
    }

    private fun initView() {
        pieChart = findViewById(R.id.pieChart)
        lineChart = findViewById(R.id.lineChart)
        tvFilterWaktu = findViewById(R.id.tvFilterWaktu)
        tvChartTitle = findViewById(R.id.tvChartTitle)
        btnSwitchPengeluaran = findViewById(R.id.btnSwitchPengeluaran)
        btnSwitchPemasukan = findViewById(R.id.btnSwitchPemasukan)

        tvFilterWaktu.setOnClickListener { showFilterWaktuDialog() }
        
        btnSwitchPengeluaran.setOnClickListener {
            if (filterJenis != "Pengeluaran") {
                filterJenis = "Pengeluaran"
                updateSwitchButtons()
                updateCharts()
            }
        }
        
        btnSwitchPemasukan.setOnClickListener {
            if (filterJenis != "Pemasukan") {
                filterJenis = "Pemasukan"
                updateSwitchButtons()
                updateCharts()
            }
        }
        
        updateSwitchButtons()
    }
    
    private fun updateSwitchButtons() {
        if (filterJenis == "Pengeluaran") {
            btnSwitchPengeluaran.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green_primary)
            btnSwitchPengeluaran.setTextColor(Color.WHITE)
            
            btnSwitchPemasukan.backgroundTintList = ContextCompat.getColorStateList(this, R.color.bg_card_white)
            btnSwitchPemasukan.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            
            tvChartTitle.text = "Analisis Pengeluaran"
        } else {
            btnSwitchPemasukan.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green_primary)
            btnSwitchPemasukan.setTextColor(Color.WHITE)
            
            btnSwitchPengeluaran.backgroundTintList = ContextCompat.getColorStateList(this, R.color.bg_card_white)
            btnSwitchPengeluaran.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            
            tvChartTitle.text = "Analisis Pemasukan"
        }
    }

    private fun loadData() {
        val pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        listTransaksi.clear()
        pref.getString("listTransaksi", null)?.let {
            val type = object : TypeToken<MutableList<Transaksi>>() {}.type
            listTransaksi.addAll(Gson().fromJson(it, type))
        }
    }

    private fun setupCharts() {
        // Pie Chart Setup
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.transparentCircleRadius = 61f
        pieChart.animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)

        // Line Chart Setup
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = true
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.setDrawGridLines(false)
        lineChart.axisLeft.setDrawGridLines(true)
        lineChart.axisRight.isEnabled = false
        lineChart.animateX(1000)
    }
    
    private fun updateCharts() {
        updatePieChart()
        updateLineChart()
    }

    private fun updatePieChart() {
        val filteredList = getFilteredData()
        
        if (filteredList.isEmpty()) {
            pieChart.clear()
            pieChart.setNoDataText("Belum ada data $filterJenis untuk periode ini")
            pieChart.setNoDataTextColor(Color.GRAY)
            pieChart.invalidate()
            return
        }

        val categoryMap = filteredList.groupBy { it.kategori }
            .mapValues { entry -> entry.value.sumOf { it.nominal } }

        val entries = ArrayList<PieEntry>()
        categoryMap.forEach { (key, value) ->
            entries.add(PieEntry(value.toFloat(), key))
        }

        val dataSet = PieDataSet(entries, filterJenis)
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        
        val colors = if (filterJenis == "Pengeluaran") getExpenseColors() else getIncomeColors()
        dataSet.colors = colors

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        
        pieChart.data = data
        pieChart.highlightValues(null)
        pieChart.invalidate()
    }
    
    private fun updateLineChart() {
        val filteredList = getFilteredData()

        if (filteredList.isEmpty()) {
            lineChart.clear()
            lineChart.setNoDataText("Belum ada data $filterJenis untuk periode ini")
            lineChart.invalidate()
            return
        }

        // Group by date (day of month for "BULAN", or day of week for "MINGGU")
        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
        val groupedByDate = filteredList.groupBy {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = it.timestamp
            calendar.get(Calendar.DAY_OF_MONTH) // Example: group by day of month
        }.mapValues { entry ->
            entry.value.sumOf { it.nominal }.toFloat()
        }

        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()
        
        // Sort by day to have a correct timeline
        val sortedKeys = groupedByDate.keys.sorted()
        sortedKeys.forEachIndexed { index, day ->
            entries.add(Entry(index.toFloat(), groupedByDate[day]!!))
            labels.add(day.toString())
        }

        val dataSet = LineDataSet(entries, filterJenis)
        dataSet.color = ContextCompat.getColor(this, if(filterJenis == "Pengeluaran") R.color.red_primary else R.color.green_primary)
        dataSet.setCircleColor(dataSet.color)
        dataSet.circleRadius = 4f
        dataSet.lineWidth = 2.5f
        dataSet.valueTextSize = 10f

        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        lineChart.data = LineData(dataSet)
        lineChart.invalidate()
    }
    
    private fun getFilteredData(): List<Transaksi> {
        val filteredByTime = when (filterWaktu) {
            "HARI" -> listTransaksi.filter { isToday(it.timestamp) }
            "MINGGU" -> listTransaksi.filter { isThisWeek(it.timestamp) }
            else -> listTransaksi.filter { isThisMonth(it.timestamp) }
        }
        return filteredByTime.filter { it.jenis == filterJenis }
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
                tvFilterWaktu.text = "${items[which]} ▾"
                updateCharts()
            }
            .show()
    }
    
    // Color helpers for Pie Chart
    private fun getExpenseColors(): List<Int> {
        return listOf(
            Color.parseColor("#E57373"), Color.parseColor("#F06292"), Color.parseColor("#BA68C8"),
            Color.parseColor("#9575CD"), Color.parseColor("#7986CB"), Color.parseColor("#FF8A65")
        )
    }
    
    private fun getIncomeColors(): List<Int> {
        return listOf(
            Color.parseColor("#81C784"), Color.parseColor("#4DB6AC"), Color.parseColor("#4DD0E1"),
            Color.parseColor("#64B5F6"), Color.parseColor("#AED581"), Color.parseColor("#FFF176")
        )
    }

    // Date helpers
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
}