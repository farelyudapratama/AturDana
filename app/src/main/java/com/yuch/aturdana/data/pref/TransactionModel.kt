package com.yuch.aturdana.data.pref

data class TransactionModel(
    val id: String? = "",
    val amount: String? = "",
    val category_id: String? = "",
    val date: String? = "",
    val description: String? = "",
    val time: String? = "",
    val type: String? = "",
    val userId: String? = ""
)
