package mx.utng.mamr.smarthealthmonitor.tv

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter
import mx.utng.mamr.smarthealthmonitor.data.db.LecturaFC

class DetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {
    override fun onBindDescription(
        viewHolder: ViewHolder,
        item: Any
    ) {
        val lectura = item as LecturaFC
        
        // Título: valor de FC
        viewHolder.title.text = "${lectura.bpm} bpm"
        
        // Subtítulo: estado de salud
        viewHolder.subtitle.text = if (lectura.estado == "Normal")
            "✓ Frecuencia normal"
        else
            "⚠ Fuera de rango (${lectura.estado}) — consulta al médico"
            
        // Cuerpo: hora y timestamp completo
        viewHolder.body.text =
            "Registrado a las ${lectura.hora}\n" +
            "ID de lectura: ${lectura.id} | Dispositivo: ${lectura.dispositivo}"
    }
}
