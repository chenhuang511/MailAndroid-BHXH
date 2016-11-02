package vn.bhxh.bhxhmail.mail;

import android.content.Context;

import java.util.List;

import vn.bhxh.bhxhmail.mail.power.TracingPowerManager;

public interface PushReceiver {
    Context getContext();
    void syncFolder(Folder folder);
    void messagesArrived(Folder folder, List<Message> mess);
    void messagesFlagsChanged(Folder folder, List<Message> mess);
    void messagesRemoved(Folder folder, List<Message> mess);
    String getPushState(String folderName);
    void pushError(String errorMessage, Exception e);
    void authenticationFailed();
    void setPushActive(String folderName, boolean enabled);
    void sleep(TracingPowerManager.TracingWakeLock wakeLock, long millis);
}
