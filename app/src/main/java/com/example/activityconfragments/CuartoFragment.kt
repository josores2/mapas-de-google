package com.example.activityconfragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.Polyline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class CuartoFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private lateinit var map: GoogleMap
    private var start:String = ""
    private var end:String = ""
    var poly : Polyline? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_cuarto, container, false)

        /*Llamamos a crearFragment8) mejor en onViewCreated, así nos aseguramos que la vista está
        inflada, antes de hacer nada*/
        //crearFragment()


        // Inflate the layout for this fragment
        return root
    }

    /*Menos tenido que modificar este código porque al asignar mapFragment, la actividad no tiene porque estar creada,
    así que daba null. Se soluciona invocando a onViewCreated, o en cualquier caso,
    crear una variable que infle la vista, antes del return*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val botonLimpiar = requireActivity().findViewById<Button>(R.id.botonLimpiar)

        /*Este boton me limpiará las variables start y end de la ruta y la ruta en sí misma*/
        botonLimpiar.setOnClickListener {
            start = ""
            end = ""
            /*De esta forma limpiamos el mapa si hubiera alguna ruta ya pintada*/
            poly?.remove()
            Toast.makeText(requireContext(),"Selecciona ORIGEN y DESTINO",Toast.LENGTH_SHORT).show()
            poly = null
        }
        crearFragment()
    }

    private fun crearFragment() {
            val mapFragment = childFragmentManager.findFragmentById(R.id.mapa) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }

    override fun onMapReady(googlemap: GoogleMap) {
        map=googlemap
        /*Nos subscribimos al listener del click en el mapa, para que capture nuestros clicks*/
        map.setOnMapClickListener(this)
       /*Hacemos un zoom para que nos sea más fácil calcular una ruta*/
        val coordenadas = LatLng(39.4860415,-0.4044917)
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(coordenadas,18f),
            4000,
            null)
    }
    /*Con esta función recogemos el objeto Retrofit. Este objeto sirve para hacer llamadas a servicios Web
    y recoger valores. Lo usaremos para recoger la ruta de la web de OpenRoute*/
    fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    /*Ahora necesitamos crear una API Service, que será la interfaz que llame al método GET de ese endpoint
    La hemos creado como interfaz (Kotlin Class), ServicioApi*/
    fun crearRuta(){
        Log.i("PEPE CREAR RUTA START: ",start)
        Log.i("PEPE CREAR RUTA END: ",end)
        /*Las llamadas a internet no se pueden hacer en el hilo principal, hay que hacerlo en corutinas
        para ello ejecutamos CoroutineScope(Dispatcher)  */

        CoroutineScope(Dispatchers.IO).launch {
            /*Lanzamos el hilo a través de la interfaz que hemos creado y la func conseguirRuta de la misma*/
            val llamada = getRetrofit().create(ServicioApi::class.java).conseguirRuta("5b3ce3597851110001cf62482f4f1876a2644f81ac0aaf6d860f35de",start,end)
            /*getRetrofit() puede devolver codigos de error si nos hemos equivocado al construir la url, si la web no está
            disponible, etc. En ese caso, no entraría en el siguiente if:*/
            if(llamada.isSuccessful){
                /*Si la llamada ha recogido la ruta, ya la tengo en esta variable*/
                /*Necesitamos permiso de internet en el manifest, para poder acceder a la web de rutas*/
                Log.i("PEPE TENGO RUTA:", "OK")
                dibujarRuta(llamada.body())
            }else{
                Log.i("PEPE TENGO RUTA:","NO")
            }
        }
    }

    private fun dibujarRuta(respuestaDeRuta: RespuestaDeRuta?) {
        /*Rellenamos el poliline con los datos que nos ha devuelto el servicio*/
        val polilineOptions = PolylineOptions()


        respuestaDeRuta?.features?.first()?.geometry?.coordinates?.forEach{
            /*para cada linea de la listaagregamos a la variable polilineOptions, el valor capturado(lati,longit)*/
            polilineOptions.add(LatLng(it[1],it[0]))
            /*Ponemos 1 primero, pq la api devuelve primero la latitud y no la longitud*/
        }
        activity?.runOnUiThread{
            /*Ejecutamos la acción de dibujado en la corutina*/
            poly = map.addPolyline(polilineOptions)
        }
    }

    override fun onMapClick(p0: LatLng) {
        if(!::map.isInitialized) {}
        else {
            /*Comentamos este listener, ya que el primer click no lo capturaba,
            * es decir, con el onMapClick bastaba*/
               /* map.setOnMapClickListener {*/
                    /*Con esto capturaremos el listener de cada click*/
                    if (start.isEmpty()) {
                        /*significa que estamos en el primer click en el mapa*/
                        /*necesitamos el siguiente formado de coordenada: long,lati en string*/
                        //start = "${it.longitude},${it.latitude}"
                        start = "${p0.longitude},${p0.latitude}"
                    } else if (end.isEmpty()) {
                        //end = "${it.longitude},${it.latitude}"
                        end = "${p0.longitude},${p0.latitude}"
                        crearRuta()
                   } /*else {*/
                        /*Si entra aqui será la tercera vez que se clica en el mapa*/

                    /*}*/
        }
    }
}