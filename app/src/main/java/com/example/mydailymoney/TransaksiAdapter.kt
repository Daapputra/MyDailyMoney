package com.example.mydailymoney

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransaksiAdapter(
    private val list: List<Transaksi>
) : RecyclerView.Adapter<TransaksiAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvKategori: TextView = view.findViewById(R.id.tvKategori)
        val tvCatatan: TextView = view.findViewById(R.id.tvCatatan)
        val tvTanggal: TextView = view.findViewById(R.id.tvTanggal)
        val tvNominal: TextView = view.findViewById(R.id.tvNominal)
        val imgKategori: ImageView = view.findViewById(R.id.imgKategori)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaksi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        val ctx = holder.itemView.context

        // Kategori is non-nullable, so direct assignment is safe.
        holder.tvKategori.text = data.kategori

        // Correctly and safely handle nullable Catatan.
        if (!data.catatan.isNullOrEmpty()) {
            holder.tvCatatan.visibility = View.VISIBLE
            holder.tvCatatan.text = data.catatan
        } else {
            holder.tvCatatan.visibility = View.GONE
        }
        
        // Format Tanggal
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))
        holder.tvTanggal.text = sdf.format(Date(data.timestamp))

        // Nominal & Warna
        if (data.jenis == "Pemasukan") {
            holder.tvNominal.text = "+ ${formatRupiah(data.nominal)}"
            holder.tvNominal.setTextColor(ContextCompat.getColor(ctx, R.color.green_primary))
        } else {
            holder.tvNominal.text = "- ${formatRupiah(data.nominal)}"
            holder.tvNominal.setTextColor(ContextCompat.getColor(ctx, R.color.red_primary))
        }
        
        // Icon mapping (kategori is non-nullable).
        holder.imgKategori.setImageResource(getIconForCategory(data.kategori))
    }

    // This function correctly expects a non-nullable String, as defined in Transaksi.kt.
    private fun getIconForCategory(kategori: String): Int {
        return when (kategori.lowercase(Locale.getDefault())) {
            "makanan" -> R.drawable.ic_cat_makanan
            "transportasi" -> R.drawable.ic_cat_transportasi
            "belanja" -> R.drawable.ic_cat_belanja
            "tagihan" -> R.drawable.ic_cat_tagihan
            "hiburan" -> R.drawable.ic_cat_hiburan
            "kesehatan" -> R.drawable.ic_cat_kesehatan
            "pendidikan" -> R.drawable.ic_cat_pendidikan
            else -> R.drawable.ic_cat_lainnya
        }
    }

    override fun getItemCount(): Int = list.size

    private fun formatRupiah(value: Long): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return formatter.format(value).replace(",00", "")
    }
}