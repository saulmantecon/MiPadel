package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.model.Partido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EstadisticasViewModel : ViewModel() {

    val usuario = CurrentUserManager.usuario

    private val _partidos = MutableStateFlow<List<Partido>>(emptyList())
    val partidos: StateFlow<List<Partido>> = _partidos

    init {
        val uid = CurrentUserManager.getUsuario()?.uid

        viewModelScope.launch {
            //_partidos.value = PartidoRepository.obtenerPartidosDeUsuario(uid)
        }
    }
}
