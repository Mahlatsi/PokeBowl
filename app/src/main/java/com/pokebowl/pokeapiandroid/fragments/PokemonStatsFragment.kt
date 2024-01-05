package com.pokebowl.pokeapiandroid.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import dagger.hilt.android.AndroidEntryPoint
import com.pokebowl.pokeapiandroid.R
import com.pokebowl.pokeapiandroid.adapters.StatsAdapter
import com.pokebowl.pokeapiandroid.databinding.FragmentPokemonStatsBinding
import com.pokebowl.pokeapiandroid.model.PokemonResult
import com.pokebowl.pokeapiandroid.model.Stats
import com.pokebowl.pokeapiandroid.utils.NetworkResource
import com.pokebowl.pokeapiandroid.utils.toast
import com.pokebowl.pokeapiandroid.viewmodels.PokemonStatsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PokemonStatsFragment : Fragment(R.layout.fragment_pokemon_stats) {

    private lateinit var binding: FragmentPokemonStatsBinding
    private val adapter = StatsAdapter()
    private val args = PokemonStatsFragmentArgs
    private val viewModel: PokemonStatsViewModel by viewModels()
    private var dominantColor: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentPokemonStatsBinding.bind(view)
        val argument = arguments?.let { args.fromBundle(it) }
        val pokemonResult = argument?.pokemonResult
        dominantColor = argument?.dominantColor
        val picture = argument?.picture

        if (dominantColor != 0) {
            dominantColor?.let { theColor ->
                binding.card.setBackgroundColor(theColor)
                binding.toolbar.setBackgroundColor(theColor)
                requireActivity().window.statusBarColor = theColor
            }
        }

        val toolbar = binding.toolbar
        toolbar.elevation = 0.0F
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.title = pokemonResult?.name?.capitalize()
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.setHomeButtonEnabled(true)

        toolbar.setNavigationOnClickListener {
            binding.root.findNavController().navigateUp()
        }

        //load pic
        binding.apply {
            Glide.with(root)
                .load(picture)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(pokemonItemImage)
        }
        pokemonResult?.let { loadSinglePokemon(it) }
    }

    private fun loadSinglePokemon(pokemonResult: PokemonResult) {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(300)
            viewModel.getSinglePokemon(pokemonResult.url).collect {
                when (it) {
                    is NetworkResource.Success -> {
                        binding.progressCircular.isVisible = false
                        binding.apply {
                            pokemonStatList.adapter = adapter
                            adapter.setStats(it.value.stats as ArrayList<Stats>, dominantColor)
                        }
                    }
                    is NetworkResource.Failure -> {
                        binding.progressCircular.isVisible = false
                        requireContext().toast("There was an error loading the pokemon")
                    }
                    is NetworkResource.Loading -> {
                        binding.progressCircular.isVisible = true
                    }
                }
            }
        }
    }
}