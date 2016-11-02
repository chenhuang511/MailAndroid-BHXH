package vn.bhxh.bhxhmail.notification;


import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;

import vn.bhxh.bhxhmail.Account;
import vn.bhxh.bhxhmail.K9;


abstract class BaseNotifications {
    protected static final String NOTIFICATION_GROUP_KEY = "newMailNotifications";


    protected final Context context;
    protected final NotificationController controller;
    protected final NotificationActionCreator actionCreator;


    protected BaseNotifications(NotificationController controller, NotificationActionCreator actionCreator) {
        this.context = controller.getContext();
        this.controller = controller;
        this.actionCreator = actionCreator;
    }

    protected NotificationCompat.Builder createBigTextStyleNotification(Account account, NotificationHolder holder,
            int notificationId) {
        String accountName = controller.getAccountName(account);
        NotificationContent content = holder.content;

        NotificationCompat.Builder builder = createAndInitializeNotificationBuilder(account)
                .setTicker(content.summary)
                .setGroup(NOTIFICATION_GROUP_KEY)
                .setContentTitle(content.sender)
                .setContentText(content.subject)
                .setSubText(accountName);

        NotificationCompat.BigTextStyle style = createBigTextStyle(builder);
        style.bigText(content.preview);

        builder.setStyle(style);

        PendingIntent contentIntent = actionCreator.createViewMessagePendingIntent(
                content.messageReference, notificationId);
        builder.setContentIntent(contentIntent);

        return builder;
    }

    protected NotificationCompat.Builder createAndInitializeNotificationBuilder(Account account) {
        return controller.createNotificationBuilder()
                .setSmallIcon(getNewMailNotificationIcon())
                .setColor(account.getChipColor())
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);
    }

    protected boolean isDeleteActionEnabled() {
        K9.NotificationQuickDelete deleteOption = K9.getNotificationQuickDeleteBehaviour();
        return deleteOption == K9.NotificationQuickDelete.ALWAYS || deleteOption == K9.NotificationQuickDelete.FOR_SINGLE_MSG;
    }

    protected BigTextStyle createBigTextStyle(Builder builder) {
        return new BigTextStyle(builder);
    }

    private int getNewMailNotificationIcon() {
        return vn.bhxh.bhxhmail.R.drawable.notification_icon_new_mail;
    }
}
