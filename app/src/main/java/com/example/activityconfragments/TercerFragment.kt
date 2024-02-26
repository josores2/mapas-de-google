package com.example.activityconfragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse


class TercerFragment : Fragment(), OnMapReadyCallback,OnCompleteListener<FindCurrentPlaceResponse>, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener{
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    companion object{
        const val REQUEST_CODE_LOCATION = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tercer, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crearFragment()
        buscaGasolineras()
    }

    private fun crearFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapa) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googlemap: GoogleMap) {
        map=googlemap

        /*Nos subscribimos al método del botón de ubicación, desde esta clase, porque ya lo tenemos implementado.
        Si no nos subscribimos desde este fragment, no nos hace caso*/
        map.setOnMyLocationButtonClickListener(this)
        /*Nos subscribimos a este listener, para que el pulsar sobre nuestra ubicación, haga algo, en este caso,
        mostrar un msg que diga las coordenadas de donde estamos*/
        map.setOnMyLocationClickListener(this)


        activarLocalizacion()
        // Inicializar la API de Places
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(),"@string/google_maps_key")
        }

    }

    private fun buscaGasolineras() {
        // 1. Obtener la ubicación actual del usuario
        // (Puedes usar FusedLocationProviderClient o LocationManager)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        fusedLocationClient.lastLocation.addOnSuccessListener { location:Location? ->
        if (location == null)
            Toast.makeText(requireContext(),"Localizacion NULL!!", Toast.LENGTH_SHORT).show()
        else{
            // 2. Buscar gasolineras cercanas usando la API de Places
            val placesClient = Places.createClient(requireContext())
            val request = FindCurrentPlaceRequest.newInstance(listOf(Place.Field.NAME, Place.Field.LAT_LNG))
            val placeResponse = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val likelyPlaces = task.result
                    val results = FloatArray(5)
                    for (placeLikelihood in likelyPlaces?.placeLikelihoods ?: emptyList()) {
                        // 3. Filtrar gasolineras cercanas
                        if (placeLikelihood.place.types.contains(Place.Type.GAS_STATION)) {
                            // 4. Calcular la distancia entre la ubicación actual y la gasolinera
                            Location.distanceBetween(
                                location.latitude, location.longitude,
                                placeLikelihood.place.latLng?.latitude ?: 0.0,
                                placeLikelihood.place.latLng?.longitude ?: 0.0,
                                results
                            )
                            val distance = results[0]
                            // 5. Mostrar la gasolinera en el mapa si está dentro del radio de 10 km
                            if (distance <= 10000) { // 10 km en metros
                                map.addMarker(MarkerOptions().position(placeLikelihood.place.latLng!!).title(placeLikelihood.place.name))
                            }
                        }
                    }
                }
            }
        }}
            .addOnFailureListener { exception ->
                // Manejar cualquier error aquí
                Log.e("Location", "Error al obtener la ubicación: $exception")
            }
    }
    /*Esta funcion comprueba si el permiso de localización está activada o no*/
    private fun localizacionPermitida():Boolean{
        /*Comprobamos si está el permiso concedido o no. Pasamos requireContext en vez de this,
        ya que estamos dentro de un fragment y necesitamos el contexto*/
        return ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    /**/
    private fun activarLocalizacion(){
        /*si el mapa no ha sido inicializado, salimos, ya que aun no podemos pedir nada*/
        if(!::map.isInitialized) return
        if(localizacionPermitida()){
            /*el usuario ha permitido la localizacion*/
            map.isMyLocationEnabled = true
        }else{
            /*El usuario no ha permitido la localizacion, entonces se lo pedimos*/
            pedirLocalizacion()
        }
    }
    private fun pedirLocalizacion(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
            /*Se pidió el permiso, pero el usuario no lo aceptó*/
            Toast.makeText(requireContext(),"Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        }else{
            /*Es la primera vez que se piden los permisos. Le pasamos el ultimo argumento, con una constante que hemos creado como
            * companion object de esta clase, para almacenar si el usuario acepta o no el permiso*/
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                SecondFragment.REQUEST_CODE_LOCATION
            )
        }

    }

    /*En esta funcion tenemos que capturar la respuesta del usuario que acepta los permisos.
    * La buscamos en Code*/
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        /*Aqui implementamos la lógica para ver si el permiso ha sido aceptado*/
        when(requestCode){
            SecondFragment.REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                map.isMyLocationEnabled=true
            }else{
                /*El usuario ha rechazado el permiso*/
                Toast.makeText(requireContext(),"Para activar la ubicación, ve a ajustes y acepta el permiso", Toast.LENGTH_SHORT).show()
            }
            /*Si se hubiera pasado otro permiso que no fuera el de ubicacion, que no va a pasar*/
            else -> {}
        }
    }

    /*Tengo que crear esta función por si el usuario ha minimizado nuestra app y ha revocado los permisos*/
    override fun onResume() {
        super.onResume()
        /*Primero comprobamos si el mapa está cargado*/
        if(!::map.isInitialized) return
        if(!localizacionPermitida()){
            map.isMyLocationEnabled = false
            Toast.makeText(requireContext(),"Para activar la ubicación, ve a ajustes y acepta el permiso", Toast.LENGTH_SHORT).show()

        }
    }
    /*Esta funcion es para que el botón de ubicación me lleve a mi ubicación o no. Si devuelve false, me lleva*/
    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(requireContext(),"Botón de llevar a ubicación pulsado", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(requireContext(),"Estás en ${p0.latitude}, ${p0.longitude}", Toast.LENGTH_SHORT).show()
    }

    override fun onComplete(p0: Task<FindCurrentPlaceResponse>) {

    }

}