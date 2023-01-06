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
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*

class SignIn : AppCompatActivity() {

    // Cria variaveis das caixa de texto:
    private lateinit var edEmail: TextInputEditText
    private lateinit var edSenha: TextInputEditText
    private lateinit var edRsenha: TextInputEditText

    // Cria as variveis da alertDialog:
    private lateinit var tituloAlert: String
    private lateinit var textoAlert: String
    private var indiceAlert: Int = 0
    private var colorAlert: Int = 0

    // Cria variavel do firebaseAuth:
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Inicializar as variveis das caixa de textos:
        edEmail = findViewById(R.id.Ed_email_signin)
        edSenha = findViewById(R.id.Ed_senha_signin)
        edRsenha = findViewById(R.id.Ed_rsenha_signin)

        // Inicializa a variavel do firebaseAuth:
        auth = Firebase.auth

        // Clique do botão ja tem conta:
        findViewById<Button>(R.id.Bt_JtConta).setOnClickListener {
            startActivity(Intent(this, LogIn::class.java))
            finish()
        }

        // Clique do botão realizar cadastro:
        findViewById<Button>(R.id.Bt_RealCadastro).setOnClickListener {
            // Toast.makeText(this, "Realizar cadastro!!!", Toast.LENGTH_SHORT).show()
            realizarCadastro()
        }
    }

    // função que realiza cadastro
    private fun realizarCadastro() {
        // obtem o texto de cada campo
        val email = edEmail.text.toString().trim()
        val senha = edSenha.text.toString()
        val rsenha = edRsenha.text.toString()

        if (email.isEmpty() or senha.isEmpty() or rsenha.isEmpty()){
            // verifica se há algum campo em branco
            tituloAlert = "Erro!!"
            textoAlert = "Há algum campo em branco!" +
                    "\nVerifique os campos e tente novamente!"
            indiceAlert = 1
            callCustomAlert()
        }else if (senha.length < 7){
            // verifica se a senha tem menos de 7 caracteres
            tituloAlert = "Erro!!!"
            textoAlert = "Senha muito curta!"
            indiceAlert = 1
            callCustomAlert()
        }else if(senha != rsenha){
            // verifica se as senhas sao diferentes
            tituloAlert = "Erro!!!"
            textoAlert = "Senhas são diferentes"
            indiceAlert = 1
            callCustomAlert()
        }else{
            // realiza o cadastro do usuario
            auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful){
                        // cadastro realizado com sucesso
                        tituloAlert = "Sucesso!!!"
                        textoAlert = "Usuário cadastrado com sucesso"
                        indiceAlert = 2
                        callCustomAlert()
                        // vai pra tela de inicio
                        startActivity(Intent(this, Home::class.java))
                        finish()
                    }else{
                        // erro ao realizar logn
                        Log.i("ErroSignIn", task.exception.toString())
                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            // erro, o endereço de email está mal formatado
                            tituloAlert = "Erro!!!"
                            textoAlert = "O endereço de e-mail está mal formatado!"
                            indiceAlert = 1
                            callCustomAlert()
                        } catch (e: FirebaseAuthUserCollisionException){
                            // erro o email ja ta cadastrado
                            tituloAlert = "Erro!!!"
                            textoAlert = "O endereço de e-mail já está sendo usado por outra conta!"
                            indiceAlert = 1
                            callCustomAlert()
                        } catch (e: FirebaseAuthException){
                            // erro abrangente
                            tituloAlert = "Erro!!!"
                            textoAlert = "Erro ao relizar o cadastro!"
                            indiceAlert = 1
                            callCustomAlert()
                        }
                    }
                }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, LogIn::class.java))
        finish()
    }

    @SuppressLint("InflateParams")
    private fun callCustomAlert() {
        // cria a alertDialog
        val dialogBinding = layoutInflater.inflate(R.layout.custom_alert, null)
        val myDialog = Dialog(this)
        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(true)
        myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        myDialog.show()

        // atribui os valores de texto nos determinados campos:
        myDialog.findViewById<TextView>(R.id.Txt_Titulo_Alert).text = tituloAlert
        myDialog.findViewById<TextView>(R.id.Txt_Texto_Alert).text = textoAlert

        // verifica o indice e define as cores:
        // 1 = error, 2 == sucess
        if (indiceAlert == 1){
            colorAlert = ContextCompat.getColor(this, R.color.red)
            myDialog.findViewById<TextView>(R.id.Txt_Titulo_Alert).setTextColor(Color.parseColor(java.lang.String.format("#%06X", 0xFFFFFF and colorAlert)))
            myDialog.findViewById<ImageView>(R.id.iconAlertImg).setImageResource(R.drawable.alert_error)
            val tempo = Timer()
            tempo.schedule(object : TimerTask() {
                override fun run() {
                    myDialog.dismiss()
                    tempo.cancel()
                }
            }, 3000)
        }else if (indiceAlert == 2){
            colorAlert = ContextCompat.getColor(this, R.color.blue)
            myDialog.findViewById<TextView>(R.id.Txt_Titulo_Alert).setTextColor(Color.parseColor(java.lang.String.format("#%06X", 0xFFFFFF and colorAlert)))
            myDialog.findViewById<ImageView>(R.id.iconAlertImg).setImageResource(R.drawable.alert_sucess)
        }
    }
}