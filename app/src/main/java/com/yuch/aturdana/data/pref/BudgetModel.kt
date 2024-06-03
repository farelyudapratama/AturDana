package com.yuch.aturdana.data.pref

data class BudgetModel (
    val id : String? = "",
    val budgetId : String? = "",
    val category_id : String? = "",
    val amount : String? = "",
    val month : String? = "",
    val user_id : String? = "",
    val year : String? = "",
){
    // Convenience method to get amount as Double
    val amountAsDouble: Double
        get() = amount?.toDoubleOrNull() ?: 0.0
}