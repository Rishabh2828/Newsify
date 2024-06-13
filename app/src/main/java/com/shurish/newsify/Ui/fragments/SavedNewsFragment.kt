package com.shurish.newsify.Ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.shurish.newsify.Db.NewsDatabase
import com.shurish.newsify.NewsAdapter
import com.shurish.newsify.R
import com.shurish.newsify.Repository.NewsRepository
import com.shurish.newsify.Ui.NewsViewModel
import com.shurish.newsify.Ui.NewsViewModelProviderFactory

class SavedNewsFragment : Fragment() {

    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    private lateinit var rvSavedNews: RecyclerView
    private lateinit var textEmptyView: TextView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvSavedNews = view.findViewById(R.id.rvSavedNews)
        val dao = NewsDatabase.getInstance(requireActivity()).newsDao
        val repository = NewsRepository(dao)
        val factory = NewsViewModelProviderFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, factory)[NewsViewModel::class.java]
        textEmptyView=view.findViewById(R.id.textEmptyView)


        setupRecyclerView()

        viewModel.getnews().observe(viewLifecycleOwner, Observer { articles ->
            newsAdapter.differ.submitList(articles)

            if (articles.isEmpty()) {
                textEmptyView.visibility = View.VISIBLE // Show empty view
                rvSavedNews.visibility = View.GONE // Hide RecyclerView
            } else {
                textEmptyView.visibility = View.GONE // Hide empty view
                rvSavedNews.visibility = View.VISIBLE // Show RecyclerView
            }

        })

        newsAdapter.setOnItemClickListener { article ->
            article?.let {
                // Create a bundle and pass the "article" argument as Parcelable
                val bundle = Bundle().apply {
                    putParcelable("article", it) // Ensure the argument key is "article"
                }

                // Navigate to ArticleFragment and pass the bundle containing the "article" argument
                findNavController().navigate(
                    R.id.action_savedNewsFragment_to_articleFragment,
                    bundle
                )
            }
        }



        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article = newsAdapter.differ.currentList[position]
                viewModel.deleteAllArtciles(article)
                Snackbar.make(view, "Successfully deleted article", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        viewModel.insertArticle(article)
                    }
                    show()
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(rvSavedNews)
        }






    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        rvSavedNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }




    }

