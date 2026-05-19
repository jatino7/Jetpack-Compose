package com.o7solutions.android_compose.Topics


import android.Manifest
import android.content.Context
import android.provider.CallLog
import android.provider.Telephony
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun LogsScreen() {

    val context = LocalContext.current

    var smsList by remember {
        mutableStateOf(listOf<SmsData>())
    }

    var callLogs by remember {
        mutableStateOf(listOf<CallLogData>())
    }

    var permissionGranted by remember {
        mutableStateOf(false)
    }

    val launcher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val smsPermission =
                permissions[Manifest.permission.READ_SMS]
                    ?: false

            val callPermission =
                permissions[Manifest.permission.READ_CALL_LOG]
                    ?: false

            permissionGranted =
                smsPermission && callPermission

            if (permissionGranted) {

                smsList = getSms(context)

                callLogs = getCallLogs(context)
            }
        }

    LaunchedEffect(Unit) {

        launcher.launch(
            arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_CALL_LOG
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "SMS Messages",
            style =
                MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {

            items(smsList) { sms ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {

                        Text(text = sms.address)

                        Spacer(
                            modifier =
                                Modifier.height(5.dp)
                        )

                        Text(text = sms.body)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Call Logs",
            style =
                MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {

            items(callLogs) { call ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {

                        Text(text = call.number)

                        Spacer(
                            modifier =
                                Modifier.height(5.dp)
                        )

                        Text(
                            text =
                                "Duration: ${call.duration}"
                        )
                    }
                }
            }
        }
    }
}

fun getSms(
    context: Context
): List<SmsData> {

    val smsList = mutableListOf<SmsData>()

    val cursor =
        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            null,
            null,
            null,
            null
        )

    cursor?.use {

        while (it.moveToNext()) {

            val address =
                it.getString(
                    it.getColumnIndexOrThrow(
                        Telephony.Sms.ADDRESS
                    )
                )

            val body =
                it.getString(
                    it.getColumnIndexOrThrow(
                        Telephony.Sms.BODY
                    )
                )

            smsList.add(
                SmsData(address, body)
            )
        }
    }

    return smsList
}

fun getCallLogs(
    context: Context
): List<CallLogData> {

    val callList =
        mutableListOf<CallLogData>()

    val cursor =
        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            CallLog.Calls.DATE + " DESC"
        )

    cursor?.use {

        while (it.moveToNext()) {

            val number =
                it.getString(
                    it.getColumnIndexOrThrow(
                        CallLog.Calls.NUMBER
                    )
                )

            val duration =
                it.getString(
                    it.getColumnIndexOrThrow(
                        CallLog.Calls.DURATION
                    )
                )

            callList.add(
                CallLogData(number, duration)
            )
        }
    }

    return callList
}

data class CallLogData(
    val number: String,
    val duration: String
)

data class SmsData(

    val address: String,

    val body: String

)