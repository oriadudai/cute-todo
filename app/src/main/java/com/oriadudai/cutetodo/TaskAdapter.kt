package com.oriadudai.cutetodo

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox

class TaskAdapter(
    private val onToggle: (Task) -> Unit,
) : ListAdapter<Task, TaskAdapter.Holder>(Diff) {

    object Diff : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(a: Task, b: Task) = a.id == b.id
        override fun areContentsTheSame(a: Task, b: Task) = a == b
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.card)
        val check: MaterialCheckBox = view.findViewById(R.id.check)
        val emoji: TextView = view.findViewById(R.id.emoji)
        val title: TextView = view.findViewById(R.id.title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false))

    override fun onBindViewHolder(h: Holder, position: Int) {
        val task = getItem(position)
        h.emoji.text = task.emoji
        h.title.text = task.title

        h.check.setOnCheckedChangeListener(null)
        h.check.isChecked = task.done
        h.check.setOnCheckedChangeListener { _, _ -> onToggle(task) }
        h.card.setOnClickListener { onToggle(task) }

        if (task.done) {
            h.title.paintFlags = h.title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            h.card.setCardBackgroundColor(0xFFF3F0F2.toInt())
            h.card.alpha = 0.6f
        } else {
            h.title.paintFlags = h.title.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            h.card.setCardBackgroundColor(Palette.CARD_COLORS[task.colorIndex.mod(Palette.CARD_COLORS.size)])
            h.card.alpha = 1f
        }
    }

    fun taskAt(position: Int): Task? = currentList.getOrNull(position)
}
