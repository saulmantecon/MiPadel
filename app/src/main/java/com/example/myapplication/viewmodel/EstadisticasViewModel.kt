package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.model.Partido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.myapplication.data.repository.PartidoFinalizadoRepository
import com.example.myapplication.model.PartidoFinalizado

class EstadisticasViewModel : ViewModel() {

    val usuario = CurrentUserManager.usuario

    private val _partidos = MutableStateFlow<List<PartidoFinalizado>>(emptyList())
    val partidos: StateFlow<List<PartidoFinalizado>> = _partidos

    fun cargarPartidosFinalizados(uid: String) {
        viewModelScope.launch {

            Log.d("EstadisticasViewModel", "Cargando partidos finalizados para el usuario $uid")
            val res = PartidoFinalizadoRepository.obtenerPartidosDeUsuario(uid)
            val lista = res.getOrDefault(emptyList())
            Log.d("ESTAD", "Encontrados ${lista.size} partidos finalizados")
            lista.forEach { pf ->
                Log.d("ESTAD", "PF => id=${pf.id}  ubic=${pf.ubicacion}  posiciones=${pf.posiciones}")
            }
            _partidos.value = res.getOrDefault(emptyList())
        }
    }
}
