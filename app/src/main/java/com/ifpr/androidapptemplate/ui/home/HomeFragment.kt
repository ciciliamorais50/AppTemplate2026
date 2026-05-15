package com.ifpr.androidapptemplate.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.ui.ai.AiLogicActivity
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var currentAddressTextView: TextView
    private lateinit var distanciaTextView: TextView
    private lateinit var btnCalcularDistancia: Button
    private var ultimaLocalizacao: Location? = null

    private val PONTO_FIXO_LAT = -26.4837
    private val PONTO_FIXO_LNG = -51.9978
    private val PONTO_FIXO_NOME = "IFPR Campus Palmas"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        currentAddressTextView = view.findViewById(R.id.currentAddressTextView)
        distanciaTextView = view.findViewById(R.id.distanciaTextView)
        btnCalcularDistancia = view.findViewById(R.id.btnCalcularDistancia)

        btnCalcularDistancia.setOnClickListener {
            val loc = ultimaLocalizacao
            if (loc != null) {
                calcularDistancia(loc)
            } else {
                Toast.makeText(context, "Aguarde, obtendo localização...", Toast.LENGTH_SHORT).show()
            }
        }

        val itemContainer = view.findViewById<LinearLayout>(R.id.itemContainer)
        carregarItens(itemContainer)

        val fab = view.findViewById<FloatingActionButton>(R.id.fab_ai)
        fab.setOnClickListener {
            val intent = Intent(view.context, AiLogicActivity::class.java)
            view.context.startActivity(intent)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        iniciarLocalizacao()

        return view
    }

    private fun iniciarLocalizacao() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
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
                obterEndereco(location.latitude, location.longitude)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun obterEndereco(lat: Double, lng: Double) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val enderecos = geocoder.getFromLocation(lat, lng, 1)
            if (!enderecos.isNullOrEmpty()) {
                val endereco = enderecos[0]
                val rua = endereco.thoroughfare ?: ""
                val cidade = endereco.locality ?: ""
                val estado = endereco.adminArea ?: ""
                currentAddressTextView.text = "📍 $rua, $cidade - $estado"
            } else {
                currentAddressTextView.text = "📍 Endereço não encontrado"
            }
        } catch (e: Exception) {
            currentAddressTextView.text = "📍 Erro ao obter endereço"
        }
    }

    private fun calcularDistancia(location: Location) {
        val pontoFixo = Location("pontoFixo").apply {
            latitude = PONTO_FIXO_LAT
            longitude = PONTO_FIXO_LNG
        }

        val distanciaMetros = location.distanceTo(pontoFixo)

        val distanciaFormatada = if (distanciaMetros >= 1000) {
            "%.1f km".format(distanciaMetros / 1000)
        } else {
            "%.0f m".format(distanciaMetros)
        }

        distanciaTextView.text = "🏫 Distância até $PONTO_FIXO_NOME: $distanciaFormatada"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1001 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            iniciarLocalizacao()
        } else {
            currentAddressTextView.text = "📍 Permissão de localização negada"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun carregarItens(container: LinearLayout) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(context, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance("https://conversor-de-unidades-8181f-default-rtdb.firebaseio.com")
            .getReference("itens")
            .child(uid)
        container.removeAllViews()

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                container.removeAllViews()
                for (itemSnap in snapshot.children) {
                    val item = itemSnap.getValue(Item::class.java)

                    val itemView = LayoutInflater.from(container.context)
                        .inflate(R.layout.item_template, container, false)

                    itemView.findViewById<TextView>(R.id.titulo).text =
                        item?.identificador ?: "Não informado"
                    itemView.findViewById<TextView>(R.id.descricao).text =
                        item?.valor ?: "Não informado"

                    container.addView(itemView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Erro ao carregar dados: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}