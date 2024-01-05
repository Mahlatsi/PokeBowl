package com.pokebowl.pokeapiandroid.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pokebowl.pokeapiandroid.databinding.StatItemPokemonBinding
import com.pokebowl.pokeapiandroid.model.Stats

class StatsAdapter :
    RecyclerView.Adapter<StatsAdapter.CartViewHolder>() {
    private val stats = ArrayList<Stats>()
    private var dominantColor: Int? = null

    fun setStats(newList: ArrayList<Stats>, dominantColor: Int?) {
        stats.clear()
        stats.addAll(newList)
        notifyDataSetChanged()
        this.dominantColor = dominantColor
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {

        return CartViewHolder(
            StatItemPokemonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(stats[position],dominantColor)
    }

    override fun getItemCount(): Int {
        return stats.size
    }

    class CartViewHolder(private val binding: StatItemPokemonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(stat: Stats, dominantColor: Int?) {
            binding.apply {
                statName.text = stat.stat.name.capitalize()

                if (stat.stat.name.contains("-")) {
                    val first = stat.stat.name.substringBefore("-").capitalize()
                    val second = stat.stat.name.substringAfter("-").capitalize()

                    "$first - $second".also { statName.text = it }
                }
                statCount.text = stat.base_stat.toString()
                detailCard.setBackgroundColor(dominantColor!!)
            }
        }
    }
}