package com.example.mydailymoney

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TambahTransaksiActivity : AppCompatActivity() {

    private lateinit var etNominal: EditText
    private lateinit var etKategori: EditText // Hidden but necessary for logic
    private lateinit var etCatatan: EditText
    private lateinit var cgJenis: ChipGroup
    private lateinit var chipMasuk: Chip
    private lateinit var chipKeluar: Chip
    private lateinit var btnSimpan: Button
    private lateinit var rvKategori: RecyclerView
    private lateinit var btnBack: ImageButton
    private lateinit var btnPickDate: LinearLayout
    private lateinit var tvTanggal: TextView

    private lateinit var kategoriAdapter: KategoriAdapter
    private var selectedKategori: Kategori? = null

    private val PREF_NAME = "mydailymoney_pref"
    private var selectedTimestamp = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_transaksi)

        initView()
        setupAction()
        setupNominalFormatter()
        setupCategoryGrid()
        setupDatePicker()
        updateDateLabel()
    }

    private fun initView() {
        etNominal = findViewById(R.id.etNominal)
        etKategori = findViewById(R.id.etKategori)
        etCatatan = findViewById(R.id.etCatatan)
        cgJenis = findViewById(R.id.cgJenis)
        chipMasuk = findViewById(R.id.chipMasuk)
        chipKeluar = findViewById(R.id.chipKeluar)
        btnSimpan = findViewById(R.id.btnSimpan)
        rvKategori = findViewById(R.id.rvKategori)
        btnBack = findViewById(R.id.btnBack)
        btnPickDate = findViewById(R.id.btnPickDate)
        tvTanggal = findViewById(R.id.tvTanggal)
    }

    private fun setupAction() {
        btnBack.setOnClickListener { onBackPressed() }
        btnSimpan.setOnClickListener { simpanTransaksi() }
        
        // Set default choice
        chipKeluar.isChecked = true
    }

    private fun setupNominalFormatter() {
        etNominal.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    etNominal.removeTextChangedListener(this)
                    val cleanString = s.toString().replace("[Rp,.\\s]".toRegex(), "")
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        val formatted = NumberFormat.getNumberInstance(Locale("in", "ID")).format(parsed)
                        current = formatted
                        etNominal.setText(formatted)
                        etNominal.setSelection(formatted.length)
                    } else {
                        current = ""
                        etNominal.setText("")
                    }
                    etNominal.addTextChangedListener(this)
                }
            }
        })
    }

    private fun getKategoriList(): List<Kategori> {
        return listOf(
            Kategori("Makanan", R.drawable.ic_cat_makanan),
            Kategori("Transportasi", R.drawable.ic_cat_transportasi),
            Kategori("Belanja", R.drawable.ic_cat_belanja),
            Kategori("Tagihan", R.drawable.ic_cat_tagihan),
            Kategori("Hiburan", R.drawable.ic_cat_hiburan),
            Kategori("Kesehatan", R.drawable.ic_cat_kesehatan),
            Kategori("Pendidikan", R.drawable.ic_cat_pendidikan),
            Kategori("Lainnya", R.drawable.ic_cat_lainnya)
        )
    }

    private fun setupCategoryGrid() {
        val categories = getKategoriList()
        kategoriAdapter = KategoriAdapter(categories) { kategori ->
            selectedKategori = kategori
            etKategori.setText(kategori.nama) // Keep this for validation logic
            kategoriAdapter.setSelected(kategori)
        }

        rvKategori.layoutManager = GridLayoutManager(this, 4)
        rvKategori.adapter = kategoriAdapter
    }

    private fun setupDatePicker() {
        btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedTimestamp
            
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedTimestamp = calendar.timeInMillis
                    updateDateLabel()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    private fun updateDateLabel() {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID"))
        val today = Calendar.getInstance()
        val selected = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
        
        if (today.get(Calendar.YEAR) == selected.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == selected.get(Calendar.DAY_OF_YEAR)) {
            tvTanggal.text = "Hari Ini"
        } else {
            tvTanggal.text = sdf.format(selected.time)
        }
    }

    private fun simpanTransaksi() {
        val nominalStr = etNominal.text.toString().replace("[Rp,.\\s]".toRegex(), "")
        val catatan = etCatatan.text.toString()

        if (nominalStr.isEmpty()) {
            etNominal.error = "Isi nominal"
            return
        }
        
        if (selectedKategori == null) {
            Toast.makeText(this, "Pilih kategori terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val nominal = nominalStr.toLongOrNull() ?: 0
        val jenis = when (cgJenis.checkedChipId) {
            R.id.chipMasuk -> "Pemasukan"
            else -> "Pengeluaran"
        }
        
        val transaksi = Transaksi(
            id = java.util.UUID.randomUUID().toString(),
            jenis = jenis,
            nominal = nominal,
            kategori = selectedKategori!!.nama,
            catatan = catatan,
            timestamp = selectedTimestamp
        )

        saveToPref(transaksi)
        
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun saveToPref(transaksi: Transaksi) {
        val pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val listJson = pref.getString("listTransaksi", "[]")
        val type = object : TypeToken<MutableList<Transaksi>>() {}.type
        val list: MutableList<Transaksi> = Gson().fromJson(listJson, type)

        list.add(0, transaksi)
        
        val editor = pref.edit()
        editor.putString("listTransaksi", Gson().toJson(list))

        if (transaksi.jenis == "Pemasukan") {
            val totalMasuk = pref.getLong("totalMasuk", 0) + transaksi.nominal
            editor.putLong("totalMasuk", totalMasuk)
        } else {
            val totalKeluar = pref.getLong("totalKeluar", 0) + transaksi.nominal
            editor.putLong("totalKeluar", totalKeluar)
        }
        
        editor.apply()
    }
}

// --- Kategori Data Class and Adapter ---

data class Kategori(val nama: String, val iconRes: Int)

class KategoriAdapter(
    private val items: List<Kategori>,
    private val onItemClick: (Kategori) -> Unit
) : RecyclerView.Adapter<KategoriAdapter.ViewHolder>() {

    private var selectedItem: Kategori? = null

    fun setSelected(kategori: Kategori) {
        val oldSelected = selectedItem
        selectedItem = kategori
        
        // Notify change for old and new selected items
        oldSelected?.let { notifyItemChanged(items.indexOf(it)) }
        notifyItemChanged(items.indexOf(kategori))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_kategori_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, item == selectedItem)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivKategori: ImageView = itemView.findViewById(R.id.ivKategori)
        private val tvKategori: TextView = itemView.findViewById(R.id.tvKategori)

        fun bind(item: Kategori, isSelected: Boolean) {
            ivKategori.setImageResource(item.iconRes)
            tvKategori.text = item.nama
            itemView.isSelected = isSelected
        }
    }
}