package mx.utng.mamr.smarthealthmonitor.data.sync

import android.content.Context
import androidx.work.*
import mx.utng.mamr.smarthealthmonitor.data.repository.SyncRepository
import mx.utng.mamr.smarthealthmonitor.data.db.SmartHealthDB
import java.util.concurrent.TimeUnit

class NeonSyncWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = SmartHealthDB.getDatabase(applicationContext)
            val repo = SyncRepository(db.lecturaDao())
            
            // 1. Enviar pendientes locales a Neon
            repo.enviarPendientes()
            
            // 2. Descargar los más recientes de Neon
            repo.sincronizarDesdeNeon(limite = 100)
            
            android.util.Log.d("SYNC_WORKER", "✅ Sync completado")
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("SYNC_WORKER", "❌ Sync fallido: ${e.message}")
            Result.retry() // WorkManager reintentará automáticamente
        }
    }

    companion object {
        const val WORK_NAME = "NeonSyncWork"
        
        /** Programar sync periódico cada 30 minutos */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
                
            val request = PeriodicWorkRequestBuilder<NeonSyncWorker>(
                30, TimeUnit.MINUTES
            ).setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()
                
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
