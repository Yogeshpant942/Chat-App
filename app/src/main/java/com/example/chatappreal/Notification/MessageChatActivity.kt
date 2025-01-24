package com.example.chatappreal.Notification

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatappreal.Adapter.chatAdapter
import com.example.chatappreal.Fragment.APISERVICE
import com.example.chatappreal.Model.Users
import com.example.chatappreal.Model.chat
import com.example.chatappreal.databinding.ActivityMessageChatBinding
import com.google.android.gms.common.api.Api.Client
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class MessageChatActivity : AppCompatActivity() {
    private var useridVisit: String = ""
    private var firebaseUser: FirebaseUser? = null
    private lateinit var binding: ActivityMessageChatBinding
    private var list: List<chat>? = null
    private var adapter: chatAdapter? = null
    private var reference: DatabaseReference? = null
    private var notify = false
    private var seenListener: ValueEventListener? = null
    var apiService : APISERVICE? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.Rv.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        binding.Rv.layoutManager = linearLayoutManager

        apiService = client.client.getClient("https://fcm.googleapis.com/")!!.create(APISERVICE::class.java)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        useridVisit = intent.getStringExtra("visit_id").toString()

        // Retrieve visited user's data
        reference = FirebaseDatabase.getInstance().reference.child("Users").child(useridVisit)
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: Users? = snapshot.getValue(Users::class.java)
                if (user != null) {
                    binding.userName.text = user.username
                    Glide.with(this@MessageChatActivity).load(user.profile).into(binding.profileChat)
                    retrieveData(firebaseUser!!.uid, useridVisit, user.profile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MessageChatActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })

        binding.sendButton.setOnClickListener {
            notify = true
            val message = binding.textMessage.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(this, "Cannot send an empty message", Toast.LENGTH_SHORT).show()
            } else {
                sendMessage(firebaseUser!!.uid, useridVisit, message)
                binding.textMessage.setText("")
            }
        }

        binding.attachFile.setOnClickListener {
            notify = true
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), 433)
        }

        seenMessage(useridVisit)
    }

    private fun sendMessage(uid: String, userId: String, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key ?: return

        val messageHashMap = HashMap<String, Any>()
        messageHashMap["sender"] = uid
        messageHashMap["message"] = message
        messageHashMap["reciever"] = userId
        messageHashMap["isSeen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey

        reference.child("Chats").child(messageKey).setValue(messageHashMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Update chat lists
                val chatReferenceSender = FirebaseDatabase.getInstance().reference
                    .child("ChatList").child(firebaseUser!!.uid).child(userId)
                chatReferenceSender.child("id").setValue(userId)

                val chatReferenceReceiver = FirebaseDatabase.getInstance().reference
                    .child("ChatList").child(userId).child(firebaseUser!!.uid)
                chatReferenceReceiver.child("id").setValue(firebaseUser!!.uid)


            }
        }
         val userref = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        userref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                if (notify) {
                    sendNotification(uid, user!!.username, message)
                }
                notify = false
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 433 && resultCode == RESULT_OK && data != null && data.data != null) {
            val loadingBar = ProgressDialog(this)
            loadingBar.setMessage("Please wait, image is sending...")
            loadingBar.show()

            val fileUri = data.data
            val storagReference = FirebaseStorage.getInstance().reference.child("chat_image")
            val messageId = FirebaseDatabase.getInstance().reference.push().key ?: return
            val filePath = storagReference.child("$messageId.jpg")

            val uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result.toString()
                    val messageHashMap = HashMap<String, Any>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["message"] = "sent you an image"
                    messageHashMap["reciever"] = useridVisit
                    messageHashMap["isSeen"] = false
                    messageHashMap["url"] = downloadUrl
                    messageHashMap["messageId"] = messageId

                    FirebaseDatabase.getInstance().reference.child("Chats").child(messageId)
                        .setValue(messageHashMap).addOnCompleteListener {
                            if (it.isSuccessful) {
                                loadingBar.dismiss()
                                sendNotification(firebaseUser!!.uid, "sent you an image", downloadUrl)
                            }
                        }
                }
            }
        }
    }

    private fun sendNotification(uid: String, username: String, message: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val query = ref.orderByKey().equalTo(useridVisit)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val token = dataSnapshot.getValue(Token::class.java)
                  val data = Data(firebaseUser!!.uid,username,"$message","New message",useridVisit)
                    // Logic for sending notification goes here
                    val sender = sender(data!!,token!!.token.toString())
                    apiService!!.sendNotification(sender)
                        .enqueue(object :Callback<MyResponse>{
                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {

                            }
                            override fun onResponse(
                                call: Call<MyResponse>,
                                response: Response<MyResponse>
                            ) {
                                if(response.code() == 200){
                                    if(response.body()!!.success!=1){
                                        Toast.makeText(this@MessageChatActivity,"failed",Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun retrieveData(senderID: String, receiverID: String, imageUrl: String) {
        list = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (list as ArrayList<chat>).clear()
                for (data in snapshot.children) {
                    val chat = data.getValue(chat::class.java)
                    if (chat != null && ((chat.reciever == senderID && chat.sender == receiverID) ||
                                (chat.reciever == receiverID && chat.sender == senderID))
                    ) {
                        (list as ArrayList<chat>).add(chat)
                    }
                }
                adapter = chatAdapter(this@MessageChatActivity, list as ArrayList<chat>, imageUrl)
                binding.Rv.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun seenMessage(userId: String) {
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        seenListener = reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val chat = data.getValue(chat::class.java)
                    if (chat != null && chat.reciever == firebaseUser!!.uid && chat.sender == userId) {
                        val hashMap = hashMapOf<String, Any>("isSeen" to true)
                        data.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onPause() {
        super.onPause()
        seenListener?.let { reference?.removeEventListener(it) }
    }
}
