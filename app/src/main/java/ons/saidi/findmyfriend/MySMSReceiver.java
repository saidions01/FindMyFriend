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

import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MySMSReceiver extends BroadcastReceiver {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String messageBody,phoneNumber;
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
        {
            Bundle bundle =intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                if (messages.length > -1) {
                    messageBody = messages[0].getMessageBody();
                    phoneNumber = messages[0].getDisplayOriginatingAddress();

                    if(messageBody.contains("FindMyFriends: Envoyer moi votre position")){
                        Intent i =new Intent(context, MyGpsLocationService.class);
                        i.putExtra("sender", phoneNumber);
                        context.startService(i);
                    }

                    if(messageBody.startsWith("FindMyFriends: ma position est")){
                        String []t=messageBody.split("#");
                        String longitude=t[1];
                        String latitude=t[2];

                        //notification


                        NotificationCompat.Builder mynotif = new NotificationCompat.Builder(context, "FindMyFriends_ChannelID");
                        mynotif.setContentTitle("Position recue");
                        mynotif.setContentText("Appuyer pour la position sur map");
                        mynotif.setSmallIcon(android.R.drawable.ic_dialog_map);
                        mynotif.setAutoCancel(true);

                        Intent i=new Intent(context, MapsActivity.class);
                        i.putExtra("longitude", longitude);
                        i.putExtra("latitude", latitude);
                        PendingIntent pi = PendingIntent.getActivity(
                                context,0,i,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                        mynotif.setContentIntent(pi);

                        //lancer la notif
                        NotificationManagerCompat managerCompat=NotificationManagerCompat.from(context);
                        NotificationChannel canal = new NotificationChannel("FindMyFriends_ChannelID",
                                "canal pr notre app findfriends",
                                NotificationManager.IMPORTANCE_DEFAULT);
                        managerCompat.createNotificationChannel(canal);

                        managerCompat.notify(0, mynotif.build());
                    }
                }
            }
        }

    }
}