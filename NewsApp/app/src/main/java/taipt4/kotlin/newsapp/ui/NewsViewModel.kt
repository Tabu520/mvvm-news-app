package taipt4.kotlin.newsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import taipt4.kotlin.newsapp.NewsApplication
import taipt4.kotlin.newsapp.models.Article
import taipt4.kotlin.newsapp.models.NewsResponse
import taipt4.kotlin.newsapp.repository.NewsRepository
import taipt4.kotlin.newsapp.util.Resource
import java.io.IOException

class NewsViewModel(
    val app: Application,
    val newsRepository: NewsRepository
) : AndroidViewModel(app) {

    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewsResponse: NewsResponse? = null
    var breakingNewsPage = 1

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsResponse: NewsResponse? = null
    var searchNewsPage = 1


    init {
        Log.d("TaiPT", "NewsViewModel::init()")
        getBreakingNews("us")
    }

    fun getBreakingNews(countryCode: String) = viewModelScope.launch {
        Log.d("TaiPT", "NewsViewModel::getBreakingNews()")
        safeBreakingNewsCall(countryCode)
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        Log.d("TaiPT", "NewsViewModel::searchNews()")
        safeSearchNewsCall(searchQuery)
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        Log.d("TaiPT", "NewsViewModel::handleBreakingNewsResponse()")
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                breakingNewsPage++
                if (breakingNewsResponse == null) {
                    breakingNewsResponse = resultResponse
                } else {
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(breakingNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        Log.d("TaiPT", "NewsViewModel::handleSearchNewsResponse()")
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse = resultResponse
                } else {
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun saveArticle(article: Article) = viewModelScope.launch {
        Log.d("TaiPT", "NewsViewModel::handleSearchNewsResponse()")
        newsRepository.upsert(article)
    }

    fun getSavedNews() = newsRepository.getSavedNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        Log.d("TaiPT", "NewsViewModel::deleteArticle()")
        newsRepository.deleteArticle(article)
    }

    private suspend fun safeBreakingNewsCall(countryCode: String) {
        Log.d("TaiPT", "NewsViewModel::safeBreakingNewsCall()")
        breakingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getBreakingNews(countryCode, breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            } else{
                breakingNews.postValue(Resource.Error("No internet connection!"))
            }
        } catch (t: Throwable) {
            when(t) {
                is IOException -> breakingNews.postValue(Resource.Error("Network Failure"))
                else -> breakingNews.postValue(Resource.Error("Conversion Error!"))
            }
        }
    }

    private suspend fun safeSearchNewsCall(searchQuery: String) {
        Log.d("TaiPT", "NewsViewModel::safeSearchNewsCall()")
        searchNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.searchNews(searchQuery, searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            } else{
                searchNews.postValue(Resource.Error("No internet connection!"))
            }
        } catch (t: Throwable) {
            when(t) {
                is IOException -> searchNews.postValue(Resource.Error("Network Failure"))
                else -> searchNews.postValue(Resource.Error("Conversion Error!"))
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        Log.d("TaiPT", "NewsViewModel::hasInternetConnection()")
        val connectivityManager =
            getApplication<NewsApplication>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(TRANSPORT_WIFI) -> true
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

}