package com.example.kauppalista

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject

// Luokka, joka toimii yhdessä ApiService luokan kanssa sovelluksen ja
// palvelimen välisessä tiedon siirrossa. Tässä käytetään Retrofit-kirjastoa
// HTTP-pyyntöjen tekemiseen taustapalvelimelle ja Gson-kirjastoa
// JSON-muotoisten vastausten käsittelyyn. Kommunikaatiota sovelluksen ja
// tietokannan välillä en olisi saanut ilman tekoälyä toimimaan.
class ItemRepository {
    // Luodaan ApiService-instanssi
    private val apiService = RetrofitClient.getApiService()

    // Metodi joka hakee kaikki kohteet palvelimelta
    fun getItems(onSuccess: (List<Item>) -> Unit, onError: (String) -> Unit) {
        // Luodaan kutsu kohteet kohteiden hakemiseksi palvelimelta
        val call = apiService.getItems()

        // Suoritetaan kutsu retrofit kijaston toimintoja käyttäen
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                // Käsittellään vastaus
                if (response.isSuccessful) {
                    // Onnistunut vastaus
                    val body = response.body()
                    if (body != null) {
                        // Muuetaan vastaus (body muuttujassa) JSON-stringiksi
                        val jsonString = body.string()

                        // JSON-merkkijono muunnetaan gson-kirjastoa apunakäyttäen listaksi Item-olioita (item-luokan mukaan)
                        val items = Gson().fromJson<List<Item>>(jsonString, object : TypeToken<List<Item>>() {}.type)

                        // Asetetaan kohteet onSuccess-funktion parametreiksi
                        onSuccess(items)
                    } else {
                        // Virhe jos vastaus on tyhjä
                        onError("Haku ei tuottanut tuloksia")
                    }
                } else {
                    // Virhe jos haku ei onnistunut
                    onError("Virhe! Tietojen haku ei onnistunut: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Virhe pyynnön tekemisessä
                onError("Virhe! Tietojen haku ei onnistunut: ${t.message}")
            }
        })
    }

    // Metodi jolla luodaan uusi tuote palvelimelle
    fun createItem(itemName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val call = apiService.createItem(itemName)

        call.enqueue(object : Callback<Item> {
            override fun onResponse(call: Call<Item>, response: Response<Item>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Tuotteen lisääminen epäonnistui: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Item>, t: Throwable) {
                onError("Tuotteen lisääminen epäonnistui: ${t.message}")
            }
        })
    }

    // Metodi jolla poistetaan tuote palvelimelta sen ID:n perusteella
    fun deleteItem(id: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val call = apiService.deleteItem(id)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Tuotteen poistaminen epäonnistui!")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onError(t.message ?: "Virhe!")
            }
        })
    }

    // Metodi jolla päivitetään kohteen tietoja palvelimella
    fun updateItem(id: Int, item: String, version: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {

        // Luodaan "runko" lähetettävistä tiedoista
        val requestBody = JSONObject().apply {
            put("id", id.toString())
            put("tuote", item)
            put("versio", version.toString())
        }.toString().toRequestBody("application/json".toMediaTypeOrNull())

        // Lähetetään luotu "runko"
        apiService.updateItem(requestBody).enqueue(object : Callback<Item> {
            override fun onResponse(call: Call<Item>, response: Response<Item>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Tuotteen muuttaminen epäonnistui!")
                }
            }

            override fun onFailure(call: Call<Item>, t: Throwable) {
                onError(t.message ?: "Muutoksien tallentaminen epäonnistui!")
            }
        })
    }

}





