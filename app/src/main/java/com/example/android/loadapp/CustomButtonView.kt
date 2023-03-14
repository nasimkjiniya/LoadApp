package com.example.android.loadapp

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider


class CustomButtonView @JvmOverloads constructor(context: Context,attributeSet: AttributeSet?=null)
    : View(context,attributeSet)
{

    private lateinit var notificationManager: NotificationManager


    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        color=ContextCompat.getColor(context,R.color.purple_500)
        strokeWidth =1f
        textAlign = Paint.Align.CENTER
        textSize = 70.0f
        typeface = Typeface.create( "", Typeface.BOLD)
    }

    private lateinit var rect : Rect


    init {

        isClickable = true
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
       // rect = Rect(0,0,w,h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //use this method to set your own colors

        //draw the Rectangle
        rect = Rect(0,0,width,height)
        canvas.drawRect(rect,paint)
        paint.color = Color.WHITE
        canvas.drawText("label", (width/2).toFloat(), (height/1.6).toFloat(),paint)
        paint.color = ContextCompat.getColor(context,R.color.purple_500)
    }

    override fun performClick(): Boolean {
       // if (super.performClick()) return true

        val url = MainActivity.url
        if(url != "")
            download(MainActivity.url)
        else
            Toast.makeText(context,"Please select a url",Toast.LENGTH_LONG).show()

        invalidate()
        return true
    }


    private fun download(url : String)
    {
        val manager : DownloadManager = context.getSystemService(DownloadManager::class.java)

        val uri: Uri =
            Uri.parse(url)
        val request = DownloadManager.Request(uri)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        var reference: Long = 0L
        try {
            reference = manager.enqueue(request)
        }catch (e:Exception)
        {
            e.printStackTrace()
        }

        // using query method
        var finishDownload : Boolean = false
        var process : Int =0
        while (!finishDownload) {
            val cursor: Cursor =
                manager.query(DownloadManager.Query().setFilterById(reference))
            if (cursor.moveToFirst()) {
                val status: Int =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_FAILED -> {
                        finishDownload = true
                        process =-1
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        finishDownload = true
                        process =1
                    }
                    else -> {
                        process =0
                    }
                }
            }
        }

        notificationManager.cancelAll()
        sendNotification(process,url)

    }

    private fun sendNotification(status : Int,url: String)
    {
        val NOTIFICATION_ID = 20
        var contentIntent = Intent(context, DetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        contentIntent.putExtra("FileName",url)
        contentIntent.putExtra("Status",status)

        var contentPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id))
            .setSmallIcon(R.drawable.download)
            .setContentTitle("Download")
            .setContentText("Click to check the download status")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID,builder.build())

    }

}