package com.sriharilabs.harisharan.quiz.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class SqlLiteDbHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "questionManager.sqlite";
    private static final String DB_PATH_SUFFIX = "/databases/";
    private static Context ctx;
    // Contacts table name
    private static final String TABLE_QUESTION = "question";

    public SqlLiteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        ctx = context;
    }

    /**
     * Get all question
     */
    public ArrayList<QuestionBean> getAllQuestion() {
        ArrayList<QuestionBean> questionList = new ArrayList<QuestionBean>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_QUESTION;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                QuestionBean questionBean = new QuestionBean();
                questionBean.setId(Integer.parseInt(cursor.getString(0)));
                questionBean.setAnswer(cursor.getString(1));
                questionBean.setImage(cursor.getString(2));
                questionBean.setType(cursor.getString(3));
                // Adding contact to list
                questionList.add(questionBean);
            } while (cursor.moveToNext());
        }

        // return contact list
        return questionList;
    }

    public void copyDataBaseFromAsset() throws IOException {
        InputStream myInput = ctx.getAssets().open(DATABASE_NAME);
        // Path to the just created empty db
        String outFileName = getDatabasePath();
        // if the path doesn't exist first, create it
        File f = new File(ctx.getApplicationInfo().dataDir + DB_PATH_SUFFIX);
        if (!f.exists()) {
            f.mkdir();
        }
        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);
        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    private static String getDatabasePath() {
        return ctx.getApplicationInfo().dataDir + DB_PATH_SUFFIX
                + DATABASE_NAME;
    }

    public SQLiteDatabase openDataBase() throws SQLException {
        File dbFile = ctx.getDatabasePath(DATABASE_NAME);

        if (!dbFile.exists()) {
            try {
                copyDataBaseFromAsset();
            } catch (IOException e) {
                throw new RuntimeException("Error creating source database", e);
            }
        }

        return SQLiteDatabase.openDatabase(dbFile.getPath(),
                null, SQLiteDatabase.NO_LOCALIZED_COLLATORS |
                SQLiteDatabase.CREATE_IF_NECESSARY);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}