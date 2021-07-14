package com.melq.seizonkakuninbutton

import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.contentValuesOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.melq.seizonkakuninbutton.model.user.User
import com.melq.seizonkakuninbutton.model.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    companion object{
        private val repository = UserRepository()
    }

    val auth: FirebaseAuth = Firebase.auth
    val firebaseUser: FirebaseUser get() = auth.currentUser!!

    var user: User = User("", "", mutableListOf())
    var isUserLoaded: MutableLiveData<Boolean> = MutableLiveData(false)
    var eMessage: MutableLiveData<Int> = MutableLiveData(0)
    var done: MutableLiveData<Boolean> = MutableLiveData(false)

    val canPush: MutableLiveData<Boolean> = MutableLiveData(true)
    val countDown: MutableLiveData<Int> = MutableLiveData(0)
    var isWatcher = false

    fun buttonPushed(comment: String) {
        if (canPush.value == false) return

        viewModelScope.launch(Dispatchers.Main) {
            for (i in 10 downTo 1) {
                countDown.value = i
                delay(1000)
            }
            canPush.value = true
            countDown.value = 0
        }

        val now = Timestamp.now()
        val info = mapOf(
            "timestamp" to now,
            "comment" to comment)
        repository.reportLiving(firebaseUser.uid, info)
        user.pushHistory.add(info)

        canPush.value = false
        done.value = true
    }

    fun loginPushed(email: String, password: String, isWatcher: Boolean) {
        val tag = "LOGIN_PUSHED"
        if (email.isBlank() || password.isBlank()) {
            eMessage.value = R.string.enter_info
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(tag, "signInWithEmail: success")
                    this.isWatcher = isWatcher
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

    fun createPushed(name: String, email: String, password: String, isWatcher: Boolean) {
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
                            mutableListOf()
                        )) {
                        this.isWatcher = isWatcher
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

    fun getUserData() {
        repository.getUserData(firebaseUser.uid) {
            user = it
            isUserLoaded.value = true
        }
    }

    fun updateNameClicked(newName: String) {
        if (newName != user.name && newName.isNotBlank()) {
            repository.updateName(firebaseUser.uid, newName) {
                user.name = newName // User取得をActivityに移したときのためにこれは残す
                eMessage.value = R.string.name_updated
            }
        } else {
            eMessage.value = R.string.same_name
        }
    }

    fun logoutClicked() {
        auth.signOut()
        isWatcher = false
        done.value = true
    }
}