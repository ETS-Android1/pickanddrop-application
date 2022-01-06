package uk.ac.tees.aad.W9517102.Sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import uk.ac.tees.aad.W9517102.Common.History;


public class DbHistory extends SQLiteOpenHelper {

    static final int DB_VERSION = 1;
    static final String DB_NAME = "My_DB";
    static final String TABLE_NAME = "MY_TABLE";
    Context context;

    public DbHistory(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
        context = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                + "id_auto" + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "driveName" + " TEXT, "
                + "customerName" + " TEXT, "
                + "date" + " TEXT, "
                + "latitudeStart" + " DOUBLE, "
                + "longitudeStart" + " DOUBLE,"
                + "latitudeEnd" + " DOUBLE,"
                + "longitudeEnd" + " DOUBLE " + ")";

        db.execSQL(CREATE_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public List<History> getAlarmList() {
        SQLiteDatabase db = getWritableDatabase();
        List<History> listAlarm = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                String driveName = cursor.getString(1);
                String customerName = cursor.getString(2);
                String date = cursor.getString(3);
                Double latitudeStart = cursor.getDouble(4);
                Double longitudeStart = cursor.getDouble(5);
                Double latitudeEnd = cursor.getDouble(6);
                Double longitudeEnd = cursor.getDouble(7);

                listAlarm.add(new History(driveName, customerName, date, latitudeStart, longitudeStart, latitudeEnd, longitudeEnd));
            } while (cursor.moveToNext());
        }

        return listAlarm;
    }

    public void deleteHistory(String locationID) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "location_id" + " = ?",
                new String[]{String.valueOf(locationID)});
        db.close();
    }

    public void addHistory(History history) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("driveName", history.getDriveName());
        values.put("customerName", history.getCustomerName());
        values.put("date", history.getDate());
        values.put("latitudeStart", history.getLatitudeStart());
        values.put("longitudeStart", history.getLongitudeStart());
        values.put("latitudeEnd", history.getLatitudeEnd());
        values.put("longitudeEnd", history.getLongitudeEnd());
        long insert = db.insert(TABLE_NAME, null, values);
        db.close();
    }

}


