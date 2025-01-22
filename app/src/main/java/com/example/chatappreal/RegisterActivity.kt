package com.example.chatappreal

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.chatappreal.databinding.ActivityLoginBinding
import com.example.chatappreal.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.util.Locale

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private  var auth = FirebaseAuth.getInstance()
    private lateinit var refUsers:DatabaseReference
    private var firebaseUserUid:String = ""

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)



        setSupportActionBar(binding.toolbarRegister)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDefaultDisplayHomeAsUpEnabled(true)
        binding.toolbarRegister.setNavigationOnClickListener {
            val intent  = Intent(this@RegisterActivity,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.button.setOnClickListener{
            registerUser()
        }



    }

    private fun registerUser() {
        val userName:String = binding.usernameRegister.text.toString()
        val userEmail:String = binding.EmailRegister.text.toString()
        val userPassword:String = binding.passwordRegister.text.toString()

        if(userName.isEmpty() || userPassword.isEmpty() || userEmail.isEmpty()){
            Toast.makeText(this,"Fill the missing field",Toast.LENGTH_LONG).show()
        }
        else{
            auth.createUserWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener{task->
                if(task.isSuccessful){
                    firebaseUserUid = auth.currentUser!! .uid
                    refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserUid)
                    val userMap = HashMap<String,Any>()
                    userMap["uid"] = firebaseUserUid
                    userMap["userName"] = userName
                    userMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/adminpanel-d62fa.appspot.com/o/img.png?alt=media&token=1167b191-13a0-4c4e-bd6f-4439b8881f51"
                    userMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/adminpanel-d62fa.appspot.com/o/img_1.png?alt=media&token=512bbecc-8d0f-4f7e-a3cd-f88da3ca72fa"
                    userMap["status"] = "offilne"
                    userMap["search"] = userName.lowercase(Locale.ROOT)
                    userMap["facebook"] = "https://m.facebook.com"
                    userMap["instagram"] = "https://m.instagram.com"
                    userMap["website"] = "https://m.google.com"
                    refUsers.updateChildren(userMap).addOnCompleteListener { task->
                        if(task.isSuccessful){
                            val intent  = Intent(this@RegisterActivity,MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }


                }
                else{
                    Toast.makeText(this,task.exception?.localizedMessage,Toast.LENGTH_LONG).show()
                }

            }
        }
    }
}
