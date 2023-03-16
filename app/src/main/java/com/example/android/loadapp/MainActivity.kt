package com.example.android.loadapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.android.loadapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private lateinit var radioButton1: RadioButton
    private lateinit var radioButton2: RadioButton
    private lateinit var radioButton3: RadioButton

    companion object{
        var url = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main)
        viewModel= ViewModelProvider(this).get(MainViewModel::class.java)   //init viewModel

        getElements()
        initElements()

    }

    private fun getElements()
    {
        checkForPermssion()
        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_channel_name)
        )
        //registerReceiver(broadcastReceiver,filter)

        radioButton1=findViewById(R.id.radioButton1)
        radioButton2=findViewById(R.id.radioButton2)
        radioButton3=findViewById(R.id.radioButton3)
    }

    private fun initElements()
    {
        radioButton1.setOnClickListener(View.OnClickListener {
            if(radioButton1.isChecked)
            {
                url = "https://github.com/bumptech/glide"
            }
        })

        radioButton2.setOnClickListener(View.OnClickListener {
            if(radioButton2.isChecked)
            {
                url ="https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"
            }
        })

        radioButton3.setOnClickListener(View.OnClickListener {
            if(radioButton3.isChecked)
            {
                url = "https://github.com/square/retrofit"
            }
        })
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied.
        }
    }

    private fun checkForPermssion()
    {
        if(ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.POST_NOTIFICATIONS
            )!== PackageManager.PERMISSION_GRANTED)
        {
            requestPermissionLauncher.launch(
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
                .apply {
                    description = getString(R.string.notification_desc)
                }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}