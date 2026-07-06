package mx.utng.mamr.smarthealthmonitor.tv

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

/**
 * MainActivity para Android TV.
 * Es solo el contenedor: carga MainFragment.
 * TODA la lógica de UI va en el Fragment.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_browse_fragment, MainFragment())
                .commit()
        }
    }
}
