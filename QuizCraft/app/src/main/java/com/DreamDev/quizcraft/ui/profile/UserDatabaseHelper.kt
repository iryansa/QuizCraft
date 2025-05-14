package com.DreamDev.quizcraft.ui.profile

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.database.Cursor

class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "UserDB", null, 1) {

    //    override fun onCreate(db: SQLiteDatabase) {
//        db.execSQL("CREATE TABLE UserProfile(name TEXT, email TEXT, xp INTEGER, streak INTEGER)")
//    }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE UserProfile(name TEXT, email TEXT, xp INTEGER, streak INTEGER, pendingSync INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS UserProfile")
        onCreate(db)
    }

    fun insertOrUpdateUser(
        name: String,
        email: String,
        xp: Int,
        streak: Int,
        pendingSync: Boolean = false
    ) {
        val db = this.writableDatabase
        db.delete("UserProfile", null, null)
        val values = ContentValues().apply {
            put("name", name)
            put("email", email)
            put("xp", xp)
            put("streak", streak)
            put("pendingSync", if (pendingSync) 1 else 0)
        }
        db.insert("UserProfile", null, values)
    }

    fun getUser(): UserProfile? {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM UserProfile LIMIT 1", null)
        return if (cursor.moveToFirst()) {
            val name = cursor.getString(0)
            val email = cursor.getString(1)
            val xp = cursor.getInt(2)
            val streak = cursor.getInt(3)
            val pendingSync = cursor.getInt(4) == 1
            cursor.close()
            UserProfile(name, email, xp, streak, pendingSync)
        } else {
            cursor.close()
            null
        }
    }

    fun clearPendingSync() {
        val db = writableDatabase
        db.execSQL("UPDATE UserProfile SET pendingSync = 0")
    }

    data class UserProfile(
        val name: String,
        val email: String,
        val xp: Int,
        val streak: Int,
        val pendingSync: Boolean
    )

}