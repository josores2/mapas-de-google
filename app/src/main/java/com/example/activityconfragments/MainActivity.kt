package com.example.activityconfragments

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Capturamos el botón
        val botonCambio=findViewById<Button>(R.id.botonCambio)
        val botonGasolineras=findViewById<Button>(R.id.botonGasolineras)

        // Cargamos un fragment en el contenedor de fragments
        val firstFragment = FirstFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, firstFragment)
            .commit()

        //Tenemos un botón para alternar en el contededor entre un fragment u otro
        botonCambio.setOnClickListener {
            val fragment = if (isFirstFragmentVisible()) SecondFragment() else FirstFragment()
            replaceFragment(fragment)
        }

        botonGasolineras.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container,CuartoFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun isFirstFragmentVisible(): Boolean {
        return supportFragmentManager.findFragmentById(R.id.fragment_container) is FirstFragment
    }
}