package com.example.chatappreal.Notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessaging:FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)


        val sented  = message.data["sented"]
        var user = message.data["users"]
        var sharedPref = getSharedPreferences("PREFS",Context.MODE_PRIVATE)
        var currentOnLineUser = sharedPref.getString("currentUser","none")
        val firebaseUer = FirebaseAuth.getInstance().currentUser
        if(firebaseUer!!.uid!= null && sented == firebaseUer!!.uid){
            if(currentOnLineUser != user){
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){

                    sendOreoNotification(message)
                }else{
                    sendNotification(message)

                }
            }
        }
    }

    private fun sendNotification(message: RemoteMessage) {
        var user = message.data["users"]
        var icon = message.data["icon"]
        var title = message.data["title"]
        var body = message.data["body"]

        val notification = message.notification
        val j = user!!.replace("[\\D]".toRegex(),"").toInt()
        val intent = Intent(this, MessageChatActivity::class.java)

        val bundel = Bundle()
        bundel.putString("userId",user)
        intent.putExtras(bundel)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this,j,PendingIntent.FLAG_ONE_SHOT)
        var defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder : NotificationCompat.Builder = NotificationCompat.Builder(this)
            .setSmallIcon(icon!!.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(defaultSound)

        var noti = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager



        var i = 0
        if(j>0){
            i = j
        }
        oreoNotification.getManager!!.notify(i,builder.build())
    }

    private fun sendOreoNotification(message: RemoteMessage) {
        var user = message.data["users"]
        var icon = message.data["icon"]
        var title = message.data["title"]
        var body = message.data["body"]

        val notification = message.notification
        val j = user!!.replace("[\\D]".toRegex(),"").toInt()
        val intent = Intent(this, MessageChatActivity::class.java)

        val bundel = Bundle()
        bundel.putString("userId",user)
        intent.putExtras(bundel)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this,j,PendingIntent.FLAG_ONE_SHOT)
        var defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val oreoNotification = OreoNotification(this)
        val builder:Notification.Builder = oreoNotification.getOreoNotification(title,body,pendingIntent,defaultSound,icon)
        var i = 0
        if(j>0){
            i = j
        }
        oreoNotification.getManager!!.notify(i,builder.build())

    }

}