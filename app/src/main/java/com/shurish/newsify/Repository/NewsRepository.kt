package com.shurish.newsify.Repository

import androidx.lifecycle.LiveData
import com.shurish.newsify.Api.RetrofitInstance
import com.shurish.newsify.Db.NewsDao
import com.shurish.newsify.Models.Article

class NewsRepository (val newsDao: NewsDao){
    suspend fun getBreakingNews(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.getBreakingNews(countryCode, pageNumber)

    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        RetrofitInstance.api.searchForNews(searchQuery, pageNumber)



    fun getAllSavedNews() : LiveData<List<Article>> {
        return newsDao.getAllNews()
    }



   suspend fun deleteAll(del_article: Article){
        newsDao.deleteArticle(del_article)
    }


    suspend fun insertNews(savedArticle: Article) {

        newsDao.insertNews(savedArticle)
    }


}