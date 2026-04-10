package com.ifpr.androidapptemplate.ui.home

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // CORREÇÃO: Usar o binding corretamente
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Chamar a função usando o ID que está no seu XML (fragment_home.xml)
        // Certifique-se que o ID no XML seja itemContainer
        carregarItensMarketplace()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun carregarItensMarketplace() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("itens")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Se você mudou o fragment_home para GridLayout, ele limpa aqui
                // Se ainda for LinearLayout, ele funciona, mas em lista.
                val container = binding.itemContainer
                container.removeAllViews()

                for (userSnapshot in snapshot.children) {
                    for (itemSnapshot in userSnapshot.children) {
                        val item = itemSnapshot.getValue(Item::class.java) ?: continue

                        val itemView = LayoutInflater.from(requireContext())
                            .inflate(R.layout.item_template, container, false)

                        // IDs devem bater com o seu item_template.xml
                        val imageView = itemView.findViewById<ImageView>(R.id.item_image)
                        val enderecoView = itemView.findViewById<TextView>(R.id.item_endereco)
                        val valorView = itemView.findViewById<TextView>(R.id.item_valor)

                        enderecoView.text = item.endereco ?: "Sem nome"
                        valorView?.text = item.valor ?: ""



                        container.addView(itemView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(context, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}