package vn.bhxh.bhxhmail.mailstore.migrations;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;

import vn.bhxh.bhxhmail.Account;
import vn.bhxh.bhxhmail.K9;
import vn.bhxh.bhxhmail.mail.Message;
import vn.bhxh.bhxhmail.mailstore.LocalFolder;
import vn.bhxh.bhxhmail.mailstore.LocalStore;


class MigrationTo43 {
    public static void fixOutboxFolders(SQLiteDatabase db, MigrationsHelper migrationsHelper) {
        try {
            LocalStore localStore = migrationsHelper.getLocalStore();
            Account account = migrationsHelper.getAccount();
            Context context = migrationsHelper.getContext();

            // If folder "OUTBOX" (old, v3.800 - v3.802) exists, rename it to
            // "K9MAIL_INTERNAL_OUTBOX" (new)
            LocalFolder oldOutbox = new LocalFolder(localStore, "OUTBOX");
            if (oldOutbox.exists()) {
                ContentValues cv = new ContentValues();
                cv.put("name", Account.OUTBOX);
                db.update("folders", cv, "name = ?", new String[] { "OUTBOX" });
                Log.i(K9.LOG_TAG, "Renamed folder OUTBOX to " + Account.OUTBOX);
            }

            // Check if old (pre v3.800) localized outbox folder exists
            String localizedOutbox = context.getString(vn.bhxh.bhxhmail.R.string.special_mailbox_name_outbox);
            LocalFolder obsoleteOutbox = new LocalFolder(localStore, localizedOutbox);
            if (obsoleteOutbox.exists()) {
                // Get all messages from the localized outbox ...
                List<? extends Message> messages = obsoleteOutbox.getMessages(null, false);

                if (messages.size() > 0) {
                    // ... and move them to the drafts folder (we don't want to
                    // surprise the user by sending potentially very old messages)
                    LocalFolder drafts = new LocalFolder(localStore, account.getDraftsFolderName());
                    obsoleteOutbox.moveMessages(messages, drafts);
                }

                // Now get rid of the localized outbox
                obsoleteOutbox.delete();
                obsoleteOutbox.delete(true);
            }
        } catch (Exception e) {
            Log.e(K9.LOG_TAG, "Error trying to fix the outbox folders", e);
        }
    }
}
