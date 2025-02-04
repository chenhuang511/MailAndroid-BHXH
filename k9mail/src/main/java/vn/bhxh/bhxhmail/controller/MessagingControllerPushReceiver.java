package vn.bhxh.bhxhmail.controller;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import vn.bhxh.bhxhmail.Account;
import vn.bhxh.bhxhmail.K9;
import vn.bhxh.bhxhmail.mail.Folder;
import vn.bhxh.bhxhmail.mail.Message;
import vn.bhxh.bhxhmail.mail.PushReceiver;
import vn.bhxh.bhxhmail.mail.power.TracingPowerManager;
import vn.bhxh.bhxhmail.mailstore.LocalFolder;
import vn.bhxh.bhxhmail.mailstore.LocalStore;
import vn.bhxh.bhxhmail.service.SleepService;

public class MessagingControllerPushReceiver implements PushReceiver {
    final Account account;
    final MessagingController controller;
    final Context context;

    public MessagingControllerPushReceiver(Context context, Account nAccount, MessagingController nController) {
        account = nAccount;
        controller = nController;
        this.context = context;
    }

    public void messagesFlagsChanged(Folder folder,
                                     List<Message> messages) {
        controller.messagesArrived(account, folder, messages, true);
    }
    public void messagesArrived(Folder folder, List<Message> messages) {
        controller.messagesArrived(account, folder, messages, false);
    }
    public void messagesRemoved(Folder folder, List<Message> messages) {
        controller.messagesArrived(account, folder, messages, true);
    }

    public void syncFolder(Folder folder) {
        if (K9.DEBUG)
            Log.v(K9.LOG_TAG, "syncFolder(" + folder.getName() + ")");
        final CountDownLatch latch = new CountDownLatch(1);
        controller.synchronizeMailbox(account, folder.getName(), new MessagingListener() {
            @Override
            public void synchronizeMailboxFinished(Account account, String folder,
            int totalMessagesInMailbox, int numNewMessages) {
                latch.countDown();
            }

            @Override
            public void synchronizeMailboxFailed(Account account, String folder,
            String message) {
                latch.countDown();
            }
        }, folder);

        if (K9.DEBUG)
            Log.v(K9.LOG_TAG, "syncFolder(" + folder.getName() + ") about to await latch release");
        try {
            latch.await();
            if (K9.DEBUG)
                Log.v(K9.LOG_TAG, "syncFolder(" + folder.getName() + ") got latch release");
        } catch (Exception e) {
            Log.e(K9.LOG_TAG, "Interrupted while awaiting latch release", e);
        }
    }

    @Override
    public void sleep(TracingPowerManager.TracingWakeLock wakeLock, long millis) {
        SleepService.sleep(context, millis, wakeLock, K9.PUSH_WAKE_LOCK_TIMEOUT);
    }

    public void pushError(String errorMessage, Exception e) {
        String errMess = errorMessage;

        controller.notifyUserIfCertificateProblem(account, e, true);
        if (errMess == null && e != null) {
            errMess = e.getMessage();
        }
        controller.addErrorMessage(account, errMess, e);
    }

    @Override
    public void authenticationFailed() {
        controller.handleAuthenticationFailure(account, true);
    }

    public String getPushState(String folderName) {
        LocalFolder localFolder = null;
        try {
            LocalStore localStore = account.getLocalStore();
            localFolder = localStore.getFolder(folderName);
            localFolder.open(Folder.OPEN_MODE_RW);
            return localFolder.getPushState();
        } catch (Exception e) {
            Log.e(K9.LOG_TAG, "Unable to get push state from account " + account.getDescription()
                  + ", folder " + folderName, e);
            return null;
        } finally {
            if (localFolder != null) {
                localFolder.close();
            }
        }
    }

    public void setPushActive(String folderName, boolean enabled) {
        for (MessagingListener l : controller.getListeners()) {
            l.setPushActive(account, folderName, enabled);
        }
    }

    @Override
    public Context getContext() {
        return context;
    }

}
