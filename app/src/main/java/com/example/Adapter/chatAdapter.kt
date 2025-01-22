package com.example.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.Model.chat
import com.example.chatappreal.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import de.hdodenhof.circleimageview.CircleImageView

class chatAdapter(
    private val context: Context,
    private val chatList: List<chat>,
    private val imageUrl: String
) : RecyclerView.Adapter<chatAdapter.AddItemViewHolder>() {

    private var firebaseuser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddItemViewHolder {
        return if (viewType == 1) {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.message_item_right, parent, false)
            AddItemViewHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.message_item_left, parent, false)
            AddItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: AddItemViewHolder, position: Int) {
        val chats: chat = chatList[position]

        // Showing profile image on receive side
        Glide.with(context).load(imageUrl).into(holder.profile_image)

        // Image message
        if (chats.message == "send you an image" && chats.url.isNotEmpty()) {
            if (chats.sender == firebaseuser!!.uid) {
                holder.show_text_message?.visibility = View.GONE
                holder.right_image_view?.visibility = View.VISIBLE
                Glide.with(context).load(chats.url).into(holder.right_image_view!!)
            } else {
                holder.show_text_message?.visibility = View.GONE
                holder.left_image_view?.visibility = View.VISIBLE
                Glide.with(context).load(chats.url).into(holder.left_image_view!!)
            }
        }
        // Text message
        else {
            holder.show_text_message?.visibility = View.VISIBLE
            holder.show_text_message?.text = chats.message
            holder.right_image_view?.visibility = View.GONE
            holder.left_image_view?.visibility = View.GONE
        }

        //send and seen message
        if(chats.isSeen){
            if(position == chatList.size){
                holder.text_seen.text == "Seen"
                if(chats.message == "send you an image" && chats.url.isNotEmpty()){
                    val lp:RelativeLayout.LayoutParams? = holder.text_seen?.layoutParams as RelativeLayout.LayoutParams
                    lp?.setMargins(0,245,10,0)
                    holder!!.text_seen?.layoutParams = lp
                }
            }
            else{
                holder.text_seen!!.visibility = View.GONE
            }
        }
        else{
            if(position == chatList.size){
                holder.text_seen.text == "Sent"
                if(chats.message == "send you an image" && chats.url.isNotEmpty()){
                    val lp:RelativeLayout.LayoutParams? = holder.text_seen?.layoutParams as RelativeLayout.LayoutParams
                    lp?.setMargins(0,245,10,0)
                    holder!!.text_seen?.layoutParams = lp
                }
            }
            else{
                holder.text_seen!!.visibility = View.GONE
            }
        }


    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    inner class AddItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profile_image: CircleImageView? = null
        var show_text_message: TextView? = null
        var left_image_view: ImageView? = null
        var right_image_view: ImageView? = null
        var text_seen:TextView? = null

        init {
            profile_image = itemView.findViewById(R.id.profile_image)
            show_text_message = itemView.findViewById(R.id.showTextMessage)
            left_image_view = itemView.findViewById(R.id.leftImageView)
            right_image_view = itemView.findViewById(R.id.right_image_view)
            text_seen = itemView.findViewById(R.id.text_seen)

        }
    }

    override fun getItemViewType(position: Int): Int {
        firebaseuser = FirebaseAuth.getInstance().currentUser
        return if (chatList[position].sender == firebaseuser!!.uid) {
            1
        } else {
            0
        }
    }
}
