package com.ifpr.androidapptemplate.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.util.Base64
import android.widget.*
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.databinding.FragmentHomeBinding
import com.ifpr.androidapptemplate.ui.ai.AiLogicActivity

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*


class HomeFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val container = view.findViewById<LinearLayout>(R.id.itemContainer)
        carregarItens(container)

        // Firebase
        carregarItens(container)

        val fab = view.findViewById<FloatingActionButton>(R.id.fab_ai)

        fab.setOnClickListener {
            val context = view.context
            val intent = Intent(context, AiLogicActivity::class.java)
            context.startActivity(intent)
        }

        return view
    }

    // ================= FIREBASE =================

    private fun carregarItens(container: LinearLayout) {
        val ref = FirebaseDatabase.getInstance().getReference("itens")
        container.removeAllViews()


        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {



                for (user in snapshot.children) {
                    for (itemSnap in user.children) {
                        val item = itemSnap.getValue(Item::class.java)

                        val itemView = LayoutInflater.from(container.context)
                            .inflate(R.layout.item_template, container, false)

                        val tituloView = itemView.findViewById<TextView>(R.id.titulo)
                        val descricaoView = itemView.findViewById<TextView>(R.id.descricao)

                        tituloView.text = "${item?.identificador ?: "Não informado"}"
                        descricaoView.text = "${item?.valor ?: "Não informado"}"

                        container.addView(itemView)
                    }
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