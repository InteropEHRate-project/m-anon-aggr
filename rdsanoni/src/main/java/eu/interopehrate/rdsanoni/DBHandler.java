package eu.interopehrate.rdsanoni;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "rds.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE = "pseudo";
    public static final String COLUMN_ID = "study_id";
    public static final String COLUMN_PSEUDO = "pseudo";
    public static final String COLUMN_TYPE = "pseudo_type";


    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE + "(" + COLUMN_ID + " TEXT PRIMARY KEY, " + COLUMN_PSEUDO + " TEXT, " + COLUMN_TYPE + " TEXT" + ")";
        //System.out.println("Create Table Query:" + CREATE_TABLE);
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE;
        //System.out.println("Drop Table Query:" + DROP_TABLE);
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    //All three variables should have been defined (not null values or empty strings).
    public void insertPseudo(String pseudoType, String pseudo, String studyID){
        if(!primaryKeyExists(studyID)){
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, studyID);
            values.put(COLUMN_PSEUDO, pseudo);
            values.put(COLUMN_TYPE, pseudoType);

            SQLiteDatabase db = this.getWritableDatabase();
            db.insert(TABLE, null, values);

            //System.out.println("One row added successfully.");
            db.close();
        }
    }

    //Variable 'studyID' should have been defined (not null value or empty string).
    public String retrievePseudo(String studyID){
        String SELECT_QUERY = "SELECT * FROM " + TABLE + " WHERE " + COLUMN_ID + " = \'" + studyID + "\' ";
        //System.out.println("Select Query:" + SELECT_QUERY);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(SELECT_QUERY, null);

        String pseudo;

        if(cursor.moveToFirst()) {
            cursor.moveToFirst();
            System.out.println("Study ID: " + cursor.getString(0));
            System.out.println("Pseudo: " + cursor.getString(1));
            System.out.println("Type of Pseudo: " + cursor.getString(2));
            pseudo = cursor.getString(1);
            cursor.close();
        } else {
            pseudo = "";
            //System.out.println("Study ID was not found.");
        }

        db.close();

        return pseudo;

    }

    private boolean primaryKeyExists(String studyID) {
        boolean flag;
        String SELECT_QUERY = "SELECT * FROM " + TABLE + " WHERE " + COLUMN_ID + " = \'" + studyID + "\' ";
        //System.out.println("Select Query:" + SELECT_QUERY);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(SELECT_QUERY, null);
        if (cursor.moveToFirst()) {
            //System.out.println("Primary key (studyID) already exists.");
            flag = true;
        } else {
            //System.out.println("Primary key (studyID) does not exist.");
            flag = false;
        }
        cursor.close();
        return flag;
    }
}
