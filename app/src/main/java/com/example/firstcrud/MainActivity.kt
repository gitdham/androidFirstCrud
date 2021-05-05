package com.example.firstcrud

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstcrud.room.Constant
import com.example.firstcrud.room.Note
import com.example.firstcrud.room.NoteDB
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

  val db by lazy { NoteDB(this) }
  lateinit var noteAdapter: NoteAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setupListener()
    setupRecylerView()
  }

  override fun onStart() {
    super.onStart()
    loadData()
  }

  fun loadData() {
    CoroutineScope(Dispatchers.IO).launch {
      val notes = db.noteDao().getNotes()
      Log.d("MainActivity", "dbResponse: $notes")
      withContext(Dispatchers.Main) {
        noteAdapter.setData(notes)
      }
    }
  }

  private fun setupListener() {
    button_create.setOnClickListener {
      intentEdit(0, Constant.TYPE_CREATE)
    }
  }

  fun intentEdit(noteId: Int, intentType: Int) {
    startActivity(
      Intent(applicationContext, EditActivity::class.java)
        .putExtra("intent_id", noteId)
        .putExtra("intent_type", intentType)
    )
  }

  private fun setupRecylerView() {
    noteAdapter = NoteAdapter(arrayListOf(), object : NoteAdapter.OnAdapterListener {
      override fun onClick(note: Note) {
        intentEdit(note.id, Constant.TYPE_READ)
      }

      override fun onUpdate(note: Note) {
        intentEdit(note.id, Constant.TYPE_UPDATE)
      }

      override fun onDelete(note: Note) {
        deleteDialog(note)
      }
    })
    list_note.apply {
      layoutManager = LinearLayoutManager(applicationContext)
      adapter = noteAdapter
    }
  }

  private fun deleteDialog(note: Note) {
    val alertDialog = AlertDialog.Builder(this)
    alertDialog.apply {
      setTitle("Konfirmasi")
      setMessage("Yakin delete ${note.title}?")
      setNegativeButton("Cancel") { dialog, _ ->
        dialog.dismiss()
      }
      setPositiveButton("Delete") { dialog, _ ->
        dialog.dismiss()
        CoroutineScope(Dispatchers.IO).launch {
          db.noteDao().deleteNote(note)
          loadData()
        }
      }
    }
    alertDialog.show()
  }
}