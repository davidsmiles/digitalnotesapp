package com.app.takenotes

import android.graphics.Canvas
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

/** class [NoteItemTouchHelper] responsible for the movements associated with the Notes Recycler view on main page
 */
class NoteItemTouchHelper(dragDirs: Int, swipeDirs: Int, listener: NoteItemTouchListener):
    ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

    var listener: NoteItemTouchListener?
    init {
        this.listener = listener
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder1: RecyclerView.ViewHolder, viewHolder2: RecyclerView.ViewHolder) = false

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return 0.2F
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.3F
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if(listener != null){
            listener!!.onSwiped(viewHolder, direction, viewHolder.adapterPosition)
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val foreground = (viewHolder as NotesAdapter.ViewHolder).foreground
        getDefaultUIUtil().onDraw(c, recyclerView, foreground, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onChildDrawOver(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder?,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ){
        val foreground = (viewHolder as NotesAdapter.ViewHolder).foreground
        getDefaultUIUtil().onDrawOver(c, recyclerView, foreground, dX, dY, actionState, isCurrentlyActive)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        val foreground = (viewHolder as NotesAdapter.ViewHolder).foreground
        getDefaultUIUtil().clearView(foreground)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if(viewHolder != null) {
            val foreground = (viewHolder as NotesAdapter.ViewHolder).foreground
            getDefaultUIUtil().onSelected(foreground)
        }
    }
}