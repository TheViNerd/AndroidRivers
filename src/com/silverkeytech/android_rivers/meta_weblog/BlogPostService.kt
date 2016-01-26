/*
Android Rivers is an app to read and discover news using RiverJs, RSS and OPML format.
Copyright (C) 2012 Dody Gunawinata (dodyg@silverkeytech.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package com.silverkeytech.android_rivers.meta_weblog

import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.res.Resources
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.RemoteViews
import com.silverkeytech.android_rivers.Params
import com.silverkeytech.android_rivers.R
import com.silverkeytech.android_rivers.activities.MainWithFragmentsActivity
import com.silverkeytech.android_rivers.isModernAndroid
import com.silverkeytech.android_rivers.with
import java.util.*
import kotlin.properties.Delegates

class BlogPostService(): IntentService("DownloadService"){
    companion object{
        val TAG: String = BlogPostService::class.java.simpleName
    }

    var config: HashMap<String, String> by Delegates.notNull()
    var post: HashMap<String, String> by Delegates.notNull()

    fun prepareNotification(title: String): Notification {
        val notificationIntent = Intent(Intent.ACTION_MAIN)
        notificationIntent.setClass(applicationContext!!, MainWithFragmentsActivity::class.java)
        //notificationIntent.putExtra(Params.DOWNLOAD_LOCATION_PATH, filePath)

        val contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val notification = NotificationCompat.Builder(this)
                .setTicker("Posting $title")
        ?.setWhen(System.currentTimeMillis())
        ?.setContentIntent(contentIntent)
        ?.build()

        notification!!.icon = android.R.drawable.stat_sys_download

        val remote = RemoteViews(applicationContext!!.packageName, R.layout.notification_download_progress).with {
            this.setImageViewResource(R.id.notification_download_progress_status_icon, android.R.drawable.stat_sys_download_done)
            this.setProgressBar(R.id.notification_download_progress_status_progress, 100, 0, false)
            this.setTextViewText(R.id.notification_download_progress_status_text, "Posting")
        }

        if (isModernAndroid()){
            //workaround on grey background on Android 4.03   https://code.google.com/p/android/issues/detail?id=23863&thanks=23863&ts=1325611036
            val id = Resources.getSystem()!!.getIdentifier("status_bar_latest_event_content", "id", "android")
            notification.contentView?.removeAllViews(id)
            notification.contentView!!.addView(id, remote)
        } else
            notification.contentView = remote

        return notification
    }

    override fun onHandleIntent(p0: Intent?) {
        config = p0!!.getSerializableExtra(Params.BLOG_CONFIGURATION)!! as HashMap<String, String>
        post = p0.getSerializableExtra(Params.BLOG_PAYLOAD)!!  as HashMap<String, String>

        Log.d(TAG, " Server is ${config.get(Params.BLOG_SERVER)}")

        val server = config.get(Params.BLOG_SERVER)!!
        val username = config.get(Params.BLOG_USERNAME)!!
        val password = config.get(Params.BLOG_PASSWORD)!!

        val postContent = post.get(Params.POST_CONTENT)!!
        val postLink = post.get(Params.POST_LINK)


        val blg = Blog(null, server, username, password)

        if (postLink.isNullOrEmpty()){
            val content = postContent

            val pst = statusPost(content)
            blg.newPost(pst)

        }
        else {
            val pst = linkPost(postContent, postLink!!)
            blg.newPost(pst)
        }


        //val notification = prepareNotification("Posting blog")

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "OnStartCommand")

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        Log.d(TAG, "Service created")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service created")
    }
}