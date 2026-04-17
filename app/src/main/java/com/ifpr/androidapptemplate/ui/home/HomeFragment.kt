package com.ifpr.androidapptemplate.ui.home

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    private val lista = mutableListOf<Item>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        recyclerView.setHasFixedSize(true)
        recyclerView.clipToPadding = false
        recyclerView.setPadding(8, 8, 8, 80)

        // Firebase
        carregarItens()

        return view
    }

    // ================= FIREBASE =================

    private fun carregarItens() {
        val ref = FirebaseDatabase.getInstance().getReference("itens")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                lista.clear()

                for (user in snapshot.children) {
                    for (itemSnap in user.children) {
                        val item = itemSnap.getValue(Item::class.java)
                        item?.let { lista.add(it) }
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