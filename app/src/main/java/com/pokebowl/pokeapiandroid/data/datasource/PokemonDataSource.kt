package com.pokebowl.pokeapiandroid.data.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.pokebowl.pokeapiandroid.api.PokemonApi
import com.pokebowl.pokeapiandroid.model.PokemonResult
import com.pokebowl.pokeapiandroid.utils.LOAD_MAX
import com.pokebowl.pokeapiandroid.utils.STARTING_OFFSET_INDEX
import java.io.IOException


class PokemonDataSource(private val pokemonApi: PokemonApi, private val searchString: String?) :
    PagingSource<Int, PokemonResult>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PokemonResult> {
        val offset = params.key ?: STARTING_OFFSET_INDEX

        val loadSize = if (searchString == null) params.loadSize else LOAD_MAX
        return try {
            val data = pokemonApi.getPokemons(loadSize, offset)

            val filteredData = when {
                searchString != null -> {
                    data.results.filter { it.name.contains(searchString, true) }
                }
                else -> {
                    data.results
                }
            }

            LoadResult.Page(
                data = filteredData,
                prevKey = if (offset == STARTING_OFFSET_INDEX) null else offset - loadSize,
                nextKey = if (data.next == null) null else offset + loadSize
            )
        } catch (t: Throwable) {
            var exception = t

            if (t is IOException) {
                exception = IOException("Please check internet connection")
            }
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, PokemonResult>): Int? {
        return state.anchorPosition
    }
}