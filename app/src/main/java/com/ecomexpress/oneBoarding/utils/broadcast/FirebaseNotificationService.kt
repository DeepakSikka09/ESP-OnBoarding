package com.ecomexpress.oneBoarding.utils.broadcast

/*
* Dependencies: Ensure Firebase messaging is included.
* Service Class: MyFirebaseMessagingService class that extends FirebaseMessagingService.
* onNewToken: This method is called whenever a new token is generated. It logs the new token and optionally
               sends it to your server using sendRegistrationToServer.
* onMessageReceived: This method is called when a message is received. It handles both data payload and
               notification payload.
* Manifest Declaration: The service is declared inside the <application> tag with
               an <intent-filter> for com.google.firebase.MESSAGING_EVENT to handle Firebase messaging events.

//note : The ConstraintLayout is not supported in RemoteViews due to its complexity and the way RemoteViews need
// to be inflated in a separate process (such as in the notification bar or on the home screen). RemoteViews are
// designed to be lightweight, and therefore, only support a limited set of layout types: FrameLayout, LinearLayout,
// RelativeLayout, and GridLayout.
*/


import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.ui.onBoard.activity.FinalStatusActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

//channelId: A unique string ID for the channel. This is used to reference the channel programmatically.
//channelName: A user-visible name for the channel. This name will be shown in the system settings under notification settings.

const val channelId = "notifiaction_channel"
const val channelName = "com.ecomexpress.oneBoarding"

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FirebaseNotificationService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    @SuppressLint("UnspecifiedImmutableFlag", "ResourceAsColor")
    fun generateNotification(title: String, message: String) {
        //to which activity i need to go
        val intent = Intent(this, FinalStatusActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        // FLAG_ONE_SHOT : used only once means notifiaction click hogi to notification get destroyed
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)

        val color = Color.argb(0, 253, 61, 181)
        //channel id, channel name
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(title)
                .setContentText(message)
                .setColor(color)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000)) //vibrate for 1s rest 1s
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(0, builder.build())
    }

    override fun onMessageReceived(remotemessage: RemoteMessage) {
        if (remotemessage.notification != null) {
            remotemessage.notification?.title?.let {
                remotemessage.notification?.body?.let { it1 ->
                    generateNotification(
                        it,
                        it1
                    )
                }
            }

        }
    }
}