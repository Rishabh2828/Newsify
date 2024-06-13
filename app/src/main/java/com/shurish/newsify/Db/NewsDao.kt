package com.shurish.newsify.Db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.shurish.newsify.Models.Article

@Dao
interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(savedArticle: Article)


    @Query("SELECT * FROM  NEWSARTICLE")
    fun getAllNews() : LiveData<List<Article>>


    @Delete()
    suspend fun deleteArticle(article: Article)

}