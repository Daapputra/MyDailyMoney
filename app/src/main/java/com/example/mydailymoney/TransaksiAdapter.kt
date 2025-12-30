package com.example.mydailymoney

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class TransaksiAdapter(
    private val list: List<Transaksi>
) : RecyclerView.Adapter<TransaksiAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvKategori: TextView = view.findViewById(R.id.tvKategori)
        val tvJenis: TextView = view.findViewById(R.id.tvJenis)
        val tvNominal: TextView = view.findViewById(R.id.tvNominal)
        val tvCatatan: TextView = view.findViewById(R.id.tvCatatan)

        // 🔑 TAMBAHAN ICON
        val imgJenis: ImageView = view.findViewById(R.id.imgJenis)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaksi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        val ctx = holder.itemView.context

        holder.tvKategori.text = data.kategori
        holder.tvJenis.text = data.jenis

        // ===== NOMINAL + WARNA =====
        if (data.jenis == "Pemasukan") {
            holder.tvNominal.text = "+ ${formatRupiah(data.nominal)}"
            holder.tvNominal.setTextColor(
                ContextCompat.getColor(ctx, android.R.color.holo_green_dark)
            )

            // ICON PEMASUKAN
            holder.imgJenis.setImageResource(R.drawable.ic_arrow_up)
            holder.imgJenis.setBackgroundResource(R.drawable.bg_circle_green)

        } else {
            holder.tvNominal.text = "- ${formatRupiah(data.nominal)}"
            holder.tvNominal.setTextColor(
                ContextCompat.getColor(ctx, android.R.color.holo_red_dark)
            )

            // ICON PENGELUARAN
            holder.imgJenis.setImageResource(R.drawable.ic_arrow_down)
            holder.imgJenis.setBackgroundResource(R.drawable.bg_circle_red)
        }

        // ===== CATATAN (JIKA ADA) =====
        if (!data.catatan.isNullOrBlank()) {
            holder.tvCatatan.visibility = View.VISIBLE
            holder.tvCatatan.text = "📝 ${data.catatan}"
        } else {
            holder.tvCatatan.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = list.size

    private fun formatRupiah(value: Long): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return formatter.format(value).replace(",00", "")
    }
}
