package taipt4.kotlin.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import taipt4.kotlin.newsapp.R
import taipt4.kotlin.newsapp.adapters.NewsAdapter
import taipt4.kotlin.newsapp.databinding.FragmentBreakingNewsBinding
import taipt4.kotlin.newsapp.models.Article
import taipt4.kotlin.newsapp.models.NewsResponse
import taipt4.kotlin.newsapp.ui.NewsActivity
import taipt4.kotlin.newsapp.ui.NewsViewModel
import taipt4.kotlin.newsapp.util.Constants.Companion.QUERY_PAGE_SIZE
import taipt4.kotlin.newsapp.util.Resource

class BreakingNewsFragment : Fragment() {

    val TAG = "BreakingNewsFragment"

    private var _binding: FragmentBreakingNewsBinding? = null
    private val binding get() = _binding!!
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter

    val args2: BreakingNewsFragmentArgs by navArgs()

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
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate =
                isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                viewModel.getBreakingNews("us")
                isScrolling = false
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("TaiPT", "BreakingNewsFragment::onCreateView()")
        _binding = FragmentBreakingNewsBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articleFragment,
                bundle
            )
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("TaiPT", "BreakingNewsFragment::onStart()")
        viewModel = (activity as NewsActivity).viewModel
        viewModel.breakingNews.observe(
            viewLifecycleOwner
        ) { response: Resource<NewsResponse> ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newsResponse: NewsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.breakingNewsPage == totalPages
                        if (isLastPage) {
                            binding.rvBreakingNews.setPadding(0, 0, 0, 0)
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
        val article2: Article? = args2.article2
        Log.d(TAG, "onStart: ${article2?.description}")
    }

    private fun setupRecyclerView() {
        Log.d("TaiPT", "BreakingNewsFragment::setupRecyclerView()")
        newsAdapter = NewsAdapter()
        binding.rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }
    }

    private fun hideProgressBar() {
        Log.d("TaiPT", "BreakingNewsFragment::hideProgressBar()")
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        Log.d("TaiPT", "BreakingNewsFragment::showProgressBar()")
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("TaiPT", "BreakingNewsFragment::onDestroyView()")
        _binding = null
    }
}