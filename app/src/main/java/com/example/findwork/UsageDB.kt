package com.example.findwork

import android.content.ContentValues
import android.content.Context

class UsageDB(context: Context){
    private val dbHelper = DBHelper(context)

    fun insertDB(token:String, role:Int): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DB.COLUMN_NAME_TOKEN, token)
            put(DB.COLUMN_NAME_ROLE, role)
        }

        val newRowId = db.insert(DB.TABLE_NAME, null, values)
        db.close()
        return newRowId
    }

    fun readDB(Columns:Array<String>?, Selection:String?, SelectionArgs: Array<String>?, SortColumn:String, Sort: Int): MutableList<MutableList<String>> {
        val db = dbHelper.readableDatabase

        // Filter results WHERE "title" = 'My Title'
        val selection = if(Selection!=null) "$Selection = ?" else null

        // How you want the results sorted in the resulting Cursor
        val sort:Array<String> = arrayOf("DESC","ASC")
        val sortOrder =  "$SortColumn ${sort[Sort]}"
        //  val sortOrder = "${DB.COLUMN_NAME_SUBTITLE} DESC"

        val cursor = db.query(
            DB.TABLE_NAME ,   // The table to query
            Columns ,             // The array of columns to return (pass null to get all)
            selection ,              // The columns for the WHERE clause
            SelectionArgs ,  // The values for the WHERE clause
            null ,                   // don't group the rows
            null ,                   // don't filter by row groups
            sortOrder               // The sort order
        )

        val itemIds:MutableList<MutableList<String>> = mutableListOf()
        var itemId:MutableList<String> = mutableListOf()
        with(cursor) {
            while (moveToNext()) {
                for(i in 0 until columnCount) {
//                    val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID)).toString()

                    itemId.add(getString(i))
                }
                itemIds.add(itemId)
                itemId= mutableListOf()
            }
        }
        cursor.close()
        db.close()
        return itemIds
    }

    fun deleteDB(Selection:String?,SelectionArgs:Array<String>?): Int {
        val db = dbHelper.writableDatabase
        val selection = if(Selection!=null)"$Selection LIKE ?" else null

        val deletedRows = db.delete(DB.TABLE_NAME, selection, SelectionArgs)
        db.close()
        return deletedRows
    }

    fun updateDB(Column: String?, ColumnArgs: String?, Selection:String?, SelectionArgs:Array<String>){
        val db = dbHelper.writableDatabase

// New value for one column
//        val title = "MyNewTitle"
        val values = if(Column!=null) ContentValues().apply {
            put(Column, ColumnArgs)
        } else null

// Which row to update, based on the title
        val selection = "$Selection LIKE ?"
        val count = db.update(
            DB.TABLE_NAME,
            values,
            selection,
            SelectionArgs)
        db.close()
    }

}