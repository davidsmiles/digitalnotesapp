package com.app.takenotes

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.miguelcatalan.materialsearchview.MaterialSearchView
import kotlinx.android.synthetic.main.activity_notes.*
import org.jetbrains.anko.alert
import java.util.*


class NotesActivity : AppCompatActivity(), NoteItemTouchListener {

    companion object {
        var favorite_clicked = false
        var default_order = "TIME DESC"
    }

    /** var [current_view_page] checks what page user is on
     *  1 to represent All Notes page
     *  2 to represent Favorite Notes Page*/
    var current_view_page = 1

    val notes_list = ArrayList<Notes>()
    lateinit var noteAdapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        setSupportActionBar(toolbar as Toolbar)
        supportActionBar!!.title = getString(R.string.notes)

        retrieveNotes()
        setViews()
    }

    private fun setViews(){
        noteAdapter = NotesAdapter(this, notes_list)
        notes_recycler.apply {
            val linearLayoutManager = LinearLayoutManager(this@NotesActivity)
            notes_recycler.apply {
                setHasFixedSize(true)
                adapter = noteAdapter
                layoutManager = linearLayoutManager
                itemAnimator = DefaultItemAnimator()
                addItemDecoration(DividerItemDecoration(this@NotesActivity, linearLayoutManager.orientation))
            }

            val itemTouchHelperCallback = NoteItemTouchHelper(0, ItemTouchHelper.RIGHT, this@NotesActivity)
            ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(this)
        }

        materialSearchView.apply {
            setHint(getString(R.string.search_note))
            showVoice(true)
            setVoiceSearch(true)
            setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    filterSearch(query!!)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    filterSearch(newText!!)
                    return true
                }

            })

            setOnSearchViewListener(object: MaterialSearchView.SearchViewListener{
                override fun onSearchViewClosed() {
                    if(favorite_clicked) retrieveFavorites() else retrieveNotes()
                }

                override fun onSearchViewShown() {

                }

            })
        }

        note_fab_button.setOnClickListener {
            val intent = Intent(this@NotesActivity, AddNote::class.java)
            startActivity(intent)
        }
    }

    /**
     *  [NoteItemTouchListener] implemented method [onSwiped] gets called as User swipes Note
     *  onSwipe calls [NotesAdapter.shareNote] which shares the note
     */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        if(viewHolder is NotesAdapter.ViewHolder){
            val index = viewHolder.adapterPosition
            noteAdapter.shareNote(index)
        }
    }

    /**
     *  Gets the user's string [query] in the Search field and filters through Arrays of Notes
     *  and updates the adapter based on results found
     */
    private fun filterSearch(query: String){
        val notes_list = notes_list.filter {
            it.text.contains(query)
        }

        noteAdapter = NotesAdapter(this, notes_list as ArrayList<Notes>)
        notes_recycler.adapter = noteAdapter

        viewIfEmpty()
    }

    /** If results returned from the database is empty, then this code takes effect
     *  and changes the view to display the empty page view*/
    fun viewIfEmpty(){
        empty_view.apply {
            visibility = if(notes_list.size == 0) View.VISIBLE else View.GONE
            empty_view_text.text = if(current_view_page == 1)
                String.format(Locale.getDefault(), "Ooops, you do not have any notes saved up. To write a note, please click on the button below")
            else
                String.format(Locale.getDefault(), "You have no notes marked as favorite.")
        }
    }

    /**
     *  Method call to function [RetrieveNotes]
     */
    private fun retrieveNotes(){
        RetrieveNotes(this@NotesActivity).execute()
    }

    /**
     *  Method call to function [RetrieveFavorites]
     */
    fun retrieveFavorites(){
        RetrieveFavorites(this@NotesActivity).execute()
    }

    /**
     *  Asynchronous class [RetrieveNotes] fetches the data from database retrieving all notes
     *  and updates the Adapter
     */
    class RetrieveNotes(val ctx: NotesActivity): AsyncTask<Void, Void, ArrayList<Notes>>(){

        val _notes_list = ArrayList<Notes>()

        override fun doInBackground(vararg params: Void?): ArrayList<Notes> {
            val notesDb = NotesDb(ctx)
            val rdb = notesDb.readableDatabase
            val cursor =
                rdb.query("notes_db", arrayOf("_id", "NOTE_TEXT", "CATEGORY", "TIME", "FAVORITE"),
                    null, null, null, null, ctx.getOrder())

            while(cursor.moveToNext()){
                val index = cursor.getInt(cursor.getColumnIndex("_id"))
                val text = cursor.getString(cursor.getColumnIndex("NOTE_TEXT"))
                val cate = cursor.getString(cursor.getColumnIndex("CATEGORY"))
                val time = cursor.getLong(cursor.getColumnIndex("TIME"))
                val favryt = cursor.getString(cursor.getColumnIndex("FAVORITE"))
                _notes_list.add(Notes(index, text, cate, time, favryt))
            }

            cursor.close()
            rdb.close()
            return _notes_list
        }

        override fun onPostExecute(result: ArrayList<Notes>?) {
            super.onPostExecute(result)
            ctx.apply {
                current_view_page = 1

                notes_list.clear()
                notes_list.addAll(result!!)
                noteAdapter.notifyDataSetChanged()

                viewIfEmpty()
            }
        }
    }

    /**
     *  Asynchronous class [RetrieveNotes] fetches the data from database retrieving all marked favorite notes
     *  and updates the Adapter
     */
    class RetrieveFavorites(val ctx: NotesActivity): AsyncTask<Void, Void, ArrayList<Notes>>(){
        val notes_list = ArrayList<Notes>()

        override fun doInBackground(vararg params: Void?): ArrayList<Notes> {
            val notesDb = NotesDb(ctx)
            val rdb = notesDb.readableDatabase
            val cursor =
                rdb.query("notes_db", arrayOf("_id", "NOTE_TEXT", "CATEGORY", "TIME", "FAVORITE"),
                    "FAVORITE = ?", arrayOf("1"), null, null, ctx.getOrder())

            while(cursor.moveToNext()){
                val index = cursor.getInt(cursor.getColumnIndex("_id"))
                val text = cursor.getString(cursor.getColumnIndex("NOTE_TEXT"))
                val cate = cursor.getString(cursor.getColumnIndex("CATEGORY"))
                val time = cursor.getLong(cursor.getColumnIndex("TIME"))
                val favryt = cursor.getString(cursor.getColumnIndex("FAVORITE"))
                notes_list.add(Notes(index, text, cate, time, favryt))
            }

            cursor.close()
            rdb.close()
            return notes_list
        }

        override fun onPostExecute(result: ArrayList<Notes>?) {
            super.onPostExecute(result)
            ctx.apply {
                current_view_page = 2

                notes_list.clear()
                notes_list.addAll(result!!)
                noteAdapter.notifyDataSetChanged()

                viewIfEmpty()
            }
        }
    }

    /**
     * Gets the [default_order] of how the Database should return the Notes data
     */
    private fun getOrder(): String? {
        val order = getSharedPreferences("notes", Context.MODE_PRIVATE)
        return order.getString("order", default_order)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_note, menu)
        val item = menu!!.findItem(R.id.search)
        materialSearchView.setMenuItem(item)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.favourite -> {
                favorite_clicked = !favorite_clicked
                if(favorite_clicked){
                    retrieveFavorites()
                    item.title = getString(R.string.all_notes)
                    item.icon = ContextCompat.getDrawable(this, R.drawable.ic_note)
                }
                else{
                    retrieveNotes()
                    item.title = getString(R.string.favourite)
                    item.icon = ContextCompat.getDrawable(this, R.drawable.ic_favorite)
                }
                return true
            }
            R.id.sort_time -> {
                getSharedPreferences("notes", Context.MODE_PRIVATE).edit().run{
                    if(getOrder()!!.contentEquals(default_order)){
                        putString("order", "TIME ASC")
                        apply()
                    }
                    else{
                        putString("order", default_order)
                        apply()
                    }
                }
                retrieveNotes()
                return true
            }
            R.id.sort_category -> {
                getSharedPreferences("notes", Context.MODE_PRIVATE).edit().run{
                    putString("order", "CATEGORY ASC")
                    apply()
                }
                retrieveNotes()
                return true
            }
            R.id.delete_all -> {
                alert("All notes will be deleted.", "Delete"){
                    positiveButton("OK"){
                        NotesDb(this@NotesActivity).writableDatabase
                            .delete("notes_db", null, null)

                        retrieveNotes()
                    }
                    negativeButton("CANCEL"){}
                }.show()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MaterialSearchView.REQUEST_VOICE -> if (resultCode == RESULT_OK) {
                val result = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val searchword = result[0]
                materialSearchView.setQuery(searchword, true)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }

    override fun onResume() {
        super.onResume()
        if(favorite_clicked) retrieveFavorites() else retrieveNotes()
    }
}
