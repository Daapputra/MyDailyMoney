package com.example.mydailymoney

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RiwayatActivity : AppCompatActivity() {

    private lateinit var rvRiwayat: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var btnFilterTanggal: LinearLayout
    private lateinit var tvFilterTanggal: TextView
    private lateinit var adapter: TransaksiAdapter
    
    private val listTransaksi = mutableListOf<Transaksi>()
    private val listFiltered = mutableListOf<Transaksi>()
    private var selectedDate: Long? = null

    private val PREF_NAME = "mydailymoney_pref"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        rvRiwayat = findViewById(R.id.rvRiwayat)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        btnFilterTanggal = findViewById(R.id.btnFilterTanggal)
        tvFilterTanggal = findViewById(R.id.tvFilterTanggal)

        setupRecycler()
        setupFilter()
        loadData()
    }

    private fun setupRecycler() {
        adapter = TransaksiAdapter(listFiltered)
        rvRiwayat.layoutManager = LinearLayoutManager(this)
        rvRiwayat.adapter = adapter
    }

    private fun setupFilter() {
        btnFilterTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            if (selectedDate != null) {
                calendar.timeInMillis = selectedDate!!
            }

            // Jika sudah ada filter, tampilkan opsi reset
            if (selectedDate != null) {
                val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Filter Tanggal")
                    .setMessage("Pilih aksi")
                    .setPositiveButton("Ubah Tanggal") { _, _ -> showDatePicker(calendar) }
                    .setNegativeButton("Reset Filter") { _, _ -> 
                        selectedDate = null
                        tvFilterTanggal.text = "Semua Waktu"
                        filterData()
                    }
                    .show()
            } else {
                showDatePicker(calendar)
            }
        }
    }

    private fun showDatePicker(calendar: Calendar) {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance()
                selected.set(year, month, dayOfMonth)
                selectedDate = selected.timeInMillis
                
                val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID"))
                tvFilterTanggal.text = sdf.format(selected.time)
                filterData()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadData() {
        val pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        
        listTransaksi.clear()
        pref.getString("listTransaksi", null)?.let {
            val type = object : TypeToken<MutableList<Transaksi>>() {}.type
            listTransaksi.addAll(Gson().fromJson(it, type))
        }

        // Sort terbaru diatas
        listTransaksi.sortByDescending { it.timestamp }
        
        filterData()
    }

    private fun filterData() {
        listFiltered.clear()
        if (selectedDate == null) {
            listFiltered.addAll(listTransaksi)
        } else {
            val filterCal = Calendar.getInstance().apply { timeInMillis = selectedDate!! }
            val itemCal = Calendar.getInstance()
            
            val filtered = listTransaksi.filter {
                itemCal.timeInMillis = it.timestamp
                itemCal.get(Calendar.YEAR) == filterCal.get(Calendar.YEAR) &&
                itemCal.get(Calendar.DAY_OF_YEAR) == filterCal.get(Calendar.DAY_OF_YEAR)
            }
            listFiltered.addAll(filtered)
        }
        
        adapter.notifyDataSetChanged()
        
        if (listFiltered.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvRiwayat.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvRiwayat.visibility = View.VISIBLE
        }
    }
}