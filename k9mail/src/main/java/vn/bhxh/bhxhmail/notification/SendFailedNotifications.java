package vn.bhxh.bhxhmail.notification;


import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import vn.bhxh.bhxhmail.Account;
import vn.bhxh.bhxhmail.helper.ExceptionHelper;


class SendFailedNotifications {
    private final NotificationController controller;
    private final NotificationActionCreator actionBuilder;


    public SendFailedNotifications(NotificationController controller, NotificationActionCreator actionBuilder) {
        this.controller = controller;
        this.actionBuilder = actionBuilder;
    }

    public void showSendFailedNotification(Account account, Exception exception) {
        Context context = controller.getContext();
        String title = context.getString(vn.bhxh.bhxhmail.R.string.send_failure_subject);
        String text = ExceptionHelper.getRootCauseMessage(exception);

        int notificationId = NotificationIds.getSendFailedNotificationId(account);
        PendingIntent folderListPendingIntent = actionBuilder.createViewFolderListPendingIntent(
                account, notificationId);

        NotificationCompat.Builder builder = controller.createNotificationBuilder()
                .setSmallIcon(getSendFailedNotificationIcon())
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(folderListPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        controller.configureNotification(builder, null, null, NotificationController.NOTIFICATION_LED_FAILURE_COLOR,
                NotificationController.NOTIFICATION_LED_BLINK_FAST, true);

        getNotificationManager().notify(notificationId, builder.build());
    }

    public void clearSendFailedNotification(Account account) {
        int notificationId = NotificationIds.getSendFailedNotificationId(account);
        getNotificationManager().cancel(notificationId);
    }

    private int getSendFailedNotificationIcon() {
        //TODO: Use a different icon for send failure notifications
        return vn.bhxh.bhxhmail.R.drawable.notification_icon_new_mail;
    }

    private NotificationManagerCompat getNotificationManager() {
        return controller.getNotificationManager();
    }
}
