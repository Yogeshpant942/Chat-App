package com.example.chatappreal.Adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatappreal.Model.Users
import com.example.chatappreal.Model.chat
import com.example.chatappreal.Notification.MessageChatActivity
import com.example.chatappreal.databinding.UserSearchItemBinding
import com.example.chatappreal.visitUserProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class UserAdapter(
    private val context: Context,
    private val list:List<Users>,
    private val isChatCheck:Boolean

):RecyclerView.Adapter<UserAdapter.AddItemViewHolder>() {
    var lastmsg :String = ""
   inner class AddItemViewHolder(val binding:UserSearchItemBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int){
            binding.apply {
                val a: Users = list[position]
                userName.text = a.username
                Glide.with(context).load(a.profile).into(profileImage)
                if(isChatCheck == true){
                    retrieveLastMessage(a.uid,binding.messageLast)
                }
                else{
                    binding.messageLast.visibility = View.GONE
                }
                if(isChatCheck){
                    if (a.status == "online"){
                        binding.imageOnline.visibility = View.VISIBLE
                        binding.imageOffline.visibility = View.GONE
                    }
                    else{
                        binding.imageOnline.visibility = View.GONE
                        binding.imageOffline.visibility = View.VISIBLE
                    }
                }
                else{
                    binding.imageOnline.visibility = View.GONE
                    binding.imageOffline.visibility = View.GONE
                }
                root.setOnClickListener {
                    val option = arrayOf<CharSequence>(
                        "send message"
                        ,"visit profile"
                    )
                    val builder:AlertDialog.Builder = AlertDialog.Builder(context)
                    builder.setTitle("What do you want")
                    builder.setItems(option,DialogInterface.OnClickListener{
                        diaglog,which->
                        if(which == 0){
                            val intent = Intent(context,MessageChatActivity::class.java)
                            intent.putExtra("visit_id",a.uid)
                            context.startActivity(intent)
                        }
                        else if (position == 1){
                            val intent = Intent(context,visitUserProfileActivity::class.java)
                            intent.putExtra("visit_id",a.uid)
                            context.startActivity(intent)
                        }
                    })
                }
            }
        }
    }
    @SuppressLint("SuspiciousIndentation")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddItemViewHolder {
      var binding = UserSearchItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return AddItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddItemViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun retrieveLastMessage(chatUserId: String, messageLast: TextView)
    {
        lastmsg = "defaultMsg"
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference.child("chats")

        reference.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for(datasnapshot in snapshot.children){
                    val chat:chat? = datasnapshot.getValue(chat::class.java)
                    if(firebaseUser!= null && chat != null){
                        if(chat.reciever == firebaseUser.uid &&
                            chat.sender == chatUserId ||chat.reciever == chatUserId && chat.sender == firebaseUser.uid){

                            lastmsg = chat.message

                        }
                    }
                    when(lastmsg){
                        "defaultMag"-> messageLast.text = "no Message"
                        "send you a message"->messageLast.text = "image sent"
                        else-> messageLast.text = lastmsg
                    }
                    lastmsg = "defaultMsg"

                }

            }
        })

    }
}