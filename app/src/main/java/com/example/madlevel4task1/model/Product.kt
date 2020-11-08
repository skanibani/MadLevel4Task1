package com.example.madlevel4task1.model

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(tableName = "productTable")
data class Product(
        @ColumnInfo(name = "name")
        var productName: String,

        @ColumnInfo(name = "quantity")
        var productQuantity: Int,

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var id: Long? = null
)