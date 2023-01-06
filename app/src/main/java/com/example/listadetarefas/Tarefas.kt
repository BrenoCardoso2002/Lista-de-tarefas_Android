package com.example.listadetarefas

// calsse que vai armazenar os dados de cada tarefa
data class Tarefas(val id: String ?= null, var titulo: String ?= null, var resumo: String ?= null, var descricao: String ?= null, var data: String ?= null){}
// obs.:
// O nome e o tipo devem ser igual no firestore
// se o nome for diferente vai pegar valor nulo
// se o tipo for difetente nem rola