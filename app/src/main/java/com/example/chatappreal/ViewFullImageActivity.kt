package com.example.chatappreal

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class ViewFullImageActivity : AppCompatActivity() {
    lateinit var imageView :ImageView
    private var imgeUrl :String = ""
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_full_image)
        imageView = findViewById(R.id.imageViewer)
        imgeUrl = intent.getStringExtra("url").toString()
        Glide.with(this).load(imgeUrl).into(imageView)

    }
}