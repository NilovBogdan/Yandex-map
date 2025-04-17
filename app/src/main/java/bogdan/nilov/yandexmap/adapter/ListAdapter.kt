package bogdan.nilov.yandexmap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bogdan.nilov.yandexmap.OnInteractiveListener
import bogdan.nilov.yandexmap.databinding.RowBinding
import bogdan.nilov.yandexmap.dto.Map

class ListAdapter(
    private val onInteractiveListener: OnInteractiveListener
) : RecyclerView.Adapter<ViewHolder>() {
    var list = emptyList<Map>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onInteractiveListener)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val map = list[position]
        holder.bind(map)
    }


}
