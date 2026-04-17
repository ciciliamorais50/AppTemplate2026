package com.ifpr.androidapptemplate.ui.home

import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item

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