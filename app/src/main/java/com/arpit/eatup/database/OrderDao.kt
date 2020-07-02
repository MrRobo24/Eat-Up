package com.arpit.eatup.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.util.*
import kotlin.collections.ArrayList

@Dao
interface OrderDao {
    @Insert
    fun insertOrder(orderEntity: OrderEntity?)

    @Delete
    fun deleteOrder(orderEntity: OrderEntity)

    @Query("DELETE FROM order_cart")
    fun nukeTable()

    @Query("SELECT * FROM order_cart WHERE id = :foodId")
    fun getOrderById(foodId: String): OrderEntity?

    @Query("SELECT * FROM order_cart")
    fun getAllOrders(): List<OrderEntity>

}