package io.github.gianpamx.covid19.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.gianpamx.covid19.R
import kotlinx.android.synthetic.main.list_item.view.*
import java.text.NumberFormat

class CountryNumberAdapter(private val numberFormat: NumberFormat) :
    ListAdapter<CountryNumber, CountryNumberAdapter.ViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: CountryNumber) {
            itemView.countryTextView.text = item.country
            itemView.numberTextView.text = numberFormat.format(item.number)
        }
    }
}

private val diffCallback = object : DiffUtil.ItemCallback<CountryNumber?>() {
    override fun areItemsTheSame(oldItem: CountryNumber, newItem: CountryNumber) =
        oldItem.country == newItem.country

    override fun areContentsTheSame(oldItem: CountryNumber, newItem: CountryNumber) =
        oldItem == newItem
}
