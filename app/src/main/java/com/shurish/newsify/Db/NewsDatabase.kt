package com.shurish.newsify.Db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shurish.newsify.Models.Article



@Database(entities = [Article::class], version = 1)
@TypeConverters(ClassConvertors::class)
abstract class NewsDatabase : RoomDatabase() {

    // declaring abstract reference for the interface

    abstract  val newsDao: NewsDao

    // singleton instances

    companion object{
        // THIS MAKES THE FIELD IMMEDIATELY VISIBILE TO OTHER THREAD
        @Volatile
        private var INSTANCE : NewsDatabase? = null
        fun getInstance (context: Context) : NewsDatabase{
            synchronized(this){
                var instance  = INSTANCE

                if (instance == null){

                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        NewsDatabase::class.java,
                        "news_database"


                    ).build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }


}