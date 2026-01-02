package com.example.mydailymoney

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

data class CategoryIcon(val id: String, val name: String, val iconRes: Int)

class CategoryAdapter(
    private val items: List<CategoryIcon>,
    private val onSelected: (CategoryIcon) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var selectedPosition = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: LinearLayout = view.findViewById(R.id.layoutItem)
        val icon: ImageView = view.findViewById(R.id.ivIcon)
        val text: TextView = view.findViewById(R.id.tvName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_icon, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.text.text = item.name
        holder.icon.setImageResource(item.iconRes)

        val isSelected = position == selectedPosition
        holder.layout.isSelected = isSelected
        
        if (isSelected) {
             holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.green_primary))
             holder.text.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green_primary))
        } else {
             holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.text_secondary))
             holder.text.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.text_primary))
        }

        holder.itemView.setOnClickListener {
            val oldPos = selectedPosition
            selectedPosition = holder.bindingAdapterPosition
            notifyItemChanged(oldPos)
            notifyItemChanged(selectedPosition)
            onSelected(item)
        }
    }

    override fun getItemCount() = items.size
}