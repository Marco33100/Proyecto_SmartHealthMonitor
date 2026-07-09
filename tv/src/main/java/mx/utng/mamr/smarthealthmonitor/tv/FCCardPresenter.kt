package mx.utng.mamr.smarthealthmonitor.tv

import android.graphics.Color
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import mx.utng.mamr.smarthealthmonitor.data.db.LecturaFC

data class AlertaTV(
    val tipo: String,
    val hora: String,
    val esCritica: Boolean = true
)

data class EstadoActualTV(
    val valor: String,
    val etiqueta: String,
    val colorHex: String
)

class FCCardPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            // CRÍTICO: sin estas dos líneas,
            // el D-pad no puede navegar a este card
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(240, 180)
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any?) {
        val card = viewHolder.view as ImageCardView
        
        when (item) {
            is LecturaFC -> {
                card.titleText = "${item.valorBpm} bpm"
                card.contentText = item.hora
                // Color de fondo según si FC es normal
                val bgColor = if (item.esNormal) {
                    Color.parseColor("#1B4F8A") // primary
                } else {
                    Color.parseColor("#B3261E") // error
                }
                card.setBackgroundColor(bgColor)
            }
            is AlertaTV -> {
                card.titleText = item.tipo
                card.contentText = item.hora
                val bgColor = if (item.esCritica) {
                    Color.parseColor("#B3261E") // error / crítica
                } else {
                    Color.parseColor("#D4860A") // amber / advertencia
                }
                card.setBackgroundColor(bgColor)
            }
            is EstadoActualTV -> {
                card.titleText = item.valor
                card.contentText = item.etiqueta
                card.setBackgroundColor(Color.parseColor(item.colorHex))
            }
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        (viewHolder.view as ImageCardView).mainImage = null
    }
}
