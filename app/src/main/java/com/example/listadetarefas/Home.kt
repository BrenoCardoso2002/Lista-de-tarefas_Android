package com.example.listadetarefas


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class Home : AppCompatActivity() {

    // Cria variavel do firebaseAuth:
    private lateinit var auth: FirebaseAuth

    // Criar variavel que armazena o uid:
    private lateinit var uid: String

    // cria variaveis para o recycler view:
    private lateinit var recyclerView: RecyclerView
    private lateinit var tarefaArrayList: ArrayList<Tarefas>
    private lateinit var myAdapter: MyAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Inicializa a variavel do firebaseAuth:
        auth = Firebase.auth

        // incializa variavel que armazena o uid:
        uid = auth.currentUser?.uid.toString()

        // atualiza o id=aux
        updateIdTarefa()

        // coloca uid na textView determinada:
        findViewById<TextView>(R.id.Txt_UID).text = uid

        // Clique do botão logut:
        findViewById<Button>(R.id.Bt_Logout).setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(this, LogIn::class.java))
            finish()
        }

        // Clique do botão add:
        findViewById<Button>(R.id.Bt_AddTarefa).setOnClickListener {
            val intent = Intent(this, AdicionaTarefa::class.java)
            intent.putExtra("UID", uid)
            startActivity(intent)
            finish()
        }

        // inicializa os componetes que faram funcionar a recycler view
        tarefaArrayList = arrayListOf()
        myAdapter = MyAdapter(this, tarefaArrayList, uid)

        recyclerView = findViewById(R.id.tarefaRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        recyclerView.adapter = myAdapter


        // carregar a recycler view
        eventChangeListener()
    }

    // carregar a recycler view
    private fun eventChangeListener() {
        // incializa a variavel do banco de dados
        db = FirebaseFirestore.getInstance()
        /// faz a busca no banco de dados
        db.collection(uid).
        addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                // verifica se houve erro na busca
                if (error != null){
                    Log.e("Firestore Error", error.message.toString())
                    alertError()
                    return
                }

                // faz um laco pra pegar os documentos
                for (dc: DocumentChange in value?.documentChanges!!){

                    if (dc.type == DocumentChange.Type.ADDED){
                        val adiciona = dc.document.toObject(Tarefas::class.java)
                        // adiciona na lista
                        tarefaArrayList.add(adiciona)

                    }
                }
                myAdapter.notifyDataSetChanged()
            }
        })
    }

    // chama o alertdialog customizado
    @SuppressLint("InflateParams", "SetTextI18n")
    private fun alertError() {
        // cria a alertDialog
        val dialogBinding = layoutInflater.inflate(R.layout.custom_alert, null)
        val myDialog = Dialog(this)
        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(true)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()

        // atribui os valores de texto nos determinados campos:
        myDialog.findViewById<TextView>(R.id.Txt_Titulo_Alert).text = "Erro!!!"
        myDialog.findViewById<TextView>(R.id.Txt_Texto_Alert).text = "Não foi possivel carregar a tarefas!!!\nFeche o app e tente novamente!"

        val colorAlert = ContextCompat.getColor(this, R.color.red)
        myDialog.findViewById<TextView>(R.id.Txt_Titulo_Alert).setTextColor(Color.parseColor(java.lang.String.format("#%06X", 0xFFFFFF and colorAlert)))
        myDialog.findViewById<ImageView>(R.id.iconAlertImg).setImageResource(R.drawable.alert_error)
        val tempo = Timer()
        tempo.schedule(object : TimerTask() {
            override fun run() {
                myDialog.dismiss()
                tempo.cancel()
            }
        }, 3000)
    }

    // atualiza se tiver um id=aux, para o correto:
    private fun updateIdTarefa() {
        val db = Firebase.firestore
        db.collection(uid)
            .whereEqualTo("id", "AUX")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val documentID = document.id
                    Log.i("DocId", documentID)
                    db.collection(uid).document(documentID).update("id", documentID)
                }
            }
    }
}
