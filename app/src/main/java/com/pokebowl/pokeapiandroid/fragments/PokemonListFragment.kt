package com.pokebowl.pokeapiandroid.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import com.pokebowl.pokeapiandroid.R
import com.pokebowl.pokeapiandroid.adapters.LoadingStateAdapter
import com.pokebowl.pokeapiandroid.adapters.PokemonAdapter
import com.pokebowl.pokeapiandroid.databinding.FragmentPokemonListBinding
import com.pokebowl.pokeapiandroid.model.PokemonResult
import com.pokebowl.pokeapiandroid.utils.toast
import com.pokebowl.pokeapiandroid.viewmodels.PokemonListViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
@SuppressLint("ClickableViewAccessibility")
class PokemonListFragment : Fragment(R.layout.fragment_pokemon_list) {

    private var hasInitiatedInitialCall = false
    private lateinit var binding: FragmentPokemonListBinding
    private val viewModel: PokemonListViewModel by viewModels()
    private var job: Job? = null
    private var hasUserSearched = false

    private val adapter =
        PokemonAdapter { pokemonResult: PokemonResult, dominantColor: Int, picture: String? ->
            navigate(
                pokemonResult,
                dominantColor,
                picture
            )
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentPokemonListBinding.bind(view)

        setAdapter()
        setRefresh()
        setSearchView()
    }

    private fun setRefresh() {

        binding.swipeRefreshLayout.setOnRefreshListener {
            startFetchingPokemon(null, true)

            binding.searchView.apply {
                text = null
                isFocusable = false

            }
            hideSoftKeyboard()

        }
    }

    private fun setSearchView() {
        binding.searchView.setOnTouchListener { v, _ ->
            v.isFocusableInTouchMode = true
            false
        }
        binding.searchView.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hasUserSearched = true
                performSearch(binding.searchView.text.toString().trim())
                return@OnEditorActionListener true
            }
            false
        })

        binding.searchView.addTextChangedListener {

            if (it.toString().isEmpty() && hasUserSearched) {
                startFetchingPokemon(null, true)
                hideSoftKeyboard()
                hasUserSearched = false
            }
        }

    }

    private fun startFetchingPokemon(searchString: String?, shouldSubmitEmpty: Boolean) {

        job?.cancel()
        job = lifecycleScope.launch {
            if (shouldSubmitEmpty) adapter.submitData(PagingData.empty())
            viewModel.getAllPokemons(searchString).collectLatest {
                adapter.submitData(it)
            }
        }
    }

    private fun performSearch(searchString: String) {
        hideSoftKeyboard()
        if (searchString.isEmpty()) {
            requireContext().toast("Search cannot be empty")
            return
        }
        startFetchingPokemon(searchString, true)
    }

    private fun hideSoftKeyboard() {
        val view = requireActivity().currentFocus

        view?.let {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun setAdapter() {
        binding.pokemonList.layoutManager = GridLayoutManager(requireContext(), 1)
        binding.pokemonList.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter { retry() }
        )

        if (!hasInitiatedInitialCall) startFetchingPokemon(null, false);
        hasInitiatedInitialCall = true

        adapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading && adapter.snapshot().isEmpty()) {
                binding.progressCircular.isVisible = true
                binding.textError.isVisible = false
            } else {
                binding.progressCircular.isVisible = false
                binding.swipeRefreshLayout.isRefreshing = false

                val error = when {
                    loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                    loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                    loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                    else -> null
                }
                if (adapter.snapshot().isEmpty()) {
                    error?.let {
                        binding.textError.visibility = View.VISIBLE
                        binding.textError.setOnClickListener {
                            adapter.retry()
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.searchView.isFocusable = false
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.green)
    }

    private fun retry() {
        adapter.retry()
    }

    private fun navigate(pokemonResult: PokemonResult, dominantColor: Int, picture: String?) {
        binding.root.findNavController()
            .navigate(
                PokemonListFragmentDirections.toPokemonStatsFragment(pokemonResult, dominantColor, picture)
            )
    }
}