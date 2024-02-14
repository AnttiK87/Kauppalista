package com.example.kauppalista

// kuuntelijametodi kohteen poistoon liittye. Metodille annetaan parametrina
// sijainti/tag, jolla yhditetään toiminto ja poistettava kohde. Liittyy
// YleisetActivityssä näytettävien fragmenttejen poistoon.


interface OnItemDeleteListener {
    fun onDeleteItem(position: Int)
}