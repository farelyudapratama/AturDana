package com.yuch.aturdana.data.pref

data class ReminderModel (
    val id : String? = "",
    val reminderAmount : String? = "",
    val reminderDate : String? = "",
    val reminderDesc : String? = "",
    val reminderId : String? = "",
    val userId : String? = "",
    var status: String? = "pending"
)