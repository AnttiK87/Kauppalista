package com.example.kauppalista

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

// Aktiviteetti jonka avulla käsitellään käyttäjän määrittämiä yleisimpiä tuotteita.
// Käyttäjä voi itse lisätä ja poistaa yleisimmät tuotteet. Ylisimmät tuotteet lisätään
// fragmentteina ja tallennetaan laitteen muistiin, eikä tietokantaan. Mahdollisesti
// tämä kokonaisuus olisi voinut olla myös fragmentti, mutta itselle luontevammalta
// tuntui tehdä tästä aktiviteetti.
class YleisetActivity : AppCompatActivity(), OnItemDeleteListener {

    // muuttujat
    private lateinit var lisaa: Button
    private lateinit var frgNimi: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yleiset)

        // appbarin back buttonin toiminta
        val appBar: MaterialToolbar = findViewById(R.id.appBar4)
        setSupportActionBar(appBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // alanavigointivalikon toiminnallisuudet
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // aktiivisen tilan näyttö piti tehdä jostain syystä manuaalisesti
        when (this::class.java) {
            YleisetActivity::class.java -> bottomNavView.selectedItemId = R.id.yleinen
        }

        // navigointi toiminnallisuus alavalikolle
        bottomNavView.setOnItemSelectedListener { menuItem ->
            // Handle item selection events
            when (menuItem.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.yleinen -> {
                    true
                }
                else -> false
            }
        }

        // asetetaan ui elementit muuttujiin
        lisaa = findViewById(R.id.btnLisaa)
        frgNimi = findViewById(R.id.txvInputItem)

        //lisää napin kuuntelu ja toiminnallisuus
        lisaa.setOnClickListener {
            // tekstikentän sisältö muuttujaan
            val itemName = frgNimi.text.toString().trim()
            // tarkistetaan, ettei tekstikenttä ole tyhjä
            if (itemName.isNotEmpty()) {
                // fragmentManager fragmenttien hallintaa
                val fragmentManager: FragmentManager = supportFragmentManager
                // fragmentTransaction, jolla hallitaan fragmenttien lisäämistä
                val transaction: FragmentTransaction = fragmentManager.beginTransaction()
                // luodaan uusi YleinenFragmentti-olio
                val fragment = YleinenFragmentti().apply {
                    // annetaan fragmentille "sijainnin/tag" jonka avulla fragmentti tunnistetaan
                    arguments = Bundle().apply {
                        putInt("position", getFragmentCount()) // Aseta sijainti
                    }
                    // asetetaan fragmentille tekstikenttään syötetty tuotteen nimi
                    setData(itemName)
                }
                // lisätään fragmentti flexboxLayout ui elementtiin
                transaction.add(
                    R.id.flexboxLayout,
                    fragment,
                    "fragment${getFragmentCount()}" // Lisätään fragmentille tunniste
                )
                // vahvistetaan fragmentin lisäys
                transaction.commit()
                // kutsutaan metodi jolla tallennetaan fragmentin tiedot
                saveFragmentData(itemName)
                // tyhjennetään tekstikenttä
                frgNimi.text.clear()
            } else {
                // jos tekstikenttä on tyhjä, näytetään toast
                Toast.makeText(this, "Syötä lisättävä tuote.", Toast.LENGTH_SHORT).show()
            }
        }

        // kutsutaan metodia jolla ladataan aiemmin lisätyt tuotteet
        loadFragments()
    }

    // metodi jolla tallennetaan fragmentin tiedot laitteen muistiin
    private fun saveFragmentData(itemName: String) {
        // sharedPreferences-olio, jolla tallennetaan fragmentin tietoja
        val sharedPreferences = getSharedPreferences("FragmentData", Context.MODE_PRIVATE)
        // sharedPreferencesin muokkaus-olio
        val editor = sharedPreferences.edit()
        // fragmenttien lukumäärä
        val count = getFragmentCount()
        // annetaan fragmentille tag, käyttäen lukumäärää yksilöimään olio
        editor.putString("itemName$count", itemName)
        // päivitetään lukumäärä, jotta seuraavalla fragmentilla on uusi tunniste
        editor.putInt("count", count + 1)
        // vahvistetaan tallennus
        editor.apply()
    }

    // metodi jolla haetaan muistissa olevat fragmentit näytille
    private fun loadFragments() {
        // sharedPreferences-olio, josta ladataan fragmenttien tiedot
        val sharedPreferences = getSharedPreferences("FragmentData", Context.MODE_PRIVATE)
        // fragmenttien kokonaismäärä tai oletuksena 0
        val count = sharedPreferences.getInt("count", 0)
        // fragmentManager
        val fragmentManager: FragmentManager = supportFragmentManager
        // fragmentTransaction
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()

        // käydään läpi tallennetut fragmentit
        for (i in 0 until count) {
            // haetaan fragmentin nimi
            val itemName = sharedPreferences.getString("itemName$i", "") ?: ""
            // J´jos fragmentin nimi ei ole tyhjä
            if (itemName.isNotEmpty()) {
                // luodaan uusi ilmentymä YleinenFragmentista
                val fragment = YleinenFragmentti().apply {
                    // lähetetään fragmentille sijainnin
                    arguments = Bundle().apply {
                        putInt("position", i) // Aseta sijainti
                    }
                }
                // asetetaan fragmentille tallennetut tiedot
                fragment.setData(itemName)
                // lisätään fragmentti näkymään
                transaction.add(R.id.flexboxLayout, fragment, "fragment$i")
            }
        }
        // vahvistetaan fragmenttien lisäys
        transaction.commit()
    }


    // metodi jolla poistetaan fragmentti "sijainnin/tagin" mukaisesti
    override fun onDeleteItem(position: Int) {
        // fragmentManager
        val fragmentManager = supportFragmentManager
        // määritetään fragmentin tag
        val tag = "fragment$position"
        // etsitään fragmentti tunnisteen perusteella
        val fragment = fragmentManager.findFragmentByTag(tag)
        // jos fragmentti löytyy
        fragment?.let {
            // FragmentTransaction
            val transaction = fragmentManager.beginTransaction()
            // poista fragmentti
            transaction.remove(it)
            // vahvistetaan fragmentin poisto
            transaction.commit()
        }
        // päivitetään tallennettu tietoa sijainnin/tagin
        updateSavedData(position)
    }


    // metodi jolla päivitetään tieto tallennetusta fragmenteista
    private fun updateSavedData(position: Int) {
        // SharedPreferences-olio, jolla hallitaa tietoja
        val sharedPreferences = getSharedPreferences("FragmentData", Context.MODE_PRIVATE)
        // tallennettujen fragmenttien lukum
        val count = getFragmentCount()
        // tarkistetaan, että sijainti on kelvollinen
        if (position in 0 until count) {
            // sharedPreferences tietojen päivittämiseksi
            val editor = sharedPreferences.edit()
            // poistetaan tallennettu fragmentin nimi
            editor.remove("itemName$position")
            // vähennetään fragmenttien kokonaismäärää
            editor.putInt("count", count - 1)
            // vahvistetaan muutokset
            editor.apply()
        }
    }

    // metodi jolla lasketaan montako fragmenttia on
    private fun getFragmentCount(): Int {
        // SharedPreferences-olio, jolla hallitaa tietoja
        val sharedPreferences = getSharedPreferences("FragmentData", Context.MODE_PRIVATE)
        // lasketaan ja palautetaan kokonaismäärä tai 0
        return sharedPreferences.getInt("count", 0)
    }
}
