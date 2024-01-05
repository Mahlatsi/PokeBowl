package com.pokebowl.pokeapiandroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import com.pokebowl.pokeapiandroid.data.repositories.PokemonRepository
import com.pokebowl.pokeapiandroid.model.PokemonResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository) :
    ViewModel() {

    private var currentResult: Flow<PagingData<PokemonResult>>? = null
    fun getAllPokemons(searchString: String?): Flow<PagingData<PokemonResult>> {
        val newResult: Flow<PagingData<PokemonResult>> =
            pokemonRepository.getPokemon(searchString).cachedIn(viewModelScope)
        currentResult = newResult
        return newResult
    }
}