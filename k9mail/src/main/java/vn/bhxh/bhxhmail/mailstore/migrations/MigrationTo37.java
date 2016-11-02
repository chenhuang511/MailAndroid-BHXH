package vn.bhxh.bhxhmail.mailstore.migrations;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import vn.bhxh.bhxhmail.K9;


class MigrationTo37 {
    public static void addAttachmentsContentDispositionColumn(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE attachments ADD content_disposition TEXT");
        } catch (SQLiteException e) {
            Log.e(K9.LOG_TAG, "Unable to add content_disposition column to attachments");
        }
    }
}
