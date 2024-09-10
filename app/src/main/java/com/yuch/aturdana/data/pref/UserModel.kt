package com.yuch.aturdana.data.pref

data class UserModel(
    val username: String = "",
    val email: String = "",
    val categories: Map<String, Category> = emptyMap(),
    val avatarUrl: String = "",
    val createdAt: Long = 0L
)

data class Category(
    val name: String = "",
    val type: String = "",
    val user_id: String = ""
)
