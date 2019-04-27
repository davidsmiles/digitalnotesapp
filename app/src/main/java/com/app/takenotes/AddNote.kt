package com.app.takenotes

import android.content.ContentValues
import android.content.Intent
import android.graphics.PorterDuff
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_add_note.*
import org.jetbrains.anko.toast
import java.util.*

class AddNote : AppCompatActivity() {

    companion object {
        val from = "hasIntent"
        lateinit var Note: Notes
        val viaSpeechIntent = "hasSpeechIntent"
    }

    val SPEECH_RECOGNIZER = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        setSupportActionBar(toolbar as Toolbar)
        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_nav_before)
            title = if(intent.hasExtra(from)) getString(R.string.modify_note) else getString(R.string.newnote)
        }

        note_msg.background.mutate().setColorFilter(resources.getColor(R.color.white), PorterDuff.Mode.SRC_ATOP)

        setviewUp()
    }

    private fun setviewUp(){
        if(intent.hasExtra(from)){
            note_msg.editableText.also{
                it.replace(0, it.length, Note.text)
            }
            val index = resources.getStringArray(R.array.categories).map { it.toLowerCase() }.indexOf(Note.category)
            categories.setSelection(index, true)
        }

        if(intent.hasExtra(viaSpeechIntent)){
            val result = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            note_msg.editableText.also{
                it.replace(0, it.length, result[0])
            }
        }

        note_speech.setOnClickListener {
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.say_something))
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                startActivityForResult(this, SPEECH_RECOGNIZER)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.submit -> {
                val note = "${note_msg.text}"
                val category = (categories.selectedItem as String).toLowerCase()
                return if(!note.isEmpty()){
                    NotesDb(this@AddNote).apply {
                        val cv = ContentValues().apply {
                            put("NOTE_TEXT", note)
                            put("CATEGORY", category)
                            put("TIME", Date().time)
                            put("FAVORITE", if(intent.hasExtra(from)) Note.favorite else "0")
                        }
                        if(!intent.hasExtra(from)) {
                            writableDatabase.run {
                                insert(getString(R.string.table_name), null, cv)
                                close()

                                toast(getString(R.string.note_saved))
                            }
                        }
                        else{
                            writableDatabase.run {
                                update(getString(R.string.table_name), cv, "TIME = ?",
                                    arrayOf(Note.time.toString()))
                                close()

                                toast(getString(R.string.note_modified))
                            }
                        }
                    }

                    val previous = Intent(this@AddNote, NotesActivity::class.java)
                    startActivity(previous)
                    true
                }
                else {
                    toast("Empty note is not created")
                    val previous = Intent(this@AddNote, NotesActivity::class.java)
                    startActivity(previous)
                    true
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            SPEECH_RECOGNIZER -> {
                if(resultCode == RESULT_OK){
                    val result = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    note_msg.editableText.also{
                        if(it.isNotEmpty()) it.append(" ${result[0]}") else it.append(result[0])
                    }
                }
            }
        }
    }
}
