package taipt4.kotlin.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import taipt4.kotlin.newsapp.R
import taipt4.kotlin.newsapp.adapters.NewsAdapter
import taipt4.kotlin.newsapp.databinding.FragmentSearchNewsBinding
import taipt4.kotlin.newsapp.models.NewsResponse
import taipt4.kotlin.newsapp.ui.NewsActivity
import taipt4.kotlin.newsapp.ui.NewsViewModel
import taipt4.kotlin.newsapp.util.Constants
import taipt4.kotlin.newsapp.util.Resource

class SearchNewsFragment : Fragment() {

    val TAG = "SearchNewsFragment"
    private var _binding: FragmentSearchNewsBinding? = null
    private val binding get() = _binding!!
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter

    var isLoading: Boolean = false
    var isLastPage: Boolean = false
    var isScrolling: Boolean = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate =
                isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                viewModel.getBreakingNews(binding.etSearch.text.toString())
                isScrolling = false
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("TaiPT", "SearchNewsFragment::onCreateView()")
        _binding = FragmentSearchNewsBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("TaiPT", "SearchNewsFragment::onViewCreated()")
        super.onViewCreated(view, savedInstanceState)
        var job: Job? = null
        binding.etSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(Constants.SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty()) {
                        viewModel.searchNews(editable.toString())
                    }
                }
            }
        }
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment,
                bundle
            )
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("TaiPT", "SearchNewsFragment::onStart()")
        viewModel = (activity as NewsActivity).viewModel
        viewModel.searchNews.observe(
            viewLifecycleOwner
        ) { response: Resource<NewsResponse> ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newsResponse: NewsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.searchNewsPage == totalPages
                        if (isLastPage) {
                            binding.rvSearchNews.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Log.e(TAG, "An error occurred: $message")
                        Toast.makeText(context, "An error occurred: $message", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        Log.d("TaiPT", "SearchNewsFragment::setupRecyclerView()")
        newsAdapter = NewsAdapter()
        binding.rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
        }
    }

    private fun hideProgressBar() {
        Log.d("TaiPT", "SearchNewsFragment::hideProgressBar()")
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        Log.d("TaiPT", "SearchNewsFragment::showProgressBar()")
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("TaiPT", "SearchNewsFragment::onDestroyView()")
        _binding = null
    }
}