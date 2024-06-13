package com.shurish.newsify.Ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shurish.newsify.Db.NewsDatabase
import com.shurish.newsify.NewsAdapter
import com.shurish.newsify.R
import com.shurish.newsify.Repository.NewsRepository
import com.shurish.newsify.Ui.NewsViewModel
import com.shurish.newsify.Ui.NewsViewModelProviderFactory
import com.shurish.newsify.Utils.Constants
import com.shurish.newsify.Utils.Constants.Companion.SEARCH_NEWS_TIME_DELAY
import com.shurish.newsify.Utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchNewsFragment : Fragment() {

    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    lateinit var  etSearch : EditText

    private lateinit var rvSearchNews: RecyclerView
    private lateinit var paginationProgressBar: View
    private lateinit var itemErrorMessage: View
    private lateinit var btnRetry: Button
    private lateinit var tvErrorMessage: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dao = NewsDatabase.getInstance(requireActivity()).newsDao
        val repository = NewsRepository(dao)
        val factory = NewsViewModelProviderFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, factory)[NewsViewModel::class.java]

        etSearch= view.findViewById(R.id.etSearch)

        rvSearchNews = view.findViewById(R.id.rvSearchNews)
        paginationProgressBar= view.findViewById(R.id.paginationProgressBar)
        itemErrorMessage=view.findViewById(R.id.itemErrorMessage)
        btnRetry= view.findViewById(R.id.btnRetry)
        tvErrorMessage= view.findViewById(R.id.tvErrorMessage)

        setupRecyclerView()




        newsAdapter.setOnItemClickListener { article ->
            article?.let {
                // Create a bundle and pass the "article" argument as Parcelable
                val bundle = Bundle().apply {
                    putParcelable("article", it) // Ensure the argument key is "article"
                }

                // Navigate to ArticleFragment and pass the bundle containing the "article" argument
                findNavController().navigate(
                    R.id.action_searchNewsFragment_to_articleFragment,
                    bundle
                )
            }
        }


        var job: Job? = null
        etSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if(editable.toString().isNotEmpty()) {
                        viewModel.searchNews(editable.toString())
                    }else{
                        newsAdapter.differ.submitList(null)
                        newsAdapter.notifyDataSetChanged()
                    }
                }
            }
        }

        viewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            response?.let {

                when(response) {
                    is Resource.Success -> {
                        hideProgressBar()
                        hideErrorMessage()
                        response.data?.let { newsResponse ->
                            newsAdapter.differ.submitList(newsResponse.articles.toList())
                            val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                            isLastPage = viewModel.searchNewsPage == totalPages
                            if(isLastPage) {
                                rvSearchNews.setPadding(0, 0, 0, 0)
                            }

                        }
                    }
                    is Resource.Error -> {
                        hideProgressBar()
                        response.message?.let { message ->
                            Toast.makeText(activity, "An error occured: $message", Toast.LENGTH_LONG).show()
                            showErrorMessage(message)
                        }
                    }
                    is Resource.Loading -> {
                        showProgressBar()
                    }
                }


            }

        })

        btnRetry.setOnClickListener {
            if (etSearch.text.toString().isNotEmpty()) {
                viewModel.searchNews(etSearch.text.toString())
            } else {
                hideErrorMessage()
            }
        }







    }


    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideErrorMessage() {
        itemErrorMessage.visibility = View.INVISIBLE
        isError = false
    }

    private fun showErrorMessage(message: String) {
        itemErrorMessage.visibility = View.VISIBLE
        tvErrorMessage.text = message
        isError = true
    }

    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoErrors = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate = isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if(shouldPaginate) {
                viewModel.searchNews(etSearch.text.toString())
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }


    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)

        }
}
    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the search results when the view is destroyed
        viewModel.clearSearchResults()
    }

}
