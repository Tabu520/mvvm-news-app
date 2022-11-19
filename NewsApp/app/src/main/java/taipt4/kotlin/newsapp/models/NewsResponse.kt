package taipt4.kotlin.newsapp.models

data class NewsResponse(
    var articles: MutableList<Article>,
    var status: String,
    var totalResults: Int
)