package com.example.findwork

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

object DB: BaseColumns {

    const val COLUMN_NAME_ID = "id"
    const val TABLE_NAME = "Users"
    const val COLUMN_NAME_TOKEN = "token"
    const val COLUMN_NAME_ROLE = "role"


    const val DATABASE_VERSION = 1
    const val DATABASE_NAME = "USER"

    const val SQL_CREATE_ENTRIES =
        "CREATE TABLE ${DB.TABLE_NAME} (" +
                "${DB.COLUMN_NAME_ID} INTEGER PRIMARY KEY," +
                "${DB.COLUMN_NAME_TOKEN} TEXT," +
                "${DB.COLUMN_NAME_ROLE} INTEGER)"
    //role==1 employee / role==0 employer

    const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${DB.TABLE_NAME}"
}

class DBHelper(context: Context) : SQLiteOpenHelper(context, DB.DATABASE_NAME, null, DB.DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DB.SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase , oldVersionn: Int , newVersion: Int) {
        db.execSQL(DB.SQL_DELETE_ENTRIES)
        onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase , oldVersion: Int , newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

}
