package com.example.android.loadapp

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.internal.ContextUtils.getActivity


class DetailActivity : AppCompatActivity() {

    private lateinit var etv_fileName : TextView
    private lateinit var etv_status : TextView
    private lateinit var btn_ok : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        initElements()
    }

    private fun initElements()
    {

        var fileName = intent.extras?.getString("FileName")
        var status = intent.extras?.getInt("Status")

        etv_fileName = findViewById(R.id.etv_fileName)
        etv_status = findViewById(R.id.etv_status)
        btn_ok = findViewById(R.id.btn_ok)


        when(fileName)
        {
            "https://github.com/bumptech/glide"->
            {
                etv_fileName.text = "Glide - Image Loading Library by BumpTech"
            }
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"->
            {
                etv_fileName.text = "LoadApp - Current repository by Udacity"
            }
            else->{
                etv_fileName.text = "Retrofit - Type-safe HTTP Client for Android and Java by Square Inc."
            }
        }

        when(status)
        {
            1-> etv_status.text = "Success"
            -1 -> etv_status.text = "Failed"
            else -> {
                etv_status.text = "Pending"
            }
        }

        btn_ok.setOnClickListener(View.OnClickListener {
            finishActivity()
        })
    }

    private fun finishActivity()
    {
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent,
            ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        this.finish()
    }

    override fun onBackPressed() {
        finishActivity()
    }
}