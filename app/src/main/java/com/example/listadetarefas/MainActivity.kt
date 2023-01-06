package com.example.listadetarefas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    // Cria variavel do firebaseAuth:
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa a variavel do firebaseAuth:
        auth = Firebase.auth

        // Realiza um comando após um determinado tempo
        Handler(Looper.getMainLooper()).postDelayed({
            // aqui é onde tem o comando que será realizado após esse tempo passar
            val currentUser = auth.currentUser
            if(currentUser != null){
                // está logado, vai pra tela de inicio
                startActivity(Intent(this, Home::class.java))
                finish()
            }else{
                // nao está logado vai pra tela de login
                startActivity(Intent(this, LogIn::class.java))
                finish()
            }
        }, 3000)
    }
}