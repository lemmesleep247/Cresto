package com.nevoit.cresto.data.todo.cleanup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nevoit.cresto.data.todo.TodoRepository
import kotlinx.coroutines.CancellationException
import org.koin.core.context.GlobalContext
import java.util.concurrent.TimeUnit

class RecentlyDeletedCleanupWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        return try {
            val repository = GlobalContext.getOrNull()?.get<TodoRepository>()
                ?: return Result.retry()
            repository.deleteExpiredRecentlyDeleted()
            Result.success()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.retry()
        }
    }
}

object RecentlyDeletedCleanupScheduler {
    fun schedule(context: Context) {
        val workManager = WorkManager.getInstance(context)

        workManager.enqueueUniqueWork(
            STARTUP_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<RecentlyDeletedCleanupWorker>().build()
        )
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<RecentlyDeletedCleanupWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(1, TimeUnit.DAYS)
                .build()
        )
    }

    private const val STARTUP_WORK_NAME = "recently_deleted_cleanup_on_startup"
    private const val PERIODIC_WORK_NAME = "recently_deleted_cleanup_periodic"
}
