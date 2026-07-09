package mx.utng.mamr.smarthealthmonitor.cast

import android.content.Context
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.*

class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(ctx: Context): CastOptions =
        CastOptions.Builder()
            .setReceiverApplicationId(
                CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID
            )
            .build()

    override fun getAdditionalSessionProviders(ctx: Context): List<SessionProvider> =
        emptyList()
}
