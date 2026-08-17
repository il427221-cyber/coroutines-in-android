package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nmedia.entity.PostRemoteKeyEntity

    @Dao
    interface PostRemoteKeyDao {
        //Возвращает id самого свежего поста в БД
        @Query("SELECT max(`key`) FROM PostRemoteKeyEntity")
        suspend fun max(): Long?

        //Возвращает id самого старого поста в БД
        @Query("SELECT min(`key`) FROM PostRemoteKeyEntity")
        suspend fun min(): Long?

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(postRemoteKeyEntity: PostRemoteKeyEntity)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(postRemoteKeyEntity: List<PostRemoteKeyEntity>)

        @Query("DELETE FROM PostRemoteKeyEntity")
        suspend fun clear()

        @Query("DELETE FROM PostRemoteKeyEntity WHERE type = :type")
        suspend fun deleteByType(type: PostRemoteKeyEntity.KeyType)
    }
