package taipt4.kotlin.newsapp.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import taipt4.kotlin.newsapp.repository.NewsRepository

class NewsViewModelProviderFactory(
    val app: Application,
    var newsRepository: NewsRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
         return NewsViewModel(app, newsRepository) as T
    }
}