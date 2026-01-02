package com.example.mydailymoney

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var rgJenis: RadioGroup
    private lateinit var rbMasuk: RadioButton
    private lateinit var rbKeluar: RadioButton
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
        rgJenis = findViewById(R.id.rgJenis)
        rbMasuk = findViewById(R.id.rbMasuk)
        rbKeluar = findViewById(R.id.rbKeluar)
        btnSimpan = findViewById(R.id.btnSimpan)
        rvKategori = findViewById(R.id.rvKategori)
        btnBack = findViewById(R.id.btnBack)
        btnPickDate = findViewById(R.id.btnPickDate)
        tvTanggal = findViewById(R.id.tvTanggal)
    }

    private fun setupAction() {
        btnBack.setOnClickListener { onBackPressed() }
        btnSimpan.setOnClickListener { simpanTransaksi() }

        // Animasi saat memilih jenis transaksi
        rgJenis.setOnCheckedChangeListener { _, checkedId ->
            val selectedBtn = findViewById<View>(checkedId)
            selectedBtn?.let { view ->
                view.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction {
                        view.animate()
                            .scaleX(1.05f)
                            .scaleY(1.05f)
                            .setDuration(100)
                            .withEndAction {
                                view.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start()
                            }
                            .start()
                    }
                    .start()
            }
            // Update kategori berdasarkan jenis transaksi
            updateKategoriList(checkedId)
        }
    }

    private fun updateKategoriList(checkedId: Int) {
        val newList = if (checkedId == R.id.rbMasuk) {
            getPemasukanKategoriList()
        } else {
            getPengeluaranKategoriList()
        }
        kategoriAdapter.updateData(newList)
        selectedKategori = null
        etKategori.setText("")
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

    private fun getPengeluaranKategoriList(): List<Kategori> {
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

    private fun getPemasukanKategoriList(): List<Kategori> {
        return listOf(
            Kategori("Gaji", R.drawable.ic_cat_gaji),
            Kategori("Kerja", R.drawable.ic_cat_freelance),
            Kategori("Usaha", R.drawable.ic_cat_usaha),
            Kategori("Dagang", R.drawable.ic_cat_dagang),
            Kategori("Komisi", R.drawable.ic_cat_komisi), // Added
            Kategori("Bonus", R.drawable.ic_cat_bonus),
            Kategori("Investasi", R.drawable.ic_money),
            Kategori("Lainnya", R.drawable.ic_cat_lainnya)
        )
    }

    private fun setupCategoryGrid() {
        // Default kosong atau bisa di set ke Pemasukan/Pengeluaran jika ada default selection
        // Saat ini default kosong karena RadioGroup belum terpilih secara default di XML atau code
        val initialList = listOf<Kategori>() 
        
        kategoriAdapter = KategoriAdapter(initialList) { kategori ->
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

        if (rgJenis.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Pilih jenis transaksi terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedKategori == null) {
            Toast.makeText(this, "Pilih kategori terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val nominal = nominalStr.toLongOrNull() ?: 0
        val jenis = when (rgJenis.checkedRadioButtonId) {
            R.id.rbMasuk -> "Pemasukan"
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
        
        // Show Success Dialog
        showSuccessDialog()
    }

    private fun showSuccessDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_success, null)
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
            
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        // Auto dismiss after 1.5 seconds and finish activity
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            setResult(Activity.RESULT_OK)
            finish()
        }, 1500)
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
    private var items: List<Kategori>,
    private val onItemClick: (Kategori) -> Unit
) : RecyclerView.Adapter<KategoriAdapter.ViewHolder>() {

    private var selectedItem: Kategori? = null

    fun updateData(newItems: List<Kategori>) {
        items = newItems
        selectedItem = null // Reset selection when category changes
        notifyDataSetChanged()
    }

    fun setSelected(kategori: Kategori) {
        val oldSelected = selectedItem
        selectedItem = kategori
        
        val oldIndex = items.indexOf(oldSelected)
        val newIndex = items.indexOf(kategori)

        if (oldIndex != -1) notifyItemChanged(oldIndex)
        if (newIndex != -1) notifyItemChanged(newIndex)
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
        private val container: View = ivKategori.parent as View

        fun bind(item: Kategori, isSelected: Boolean) {
            ivKategori.setImageResource(item.iconRes)
            tvKategori.text = item.nama
            
            // Set selected state for selector drawable
            container.isSelected = isSelected
            tvKategori.isSelected = isSelected
            
            // Optional: Simple animation
            if (isSelected) {
                itemView.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).start()
            } else {
                itemView.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
            }
        }
    }
}
