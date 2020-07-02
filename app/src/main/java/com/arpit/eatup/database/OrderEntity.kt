package com.arpit.eatup.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "order_cart")
data class OrderEntity(

    @PrimaryKey val id: String,
    @ColumnInfo (name = "name") val name: String,
    @ColumnInfo (name = "cost") val cost: String,
    @ColumnInfo (name = "resId") val resId: String
)