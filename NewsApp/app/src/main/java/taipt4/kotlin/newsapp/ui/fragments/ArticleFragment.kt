package taipt4.kotlin.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import taipt4.kotlin.newsapp.R
import taipt4.kotlin.newsapp.databinding.FragmentArticleBinding
import taipt4.kotlin.newsapp.models.Article
import taipt4.kotlin.newsapp.ui.NewsActivity
import taipt4.kotlin.newsapp.ui.NewsViewModel

class ArticleFragment : Fragment() {

    private var _binding: FragmentArticleBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: NewsViewModel
    val args: ArticleFragmentArgs by navArgs()
    lateinit var article: Article

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("TaiPT", "ArticleFragment::onCreateView()")
        _binding = FragmentArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("TaiPT", "ArticleFragment::onViewCreated()")
        article = args.article
        binding.webView.apply {
            webViewClient = WebViewClient()
            loadUrl(article.url!!)
        }
        binding.fab.setOnClickListener {
            viewModel.saveArticle(article)
            Snackbar.make(view, "Article saved successfully!", Snackbar.LENGTH_SHORT).show()
        }
        binding.backButton.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("article2", article)
            }
            findNavController().navigate(
                R.id.action_articleFragment_to_breakingNewsFragment2,
                bundle
            )
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("TaiPT", "ArticleFragment::onStart()")
        viewModel = (activity as NewsActivity).viewModel
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("TaiPT", "ArticleFragment::onDestroyView()")
        _binding = null
    }
}