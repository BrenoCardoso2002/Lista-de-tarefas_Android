package com.example.listadetarefas

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.Year
import java.util.*


class AdicionaTarefa : AppCompatActivity() {

    // Cria variaveis dos campos ta tela:
    private lateinit var edTitulo: TextInputEditText
    private lateinit var edResumo: TextInputEditText
    private lateinit var edDescricao: TextInputEditText
    private lateinit var edData: TextInputEditText

    // Cria as variveis da alertDialog:
    private lateinit var tituloAlert: String
    private lateinit var textoAlert: String
    private var indiceAlert: Int = 0
    private var colorAlert: Int = 0

    // Cria a variavel do Uid do usuário:
    private lateinit var uid: String

    // Cria a varavel do id da tarefa :
    private lateinit var idTarefa: String

    // Cria a variavel do banco de dados:
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adiciona_tarefa)

        // Inicializa as variaveis dos campos ta tela:
        edTitulo = findViewById(R.id.Ed_Tarefa_Titulo)
        edResumo = findViewById(R.id.Ed_Tarefa_Resumo)
        edDescricao = findViewById(R.id.Ed_Tarefa_Descricao)
        edData = findViewById(R.id.Ed_Tarefa_Data)

        // incializa a variavel que armazena o uid:
        val extras = intent.extras
        if (extras != null){
            uid = extras.getString("UID").toString()
        }

        // inicializa o id auxiliar da tarefa
        idTarefa = "AUX"

        //inicializa o db:
        db = FirebaseFirestore.getInstance()


        // clique do botão cancelar:
        findViewById<Button>(R.id.btn_Cancelar).setOnClickListener {
            startActivity(Intent(this, Home::class.java))
            finish()
        }

        // clique do botao adicionar:
        findViewById<Button>(R.id.btn_Adicionar).setOnClickListener {
            // obtem o valor dos campos
            val data = edData.text.toString()
            val titulo = edTitulo.text.toString()
            val resumo = edResumo.text.toString()
            val descricao = edDescricao.text.toString()
            if (data.isEmpty() || titulo.isEmpty() || resumo.isEmpty() || descricao.isEmpty()){
                // Verifica se há algum campo em branco
                tituloAlert = "Erro!!!"
                textoAlert = "Há algum campo em branco!"
                indiceAlert = 1
                callCustomAlert()
            }else{
                if(!verificaDateFormat(data)){
                    // Verifica se o formato da data é invalido
                    tituloAlert = "Erro!!!"
                    textoAlert = "Data invalida!"
                    indiceAlert = 1
                    callCustomAlert()
                }else{
                    if (!verificaDataValida(data)){
                        // verifica se é uma data valida
                        tituloAlert = "Erro!!!"
                        textoAlert = "Data invalida!"
                        indiceAlert = 1
                        callCustomAlert()
                    }else{
                        // cria a tarefa
                        val tarefa = hashMapOf(
                            "id" to idTarefa,
                            "titulo" to titulo,
                            "resumo" to resumo,
                            "descricao" to descricao,
                            "data" to data
                        )
                        db.collection(uid)
                            .add(tarefa)
                            .addOnSuccessListener {
                                // Toast.makeText(this, "Adicionado com sucesso!", Toast.LENGTH_LONG).show()
                                tituloAlert = "Sucesso!!!"
                                textoAlert = "Tarefa criada com sucesso!"
                                indiceAlert = 2
                                callCustomAlert()
                                startActivity(Intent(this, Home::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                //Toast.makeText(this, "Erro ao adicionar!", Toast.LENGTH_LONG).show()
                                tituloAlert = "Erro!!!"
                                textoAlert = "Não foi possivel criar tarefa!"
                                indiceAlert = 1
                                callCustomAlert()
                            }
                    }
                }
            }
        }

        // change text data:
        edData.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val data = edData.text.toString()
                if (data.isNotEmpty()){
                    val lastchar = getLastChars(data)
                    if (data.length == 3){
                        if(lastchar != "/"){
                            // Log.i("DataTarefa", "Nope")
                            edData.setText(removeLastchar(data))
                        }
                    }else if (data.length == 6){
                        if(lastchar != "/"){
                            // Log.i("DataTarefa", "Nope")
                            edData.setText(removeLastchar(data))
                        }
                    }else {
                        if (!isNumber(lastchar)){
                            // Log.i("DataTarefa", "Nope")
                            edData.setText(removeLastchar(data))
                        }
                    }
                    edData.setSelection(edData.text.toString().length)
                }
            }
        })
    }

    // apertou no voltar
    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, Home::class.java))
        finish()
    }

    //obtem o ultimo caracter
    fun getLastChars(str: String): String {
        var lastnChars = str
        if (lastnChars.length > 1) {
            lastnChars = lastnChars.substring(lastnChars.length - 1, lastnChars.length)
        }
        return lastnChars
    }

    // ve se a string é um numero
    fun isNumber(s: String): Boolean {
        return try {
            s.toInt()
            true
            } catch (ex: NumberFormatException) {
            false
            }
    }

    // remove o ultimo caracter
    fun removeLastchar(str: String): String {
        return str.substring(0, str.length - 1)
    }

    // verifica se a data ta no formato correto
    @SuppressLint("SimpleDateFormat")
    fun verificaDateFormat(data: String): Boolean {
        return try {
            SimpleDateFormat("dd/MM/yyyy").parse(data)
            true
        } catch (ex: Exception) {
            false
        }
    }

    // verifia se a data fornecida é valida
    private fun verificaDataValida(data: String): Boolean {
        val dia = data.subSequence(0, 2).toString().toInt()
        val mes = data.subSequence(3, 5).toString().toInt()
        val ano = data.subSequence(6, 10).toString().toInt()

        if(mes < 1 || mes > 12){
            return false
        }else{
            if (dia < 1 || dia > 31){
                return false
            }else{
                if (mes == 2){
                    if (isBissesto(ano)){
                        if (dia > 29){
                            return true
                        }
                    }else{
                        if (dia > 28){
                            return false
                        }
                    }
                }else if(mes == 4 || mes == 6 || mes == 9 || mes == 11){
                    if (dia > 30){
                        return false
                    }
                }
            }
        }
        return true
    }

    // verifica se o ano é bissesto
    private fun isBissesto(year: Int): Boolean {
        return Year.isLeap(year.toLong())
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