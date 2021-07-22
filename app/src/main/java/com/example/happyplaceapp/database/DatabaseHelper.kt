package com.example.happyplaceapp.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.happyplaceapp.models.HappyPlaceModel

class DatabaseHelper(context: Context) :SQLiteOpenHelper(context, DATABASE_NAME,null,
    DATABASE_VERSION) {


    companion object{
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "happyPlaceDB"
        private const val TABLE_HAPPY_PLACES = "happyPlacesData"

        private const val KEY_ID = "id"
        private const val KEY_TITLE = "title"
        private const val KEY_IMAGE = "image"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_DATE = "date"
        private const val KEY_LOCATION = "location"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createHappyPlaceTable = ("CREATE TABLE " + TABLE_HAPPY_PLACES + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_IMAGE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_LOCATION + " TEXT,"
                + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT)"
                )

        db?.execSQL(createHappyPlaceTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_HAPPY_PLACES")
        onCreate(db)
    }

    fun addHappyPlaces(happyPlaceModel: HappyPlaceModel) :Long{
        val db = writableDatabase
        val contentValue = ContentValues()

        contentValue.put(KEY_TITLE,happyPlaceModel.title)
        contentValue.put(KEY_IMAGE,happyPlaceModel.image)
        contentValue.put(KEY_DESCRIPTION,happyPlaceModel.description)
        contentValue.put(KEY_DATE,happyPlaceModel.date)
        contentValue.put(KEY_LOCATION,happyPlaceModel.location)
        contentValue.put(KEY_LATITUDE,happyPlaceModel.latitude)
        contentValue.put(KEY_LONGITUDE,happyPlaceModel.longitude)

        val result = db.insert(TABLE_HAPPY_PLACES,null,contentValue)
        db.close()
        return result
    }


    fun getHappyPlaces() :ArrayList<HappyPlaceModel>{
        val dataList = ArrayList<HappyPlaceModel>()
        val db = readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_HAPPY_PLACES"

        try {
            val cursor:Cursor = db.rawQuery(selectQuery,null)

            if (cursor.moveToFirst()){
                do {
                    dataList.add(
                        HappyPlaceModel(
                            cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                            cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                            cursor.getString(cursor.getColumnIndex(KEY_IMAGE)),
                            cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                            cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
                            cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                            cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))
                        )
                    )
                }while (cursor.moveToNext())
            }
            return dataList
        }catch (e:SQLiteException){
            e.printStackTrace()
            return ArrayList()
        }

    }
    fun updateHappyPlaces(happyPlaceModel: HappyPlaceModel) :Int{
        val db = writableDatabase
        val contentValue = ContentValues()

        contentValue.put(KEY_TITLE,happyPlaceModel.title)
        contentValue.put(KEY_IMAGE,happyPlaceModel.image)
        contentValue.put(KEY_DESCRIPTION,happyPlaceModel.description)
        contentValue.put(KEY_DATE,happyPlaceModel.date)
        contentValue.put(KEY_LOCATION,happyPlaceModel.location)
        contentValue.put(KEY_LATITUDE,happyPlaceModel.latitude)
        contentValue.put(KEY_LONGITUDE,happyPlaceModel.longitude)

        val result = db.update(TABLE_HAPPY_PLACES,contentValue, KEY_ID + "="+happyPlaceModel.id,null)
        db.close()
        return result
    }

    fun deleteHappyPlaces(model :HappyPlaceModel) :Int{
        val db = writableDatabase
        val result = db.delete(TABLE_HAPPY_PLACES, KEY_ID + "="+model.id,null)
        db.close()
        return result
    }
}