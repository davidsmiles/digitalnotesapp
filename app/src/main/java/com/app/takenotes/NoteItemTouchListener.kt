package com.app.takenotes

import android.support.v7.widget.RecyclerView

interface NoteItemTouchListener {
    fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int)
}
