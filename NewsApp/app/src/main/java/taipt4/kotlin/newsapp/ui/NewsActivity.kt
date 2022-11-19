package taipt4.kotlin.newsapp.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import taipt4.kotlin.newsapp.R
import taipt4.kotlin.newsapp.databinding.ActivityNewsBinding
import taipt4.kotlin.newsapp.db.ArticleDatabase
import taipt4.kotlin.newsapp.repository.NewsRepository

class NewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsBinding
    lateinit var viewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TaiPT", "NewsActivity::onCreate()")
        binding = ActivityNewsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val newsRepository = NewsRepository(ArticleDatabase(this))
        val viewModelProviderFactory = NewsViewModelProviderFactory(application, newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)

        val navView: BottomNavigationView = binding.bottomNavigationView
        val navController = findNavController(R.id.newsNavHostFragment)
        navView.setupWithNavController(navController)
    }
}