package com.arpit.eatup.model

data class Order(
    val id: String,
    val resName: String,
    val total_cost: String,
    val date: String,
    val food: List<Dish>
)