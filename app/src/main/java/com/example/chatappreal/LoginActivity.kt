package com.example.chatappreal

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatappreal.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private  var auth= FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarLogin)
        supportActionBar!!.title = "Login"
        supportActionBar!!.setDefaultDisplayHomeAsUpEnabled(true)
        binding.toolbarLogin.setNavigationOnClickListener {
            finish()
        }

        binding.button.setOnClickListener{
            val userEmail:String = binding.EmailLogin.text.toString()
            val userPassword:String = binding.passwordLogin.text.toString()

            if( userPassword.isEmpty() || userEmail.isEmpty()){
                Toast.makeText(this,"Fill the missing field", Toast.LENGTH_LONG).show()
            }
            else{
                auth.createUserWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener { task->
                    if(task.isSuccessful){
                        val intent  = Intent(this@LoginActivity,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(this,task.exception?.localizedMessage,Toast.LENGTH_LONG).show()

                    }

                }
            }
        }


    }
}
