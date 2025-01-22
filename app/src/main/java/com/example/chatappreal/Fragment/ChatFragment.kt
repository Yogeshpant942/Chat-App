package com.example.chatappreal.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.Adapter.UserAdapter
import com.example.Model.Chatlist
import com.example.Model.Users
import com.example.chatappreal.R
import com.example.chatappreal.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatFragment : Fragment() {
    private var adapter:UserAdapter? = null
    private var users:List<Users>? = null
    private var usersChatList:List<Chatlist>? = null
    private var firebaseUser:FirebaseUser? = null
    lateinit var binding:FragmentChatBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(layoutInflater,container,false)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersChatList = ArrayList()
        val ref = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (usersChatList as ArrayList).clear()
                for(Data in snapshot.children){
                    val chatlist = Data.getValue(Chatlist::class.java)
                    (usersChatList as ArrayList).add(chatlist!!)
                }
                retreiveChats()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        return binding.root
    }
    private fun retreiveChats(){
        users = ArrayList()
        val ref = FirebaseDatabase.getInstance().reference.child("Users")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (users as ArrayList).clear()
                for(Data in snapshot.children){
                    val user = Data.getValue(Users::class.java)
                    for(eachChatList in usersChatList!!){
                        if(user!!.uid.equals(eachChatList.id)){
                        (users as ArrayList).add(user!!)
                        }
                    }
                }
                adapter = UserAdapter(requireContext(),(users as ArrayList),true)
                binding.RVChats.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) {
            }


        })
    }
}