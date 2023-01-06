package com.example.listadetarefas

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// classe do adaptador pra listar as tarefas
class MyAdapter(val context: Context, private val tarefaList: ArrayList<Tarefas>, private val uid: String):
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    // função que é realizada ao inicalizar, ela obtem o layout de cada item da recycler view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.tarefa_item, parent, false)

        return MyViewHolder(itemView)
    }

    // faz a vinculação e escreve nos campos os textos especificos que foram pegos no banco de dados
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val tarefa: Tarefas = tarefaList[position] // pega o conjunto de dados pela lista e com sua posicao
        // escreve no campo e converte o dado para string
        holder.titulo.text = tarefa.titulo.toString()
        holder.resumo.text = tarefa.resumo.toString()
        holder.data.text = tarefa.data.toString()
        // click do item da recycler view
        holder.itemView.setOnClickListener(){
            // obtem a referecia de para que tela vai
            val intent = Intent(context, VisualizaTarefa::class.java)

            // envia alguns dados para a tela que sera aberta
            intent.putExtra("UID", uid)
            intent.putExtra("id", tarefa.id.toString())
            intent.putExtra("titulo", tarefa.titulo.toString())
            intent.putExtra("resumo", tarefa.resumo.toString())
            intent.putExtra("descricao", tarefa.descricao.toString())
            intent.putExtra("data", tarefa.data.toString())

            // abre a tela
            context.startActivity(intent)
        }
    }

    // obtem o tamanho da lista, ou seja quantas tarefas existem
    override fun getItemCount(): Int {
        return tarefaList.size
    }

    // pega os campos do layout do item e cria uma variavel pra eles
    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val titulo: TextView = itemView.findViewById(R.id.tvTituloTarefa)
        val resumo: TextView = itemView.findViewById(R.id.tvResumoTarefa)
        val data: TextView = itemView.findViewById(R.id.tvDataTarefa)
    }
}