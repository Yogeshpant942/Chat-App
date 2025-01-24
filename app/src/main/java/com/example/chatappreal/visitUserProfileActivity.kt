package com.example.chatappreal

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.chatappreal.Model.Users
import com.example.chatappreal.Notification.MessageChatActivity
import com.example.chatappreal.databinding.ActivityVisitUserProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class visitUserProfileActivity : AppCompatActivity() {
    private var userVisitId :String = ""
     var user:Users? = null
    lateinit var  binding:ActivityVisitUserProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVisitUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userVisitId = intent?.getStringExtra("visit_id").toString()

        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(userVisitId)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue(Users::class.java)
                    binding.usernameSetting.text = user!!.username
                    Glide.with(applicationContext).load(user!!.profile).into(binding.profileImage)
                    Glide.with(applicationContext).load(user!!.cover).into(binding.coverImage)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        binding.facebook.setOnClickListener {
            val uri = Uri.parse(user!!.facebook)
            val intent = Intent(Intent.ACTION_VIEW,uri)
            startActivity(intent)
        }
        binding.instagram.setOnClickListener {
            val uri = Uri.parse(user!!.instagram)
            val intent = Intent(Intent.ACTION_VIEW,uri)
            startActivity(intent)
        }
        binding.website.setOnClickListener {
            val uri = Uri.parse(user!!.website)
            val intent = Intent(Intent.ACTION_VIEW,uri)
            startActivity(intent)
        }
      binding.sendMsgButton.setOnClickListener {
          val intent = Intent(this@visitUserProfileActivity,MessageChatActivity::class.java)
          intent.putExtra("visit_id",user!!.uid)
          startActivity(intent)
      }
    }
}