package com.example.mydailymoney

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class TabunganAdapter(
    private val list: List<Tabungan>,
    private val onClick: (Tabungan) -> Unit
) : RecyclerView.Adapter<TabunganAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNamaTabungan)
        val tvPersen: TextView = view.findViewById(R.id.tvPersen)
        val tvTerkumpul: TextView = view.findViewById(R.id.tvTerkumpul)
        val tvTarget: TextView = view.findViewById(R.id.tvTarget)
        val progress: ProgressBar = view.findViewById(R.id.progressTabungan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tabungan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvNama.text = item.nama
        
        val percent = if (item.target > 0) (item.terkumpul * 100 / item.target).toInt() else 0
        holder.tvPersen.text = "$percent%"
        holder.progress.progress = percent
        
        holder.tvTerkumpul.text = formatRupiah(item.terkumpul)
        holder.tvTarget.text = formatRupiah(item.target)
        
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = list.size

    private fun formatRupiah(v: Long): String =
        NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            .format(v)
            .replace(",00", "")
}