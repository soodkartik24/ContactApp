package com.kartik.contactapplicationkotlin.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kartik.contactapplicationkotlin.utils.NotificationUtility

class UpdateContactReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            NotificationUtility.displayNotification(
                it,
                1001,
                "Contact Updated Successfully",
                "Update Contact",
                Intent()
            )
        }
    }
}