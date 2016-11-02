package vn.bhxh.bhxhmail.notification;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import vn.bhxh.bhxhmail.Account;
import vn.bhxh.bhxhmail.activity.setup.AccountSetupIncoming;
import vn.bhxh.bhxhmail.activity.setup.AccountSetupOutgoing;


class CertificateErrorNotifications {
    private final NotificationController controller;


    public CertificateErrorNotifications(NotificationController controller) {
        this.controller = controller;
    }

    public void showCertificateErrorNotification(Account account, boolean incoming) {
        int notificationId = NotificationIds.getCertificateErrorNotificationId(account, incoming);
        Context context = controller.getContext();

        PendingIntent editServerSettingsPendingIntent = createContentIntent(context, account, incoming);
        String title = context.getString(vn.bhxh.bhxhmail.R.string.notification_certificate_error_title, account.getDescription());
        String text = context.getString(vn.bhxh.bhxhmail.R.string.notification_certificate_error_text);

        NotificationCompat.Builder builder = controller.createNotificationBuilder()
                .setSmallIcon(getCertificateErrorNotificationIcon())
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(editServerSettingsPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

//        controller.configureNotification(builder, null, null,
//                NOTIFICATION_LED_FAILURE_COLOR,
//                NOTIFICATION_LED_BLINK_FAST, true);
//
//        getNotificationManager().notify(notificationId, builder.build());
    }

    public void clearCertificateErrorNotifications(Account account, boolean incoming) {
        int notificationId = NotificationIds.getCertificateErrorNotificationId(account, incoming);
        getNotificationManager().cancel(notificationId);
    }

    PendingIntent createContentIntent(Context context, Account account, boolean incoming) {
        Intent editServerSettingsIntent = incoming ?
                AccountSetupIncoming.intentActionEditIncomingSettings(context, account) :
                AccountSetupOutgoing.intentActionEditOutgoingSettings(context, account);

        return PendingIntent.getActivity(context, account.getAccountNumber(), editServerSettingsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private int getCertificateErrorNotificationIcon() {
        //TODO: Use a different icon for certificate error notifications
        return vn.bhxh.bhxhmail.R.drawable.notification_icon_new_mail;
    }

    private NotificationManagerCompat getNotificationManager() {
        return controller.getNotificationManager();
    }
}
