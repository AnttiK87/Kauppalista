package com.example.kauppalista

import com.google.gson.annotations.SerializedName

// Data luokka jolla määritetään tietokannasta tulevien
// ja sinne lähetettävien tietueiden rakenne.
data class Item(
    @SerializedName("id")
    val id: Int?, // tietueen yksilöllinen id

    @SerializedName("tuote")
    val item: String, // tietueen/tuotteen nimi

    @SerializedName("versio")
    val version: Int?, // tietueen versio. Tätä tarvitaan, että päivitetty tuote näytetään oikein
)