package com.smd.recipehub.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_CATEGORIES (
                $COL_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CATEGORY_NAME TEXT NOT NULL UNIQUE,
                $COL_CATEGORY_CREATED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_SAVED_MEALS (
                $COL_SAVED_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SAVED_CATEGORY_ID INTEGER NOT NULL,
                $COL_SAVED_API_ID TEXT,
                $COL_SAVED_TITLE TEXT NOT NULL,
                $COL_SAVED_AREA TEXT,
                $COL_SAVED_INSTRUCTIONS TEXT,
                $COL_SAVED_IMAGE_URL TEXT,
                $COL_SAVED_NOTES TEXT,
                $COL_SAVED_RATING INTEGER DEFAULT 0,
                $COL_SAVED_CREATED_AT INTEGER NOT NULL,
                $COL_SAVED_UPDATED_AT INTEGER NOT NULL,
                FOREIGN KEY($COL_SAVED_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COL_CATEGORY_ID) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX idx_saved_meals_title ON $TABLE_SAVED_MEALS($COL_SAVED_TITLE)")
        db.execSQL("CREATE INDEX idx_saved_meals_category ON $TABLE_SAVED_MEALS($COL_SAVED_CATEGORY_ID)")

        val now = System.currentTimeMillis()
        db.execSQL("INSERT INTO $TABLE_CATEGORIES ($COL_CATEGORY_NAME, $COL_CATEGORY_CREATED_AT) VALUES ('Favorites', $now)")
        db.execSQL("INSERT INTO $TABLE_CATEGORIES ($COL_CATEGORY_NAME, $COL_CATEGORY_CREATED_AT) VALUES ('Breakfast', $now)")
        db.execSQL("INSERT INTO $TABLE_CATEGORIES ($COL_CATEGORY_NAME, $COL_CATEGORY_CREATED_AT) VALUES ('Dinner', $now)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SAVED_MEALS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "recipehub.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_CATEGORIES = "meal_categories"
        const val COL_CATEGORY_ID = "id"
        const val COL_CATEGORY_NAME = "name"
        const val COL_CATEGORY_CREATED_AT = "created_at"

        const val TABLE_SAVED_MEALS = "saved_meals"
        const val COL_SAVED_ID = "id"
        const val COL_SAVED_CATEGORY_ID = "category_id"
        const val COL_SAVED_API_ID = "meal_api_id"
        const val COL_SAVED_TITLE = "title"
        const val COL_SAVED_AREA = "area"
        const val COL_SAVED_INSTRUCTIONS = "instructions"
        const val COL_SAVED_IMAGE_URL = "image_url"
        const val COL_SAVED_NOTES = "notes"
        const val COL_SAVED_RATING = "rating"
        const val COL_SAVED_CREATED_AT = "created_at"
        const val COL_SAVED_UPDATED_AT = "updated_at"
    }
}
