package vn.bhxh.bhxhmail.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import vn.bhxh.bhxhmail.K9;
import vn.bhxh.bhxhmail.mailstore.StorageManager;

/**
 * That BroadcastReceiver is only interested in UNMOUNT events.
 *
 * <p>
 * Code was separated from {@link StorageReceiver} because we don't want that
 * receiver to be statically defined in manifest.
 * </p>
 */
public class StorageGoneReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        final Uri uri = intent.getData();

        if (uri == null || uri.getPath() == null) {
            return;
        }

        if (K9.DEBUG) {
            Log.v(K9.LOG_TAG, "StorageGoneReceiver: " + intent.toString());
        }

        final String path = uri.getPath();

        if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
            StorageManager.getInstance(context).onBeforeUnmount(path);
        } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
            StorageManager.getInstance(context).onAfterUnmount(path);
        }
    }

}
