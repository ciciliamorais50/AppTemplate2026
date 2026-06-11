package com.ifpr.androidapptemplate.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.ui.cadastro.ClassesMaterial
import com.ifpr.androidapptemplate.ui.cadastro.ListaMaterialActivity
import com.ifpr.androidapptemplate.ui.cadastro.LocationHolder
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var currentAddressTextView: TextView
    private lateinit var tvStatusGps: TextView
    private var ultimaLocalizacao: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        currentAddressTextView = view.findViewById(R.id.currentAddressTextView)
        tvStatusGps = view.findViewById(R.id.tvStatusGps)

        montarCardsClasses(view)

        val fab = view.findViewById<FloatingActionButton>(R.id.fab_ai)
        fab.setOnClickListener {
            val intent = Intent(view.context, com.ifpr.androidapptemplate.ui.ai.AiLogicActivity::class.java)
            view.context.startActivity(intent)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        iniciarLocalizacao()

        return view
    }

    private fun montarCardsClasses(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.containerClasses)
        val inflater = LayoutInflater.from(requireContext())

        for (classe in ClassesMaterial.classes) {
            val card = inflater.inflate(R.layout.item_classe_home, container, false)

            card.findViewById<TextView>(R.id.tvIconeClasse).text = classe.icone
            card.findViewById<TextView>(R.id.tvNomeClasse).text = classe.nome
            card.findViewById<TextView>(R.id.tvQtdCampos).text =
                "${classe.campos.size} campos técnicos"

            card.setOnClickListener {
                val intent = Intent(requireContext(), ListaMaterialActivity::class.java)
                intent.putExtra("classe_id", classe.id)
                intent.putExtra("classe_nome", classe.nome)
                startActivity(intent)
            }

            container.addView(card)
        }
    }

    private fun iniciarLocalizacao() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000L
        ).setMinUpdateIntervalMillis(2000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                ultimaLocalizacao = location
                LocationHolder.latitude = location.latitude
                LocationHolder.longitude = location.longitude
                obterEndereco(location.latitude, location.longitude)
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun obterEndereco(lat: Double, lng: Double) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val enderecos = geocoder.getFromLocation(lat, lng, 1)
            if (!enderecos.isNullOrEmpty()) {
                val e = enderecos[0]
                val endereco = "${e.thoroughfare ?: ""}, ${e.locality ?: ""} - ${e.adminArea ?: ""}"
                currentAddressTextView.text = "📍 $endereco"
                LocationHolder.endereco = endereco
                tvStatusGps.text = "GPS ✓"
                tvStatusGps.setTextColor(0xFF22c55e.toInt())
            } else {
                currentAddressTextView.text = "📍 Endereço não encontrado"
            }
        } catch (e: Exception) {
            currentAddressTextView.text = "📍 Erro ao obter endereço"
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            iniciarLocalizacao()
        else
            currentAddressTextView.text = "📍 Permissão negada"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::locationCallback.isInitialized)
            fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}