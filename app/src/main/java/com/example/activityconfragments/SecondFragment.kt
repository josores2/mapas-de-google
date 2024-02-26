package com.example.activityconfragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class SecondFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener{

    private lateinit var map: GoogleMap


    companion object{
        const val REQUEST_CODE_LOCATION = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_second, container, false)

        return inflater.inflate(R.layout.fragment_second, container, false)
    }

/*Menos tenido que modificar este código porque al asignar mapFragment, la actividad no tiene porque estar creada,
así que daba null. Se soluciona invocando a onActivityCreated, o en cualquier caso,
crear una variable que infle la vista, antes del return*/

   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crearFragment()
   }

    private fun crearFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapa) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googlemap: GoogleMap) {
        map=googlemap

        crearMarcador()
        /*Nos subscribimos al método del botón de ubicación, desde esta clase, porque ya lo tenemos implementado.
        Si no nos subscribimos desde este fragment, no nos hace caso*/
        map.setOnMyLocationButtonClickListener(this)
        /*Nos subscribimos a este listener, para que el pulsar sobre nuestra ubicación, haga algo, en este caso,
        mostrar un msg que diga las coordenadas de donde estamos*/
        map.setOnMyLocationClickListener(this)
        activarLocalizacion()
    }

    private fun crearMarcador() {
        val coordenadas = LatLng(39.4860415,-0.4044917)
        val marcador = MarkerOptions().position(coordenadas).title("Estoy Aqui")
        map.addMarker(marcador)

        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(coordenadas,18f),
            4000,
            null)
    }

    /*Esta funcion comprueba si el permiso de localización está activada o no*/
    private fun localizacionPermitida():Boolean{
        /*Comprobamos si está el permiso concedido o no. Pasamos requireContext en vez de this,
        ya que estamos dentro de un fragment y necesitamos el contexto*/

        return ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
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
        if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.ACCESS_FINE_LOCATION)){
            /*Se pidió el permiso, pero el usuario no lo aceptó*/
            Toast.makeText(requireContext(),"Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        }else{
            /*Es la primera vez que se piden los permisos. Le pasamos el ultimo argumento, con una constante que hemos creado como
            * companion object de esta clase, para almacenar si el usuario acepta o no el permiso*/
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION)
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
            REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
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
}