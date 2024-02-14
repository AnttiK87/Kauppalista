package com.example.kauppalista

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

// Fragmentti luokka teitokannan sisällön näyttämistä varten
// Tietokannan jokaiselle tietueelle tehdään oma ilmentymä
// Tästä fragmentista.
class ListItem : Fragment() {

    //muuttujat
    private val repository = ItemRepository() // objekti itemrepository luokasta. Tietojen palvelimelle lähetystä varten.
    private lateinit var itemText: String // teksti tuotteelle
    private var itemId: Int = -1 // tuotteen id, oletuksena -1 vertailua varten
    var itemVersion: Int = -1 // tuotteen versio numero

    //Tätä käytetään apuna fragmenttien luomiseen parametrien kanssa.
    companion object {
        fun newInstance(item: Item): ListItem {
            val fragment = ListItem()
            val args = Bundle().apply {
                putString("itemText", item.item)
                putInt("itemId", item.id ?: -1)
                putInt("itemVersion", item.version ?: 1) // Add version to the arguments bundle
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list_item, container, false)

        // Asetetaan companion objectin avulla saadut tuotteen arvot muuttujiin
        itemText = requireArguments().getString("itemText", "")
        itemId = requireArguments().getInt("itemId", -1)
        itemVersion = requireArguments().getInt("itemVersion", 1)

        // Asetetaan tuotteen nimi fragmentin ui:n tekstikenttään
        view.findViewById<TextView>(R.id.txvItem).text = itemText

        // Kuunnellaan fragmentin klikkausta
        view.setOnClickListener {
            //kutsutaan metodia editActivityyn siirtymistä varten
            navigateToNewActivity()
        }

        // Kuunnellaan poista napin klikkausta
        view.findViewById<Button>(R.id.button_delete).setOnClickListener {
            //kutsutaan metodia deleteItem fragmentin ja tietokannan tietueen poistamiseen liittyen
            deleteItem()
        }
        //näytetään fragmentti käyttäjälle
        return view
    }

    // metodi fragmentin ja tietokannan tietueen poistamiseen liittyen
    private fun deleteItem() {
            //Tarkastetaan onko todellinen tietokannan tietue
            if (itemId != -1) {
                // annetaan item repository luokan deleteItem metodille id tietueen poistamista varten
                repository.deleteItem(itemId,
                    onSuccess = {
                        // Tietueen poisto onnistui
                        Toast.makeText(requireContext(), "Tuote poistettu", Toast.LENGTH_SHORT).show()
                        // Kutsutaan removeFragment metodia.
                        removeFragment(this)
                    },
                    onError = { error ->
                        // Virhetilanteessa näytetään item repositoryn ehtojen mukainen virhe ilmoitus toast kentässä
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                // Käsitellään mahdollinen ristiriita tuotteen id:n suhteen
                Toast.makeText(requireContext(), "Tuotteen ID on virheellinen.", Toast.LENGTH_SHORT).show()
            }
    }

    //Metodi fragmentin poistamiseksi
    private fun removeFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.remove(fragment)
        transaction.commit()
    }

    //Metodi jonka avulla navigoidaan aktiviteettiin EditActivity ja välitetään
    // tuetteen tiedot aktiviteetille
    private fun navigateToNewActivity() {
        val intent = Intent(requireContext(), EditActivity::class.java)
        intent.putExtra("itemText", itemText)
        intent.putExtra("itemId", itemId)
        intent.putExtra("itemVersion", itemVersion)// Pass the text as an extra
        startActivity(intent)
    }

    // Metodi jolla päivitetään tekstikenttä päivitetyillä tiedoilla
    fun updateItemData(item: Item) {
        view?.findViewById<TextView>(R.id.txvItem)?.text = item.item
    }
}