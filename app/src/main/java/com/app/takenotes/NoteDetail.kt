package com.app.takenotes

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_note_detail.*
import org.jetbrains.anko.alert
import java.text.SimpleDateFormat
import java.util.*

class NoteDetail : AppCompatActivity() {

    companion object {
        lateinit var Note: Notes
    }

    lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        setSupportActionBar(toolbar_deet as Toolbar)
        supportActionBar!!.apply {
            title = getString(R.string.reading)
            setHomeAsUpIndicator(R.drawable.ic_nav_before)
        }

        initTTS()
        setpageUp()
    }

    /** Initialize the TextToSpeech API */
    private fun initTTS(){
        textToSpeech = TextToSpeech(this){
            TextToSpeech.OnInitListener {
                if(it != TextToSpeech.ERROR){
                    textToSpeech.language = Locale.getDefault()
                }
            }}
    }

    private fun setpageUp(){
        note_view.movementMethod = ScrollingMovementMethod()

        intent.apply {
            note_view.text = Note.text
            category_view.text = Note.category
            time_view.text = SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.getDefault())
                .format(Note.time)
        }

        edit_note.setOnClickListener {
            Intent(this@NoteDetail, AddNote::class.java).apply {
                putExtra(AddNote.from, "Modify Note")
                AddNote.Note = Note
                startActivity(this)
            }
        }

        category_view.apply {
            val array_cat = resources.getStringArray(R.array.categories).map{it.toLowerCase()}
            when(Note.category){
                array_cat[0] -> setTextColor(resources.getColor(R.color.blue))
                array_cat[1] -> setTextColor(resources.getColor(R.color.yellow))
                array_cat[2] -> setTextColor(resources.getColor(R.color.colorAccent))
                array_cat[3] -> setTextColor(resources.getColor(R.color.purple))
                else -> setTextColor(resources.getColor(R.color.green))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item!!.itemId){
            R.id.play_audio -> {
                textToSpeech.apply {
                    speak(Note.text, TextToSpeech.QUEUE_FLUSH, null)
                }

                true
            }
            R.id.delete -> {
                alert(String.format(Locale.getDefault(), "The note will be deleted."), "Delete"){
                    positiveButton("OK"){
                        val time = Note.time.toString()
                        NotesDb(this@NoteDetail).writableDatabase
                            .delete("notes_db", "TIME = ?", arrayOf(time))

                        Intent(this@NoteDetail, NotesActivity::class.java).also {
                            startActivity(it)
                        }
                    }
                    negativeButton("CANCEL"){}
                }.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        initTTS()
    }

    override fun onStop() {
        super.onStop()
        textToSpeech.shutdown()
    }
}
