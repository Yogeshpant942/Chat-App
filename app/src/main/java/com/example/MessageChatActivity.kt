package com.example

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.Adapter.chatAdapter
import com.example.Model.Users
import com.example.Model.chat
import com.example.chatappreal.databinding.ActivityMessageChatBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.util.HashMap
import kotlin.collections.ArrayList

class MessageChatActivity : AppCompatActivity() {
    var useridVisit:String = ""
    var firebaseUser:FirebaseUser? = null
    lateinit var binding:ActivityMessageChatBinding
    var list:List<chat>? =null
    var adapter:chatAdapter? = null
     var firebaseUser:FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMessageChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.Rv.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        binding.Rv.layoutManager = linearLayoutManager
        intent = intent
        firebaseUser = FirebaseAuth.getInstance().currentUser
        useridVisit = intent.getStringExtra("visit_id").toString()


        //visited user
        val reference = FirebaseDatabase.getInstance().reference.child("Users").child(useridVisit)
       reference.addValueEventListener(object :ValueEventListener{
           override fun onDataChange(snapshot: DataSnapshot) {
                    val user: Users? =snapshot.getValue(Users::class.java)
               if (user != null) {
                   binding.userName.text = user.username
               Glide.with(this@MessageChatActivity).load(user.profile).into(binding.profileChat)

                   retrieveData(firebaseUser!!.uid,useridVisit,user.profile)
               }
           }

           override fun onCancelled(error: DatabaseError) {

           }
       })

        binding.sendButton.setOnClickListener {
           val message = binding.textMessage.text.toString()
           if ((message) == null) {
               //toast

           }
           else{
               sendMessage(firebaseUser!!.uid,useridVisit,message)
           }
            binding.textMessage.setText("")
       }

        binding.attachFile.setOnClickListener {
            val intent =  Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser("Pick Image"),433)
        }

    }

    private fun sendMessage(uid: String, userId: String, message: String) {
       val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String,Any>()
        messageHashMap["sender"] = uid
        messageHashMap["message"]  = message
        messageHashMap["reciever"] = userId
        messageHashMap["isSeen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey!!
        reference.child("Chats").child(messageKey!!).setValue(messageHashMap)
            .addOnCompleteListener{task->
                if(task.isSuccessful){

                    //chatlist node we will use for total unread message and last message
                    val chatReference = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid).child(userId)
                    chatReference.addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(!snapshot.exists()){
                            chatReference.child("id").setValue(userId)}
                            val chatRecieverReference = FirebaseDatabase.getInstance().reference.child("ChatList").child(userId).child(firebaseUser!!.uid)
                            chatRecieverReference.child("id").setValue(firebaseUser!!.uid)
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                    val reference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
                }

            }
        override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?,
        ) {
            super.onActivityResult(requestCode, resultCode, data)

            if(requestCode ==433 && resultCode == RESULT_OK &&data!!.data != null){
                val loadingBar = ProgressDialog(applicationContext)
                loadingBar.setMessage("Please wait,image is sending...")
                val fileUri = data.data
                val reference = FirebaseDatabase.getInstance().reference

                val storagReference = FirebaseStorage.getInstance().reference.child("chat_image")
                val ref  = FirebaseDatabase.getInstance().reference
                val messageId = ref.push().key
                val filePath = storagReference.child("$messageId.jpg")

                val uploadTask: StorageTask<*>
                uploadTask = filePath.putFile(fileUri!!)
                uploadTask.continueWithTask<Uri?>(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation filePath.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        val url = downloadUrl.toString()
                        val messageHashMap = HashMap<String,Any>()
                        messageHashMap["sender"] = firebaseUser!!.uid
                        messageHashMap["message"]  = "sent you an image"
                        messageHashMap["reciever"] = userId
                        messageHashMap["isSeen"] = false
                        messageHashMap["url"] = ""
                        messageHashMap["messageId"] = messageId!!
                        ref.child("chats").child(messageId).setValue(messageHashMap)
                    }
                }
            }

            }
        }

    private fun retrieveData(senderID: String, recieveId: String, imageUrl: String) {
     list = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("chats")
        reference.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (list as ArrayList<chat>).clear()
                for(data in snapshot.children){
                    val chat = data.getValue(chat::class.java)

                    if(chat!!.reciever.equals(senderID) && chat.sender.equals(recieveId) || chat.reciever.equals(recieveId) &&chat.sender.equals(senderID)){
                        (list as ArrayList<chat>).add(chat)
                        adapter = chatAdapter(this@MessageChatActivity,(list as ArrayList<chat>),recieveId)
                        binding.Rv.adapter = adapter
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }
    }
