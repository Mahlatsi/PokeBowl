package com.pokebowl.pokeapiandroid.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.pokebowl.pokeapiandroid.data.repositories.PokemonRepository
import com.pokebowl.pokeapiandroid.utils.NetworkResource
import com.pokebowl.pokeapiandroid.utils.extractId
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class PokemonStatsViewModel @Inject constructor(private val pokemonRepository: PokemonRepository) :
    ViewModel() {

    suspend fun getSinglePokemon(url: String) = flow {
        val id = url.extractId()
        emit(NetworkResource.Loading)
        emit(pokemonRepository.getSinglePokemon(id))
    }

}