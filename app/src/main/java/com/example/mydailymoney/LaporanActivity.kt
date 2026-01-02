package com.example.mydailymoney

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.NumberFormat
import java.util.*

class LaporanActivity : AppCompatActivity() {

    // Views
    private lateinit var pieChart: PieChart
    private lateinit var pieChartIncome: PieChart
    private lateinit var pieChartExpense: PieChart
    private lateinit var tvFilterWaktu: TextView
    private lateinit var tvTotalBalance: TextView
    private lateinit var tvLegendIncome: TextView
    private lateinit var tvLegendExpense: TextView
    private lateinit var btnToggleExpense: MaterialButton
    private lateinit var btnToggleIncome: MaterialButton
    private lateinit var tvAnalysisTitle: TextView
    private lateinit var tvAnalysisPeriod: TextView
    private lateinit var layoutExpenseList: LinearLayout
    private lateinit var layoutIncomeList: LinearLayout
    private lateinit var rvTopIncomeCategories: RecyclerView
    private lateinit var rvTopExpenseCategories: RecyclerView

    // Data & State
    private val listTransaksi = mutableListOf<Transaksi>()
    private var mainFilterWaktu = "BULAN"
    private var mainFilterWaktuText = "Bulan Ini"
    private var analysisFilterWaktu = "BULAN"
    private var analysisFilterWaktuText = "Bulan Ini"
    private var isExpenseView = true
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

        // Update each section with its own filter
        updateMainBalanceCard()
        updateAnalysisCard()
        updateToggleView()
    }

    private fun initView() {
        pieChart = findViewById(R.id.pieChart)
        pieChartIncome = findViewById(R.id.pieChartIncome)
        pieChartExpense = findViewById(R.id.pieChartExpense)
        tvFilterWaktu = findViewById(R.id.tvFilterWaktu)
        tvTotalBalance = findViewById(R.id.tvTotalBalance)
        tvLegendIncome = findViewById(R.id.tvLegendIncome)
        tvLegendExpense = findViewById(R.id.tvLegendExpense)
        btnToggleExpense = findViewById(R.id.btnToggleExpense)
        btnToggleIncome = findViewById(R.id.btnToggleIncome)
        tvAnalysisTitle = findViewById(R.id.tvAnalysisTitle)
        tvAnalysisPeriod = findViewById(R.id.tvAnalysisPeriod)
        layoutExpenseList = findViewById(R.id.layoutExpenseList)
        layoutIncomeList = findViewById(R.id.layoutIncomeList)
        rvTopIncomeCategories = findViewById(R.id.rvTopIncomeCategories)
        rvTopExpenseCategories = findViewById(R.id.rvTopExpenseCategories)

        // Set separate click listeners for each filter
        tvFilterWaktu.setOnClickListener { showMainFilterDialog() }
        tvAnalysisPeriod.setOnClickListener { showAnalysisFilterDialog() }

        btnToggleExpense.setOnClickListener {
            if (!isExpenseView) {
                isExpenseView = true
                updateToggleView()
            }
        }
        btnToggleIncome.setOnClickListener {
            if (isExpenseView) {
                isExpenseView = false
                updateToggleView()
            }
        }

        rvTopIncomeCategories.layoutManager = LinearLayoutManager(this)
        rvTopExpenseCategories.layoutManager = LinearLayoutManager(this)
    }

    private fun updateToggleView() {
        if (isExpenseView) {
            // Expense Active
            btnToggleExpense.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#C62828")) // Red
            btnToggleExpense.setTextColor(Color.WHITE)
            btnToggleIncome.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E0E0E0")) // Grey
            btnToggleIncome.setTextColor(Color.parseColor("#757575"))

            pieChartExpense.visibility = View.VISIBLE
            pieChartIncome.visibility = View.GONE
            tvAnalysisTitle.text = "Analisis Pengeluaran"
            layoutExpenseList.visibility = View.VISIBLE
            layoutIncomeList.visibility = View.GONE
        } else {
            // Income Active
            btnToggleExpense.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E0E0E0")) // Grey
            btnToggleExpense.setTextColor(Color.parseColor("#757575"))
            btnToggleIncome.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green_primary))
            btnToggleIncome.setTextColor(Color.WHITE)

            pieChartExpense.visibility = View.GONE
            pieChartIncome.visibility = View.VISIBLE
            tvAnalysisTitle.text = "Analisis Pemasukan"
            layoutExpenseList.visibility = View.GONE
            layoutIncomeList.visibility = View.VISIBLE
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
        configureMainPieChart(pieChart)
        configureCategoryPieChart(pieChartIncome)
        configureCategoryPieChart(pieChartExpense)
    }

    // Function to update ONLY the main balance card (top section)
    private fun updateMainBalanceCard() {
        val filteredList = getFilteredDataForMain()
        var totalIncome = 0L
        var totalExpense = 0L
        filteredList.forEach {
            if (it.jenis.equals("Pemasukan", ignoreCase = true)) totalIncome += it.nominal
            else totalExpense += it.nominal
        }
        val totalBalance = totalIncome - totalExpense
        tvTotalBalance.text = formatRupiahSimple(totalBalance)
        tvLegendIncome.text = formatRupiahSimple(totalIncome)
        tvLegendExpense.text = formatRupiahSimple(totalExpense)
        updateMainPieChart(totalIncome, totalExpense)
    }

    // Function to update ONLY the analysis card (bottom section)
    private fun updateAnalysisCard() {
        val filteredList = getFilteredDataForAnalysis()
        updateCategoryCharts(filteredList)
        updateTopCategories(filteredList)
    }

    private fun getFilteredDataForMain(): List<Transaksi> {
        return when (mainFilterWaktu) {
            "HARI" -> listTransaksi.filter { isToday(it.timestamp) }
            "MINGGU" -> listTransaksi.filter { isThisWeek(it.timestamp) }
            else -> listTransaksi.filter { isThisMonth(it.timestamp) }
        }
    }

    private fun getFilteredDataForAnalysis(): List<Transaksi> {
        return when (analysisFilterWaktu) {
            "HARI" -> listTransaksi.filter { isToday(it.timestamp) }
            "MINGGU" -> listTransaksi.filter { isThisWeek(it.timestamp) }
            else -> listTransaksi.filter { isThisMonth(it.timestamp) }
        }
    }

    // Dialog for Main Filter (Top Card)
    private fun showMainFilterDialog() {
        showFilterDialog(mainFilterWaktu) { newFilter, newFilterText ->
            mainFilterWaktu = newFilter
            mainFilterWaktuText = newFilterText
            tvFilterWaktu.text = "$mainFilterWaktuText ▾"
            updateMainBalanceCard() // Update only main card
        }
    }

    // Dialog for Analysis Filter (Bottom Card)
    private fun showAnalysisFilterDialog() {
        showFilterDialog(analysisFilterWaktu) { newFilter, newFilterText ->
            analysisFilterWaktu = newFilter
            analysisFilterWaktuText = newFilterText
            tvAnalysisPeriod.text = "$analysisFilterWaktuText ▾"
            updateAnalysisCard() // Update only analysis card
        }
    }

    // Generic Dialog Function
    private fun showFilterDialog(currentFilter: String, onFilterSelected: (String, String) -> Unit) {
        val view = layoutInflater.inflate(R.layout.dialog_filter_waktu, null)
        val radioGroup = view.findViewById<android.widget.RadioGroup>(R.id.radioGroup)
        val btnBatal = view.findViewById<View>(R.id.btnBatal)
        val btnTerapkan = view.findViewById<View>(R.id.btnTerapkan)

        when (currentFilter) {
            "HARI" -> radioGroup.check(R.id.rbHari)
            "MINGGU" -> radioGroup.check(R.id.rbMinggu)
            else -> radioGroup.check(R.id.rbBulan)
        }

        val dialog = MaterialAlertDialogBuilder(this).setView(view).create()
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        btnBatal.setOnClickListener { dialog.dismiss() }
        btnTerapkan.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val newFilter = when (selectedId) {
                    R.id.rbHari -> "HARI"
                    R.id.rbMinggu -> "MINGGU"
                    else -> "BULAN"
                }
                val newFilterText = when (newFilter) {
                    "HARI" -> "Hari Ini"
                    "MINGGU" -> "Minggu Ini"
                    else -> "Bulan Ini"
                }
                onFilterSelected(newFilter, newFilterText)
            }
            dialog.dismiss()
        }
    }

    private fun updateMainPieChart(totalIncome: Long, totalExpense: Long) {
        val entries = ArrayList<PieEntry>()
        if (totalIncome > 0) entries.add(PieEntry(totalIncome.toFloat(), formatRupiahSimple(totalIncome)))
        if (totalExpense > 0) entries.add(PieEntry(totalExpense.toFloat(), formatRupiahSimple(totalExpense)))

        if (entries.isEmpty()) {
            pieChart.clear()
            pieChart.invalidate()
            return
        }

        val dataSet = PieDataSet(entries, "").apply {
            sliceSpace = 2f
            selectionShift = 5f
            colors = listOf(ContextCompat.getColor(this@LaporanActivity, R.color.green_primary), ContextCompat.getColor(this@LaporanActivity, R.color.red_primary))
            setDrawValues(false)
        }

        pieChart.data = PieData(dataSet)
        pieChart.invalidate()
    }

    private fun updateCategoryCharts(filteredList: List<Transaksi>) {
        val incomeByCategory = filteredList.filter { it.jenis.equals("Pemasukan", true) }
            .groupBy { it.kategori }
            .mapValues { it.value.sumOf { tx -> tx.nominal } }

        val incomeEntries = incomeByCategory.map { PieEntry(it.value.toFloat(), it.key) }

        if (incomeEntries.isNotEmpty()) {
            val incomeDataSet = createCategoryDataSet(incomeEntries, getVariedColors())
            pieChartIncome.data = PieData(incomeDataSet).apply { setValueFormatter(PercentFormatter(pieChartIncome)) }
        } else {
            pieChartIncome.clear()
        }
        pieChartIncome.invalidate()


        val expenseByCategory = filteredList.filter { !it.jenis.equals("Pemasukan", true) }
            .groupBy { it.kategori }
            .mapValues { it.value.sumOf { tx -> tx.nominal } }

        val expenseEntries = expenseByCategory.map { PieEntry(it.value.toFloat(), it.key) }

        if (expenseEntries.isNotEmpty()) {
            val expenseDataSet = createCategoryDataSet(expenseEntries, getVariedColors())
            pieChartExpense.data = PieData(expenseDataSet).apply { setValueFormatter(PercentFormatter(pieChartExpense)) }
        } else {
            pieChartExpense.clear()
        }
        pieChartExpense.invalidate()
    }

    private fun updateTopCategories(filteredList: List<Transaksi>) {
        val incomeList = filteredList.filter { it.jenis.equals("Pemasukan", true) }
        val totalIncomeVal = incomeList.sumOf { it.nominal }.coerceAtLeast(1)

        val topIncome = incomeList
            .groupBy { it.kategori }
            .map { (category, transactions) ->
                val total = transactions.sumOf { it.nominal }
                CategorySummary(category, "Pemasukan", total, (total.toFloat() / totalIncomeVal) * 100)
            }
            .sortedByDescending { it.total }
            .take(6)

        val expenseList = filteredList.filter { !it.jenis.equals("Pemasukan", true) }
        val totalExpenseVal = expenseList.sumOf { it.nominal }.coerceAtLeast(1)

        val topExpense = expenseList
            .groupBy { it.kategori }
            .map { (category, transactions) ->
                val total = transactions.sumOf { it.nominal }
                CategorySummary(category, "Pengeluaran", total, (total.toFloat() / totalExpenseVal) * 100)
            }
            .sortedByDescending { it.total }
            .take(6)

        rvTopIncomeCategories.adapter = TopCategoryAdapter(topIncome)
        rvTopExpenseCategories.adapter = TopCategoryAdapter(topExpense)
    }

    private fun configureMainPieChart(chart: PieChart) {
        chart.apply {
            setUsePercentValues(false)
            description.isEnabled = false
            legend.isEnabled = false
            setExtraOffsets(5f, 5f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = false
            setDrawEntryLabels(true)
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
            setEntryLabelTypeface(Typeface.DEFAULT_BOLD)
            animateY(1000)
        }
    }

    private fun configureCategoryPieChart(chart: PieChart) {
        chart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            legend.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 58f
            transparentCircleRadius = 61f
            setHoleColor(Color.TRANSPARENT)
            setDrawCenterText(true)
            setCenterTextTypeface(Typeface.DEFAULT_BOLD)
            setDrawEntryLabels(true)
            setEntryLabelTextSize(11f)
            setEntryLabelColor(Color.BLACK)
            setCenterTextColor(Color.BLACK)
            animateY(1200)
        }
    }

    private fun createCategoryDataSet(entries: List<PieEntry>, colors: List<Int>): PieDataSet {
        return PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 12f
            valueTypeface = Typeface.DEFAULT_BOLD
            valueLinePart1Length = 0.5f
            valueLinePart2Length = 0.1f
            valueLineColor = Color.GRAY
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
            valueTextColor = Color.WHITE
        }
    }

    private fun formatRupiahSimple(v: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return "Rp\u00A0" + format.format(v).replace("Rp", "").trim().replace(",00", "")
    }

    private fun getVariedColors(): List<Int> {
        return listOf(
            Color.parseColor("#FBC02D"), // Yellow 700
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#03A9F4"), // Light Blue
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#00BCD4"), // Cyan
            Color.parseColor("#F44336"), // Red
            Color.parseColor("#E91E63"), // Pink
            Color.parseColor("#CDDC39"), // Lime
            Color.parseColor("#FFC107"), // Amber
            Color.parseColor("#673AB7"), // Deep Purple
            Color.parseColor("#3F51B5"), // Indigo
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#009688"), // Teal
            Color.parseColor("#795548"), // Brown
            Color.parseColor("#9E9E9E"), // Grey
            Color.parseColor("#607D8B")  // Blue Grey
        )
    }

    private fun isToday(time: Long): Boolean {
        val now = Calendar.getInstance()
        val t = Calendar.getInstance().apply { timeInMillis = time }
        return now.get(Calendar.YEAR) == t.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == t.get(Calendar.DAY_OF_YEAR)
    }

    private fun isThisWeek(time: Long): Boolean {
        val now = Calendar.getInstance()
        val t = Calendar.getInstance().apply { timeInMillis = time }
        return now.get(Calendar.YEAR) == t.get(Calendar.YEAR) && now.get(Calendar.WEEK_OF_YEAR) == t.get(Calendar.WEEK_OF_YEAR)
    }

    private fun isThisMonth(time: Long): Boolean {
        val now = Calendar.getInstance()
        val t = Calendar.getInstance().apply { timeInMillis = time }
        return now.get(Calendar.YEAR) == t.get(Calendar.YEAR) && now.get(Calendar.MONTH) == t.get(Calendar.MONTH)
    }

    data class CategorySummary(val name: String, val type: String, val total: Long, val percentage: Float)

    inner class TopCategoryAdapter(private val items: List<CategorySummary>) : RecyclerView.Adapter<TopCategoryAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val card: MaterialCardView = view.findViewById(R.id.cardCategory)
            val ivIcon: ImageView = view.findViewById(R.id.ivCategoryIcon)
            val tvName: TextView = view.findViewById(R.id.tvCategoryName)
            val tvAmount: TextView = view.findViewById(R.id.tvCategoryAmount)
            val tvPercentage: TextView = view.findViewById(R.id.tvCategoryPercentage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context).inflate(R.layout.item_top_category, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvName.text = item.name
            holder.tvAmount.text = formatRupiahSimple(item.total)
            holder.tvPercentage.text = String.format("%.1f%%", item.percentage)
            holder.ivIcon.setImageResource(getIconForCategory(item.name))

            holder.card.setCardBackgroundColor(Color.WHITE)
            holder.tvName.setTextColor(Color.parseColor("#212121"))

            if (item.type.equals("Pemasukan", true)) {
                holder.tvAmount.setTextColor(Color.parseColor("#2E7D32"))
                holder.tvPercentage.setTextColor(Color.parseColor("#757575"))
                holder.card.strokeColor = Color.WHITE
                holder.ivIcon.setColorFilter(Color.parseColor("#4CAF50"))
            } else {
                holder.tvAmount.setTextColor(Color.parseColor("#C62828"))
                holder.tvPercentage.setTextColor(Color.parseColor("#757575"))
                holder.card.strokeColor = Color.WHITE
                holder.ivIcon.setColorFilter(Color.parseColor("#EF5350"))
            }
        }

        override fun getItemCount() = items.size
    }

    private fun getIconForCategory(category: String): Int {
        return when (category.lowercase(Locale.ROOT)) {
            "gaji" -> R.drawable.ic_cat_gaji
            "bonus" -> R.drawable.ic_cat_bonus
            "dagang" -> R.drawable.ic_cat_dagang
            "freelance" -> R.drawable.ic_cat_freelance
            "komisi" -> R.drawable.ic_cat_komisi
            "usaha" -> R.drawable.ic_cat_usaha
            "makanan" -> R.drawable.ic_cat_makanan
            "belanja" -> R.drawable.ic_cat_belanja
            "transportasi" -> R.drawable.ic_cat_transportasi
            "tagihan" -> R.drawable.ic_cat_tagihan
            "hiburan" -> R.drawable.ic_cat_hiburan
            "kesehatan" -> R.drawable.ic_cat_kesehatan
            "pendidikan" -> R.drawable.ic_cat_pendidikan
            else -> R.drawable.ic_cat_lainnya
        }
    }
}