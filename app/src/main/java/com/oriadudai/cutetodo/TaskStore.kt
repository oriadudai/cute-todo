package com.oriadudai.cutetodo

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Tasks are stored only on the phone, as JSON in SharedPreferences. */
object TaskStore {
    private const val PREFS = "cute_todo"
    private const val KEY = "tasks"

    @Synchronized
    fun load(context: Context): List<Task> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
            ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Task(
                    id = o.getLong("id"),
                    title = o.getString("title"),
                    emoji = o.optString("emoji", "🌸"),
                    colorIndex = o.optInt("color", 0),
                    done = o.optBoolean("done", false),
                    createdAt = o.optLong("createdAt", 0L),
                    doneAt = o.optLong("doneAt", 0L),
                )
            }.let(::sorted)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Open tasks first (newest on top), then done tasks (most recently done on top). */
    private fun sorted(list: List<Task>): List<Task> =
        list.filter { !it.done }.sortedByDescending { it.createdAt } +
            list.filter { it.done }.sortedByDescending { it.doneAt }

    @Synchronized
    private fun save(context: Context, list: List<Task>) {
        val arr = JSONArray()
        list.forEach { t ->
            arr.put(JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("emoji", t.emoji)
                put("color", t.colorIndex)
                put("done", t.done)
                put("createdAt", t.createdAt)
                put("doneAt", t.doneAt)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, arr.toString()).commit()
        TaskWidgetProvider.refreshAll(context)
    }

    @Synchronized
    fun add(context: Context, title: String) {
        val list = load(context)
        // Random emoji, avoiding the ones used by the 3 newest tasks so neighbours differ
        val recent = list.sortedByDescending { it.createdAt }.take(3).map { it.emoji }.toSet()
        val emoji = (Palette.EMOJIS - recent).ifEmpty { Palette.EMOJIS }.random()
        val now = System.currentTimeMillis()
        val color = ((list.maxOfOrNull { it.colorIndex } ?: -1) + 1).mod(Palette.CARD_COLORS.size)
        save(context, list + Task(id = now, title = title, emoji = emoji, colorIndex = color, createdAt = now))
    }

    @Synchronized
    fun toggle(context: Context, id: Long) {
        save(context, load(context).map {
            if (it.id == id) it.copy(done = !it.done, doneAt = if (!it.done) System.currentTimeMillis() else 0L)
            else it
        })
    }

    @Synchronized
    fun delete(context: Context, id: Long) {
        save(context, load(context).filterNot { it.id == id })
    }

    @Synchronized
    fun restore(context: Context, task: Task) {
        save(context, load(context).filterNot { it.id == task.id } + task)
    }

    @Synchronized
    fun clearDone(context: Context): List<Task> {
        val all = load(context)
        save(context, all.filterNot { it.done })
        return all.filter { it.done }
    }

    @Synchronized
    fun restoreAll(context: Context, tasks: List<Task>) {
        val ids = tasks.map { it.id }.toSet()
        save(context, load(context).filterNot { it.id in ids } + tasks)
    }
}
