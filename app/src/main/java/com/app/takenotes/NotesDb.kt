package com.app.takenotes

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NotesDb(ctx: Context): SQLiteOpenHelper(ctx, "NOTES", null, 1){
    val query = """
        CREATE TABLE ${ctx.getString(R.string.table_name)} (_id INTEGER PRIMARY KEY AUTOINCREMENT,
            ${ctx.getString(R.string.note_text)} TEXT,
            ${ctx.getString(R.string.category)} TEXT,
            ${ctx.getString(R.string.time)} NUMERIC,
            ${ctx.getString(R.string.favorite)} TEXT
        )
    """.trimIndent()

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}