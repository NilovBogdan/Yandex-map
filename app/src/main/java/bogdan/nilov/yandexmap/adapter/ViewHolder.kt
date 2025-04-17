package bogdan.nilov.yandexmap.adapter

import android.annotation.SuppressLint
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import bogdan.nilov.yandexmap.OnInteractiveListener
import bogdan.nilov.yandexmap.databinding.RowBinding
import bogdan.nilov.yandexmap.dto.Map

class ViewHolder(
    private val binding: RowBinding,
    private val onInteractiveListener: OnInteractiveListener
) : RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bind(map: Map) {
        binding.apply {
            val id = map.id
            val name = map.name
            val lat = map.lat.toDouble()
            val lon = map.lon.toDouble()
            listOfPlace.text = name

            binding.delete.setOnClickListener {
                AlertDialog.Builder(it.context)
                    .setTitle("Удалить метку?")
                    .setPositiveButton("Да") { _: DialogInterface, _: Int ->
                        onInteractiveListener.removeById(id, lat, lon)
                    }
                    .setNegativeButton("Нет"){ _: DialogInterface, _: Int -> }
                    .show()
            }

            binding.edit.setOnClickListener {
                AlertDialog.Builder(it.context)
                    .setTitle("Редактировать метку?")
                    .setPositiveButton("Да") { _: DialogInterface, _: Int ->
                        onInteractiveListener.edit(id, name, lat, lon)
                    }
                    .setNegativeButton("Нет"){ _: DialogInterface, _: Int -> }
                    .show()
            }

            binding.listOfPlace.setOnClickListener {
                onInteractiveListener.transition(id, name, lat, lon)
            }
        }

    }

}
