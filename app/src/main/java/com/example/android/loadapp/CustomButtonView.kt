package com.example.android.loadapp

import android.animation.ArgbEvaluator
import android.animation.TimeAnimator
import android.animation.ValueAnimator
import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.*
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.withStyledAttributes


class CustomButtonView @JvmOverloads constructor(context: Context,attributeSet: AttributeSet?=null)
    : View(context,attributeSet),TimeAnimator.TimeListener
{

    private lateinit var notificationManager: NotificationManager
    private lateinit var button: View
    private var label : String = "Download"
    private var widthFrom : Float = 0F
    private var widthTo : Float = 0F

    private lateinit var rect : Rect
    private var currentAngle = 0
    var colorFrom = 0
    var colorTo = 0
    var leftRect = 0
    var rightRect = 0

    private lateinit var clipDrawable: ClipDrawable
    private val LEVEL_INCREMENT = 80
    private val MAX_LEVEL = 500000
    var currentLevel = 0
    var mAnimator = TimeAnimator()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        color=colorFrom
        strokeWidth =1f
        textAlign = Paint.Align.CENTER
        textSize = 70.0f
        typeface = Typeface.create( "", Typeface.BOLD)
    }

    private val paintLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        color=Color.WHITE
        strokeWidth =1f
        textAlign = Paint.Align.CENTER
        textSize = 60.0f
        typeface = Typeface.create( "", Typeface.BOLD)
    }

    private val paintArc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        color=colorFrom
        strokeWidth =1f
        textAlign = Paint.Align.CENTER
        textSize = 60.0f
        typeface = Typeface.create( "", Typeface.BOLD)
    }

    init {

        button = findViewById(R.id.download_button)

        val layerDrawable = findViewById<View>(R.id.download_button).background as LayerDrawable
        clipDrawable = (layerDrawable.findDrawableByLayerId(R.id.clip_drawable) as ClipDrawable)!!
        mAnimator.setTimeListener(this)

        context.withStyledAttributes(attributeSet,R.styleable.CustomButtonView) {
            colorFrom = getColor(R.styleable.CustomButtonView_colorFrom, 0)
            colorTo = getColor(R.styleable.CustomButtonView_colorTo, 0)
            label = getString(R.styleable.CustomButtonView_text).toString()
            widthFrom=getDimension(R.styleable.CustomButtonView_widthFrom,0F)
            widthTo=getDimension(R.styleable.CustomButtonView_widthTo,0F)
        }

//        paintArc.color=colorFrom
//        paint.color=colorFrom
        leftRect = 100
        rightRect = 850

        isClickable = true
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val rectF = RectF(750F, 40F, 820F, 120F)

        if(currentAngle.toFloat()==200F)
        {
            paintArc.color = colorTo
            label="Download"
            leftRect = 100
            rightRect = 850
            //draw the Rectangle
            rect = Rect(leftRect,0,rightRect,height) //width end
            canvas.drawRect(rect,paint)
            clipDrawable.draw(canvas)
        }
        else{
            //draw the Rectangle
            rect = Rect(leftRect,0,rightRect,height) // width start
            canvas.drawRect(rect,paint)
            clipDrawable.draw(canvas)
            canvas.drawArc(rectF, 0F,200F+currentAngle,true, paintArc);
        }
        canvas.drawText(label, (width/2).toFloat(), (height/1.6).toFloat(),paintLabel)
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true

        val url = MainActivity.url
        currentLevel = 0
        mAnimator.start()
        if(url != "")
        {
            label = "We are Loading"
            paintArc.color=Color.YELLOW
            leftRect = 50
            rightRect = 900
            invalidate()
            download(MainActivity.url)
        }
        else
            Toast.makeText(context,"Please select a File to Download",Toast.LENGTH_SHORT).show()


        return true
    }

    override fun onTimeUpdate(animation: TimeAnimator?, totalTime: Long, deltaTime: Long) {
        clipDrawable.setLevel(currentLevel)
        if (currentLevel >= MAX_LEVEL) {
            mAnimator.cancel();
        } else {
            currentLevel = Math.min(MAX_LEVEL, currentLevel + LEVEL_INCREMENT);
        }
    }

    fun startAnimatingArc(maxAngle: Float) {
        paintArc.color=Color.YELLOW
        currentAngle = 0
        Thread {
            while (currentAngle <= maxAngle) {
                invalidate()
                try {
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                currentAngle+=40
            }
        }.start()
    }


    private fun download(url : String)
    {
        startAnimatingArc(160F)
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
        var count : Int =0
        while (!finishDownload) {
            count++
            val cursor: Cursor =
                manager.query(DownloadManager.Query().setFilterById(reference))
            if (cursor.moveToFirst()) {
                val status: Int =
                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_FAILED -> {
                        finishDownload = true
                        process =-1
                        startToastThread(-1)
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        finishDownload = true
                        process =1
                        startToastThread(1)
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

    private fun startToastThread(status: Int)
    {
        Handler().postDelayed(Runnable {
            when(status)
            {
                1->Toast.makeText(context,"Download Completed",Toast.LENGTH_SHORT).show()
                else->Toast.makeText(context,"Download Failed",Toast.LENGTH_SHORT).show()
            }
        }, 1500)
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
            .setSettingsText("Expand to check status")
            .setContentTitle("Download Completed")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(contentPendingIntent)
            .addAction(R.drawable.ic_launcher_background,"Status",contentPendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID,builder.build())

    }
}