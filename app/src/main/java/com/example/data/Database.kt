package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val currency: String,
    val type: String, // SEND, RECEIVE, RECHARGE, WITHDRAW, CONVERSION, BILL
    val category: String, // Orange Money, MTN, Moov, Wave, Cash, Bank, TV, etc.
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val content: String,
    val isFromMe: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val channel: String // "support", "dramane", "fatou", "group_afrique"
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val id: Int, // Product ID
    val title: String,
    val price: Double,
    val quantity: Int
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Singleton profile
    val nom: String = "Diallo",
    val prenom: String = "Amadou",
    val pays: String = "Sénégal",
    val ville: String = "Dakar",
    val numero: String = "+221 77 123 45 67",
    val email: String = "amadou.diallo@gaateone.com",
    val langue: String = "Wolof", // Wolof, Français, Anglais, Swahili, Yorùbá, etc.
    val devise: String = "XOF", // XOF, XAF, NGN, KES, USD
    val pin: String = "123456", // 6-digit PIN
    val kycStatus: String = "VERIFIED", // PENDING, VERIFIED, NOT_SUBMITTED
    val kycDocumentUrl: String = "",
    val biometricEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true
)

@Entity(tableName = "project_config")
data class ProjectConfig(
    @PrimaryKey val id: Int = 1,
    val projectName: String = "Gaate Dev AI",
    val founderName: String = "Bil Gaate",
    val contactNo: String = "+2250789271314",
    val email: String = "abdoulmia130@gmail.com"
)

// --- DAOs (Data Access Objects) ---

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE channel = :channel ORDER BY timestamp ASC")
    fun getMessagesForChannel(channel: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
}

@Dao
interface CartItemDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCartItem(item: CartItem)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun removeCartItem(id: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)
}

@Dao
interface ProjectConfigDao {
    @Query("SELECT * FROM project_config WHERE id = 1 LIMIT 1")
    fun getProjectConfig(): Flow<ProjectConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProjectConfig(config: ProjectConfig)
}

// --- App Database ---

@Database(
    entities = [Transaction::class, ChatMessage::class, CartItem::class, UserProfile::class, ProjectConfig::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun cartItemDao(): CartItemDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun projectConfigDao(): ProjectConfigDao
}
