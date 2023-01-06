package com.example.listadetarefas

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*

class LogIn : AppCompatActivity() {

    // Cria as variaveis das caixa de texto:
    private lateinit var edEmail: TextInputEditText
    private lateinit var edSenha: TextInputEditText

    // Cria as variveis da alertDialog:
    private lateinit var tituloAlert: String
    private lateinit var textoAlert: String
    private var indiceAlert: Int = 0
    private var colorAlert: Int = 0

    // Cria variavel do firebaseAuth:
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        // Inicializa as variaveis das caixa de texto:
        edEmail = findViewById(R.id.Ed_email_login)
        edSenha = findViewById(R.id.Ed_senha_login)

        // Inicializa a variavel do firebaseAuth:
        auth = Firebase.auth

        // Clique do botão realizar login:
        findViewById<Button>(R.id.Bt_Login).setOnClickListener {
            // Toast.makeText(this, "Realizar login!!!", Toast.LENGTH_SHORT).show()
            realizarLogin()
        }

        // Clique do botão recuperar senha:
        findViewById<Button>(R.id.Bt_Esqueci).setOnClickListener {
            // pega o texto do campo email
            val email = edEmail.text.toString()
            if (email.isEmpty()){
                // verifica se o email está embranco
                tituloAlert = "Erro!!!"
                textoAlert = "O campo e-mail está em branco!"
                indiceAlert = 1
                callCustomAlert()
            }else{
                // faz o envio do email de recuperação de senha
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful){
                            // sucesso email enviado
                            tituloAlert = "Sucesso!!!"
                            textoAlert = "E-mail de recuperação enviado!\n" +
                                    "(caso não encontre veja a caixa de span)"
                            indiceAlert = 2
                            callCustomAlert()
                        }else{
                            // email nao enviado, e exibe uma mensagem determinada de erro
                            try {
                                throw task.exception!!
                            } catch (e: FirebaseAuthInvalidCredentialsException) {
                                // erro email mal formatado
                                tituloAlert = "Erro!!!"
                                textoAlert = "O endereço de e-mail está mal formatado!"
                                indiceAlert = 1
                                callCustomAlert()
                            } catch (e: FirebaseAuthInvalidUserException){
                                // erro email nao ta cadastrado
                                tituloAlert = "Erro!!!"
                                textoAlert = "Endereço de e-mail não está cadastrado!!"
                                indiceAlert = 1
                                callCustomAlert()
                            } catch (e: FirebaseAuthException){
                                // erro abrangente
                                tituloAlert = "Erro!!!"
                                textoAlert = "Erro ao relizar o login!"
                                indiceAlert = 1
                                callCustomAlert()
                            }
                        }
                    }
            }
        }

        // Clique do botão criar conta:
        findViewById<Button>(R.id.Bt_CriarConta).setOnClickListener {
            // vai pra tela de cadastro
            startActivity(Intent(this, SignIn::class.java))
            finish()
        }
    }

    // função que realiza o login
    private fun realizarLogin() {
        // pega o texto dos campos
        val email = edEmail.text.toString().trim()
        val senha = edSenha.text.toString()

        if (email.isEmpty() or senha.isEmpty()){
            // verifica se há algum campo em branco
            tituloAlert = "Erro!!"
            textoAlert = "Há algum campo em branco!" +
                    "\nVerifique os campos e tente novamente!"
            indiceAlert = 1
            callCustomAlert()
        }else{
            // realiza o login do usuário
            auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this) {task ->
                    if (task.isSuccessful){
                        // logado com sucesso
                        tituloAlert = "Sucesso!!!"
                        textoAlert = "Usuário conectado com sucesso"
                        indiceAlert = 2
                        callCustomAlert()
                        // vai pra tela home
                        startActivity(Intent(this, Home::class.java))
                        finish()
                    }else{
                        // erro no login
                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            val errorCode = (task.exception as FirebaseAuthException?)!!.errorCode
                            if (errorCode == "ERROR_INVALID_EMAIL"){
                                // erro de endereco de email invalido
                                tituloAlert = "Erro!!!"
                                textoAlert = "Endereço de e-mail inválido!"
                                indiceAlert = 1
                                callCustomAlert()
                            }else if (errorCode == "ERROR_WRONG_PASSWORD"){
                                // ero de senha invalida
                                tituloAlert = "Erro!!!"
                                textoAlert = "Senha inválida!\n" +
                                        "Tente novamente ou clique em 'Esqueci a senha!'"
                                indiceAlert = 1
                                callCustomAlert()
                            }
                        } catch (e: FirebaseAuthInvalidUserException){
                            // erro de email nao cadastrado
                            tituloAlert = "Erro!!!"
                            textoAlert = "Endereço de e-mail não está cadastrado!!\n" +
                                    "Tente criar uma conta!"
                            indiceAlert = 1
                            callCustomAlert()
                        } catch (e: FirebaseAuthException){
                            // erro abrangente
                            tituloAlert = "Erro!!!"
                            textoAlert = "Erro ao relizar o login!"
                            indiceAlert = 1
                            callCustomAlert()
                        }
                    }
                }
        }
    }

    // chama o alertdialog customizado
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