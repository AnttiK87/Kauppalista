package com.example.kauppalista


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentTransaction

import com.google.android.material.bottomnavigation.BottomNavigationView

// Pääaktiviteetti jossa näytään ostoslistan sisältö,
// lisätään uusia tuotteita ja navikoidaan näkymien välillä
class MainActivity : AppCompatActivity() {

    // muuttujat
    private val repository = ItemRepository() // objekti itemrepository luokasta. Tietojen palvelimelle lähetystä ja hakemistavarten.
    private lateinit var lisaa: Button
    private lateinit var tuote: EditText

    // muuttujat tietojen jatkuvaa päivittämistä varten
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchPolling: Switch
    private val handler = Handler()
    private var pollingEnabled = false

    //määritetään väli jolla tiedot haetaan
    companion object {
        private const val POLLING_INTERVAL_MS = 1000 // 1 sekuntti
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //näytetään kustomoitu alkunäkymä "splashscreen"
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // alanavigointivalikon toiminnallisuudet
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // aktiivisen tilan näyttö piti tehdä jostain syystä manuaalisesti
        when (this::class.java) {
            MainActivity::class.java -> bottomNavView.selectedItemId = R.id.home
        }

        // navigointi toiminnallisuus alavalikolle
        bottomNavView.setOnItemSelectedListener { menuItem ->
            // Handle item selection events
            when (menuItem.itemId) {
                R.id.home -> {
                    true
                }
                R.id.yleinen -> {
                    startActivity(Intent(this, YleisetActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // asetetaan ui elementit muuttujiin
        lisaa = findViewById(R.id.btnLisaa)
        tuote = findViewById(R.id.txvInputItem)
        switchPolling = findViewById(R.id.swcUpdate)

        // aktivoidaan tai aotetaan pois päältä tietojen päivittäminen
        switchPolling.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startPolling()
            } else {
                stopPolling()
            }
        }

        //lisää napin kuuntelu ja toiminnallisuus
        lisaa.setOnClickListener {
            // heataan muuttujaan tekstikenttään syötetty teksti
            val itemName = tuote.text.toString().trim()
            // tarkastetaan onko onko teksti syötetty
            if (itemName.isNotEmpty()) {
                // annetaan item repository luokan createItem metodille arvo lähetettäväksi palvelimelle
                repository.createItem(itemName,
                    onSuccess = {
                        // jos lähetys onnistuu tyhjennetään teksti kenttä
                        tuote.text.clear()
                        // päivitetään näytettävät tuotteet
                        getItems()
                    },
                    onError = { error ->
                        // Virhetilanteessa näytetään item repositoryn ehtojen mukainen virhe ilmoitus toast kentässä
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(this, "Syötä lisättävä tuote.", Toast.LENGTH_SHORT).show()
            }
        }

        //Kutsutaan metodia tuotteiden näyttämistä varten
        getItems()
    }

    // Metodi tuotteiden hakemiseksi ja näyttämiseksi
    private fun getItems() {
        //kutsutaan itemrepository luokan getItem metodia tietojen hakemiseksi tietokannasta
        repository.getItems(
            onSuccess = { items ->
                // kutsutaan displayItems metodia ja annetaan sille parametri
                displayItems(items)
            },
            onError = { error ->
                // Virhetilanteessa näytetään item repositoryn ehtojen mukainen virhe ilmoitus toast kentässä
                Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // metodi fragmenttien/tietokannassa olevien tuotteiden näyttämiselle
    // Ajatuksena on, että kaikkia fragmentteja ei ladata aian uudelleen
    // vaan tarkistetaan onko tuote/id jo näytillä jos ei ole niin tehdään
    // uusi fragmentti. Tämäkin logiikka olisi jäänyt tekemättä ilman tekoälyä
    private fun displayItems(items: List<Item>) {
        // hae olemassa olevat fragmentit ja niiden ID:t
        val existingFragments = supportFragmentManager.fragments
        val existingItemIds = mutableSetOf<Int>()

        for (fragment in existingFragments) {
            // Tarkista, onko fragmentti ListItem-tyyppinen
            if (fragment is ListItem) {
                // Hae fragmentin itemId
                val itemId = fragment.arguments?.getInt("itemId")
                // Jos itemId on olemassa, lisää se existingItemIds-joukkoon
                itemId?.let { existingItemIds.add(it) }
            }
        }

        // Alusta FragmentTransaction, joka mahdollistaa fragmenttien lisäämisen tai päivittämisen
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()

        // Käy läpi kaikki items-listassa olevat Item-oliot
        for (item in items) {
            // Jos itemin id:tä ei löydy existingItemIds-joukosta, lisää uusi fragmentti
            if (!existingItemIds.contains(item.id)) {
                val fragment = ListItem.newInstance(item)
                transaction.add(R.id.fragment_container, fragment)
                existingItemIds.add(item.id ?: -1)
            } else {
                // Jos item on olemassa, päivitä sen tiedot, jos versio on muuttunut
                val existingFragment = existingFragments.firstOrNull { fragment ->
                    fragment is ListItem && fragment.arguments?.getInt("itemId") == item.id
                } as? ListItem

                existingFragment?.let {
                    // Vertaa versioita
                    if (item.version != existingFragment.itemVersion) {
                        existingFragment.updateItemData(item)
                    }
                }
            }
        }

        // Poista fragmentit, jotka eivät enää ole items-listassa
        existingItemIds.forEach { itemId ->
            if (!items.any { it.id == itemId }) {
                val fragmentToRemove = existingFragments.firstOrNull { fragment ->
                    if (fragment is ListItem) {
                        fragment.arguments?.getInt("itemId") == itemId
                    } else false
                }
                fragmentToRemove?.let { transaction.remove(it) }
            }
        }

        // Käynnistä FragmentTransaction
        transaction.commitAllowingStateLoss()
    }

    // Metodi, joka suorittaa pollauksen, eli
    // heataan tiedot tietokannasta tietyllä aikavälillä.
    // Websockettia olisi huutanut liveviewn aikaansaamiseksi,
    // mutta siihen ei nyt taidot riittäneet.
    private val pollingRunnable = object : Runnable {
        override fun run() {
            // Tarkistetaan, onko pollaus sallittu
            if (pollingEnabled) {
                // Hae kohteet palvelimelta
                getItems()
                // Aseta uusi pollaus ajastin
                handler.postDelayed(this, POLLING_INTERVAL_MS.toLong())
            }
        }
    }

    // Käynnistä pollaus
    private fun startPolling() {
        pollingEnabled = true
        handler.postDelayed(pollingRunnable, POLLING_INTERVAL_MS.toLong())
    }

    // Pysäytä pollaus
    private fun stopPolling() {
        pollingEnabled = false
        handler.removeCallbacks(pollingRunnable)
    }

}
