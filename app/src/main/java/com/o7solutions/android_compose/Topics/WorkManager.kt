package com.o7solutions.android_compose.Topics

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import java.util.UUID

class SimpleSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        delay(3000)
        return Result.success()
    }
}

class WorkViewModel(context: Context) : ViewModel() {
    private val workManager = WorkManager.getInstance(context)

    var workId by mutableStateOf<UUID?>(null)
        private set

    fun runBackgroundTask() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresCharging(false)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SimpleSyncWorker>()
            .setConstraints(constraints)
            .build()

        workId = syncRequest.id
        workManager.enqueue(syncRequest)
    }

    fun getWorkInfo(id: UUID) = workManager.getWorkInfoByIdLiveData(id)
}

@Composable
fun WorkManagerScreen() {
    val context = LocalContext.current

    val viewModel: WorkViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                WorkViewModel(context.applicationContext)
            }
        }
    )

    val idleState = remember { mutableStateOf<WorkInfo?>(null) }

    val workInfo by viewModel.workId?.let { id ->
        viewModel.getWorkInfo(id).observeAsState()
    } ?: idleState

    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Button(onClick = { viewModel.runBackgroundTask() }) {
            Text("Start Background Sync")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (workInfo?.state) {
            WorkInfo.State.ENQUEUED -> Text("Status: Enqueued...")
            WorkInfo.State.RUNNING -> Text("Status: Running...")
            WorkInfo.State.SUCCEEDED -> Text("Status: Success!")
            WorkInfo.State.FAILED -> Text("Status: Failed")
            WorkInfo.State.BLOCKED -> Text("Status: Blocked")
            WorkInfo.State.CANCELLED -> Text("Status: Cancelled")
            else -> Text("Status: Idle")
        }
    }
}