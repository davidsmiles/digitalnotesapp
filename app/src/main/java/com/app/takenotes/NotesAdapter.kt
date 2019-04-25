package com.app.takenotes

import android.content.ContentValues
import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_notes.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class NotesAdapter(val ctx: NotesActivity, val notesArrays: ArrayList<Notes>): RecyclerView.Adapter<NotesAdapter.ViewHolder>(){

    override fun onCreateViewHolder(group: ViewGroup, pos: Int): ViewHolder {
        val view = LayoutInflater.from(group.context).inflate(R.layout.single_note, group, false)
        return ViewHolder(view as FrameLayout)
    }

    override fun getItemCount(): Int = notesArrays.size

    /** Array of colors for beautiful color scheme of the Category textview */
    val array_color = intArrayOf(R.color.colorAccent, R.color.yellow, R.color.blue, R.color.purple,
        R.color.green, R.color.deepgreen, R.color.pink)

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        /** Pick a random color in [array_color] */
        val random = (Math.random() * array_color.size).toInt()

        val note = notesArrays.get(pos)
        val view = holder.foreground

        with(note){
            view.find<TextView>(R.id.note_text).text = text
            view.find<TextView>(R.id.note_category).apply {
                text = category
                setTextColor(ctx.resources.getColor(array_color[random]))
            }
            view.find<TextView>(R.id.note_time).text = SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.getDefault())
                .format(Date(time))
            view.find<ImageView>(R.id.fav_button).apply {
                if(favorite.contentEquals("1")) setImageResource(R.drawable.ic_star_on)
                else setImageResource(R.drawable.ic_star)

                setOnClickListener {
                    NotesDb(ctx).writableDatabase.update(ctx.getString(R.string.table_name), ContentValues().apply {
                        put(ctx.getString(R.string.favorite), if(favorite.contentEquals("1")) "0" else "1")
                    }, "_id = ?", arrayOf(index.toString()))

                    if(favorite.contentEquals("1")) {
                        setImageResource(R.drawable.ic_star)
                        if(NotesActivity.favorite_clicked) {
                            ctx.retrieveFavorites()
                        }
                        ctx.coordinator.snackbar(ctx.getString(R.string.note_removed)).show()
                    }
                    else{
                        setImageResource(R.drawable.ic_star_on)
                        ctx.coordinator.snackbar(ctx.getString(R.string.note_added)).show()
                    }
                }
            }
        }

        view.setOnClickListener{
            Intent(ctx, NoteDetail::class.java).apply {
                NoteDetail.Note = notesArrays.get(pos)
                ctx.startActivity(this)
            }

            if(ctx.materialSearchView.isSearchOpen) ctx.materialSearchView.closeSearch()
        }
    }

    class ViewHolder(frameLayout: FrameLayout) : RecyclerView.ViewHolder(frameLayout) {
        val view = frameLayout
        val foreground = view.find<CardView>(R.id.foreground)
    }

    /**
     *  Creates an Send Intent and attempts to send the Note in the swiped [position]
     */
    fun shareNote(position: Int){
        val note = notesArrays.get(position)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, note.text)
        }

        val chooser = Intent.createChooser(intent, ctx.getString(R.string.share_via))
        ctx.startActivity(chooser)
    }

}

