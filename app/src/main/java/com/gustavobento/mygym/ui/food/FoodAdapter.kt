package com.gustavobento.mygym.ui.food

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gustavobento.mygym.databinding.ItemFoodBinding
import com.gustavobento.mygym.model.Food
import com.gustavobento.mygym.util.Formatters

/**
 * Adapter da lista de alimentos (RecyclerView).
 * Recebe uma função [onFoodClick] que é chamada quando o usuário toca em um item.
 */
class FoodAdapter(private val onFoodClick: (Food) -> Unit) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    private var foods: List<Food> = emptyList()

    /** Troca a lista exibida (usado a cada letra digitada na busca). */
    fun submitFoods(newFoods: List<Food>) {
        foods = newFoods
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = foods.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(foods[position])
    }

    /** O ViewHolder guarda as Views de um item para reaproveitar ao rolar a lista. */
    inner class FoodViewHolder(private val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(food: Food) {
            val per100g = food.per100g
            binding.textName.text = food.name
            binding.textInfo.text = "${Formatters.integer(per100g.calories)} kcal • " +
                "P ${Formatters.number(per100g.protein)} g • C ${Formatters.number(per100g.carbs)} g • " +
                "G ${Formatters.number(per100g.fat)} g (100 g)"
            binding.textCustomTag.visibility = if (food.isCustom) View.VISIBLE else View.GONE
            binding.root.contentDescription = "${food.name}, ${Formatters.integer(per100g.calories)} quilocalorias em 100 gramas"
            binding.root.setOnClickListener { onFoodClick(food) }
        }
    }
}
