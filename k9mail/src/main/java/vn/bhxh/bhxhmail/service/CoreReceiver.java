
package vn.bhxh.bhxhmail.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import vn.bhxh.bhxhmail.K9;
import vn.bhxh.bhxhmail.mail.power.TracingPowerManager;

public class CoreReceiver extends BroadcastReceiver {

    public static final String WAKE_LOCK_RELEASE = "CoreReceiver.wakeLockRelease";

    public static final String WAKE_LOCK_ID = "CoreReceiver.wakeLockId";

    private static ConcurrentHashMap<Integer, TracingPowerManager.TracingWakeLock> wakeLocks = new ConcurrentHashMap<Integer, TracingPowerManager.TracingWakeLock>();
    private static AtomicInteger wakeLockSeq = new AtomicInteger(0);

    private static Integer getWakeLock(Context context) {
        TracingPowerManager pm = TracingPowerManager.getPowerManager(context);
        TracingPowerManager.TracingWakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CoreReceiver getWakeLock");
        wakeLock.setReferenceCounted(false);
        wakeLock.acquire(K9.BOOT_RECEIVER_WAKE_LOCK_TIMEOUT);
        Integer tmpWakeLockId = wakeLockSeq.getAndIncrement();
        wakeLocks.put(tmpWakeLockId, wakeLock);
        if (K9.DEBUG)
            Log.v(K9.LOG_TAG, "CoreReceiver Created wakeLock " + tmpWakeLockId);
        return tmpWakeLockId;
    }

    private static void releaseWakeLock(Integer wakeLockId) {
        if (wakeLockId != null) {
            TracingPowerManager.TracingWakeLock wl = wakeLocks.remove(wakeLockId);
            if (wl != null) {
                if (K9.DEBUG)
                    Log.v(K9.LOG_TAG, "CoreReceiver Releasing wakeLock " + wakeLockId);
                wl.release();
            } else {
                Log.w(K9.LOG_TAG, "BootReceiver WakeLock " + wakeLockId + " doesn't exist");
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Integer tmpWakeLockId = CoreReceiver.getWakeLock(context);
        try {
            if (K9.DEBUG)
                Log.i(K9.LOG_TAG, "CoreReceiver.onReceive" + intent);
            if (CoreReceiver.WAKE_LOCK_RELEASE.equals(intent.getAction())) {
                Integer wakeLockId = intent.getIntExtra(WAKE_LOCK_ID, -1);
                if (wakeLockId != -1) {
                    if (K9.DEBUG)
                        Log.v(K9.LOG_TAG, "CoreReceiver Release wakeLock " + wakeLockId);
                    CoreReceiver.releaseWakeLock(wakeLockId);
                }
            } else {
                tmpWakeLockId = receive(context, intent, tmpWakeLockId);
            }
        } finally {
            CoreReceiver.releaseWakeLock(tmpWakeLockId);
        }
    }

    public Integer receive(Context context, Intent intent, Integer wakeLockId) {
        return wakeLockId;
    }

    public static void releaseWakeLock(Context context, int wakeLockId) {
        if (K9.DEBUG)
            Log.v(K9.LOG_TAG, "CoreReceiver Got request to release wakeLock " + wakeLockId);
        Intent i = new Intent();
        i.setClass(context, CoreReceiver.class);
        i.setAction(WAKE_LOCK_RELEASE);
        i.putExtra(WAKE_LOCK_ID, wakeLockId);
        context.sendBroadcast(i);
    }
}
