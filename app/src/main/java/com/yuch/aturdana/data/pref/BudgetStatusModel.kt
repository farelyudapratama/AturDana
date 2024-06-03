package com.yuch.aturdana.data.pref

data class BudgetStatusModel(
    val categoryId: String,
    val budgetAmount: Double,
    val totalExpenses: Double,
    val remainingBudget: Double,
    val isOverBudget: Boolean,
    val overBudgetAmount: Double
)