package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity WHERE newStatus = 0 ORDER BY localId DESC")
    fun getAllLocalPosts(): Flow<List<PostEntity>>

    @Query("UPDATE PostEntity SET newStatus = 0 WHERE newStatus = 1")
    suspend fun markAllNewPostsVisible()

    @Query("SELECT COUNT(*) FROM PostEntity WHERE newStatus = 1")
    fun getNewPostsCount(): Flow<Int>

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity): Long

    @Update
    suspend fun update(post: PostEntity)

    @Query("SELECT * FROM PostEntity WHERE localId = :localId")
    suspend fun getPostById(localId: Long): PostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM PostEntity WHERE localId = :id")
    suspend fun removeById(id: Long)

    @Query(
        """
           UPDATE PostEntity SET
               likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
               likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
           WHERE localId = :id;
        """
    )

    suspend fun likeById(id: Long)
}
