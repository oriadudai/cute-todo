package com.oriadudai.cutetodo

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.widget.RemoteViews
import android.widget.RemoteViewsService

class TaskWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = Factory(applicationContext)

    private class Factory(private val context: Context) : RemoteViewsFactory {
        private var tasks: List<Task> = emptyList()

        override fun onCreate() {}
        override fun onDataSetChanged() { tasks = TaskStore.load(context) }
        override fun onDestroy() {}
        override fun getCount() = tasks.size
        override fun getLoadingView(): RemoteViews? = null
        override fun getViewTypeCount() = 1
        override fun getItemId(position: Int) = tasks.getOrNull(position)?.id ?: position.toLong()
        override fun hasStableIds() = true

        override fun getViewAt(position: Int): RemoteViews {
            val rv = RemoteViews(context.packageName, R.layout.widget_item)
            val task = tasks.getOrNull(position) ?: return rv

            rv.setTextViewText(R.id.item_emoji, task.emoji)
            if (task.done) {
                val s = SpannableString(task.title)
                s.setSpan(StrikethroughSpan(), 0, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                rv.setTextViewText(R.id.item_title, s)
                rv.setTextColor(R.id.item_title, 0xFFA89BA8.toInt())
                rv.setImageViewResource(R.id.item_check, R.drawable.ic_check_on)
                rv.setInt(R.id.item_root, "setBackgroundResource", R.drawable.bg_pill_done)
            } else {
                rv.setTextViewText(R.id.item_title, task.title)
                rv.setTextColor(R.id.item_title, 0xFF4A3B52.toInt())
                rv.setImageViewResource(R.id.item_check, R.drawable.ic_check_off)
                rv.setInt(
                    R.id.item_root, "setBackgroundResource",
                    Palette.PILL_DRAWABLES[task.colorIndex.mod(Palette.PILL_DRAWABLES.size)]
                )
            }

            rv.setOnClickFillInIntent(
                R.id.item_root,
                Intent().putExtra(TaskWidgetProvider.EXTRA_TASK_ID, task.id)
            )
            return rv
        }
    }
}
