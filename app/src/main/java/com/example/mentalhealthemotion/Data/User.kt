package com.example.mentalhealthemotion.Data

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

@Entity(tableName = "user")
data class User(
    @PrimaryKey val userID: Int = 0,
    var userName: String = "",
    var email: String = "",
    var password: String = "",
    var profilePicture: String = "",
    var createdDate: String? = null,
    var accountStatus: AccountStatus = AccountStatus.ACTIVE,
    var phoneNo: String = "",
    var birthDate: String? = null,
    var role: UserRole = UserRole.NORMAL
)
