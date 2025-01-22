package com.example.Adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.Model.Users
import com.example.chatappreal.databinding.UserSearchItemBinding
import java.util.ArrayList

class UserAdapter(
    private val context: Context,
    private val list:List<Users>,
    private val isChatCheck:Boolean
):RecyclerView.Adapter<UserAdapter.AddItemViewHolder>() {
   inner class AddItemViewHolder(val binding:UserSearchItemBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int){
            binding.apply {
                val a:Users = list[position]
                userName.text = a.username

                Glide.with(context).load(a.profile).into(profileImage)
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
                        else{

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
}