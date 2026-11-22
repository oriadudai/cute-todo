package com.oriadudai.cutetodo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FOCUS_INPUT = "focus_input"
    }

    private lateinit var adapter: TaskAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var input: EditText
    private lateinit var countText: TextView
    private lateinit var clearDone: TextView
    private lateinit var emptyView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val root = findViewById<View>(R.id.root)
        WindowCompat.getInsetsController(window, root).isAppearanceLightStatusBars = true
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, ime.bottom))
            insets
        }

        recycler = findViewById(R.id.recycler)
        input = findViewById(R.id.input)
        countText = findViewById(R.id.count)
        clearDone = findViewById(R.id.clear_done)
        emptyView = findViewById(R.id.empty)

        adapter = TaskAdapter(onToggle = { task ->
            TaskStore.toggle(this, task.id)
            refresh()
        })
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val task = adapter.taskAt(vh.bindingAdapterPosition) ?: return
                TaskStore.delete(this@MainActivity, task.id)
                refresh()
                Snackbar.make(recycler, getString(R.string.deleted), Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo) {
                        TaskStore.restore(this@MainActivity, task)
                        refresh()
                    }.show()
            }
        }).attachToRecyclerView(recycler)

        findViewById<View>(R.id.add_button).setOnClickListener { addTask() }
        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) { addTask(); true } else false
        }

        clearDone.setOnClickListener {
            val removed = TaskStore.clearDone(this)
            refresh()
            if (removed.isNotEmpty()) {
                Snackbar.make(recycler, getString(R.string.cleared), Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo) {
                        TaskStore.restoreAll(this, removed)
                        refresh()
                    }.show()
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        refresh() // the widget may have changed tasks while the app was in the background
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.getBooleanExtra(EXTRA_FOCUS_INPUT, false)) {
            intent.removeExtra(EXTRA_FOCUS_INPUT)
            input.requestFocus()
            input.postDelayed({
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            }, 250)
        }
    }

    private fun addTask() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) return
        TaskStore.add(this, text)
        input.setText("")
        refresh()
        recycler.post { recycler.scrollToPosition(0) }
    }

    private fun refresh() {
        val tasks = TaskStore.load(this)
        adapter.submitList(tasks)
        val open = tasks.count { !it.done }
        countText.text = when {
            tasks.isEmpty() -> getString(R.string.no_tasks_yet)
            open == 0 -> getString(R.string.all_done)
            open == 1 -> getString(R.string.one_left)
            else -> getString(R.string.n_left, open)
        }
        clearDone.visibility = if (tasks.any { it.done }) View.VISIBLE else View.GONE
        emptyView.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
    }
}
