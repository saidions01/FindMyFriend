package ons.saidi.findmyfriend;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String messageBody = "";
        String sender = "";
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus == null) {
                return; // No PDU data
            }

            SmsMessage[] messages = new SmsMessage[pdus.length];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                messageBody += messages[i].getMessageBody();
            }
            sender = messages[0].getOriginatingAddress();

            // Check if the message is the one we are looking for
            String prefix = "FindMyFriends: ma position est#";
            if (messageBody.startsWith(prefix)) {
                // It's our location SMS, now let's show a notification
                Toast.makeText(context, "Location received from " + sender, Toast.LENGTH_LONG).show();

                // We will create this helper class in the next step
                NotificationHelper.showLocationNotification(context, messageBody);
            }
        }
    }
}