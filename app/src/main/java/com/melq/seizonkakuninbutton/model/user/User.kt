package com.melq.seizonkakuninbutton.model.user

import com.google.firebase.Timestamp

class User (
    val email: String,
    var name: String,
    val pushHistory: MutableList<Map<String, Any>>
) {
    constructor() : this("", "", mutableListOf())

    override fun toString(): String {
        return hashMapOf(
            "email" to email,
            "name" to name,
            "history" to pushHistory.toString()
        ).toString()
    }
}