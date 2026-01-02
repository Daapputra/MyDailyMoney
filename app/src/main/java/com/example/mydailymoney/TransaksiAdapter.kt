package com.example.mydailymoney

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransaksiAdapter(
    private val list: List<Transaksi>,
    private val onLongClick: ((Transaksi) -> Unit)? = null
) : RecyclerView.Adapter<TransaksiAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvNominal: TextView = view.findViewById(R.id.tvNominal)
        val imgIcon: ImageView = view.findViewById(R.id.imgIcon)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        // Container for row to handle click properly if needed
        val rowContainer: View = view.findViewById(R.id.rowContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaksi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        val ctx = holder.itemView.context

        // Title: Use Catatan if available, else Kategori. If both empty, use "-"
        val titleText = if (!data.catatan.isNullOrEmpty()) {
            data.catatan
        } else {
            data.kategori.uppercase()
        }
        holder.tvTitle.text = titleText

        // Category Text
        holder.tvCategory.text = data.kategori

        // Nominal, Color, and Icon Styling
        if (data.jenis == "Pemasukan") {
            holder.tvNominal.text = "+ ${formatRupiah(data.nominal)}"
            holder.tvNominal.setTextColor(ContextCompat.getColor(ctx, R.color.green_primary))
            
            // Icon Styling for Income (Green)
            ImageViewCompat.setImageTintList(holder.imgIcon, ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.green_primary)))
            holder.imgIcon.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8F5E9")) // Green Soft
        } else {
            holder.tvNominal.text = "- ${formatRupiah(data.nominal)}"
            holder.tvNominal.setTextColor(ContextCompat.getColor(ctx, R.color.red_primary))
            
            // Icon Styling for Expense (Red)
            ImageViewCompat.setImageTintList(holder.imgIcon, ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.red_primary)))
            holder.imgIcon.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFEBEE")) // Red Soft
        }

        // Icon Logic: Use category name to fetch correct resource
        // This puts the specific category icon (Food, Transport, etc.) into the main icon slot
        holder.imgIcon.setImageResource(getIconForCategory(data.kategori))

        // Date
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))
        holder.tvDate.text = sdf.format(Date(data.timestamp))

        // Long Click Listener (untuk Edit/Hapus)
        holder.rowContainer.setOnLongClickListener {
            onLongClick?.invoke(data)
            true
        }
    }

    private fun getIconForCategory(kategori: String): Int {
        // Matches the mapping in TambahTransaksiActivity.kt
        return when (kategori.lowercase(Locale.getDefault())) {
            "makanan" -> R.drawable.ic_cat_makanan
            "transportasi" -> R.drawable.ic_cat_transportasi
            "belanja" -> R.drawable.ic_cat_belanja
            "tagihan" -> R.drawable.ic_cat_tagihan
            "hiburan" -> R.drawable.ic_cat_hiburan
            "kesehatan" -> R.drawable.ic_cat_kesehatan
            "pendidikan" -> R.drawable.ic_cat_pendidikan
            "gaji" -> R.drawable.ic_cat_gaji
            "kerja" -> R.drawable.ic_cat_freelance
            "usaha" -> R.drawable.ic_cat_usaha
            "dagang" -> R.drawable.ic_cat_dagang
            "komisi" -> R.drawable.ic_cat_komisi
            "bonus" -> R.drawable.ic_cat_bonus
            "investasi" -> R.drawable.ic_money
            else -> R.drawable.ic_cat_lainnya
        }
    }

    override fun getItemCount(): Int = list.size

    private fun formatRupiah(value: Long): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return formatter.format(value).replace(",00", "")
    }
}