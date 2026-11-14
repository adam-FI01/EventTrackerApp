package com.example.adamfineeventtrackerapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "event_tracker.db", null, 3) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE users (username TEXT PRIMARY KEY, password TEXT)")
        db.execSQL("CREATE TABLE events (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, date TEXT, time TEXT, location TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS events")
        onCreate(db)
    }

    fun addUser(user: String, pass: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("username", user)
        values.put("password", pass)
        val result = db.insert("users", null, values)
        return result != -1L
    }

    fun checkUser(user: String, pass: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE username = ? AND password = ?", arrayOf(user, pass))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun userExists(user: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE username = ?", arrayOf(user))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun addEvent(name: String, date: String, time: String, loc: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("name", name)
        values.put("date", date)
        values.put("time", time)
        values.put("location", loc)
        return db.insert("events", null, values)
    }

    fun getAllEvents(): List<Event> {
        val list = mutableListOf<Event>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM events", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Event(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun deleteEvent(id: Int) {
        val db = this.writableDatabase
        db.delete("events", "id = ?", arrayOf(id.toString()))
    }

    fun updateEvent(id: Int, name: String, date: String, time: String, loc: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("name", name)
        values.put("date", date)
        values.put("time", time)
        values.put("location", loc)
        db.update("events", values, "id = ?", arrayOf(id.toString()))
    }
}
