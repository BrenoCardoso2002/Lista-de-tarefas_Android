package com.example.listadetarefas

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.Year
import java.util.*

class VisualizaTarefa : AppCompatActivity() {

    // dados da tarefa
    private lateinit var id: String
    private lateinit var titulo: String
    private lateinit var resumo: String
    private lateinit var descricao: String
    private lateinit var data: String

    // Cria variaveis dos campos ta tela:
    private lateinit var edTitulo: TextInputEditText
    private lateinit var edResumo: TextInputEditText
    private lateinit var edDescricao: TextInputEditText
    private lateinit var edData: TextInputEditText

    // cria variavel que valida a exclusao:
    private var statusDelete: Boolean = false

    // Cria as variveis da alertDialog:
    private lateinit var tituloAlert: String
    private lateinit var textoAlert: String
    private var indiceAlert: Int = 0
    private var colorAlert: Int = 0

    // Obtem o Uid do usuário:
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualiza_tarefa)

        // incializa a variavel que armazena os dados das tarefas:
        val extras = intent.extras
        if (extras != null){
            // os dados da tarefa foram enviados com sucesso
            uid = extras.getString("UID").toString()
            id = extras.getString("id").toString()
            titulo = extras.getString("titulo").toString()
            resumo = extras.getString("resumo").toString()
            descricao = extras.getString("descricao").toString()
            data = extras.getString("data").toString()
        }else{
            // houve erro para enviar os dados, logo tarefa nao foi carregado e retorna para a tela de home
            tituloAlert = "Erro!!!"
            textoAlert = "Não fi possivel visualizar a tarefa!"
            indiceAlert = 1
            callCustomAlert()
            startActivity(Intent(this, Home::class.java))
            finish()
        }

        // incializa a variavel dos campos:
        edTitulo = findViewById(R.id.Ed_VTarefa_Titulo)
        edResumo = findViewById(R.id.Ed_VTarefa_Resumo)
        edDescricao = findViewById(R.id.Ed_VTarefa_Descricao)
        edData = findViewById(R.id.Ed_VTarefa_Data)

        // atribui textos aos campos:
        edTitulo.setText(titulo)
        edData.setText(data)
        edResumo.setText(resumo)
        edDescricao.setText(descricao)

        // Clique do botao cancelar
        findViewById<Button>(R.id.btn_VCancelar).setOnClickListener{
            startActivity(Intent(this, Home::class.java))
            finish()
        }

        // Clique do botao editar:
        findViewById<Button>(R.id.btn_Editar).setOnClickListener {
            // obtem o texto de cada campo
            val titulo = edTitulo.text.toString()
            val resumo = edResumo.text.toString()
            val descricao = edDescricao.text.toString()
            val data = edData.text.toString()

            if (data.isEmpty() || titulo.isEmpty() || resumo.isEmpty() || descricao.isEmpty()){
                // verifica se há algum campo em branco
                tituloAlert = "Erro!!!"
                textoAlert = "Há algum campo em branco!"
                indiceAlert = 1
                callCustomAlertEdit()
            }else{
                if(!verificaDateFormat(data)){
                    // verifica se a data está em formato valido
                    tituloAlert = "Erro!!!"
                    textoAlert = "Data invalida!"
                    indiceAlert = 1
                    callCustomAlertEdit()
                }else{
                    if (!verificaDataValida(data)){
                        // verifica se a data é valida
                        tituloAlert = "Erro!!!"
                        textoAlert = "Data invalida!"
                        indiceAlert = 1
                        callCustomAlertEdit()
                    }else{
                        //atualiza os campos
                        if(!updateTarefa(id, uid)){
                            // editado com sucesso
                            tituloAlert = "Sucesso!!!"
                            textoAlert = "Tarefa editada com sucesso!"
                            indiceAlert = 2
                            callCustomAlertEdit()
                        }else{
                            // erro ao editar tarefa
                            tituloAlert = "Erro!!!"
                            textoAlert = "Não foi possivel editar tarefa!"
                            indiceAlert = 1
                            callCustomAlertEdit()
                        }
                    }
                }
            }
        }

        // evento de botão deletar
        findViewById<Button>(R.id.btn_Excluir).setOnClickListener {
            // obtem o id da colecao e deleta
            getIdCollection(id, uid)
            if (!statusDelete){
                // deletado com sucesso
                tituloAlert = "Sucesso!!!"
                textoAlert = "Tarefa deleta com sucesso!"
                indiceAlert = 2
                callCustomAlert()
            }else{
                // erro ao deletar
                tituloAlert = "Erro!!!"
                textoAlert = "Não foi possivel deletar tarefa"
                indiceAlert = 1
                callCustomAlert()
            }
            // espera um tempo e vai pra tela de home após 3 segundos
            val tempo = Timer()
            tempo.schedule(object : TimerTask() {
                override fun run() {
                    goBack()
                }
            }, 3000)
        }

        // change text data:
        edData.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // obtem o texto do campo data
                val data = edData.text.toString()
                if (data.isNotEmpty()){
                    // verifica se o campo data nao esta em branco
                    val lastchar = getLastChars(data) // pega o ultimo caracter da data
                    if (data.length == 3){
                        // faz isso quando a data estiver com 3 caracteres
                        if(lastchar != "/"){
                            // verifica se o 3º caracter é diferente de uma "/"
                            edData.setText(removeLastchar(data)) // se for diferente de "/" ele remove esse caracter
                        }
                    }else if (data.length == 6){
                        // faz isso quando a data estiver com 6 caracteres
                        if(lastchar != "/"){
                            // verifica se o 6º caracter é diferente de uma "/"
                            edData.setText(removeLastchar(data)) // se for diferente de "/" ele remove esse caracter
                        }
                    }else {
                        // caso nao for o 3º e o 6º caracter
                        if (!isNumber(lastchar)){
                            // se o caracter digitado for diferente de um número ele deleta
                            edData.setText(removeLastchar(data))
                        }
                    }
                    // coloca o cursor no fim do texto da caixa de texto
                    edData.setSelection(edData.text.toString().length)
                }
            }
        })
    }

    // volta pra tela de inicio
    private fun goBack() {
        startActivity(Intent(this, Home::class.java))
        finish()
    }

    // obtem o id da collection e vai pra funcao de deletar tarefa
    private fun getIdCollection(id: String, uid: String) {
            // incializa a variavel do banco de dados
            val db = Firebase.firestore
            // cria a variavel que armazena o id da colecao
            var collectionID: String
            // faz a busca
            db.collection(uid)
                .whereEqualTo("id", id)// especifica a busca para achar o item
                .get()
                .addOnSuccessListener { documents ->
                    // achado com sucesso
                    for (document in documents) {
                        // pega o id:
                        collectionID = document.id
                        Log.i("GetId", collectionID)
                        deletarTarefa(collectionID) // chama a funcao que deleta a tarefa
                    }
                }
    }

    // funcao que atauliza a tarefa
    private fun updateTarefa(id: String, uid: String): Boolean {
        // incializa a variavel do banco de dados
        val db = Firebase.firestore
        // cria a variavel que armazena o id da colecao
        var collectionID: String
        var update = false // variavel que define o status de se foi ou nao atualizado
        // aqui pega o id da colecao
        db.collection(uid)
            .whereEqualTo("id", id) // especifica a busca para achar o item que vai ser deletado
            .get()
            .addOnSuccessListener { documents ->
                // achou o id do documento com sucesso
                for (document in documents) {
                    collectionID = document.id
                    Log.i("GetId", collectionID)
                    // faz a edicao da tarefa
                    db.collection(uid).document(collectionID).update("titulo", edTitulo.text.toString(),
                                                        "resumo", edResumo.text.toString(),
                                                                          "descricao", edDescricao.text.toString(),
                                                                          "data", edData.text.toString())
                        .addOnCompleteListener { task ->
                            // ser for suesso a variavel update recebe valor true
                            update = task.isSuccessful
                        }
                }
            }
        // retorna a variavel upadete, informando se foi editado com sucesso
        return update
    }

    // funcao que deleta a tarefa
    private fun deletarTarefa(collectionID: String) {
        // incializa a variavel do banco de dados
        val db = Firebase.firestore
        // aqui deleta a tarefa
        db.collection(uid).document(collectionID)
            .delete()
            .addOnCompleteListener { task ->
                statusDelete = task.isSuccessful
                // o status de delete vai receber de se foi ou nao deletado com sucess
            }
    }

    // apertou para voltar
    override fun onBackPressed() {
        super.onBackPressed()
        goBack()
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
        }else if (indiceAlert == 2){
            colorAlert = ContextCompat.getColor(this, R.color.blue)
            myDialog.findViewById<TextView>(R.id.Txt_Titulo_Alert).setTextColor(Color.parseColor(java.lang.String.format("#%06X", 0xFFFFFF and colorAlert)))
            myDialog.findViewById<ImageView>(R.id.iconAlertImg).setImageResource(R.drawable.alert_sucess)
        }
    }

    // verifica o formato da data:
    @SuppressLint("SimpleDateFormat")
    fun verificaDateFormat(data: String): Boolean {
        return try {
            SimpleDateFormat("dd/MM/yyyy").parse(data)
            true
        } catch (ex: Exception) {
            false
        }
    }

    // verifica se a data é valida
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

    // obtem o ultimo caracter
    fun getLastChars(str: String): String {
        var lastnChars = str
        if (lastnChars.length > 1) {
            lastnChars = lastnChars.substring(lastnChars.length - 1, lastnChars.length)
        }
        return lastnChars
    }

    // verifica se string é composta apenas por números
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

    // chama o alertdialog customizado
    // aqui pra erro na inserção de dados
    @SuppressLint("InflateParams")
    private fun callCustomAlertEdit() {
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
            val tempo = Timer()
            tempo.schedule(object : TimerTask() {
                override fun run() {
                    myDialog.dismiss()
                    tempo.cancel()
                    goBack()
                }
            }, 3000)
        }
    }
}