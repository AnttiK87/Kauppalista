package com.example.kauppalista

import okhttp3.RequestBody
import okhttp3.ResponseBody

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

// API-rajapinta, jonka eli tämän avulla palvelimella olevat php-
// tiedostot ja sovellus "kommunikoivat" keskenään.
interface ApiService {

    // GET-pyyntö hakee kaikki kohteet palvelimella olevasta tietokannasta
    // get_all.php tiedoston tehdessä varsinaisen tietokanta kyselyn
    @GET("get_all.php")
    fun getItems(): Call<ResponseBody>

    // POST-pyyntö lisää uuden kohteen palvelimelle
    @FormUrlEncoded //Muotoilee lähetettävää tietoa
    @POST("add_item.php")
    fun createItem(@Field("tuote") item: String): Call<Item>

    // DELETE-pyyntö poistaa kohteen palvelimelta sen ID:n perusteella
    @DELETE("delete_item.php")
    fun deleteItem(@Query("del_id") id: Int): Call<Void>

    // PUT-pyyntö päivittää olemassa olevan kohteen palvelimella
    @PUT("update_item.php")
    fun updateItem(@Body requestBody: RequestBody): Call<Item>

}