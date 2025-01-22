package com.example.chatappreal.Fragment
import android.app.AlertDialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.Model.Users
import com.example.chatappreal.databinding.FragmentSettingBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask

class SettingFragment : Fragment() {
    lateinit var binding: FragmentSettingBinding
    var reference: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null
    var profile_image: Uri? = null
    var cover_image: Uri? = null
    var imageUri: Uri? = null
    var storageRef: StorageReference? = null
    var checkCover: String = ""
    var checkSocial: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        binding = FragmentSettingBinding.inflate(layoutInflater, container, false)
        storageRef = FirebaseStorage.getInstance().reference.child("User Image")
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    binding.usernameSetting.text = user!!.username
                    Glide.with(requireContext()).load(user.cover.toUri()).into(binding.coverImage)
                    Glide.with(requireContext()).load(user.profile.toUri())
                        .into(binding.profileImage)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        binding.profileImage.setOnClickListener {
            pickimage.launch("image/*")
        }
        binding.coverImage.setOnClickListener {
            checkCover = "cover"
            pickimage.launch("image/*")

        }
        binding.facebook.setOnClickListener {
            checkSocial = "facebook"
            setSocialLink()
        }
        binding.instagram.setOnClickListener {
            checkSocial = "instagram"
            setSocialLink()
        }
        binding.website.setOnClickListener {
            checkSocial = "website"
            setSocialLink()
        }
        return binding.root
    }

    private fun setSocialLink() {
        val alertDiaglog:AlertDialog.Builder = AlertDialog.Builder(requireContext()!!,
            com.bumptech.glide.R.style.AlertDialog_AppCompat)
        if(checkSocial == "website"){
            alertDiaglog.setTitle("Write URL:")
        }
        else{
            alertDiaglog.setTitle("Write username:")
        }
        val editText:EditText

        if(checkSocial == "website"){
            editText.hint = "www.dsfjs"
        }
        else{
           editText.hint = "fsdjs"
        }
        alertDiaglog.setView(editText)
    alertDiaglog.setPositiveButton("Create",DialogInterface.OnClickListener{ dialog,which->
        val str = editText.text.toString()
        if(str == null){
            Toast.makeText(requireContext(),"Fill the field",Toast.LENGTH_SHORT).show()
        }
        else{
            saveSocial(str)
        }
    })
        alertDiaglog.setPositiveButton("Cancel",DialogInterface.OnClickListener { dialogInterface, i ->
            dialogInterface.cancel()
        })
    }
    private fun saveSocial(str: String) {
            val mapSocial = HashMap<String,Any>()
          when(checkSocial){
              "facebook"->{
                  mapSocial["facebook"] = "https://m.facebook.com${str}"
              }
              "instagram"->{
                  mapSocial["instagram"] = "https://m.facebook.com${str}"
              }
              "facebook"->{
                  mapSocial["website"] = "https://${str}"
              }
          }
        reference!!.updateChildren(mapSocial)
    }
    private val pickimage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            binding.profileImage.setImageURI(uri)
            imageUri = uri
        }
    }
    private fun uploadImage() {
        val ref = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")
        val uploadTask: StorageTask<*>
        uploadTask = ref.putFile(imageUri!!)
        uploadTask.continueWithTask<Uri?>(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUrl = task.result
                val url = downloadUrl.toString()
                if(checkCover == "cover")
                {
                    val mapCoverImg = HashMap<String,Any>()
                    mapCoverImg["cover"] = url
                    reference!!.updateChildren(mapCoverImg)
                    checkCover = ""
                }
                else {
                    val mapProfileImg = HashMap<String, Any>()
                    mapProfileImg["profile"] = url
                    reference!!.updateChildren(mapProfileImg)
                    checkCover = ""
                }
            }
        }
    }
}