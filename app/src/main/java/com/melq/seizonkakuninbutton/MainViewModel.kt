package com.melq.seizonkakuninbutton

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.melq.seizonkakuninbutton.model.user.User
import com.melq.seizonkakuninbutton.model.user.UserRepository

class MainViewModel : ViewModel() {
    companion object{
        private val repository = UserRepository()
    }

    lateinit var user: FirebaseUser
    val auth: FirebaseAuth = Firebase.auth

    var eMessage: MutableLiveData<Int> = MutableLiveData(0)
    var done: MutableLiveData<Boolean> = MutableLiveData(false)

    fun buttonPushed() {
        repository.reportLiving(user.uid, Timestamp.now())
    }

    fun loginPushed(email: String, password: String) {
        val tag = "LOGIN_PUSHED"
        if (email.isBlank() || password.isBlank()) {
            eMessage.value = R.string.enter_info
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(tag, "signInWithEmail: success")
                    user = auth.currentUser!!
                    done.value = true
                } else {
                    when (task.exception) {
                        is FirebaseNetworkException -> {
                            Log.w(tag, "signInWithEmail: failure", task.exception)
                            eMessage.value = R.string.err_net
                        }
                        is FirebaseAuthInvalidUserException -> {
                            Log.w(tag, "signInWithEmail: failure", task.exception)
                            eMessage.value = R.string.err_not_exist
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            Log.w(tag, "signInWithEmail: failure", task.exception)
                            eMessage.value = R.string.err_incorrect
                        }
                        else -> {
                            Log.w(tag, "signInWithEmail: failure", task.exception)
                            eMessage.value = R.string.err_occured
                        }
                    }
                }
            }
    }

    fun createPushed(name: String, email: String, password: String) {
        val tag = "CREATE_PUSHED"
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            eMessage.value = R.string.enter_info
            return
        }
        if (password.length < 6) {
            eMessage.value = R.string.pass_length
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(tag, "createUserWithEmail: success")
                    repository.createUser(
                        auth.currentUser!!.uid,
                        User(
                            email,
                            name,
                            mutableListOf(
                                Timestamp.now()
                            )
                        )) {
                        user = auth.currentUser!!
                        done.value = true
                    }
                } else {
                    when (task.exception) {
                        is FirebaseNetworkException -> {
                            Log.w(tag, "signInWithEmail: failure", task.exception)
                            eMessage.value = R.string.err_net
                        }
                        is FirebaseAuthUserCollisionException -> {
                            Log.w(tag, "signInWithEmail: failure", task.exception)
                            eMessage.value = R.string.exist_address
                        }
                        else -> {
                            Log.w(tag, "signInWithEmail: failure", task.exception)
                            eMessage.value = R.string.err_occured
                        }
                    }
                }
            }
    }
}