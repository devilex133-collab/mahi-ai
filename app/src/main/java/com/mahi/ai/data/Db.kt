package com.mahi.ai.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "user" | "assistant"
    val text: String,
    val ts: Long = System.currentTimeMillis()
)

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY id") fun observe(): Flow<List<MessageEntity>>
    @Query("SELECT * FROM messages ORDER BY id DESC LIMIT :n") suspend fun last(n: Int): List<MessageEntity>
    @Insert suspend fun insert(m: MessageEntity): Long
    @Query("DELETE FROM messages") suspend fun clear()
}

@Database(entities = [MessageEntity::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun messages(): MessageDao
}
