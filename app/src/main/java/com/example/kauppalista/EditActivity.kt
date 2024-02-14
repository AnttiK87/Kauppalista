package com.example.kauppalista

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

// Luokka jonka avulla näytetään ja aikaansaadaan tuotteiden päivittämisessä
// tarvittavat toiminnot. En tiedä olisiko ollut järkevämpää tehdä toiminto
// fragmentissa. Mutta oman activityn käyttö tuntui minulle luontevammalta.
class EditActivity : AppCompatActivity() {

    // muuttujat
    private lateinit var itemText: String // teksti tuotteelle
    private var itemId: Int = -1 // tuotteen id, oletuksena -1 vertailua varten
    private var itemVersion: Int? = null // tuotteen versio numero
    private val repository = ItemRepository() // objekti itemrepository luokasta. Tietojen palvelimelle lähetystä varten.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        // appbarin back buttonin toiminta
        val appBar: MaterialToolbar = findViewById(R.id.appBar2)
        setSupportActionBar(appBar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Saadaan listItem fragmentin ilmentymällä olevat arvot
        // id:lle, tuotteen nimelle ja versiolle tai asetetaan oletus
        // arvot jos arvo on tyhjä
        itemText = intent.getStringExtra("itemText") ?: ""
        itemId = intent.getIntExtra("itemId", -1)
        itemVersion = intent.getIntExtra("itemVersion", 1)

        // Asetetaan tuotteen nimi tekstikenttään
        val txvInputItem: TextInputEditText = findViewById(R.id.txvInputItem)
        txvInputItem.setText(itemText)

        //Tallenna napin esittely ja kuuntelu
        val btnSave = findViewById<Button>(R.id.btnLisaa)
        btnSave.setOnClickListener {
            // Kutsutaan Tuotteen päivitys metodia ja annetaan sille parametrit.
            // Versio numeroa kasvatetaan yhdellä jotta päivitetty tieto osataan näyttää
            // mainActivityssä
            updateItemInDatabase(itemId, txvInputItem.text.toString(), itemVersion!! +1)
        }
    }

    //Metodi tuotteen tietojen päivittämistä varten
    private fun updateItemInDatabase(itemId: Int, updatedText: String, itemVersion: Int) {
        //Tarkastetaan onko todellinen tietokannan tietue
        if (itemId != -1) {
            // annetaan item repository luokan updateitem metodille parametri lähetettäväksi palvelimelle
            repository.updateItem(itemId, updatedText, itemVersion,
                onSuccess = {
                    // Päivitys onnistui ja näytetään käyttäjälle toast
                    Toast.makeText(this@EditActivity, "Tuote päivitetty", Toast.LENGTH_SHORT).show()
                    // Palataan takaisin MainActivityyn
                    val intent = Intent(this@EditActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            ) { error ->
                // Virhetilanteessa näytetään item repositoryn ehtojen mukainen virhe ilmoitus toast kentässä
                Toast.makeText(this@EditActivity, error, Toast.LENGTH_SHORT).show()
            }
        } else {
            // Käsitellään mahdollinen ristiriita tuotteen id:n suhteen
            Toast.makeText(this@EditActivity, "Tuotteen ID on virheellinen.", Toast.LENGTH_SHORT).show()
        }
    }
}