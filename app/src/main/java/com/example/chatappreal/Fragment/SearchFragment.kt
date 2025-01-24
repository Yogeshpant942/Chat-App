package com.example.chatappreal.Fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatappreal.Adapter.UserAdapter
import com.example.chatappreal.Model.Users
import com.example.chatappreal.R
import com.example.chatappreal.databinding.FragmentSearchBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale


class SearchFragment : Fragment() {
    lateinit var adapter: UserAdapter
    lateinit var list : ArrayList<Users>
    lateinit var binding:FragmentSearchBinding

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(layoutInflater,container,false)

        retrieveData()
        binding.searchUserName.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
              search(p0.toString().lowercase(Locale.ROOT))
            }

            override fun afterTextChanged(p0: Editable?) {
                TODO("Not yet implemented")
            }
        })
        binding.RV.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    private fun retrieveData() {
        var firebaseUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().reference.child("Users")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                if (binding.searchUserName.text.toString() == null) {
                    for (Data in snapshot.children) {
                        val user: Users? = Data.getValue(Users::class.java)

                        if (user!!.uid != firebaseUserId) {
                            list.add(user)
                        }
                    }
                    adapter = UserAdapter(requireContext(), list, false)
                    binding.RV.adapter = adapter
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun search(str:String){
        var firebaseUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().reference.child("Users")
            .orderByChild("search").startAt(str)
            .endAt(str +"\uf8ff")

        ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for(Data in snapshot.children){
                    val user: Users? = Data.getValue(Users::class.java)
                    if(user!!.uid != firebaseUserId){
                        list.add(user)
                    }
                }
                adapter = UserAdapter(requireContext(),list,false)
                binding.RV.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


}