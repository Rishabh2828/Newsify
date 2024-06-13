package com.shurish.newsify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.shurish.newsify.Repository.NewsRepository
import com.shurish.newsify.Ui.NewsViewModel
import com.shurish.newsify.Ui.NewsViewModelProviderFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        lateinit var bottomnavigationview : BottomNavigationView
        lateinit var newsNavHostFragment : Fragment


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomnavigationview = findViewById(R.id.bottomNavigationView)
         newsNavHostFragment = supportFragmentManager.findFragmentById(R.id.newsNavHostFragment) as NavHostFragment
        val navController = newsNavHostFragment.navController

        bottomnavigationview.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.articleFragment) {
                // If the current destination is the ArticleFragment, hide the BottomNavigationView
                bottomnavigationview.visibility = View.GONE
            } else {
                // For other destinations, show the BottomNavigationView
                bottomnavigationview.visibility = View.VISIBLE
            }
        }
    }
}