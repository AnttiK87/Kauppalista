package com.example.kauppalista

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Olio, joka ottaa retrofit:n käyttöön. Yhteys ja tiedonsiirto
// palvelimen ja applikaation välillä ei olisi onnistunut ilman tekoälyä.
// Yhteenvetona tämä koodin avulla luodaan Retrofit-klientin, joka
// käyttää määriteltyä osoitetta ja lisäksi luokka tallentaa lokiin kaikki
// verkkoviestit. Lisäksi se käyttää Gson-kirjastoa JSON-vastausten käsittelyyn.
object RetrofitClient {

    // Määritetään osoite, jonka kautta saadaan yhteys palvelimeen
    private const val BASE_URL = "https://akphotography.fi/ostoslista/"

    // Metodi jolla saadaan ApiService-rajapinnan toteuttavan olion
    fun getApiService(): ApiService {

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Logataan kaikki verkkoviestit
        }

        // Luodaan OkHttpClient, joka sisältää määritetyn lokitusinterceptorin
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        // Luodaan ja konfiguroidaan Retrofit-klientti
        return Retrofit.Builder()
            .baseUrl(BASE_URL) // Asetetaan perusosoite
            .client(client) // Asetetaan OkHttpClient-klientti
            .addConverterFactory(GsonConverterFactory.create()) // JSON-muunnos Gson-kirjastolla
            .build()
            .create(ApiService::class.java) // Luodaan ApiService-olio
    }
}
