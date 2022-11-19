package taipt4.kotlin.newsapp.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import taipt4.kotlin.newsapp.util.Constants.Companion.BASE_URL

class RetrofitInstance {
    companion object {
        private val retrofit by lazy {
            var logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            var client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        val api by lazy {
            retrofit.create(NewsAPI::class.java)
        }
    }
}