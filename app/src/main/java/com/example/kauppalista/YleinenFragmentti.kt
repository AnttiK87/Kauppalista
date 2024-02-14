package com.example.kauppalista

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

// Fragmentti jonka ilmentymiä näytetään yleiset aktiviteetissä
// fragmentin toiminnallisuudet klikkaamalla tuotelisätään tietokantaan/ostoslistaan
// Pitkällä painalluksella avataan alertdialog jonka avulla fragmentin voi poistaa
class YleinenFragmentti : Fragment() {

    // muuttujat
    private var yleinenTeksti: String? = null
    private lateinit var deleteListener: OnItemDeleteListener
    private val repository = ItemRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //asetetaan yleiset activiteetista saatu teksti framentin ui komponenttiin
        yleinenTeksti?.let { view.findViewById<Button>(R.id.btnYleinen)?.text = it }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Luodaan näkymä fragmentin layoutista
        val view = inflater.inflate(R.layout.fragment_yleinen_fragmentti, container, false)
        // asetetaan ui elementti muuttujaan
        val deleteButton = view.findViewById<Button>(R.id.btnYleinen)
        // haetaan fragmentille asetettu sijainti/tag
        val position = requireArguments().getInt("position")

        // kuunnellaan napin painallusta
        deleteButton.setOnClickListener {
            // haetaan napissa oleva teksti muutujaan
            val buttonText = deleteButton.text.toString()
            // tarkistetaan, ettei teksti ole tyhjä
            if (buttonText.isNotEmpty()) {
                // annetaan item repository luokan createItem metodille arvo lähetettäväksi palvelimelle
                repository.createItem(
                    buttonText,
                    onSuccess = {
                        // muutetaan nappi näkymättämäksi kun sen sisältämä tuote on lisätty listaan/tk:hon
                        deleteButton.visibility = View.GONE
                    },
                    onError = { error ->
                        // Virhetilanteessa näytetään item repositoryn ehtojen mukainen virhe ilmoitus toast kentässä
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // kuunnellaan napin pitkää painallusta varten
        deleteButton.setOnLongClickListener {
            // Näytetään alertdialogi
            showDeleteConfirmationDialog(position)
            // true, jotta normaali klikkauskuuntelija ei käynnisty
            true
        }

        // Palautetaan luotu näkymä
        return view
    }


    //Metodi alertdialogin näytölle ja fragmentin poistolle
    private fun showDeleteConfirmationDialog(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Poista tuote?")
            .setMessage("Haluatko varmasti poistaa tämän tuotteen?")
            .setPositiveButton("Poista") { _, _ ->
                // kutsutaan yleiset activityssä olevaa poisto metodia
                deleteListener.onDeleteItem(position)
            }
            .setNegativeButton("Peruuta", null)
            .show()
    }

    // asetetaan yleiset activityssä annettu teksti muuttujaan
    fun setData(text: String) {
        this.yleinenTeksti = text
    }

    override fun onAttach(context: Context) {
        // Kutsutaan onAttach-metodia
        super.onAttach(context)
        // Tarkistetaan, että OnItemDeleteListener-rajapinta toteutuu
        if (context is OnItemDeleteListener) {
            //asetetaan deleteListener-liitäntä
            deleteListener = context
        } else {
            // muutoin näytetään virheilmoitus
            throw RuntimeException("$context ei voi keskustella on poiston kuuntelijan kanssa")
        }
    }
}
