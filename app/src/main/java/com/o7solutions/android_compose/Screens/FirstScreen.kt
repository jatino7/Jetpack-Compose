package com.o7solutions.android_compose.Screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


data class Task(
    var id: String ?= null,
    var name: String ?= null
)
@Composable
fun FirstScreen() {


    var email by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(


        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)


    ){
        Text(
            text = "Jetpack Compose",
            color = Color.Red,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()


        )


        Button(
            onClick =  {

            }
        ) {

            Text("Press Me")
        }

        Row(

        ) {

            Button(
                onClick =  {

                }
            ) {

                Text("Press Me")
            }

            Button(
                onClick =  {

                    Toast.makeText(context, "hello", Toast.LENGTH_SHORT).show()
                }
            ) {

                Text("Press Me")
            }


        }



    }
}

@Preview
@Composable
fun previewComponent() {
//    FirstScreen()

    DayTwoScreen()
}


@Composable
fun DayTwoScreen() {


    var tasks = List(20) { i->
        Task("$i","this is process for $i")
    }
    Scaffold(

        topBar = {
            Text("This is top bar")
        },

        bottomBar = {},

        floatingActionButton = {
            FloatingActionButton(
                onClick =  {

                }
            ) {
                Icon(Icons.Default.Email, contentDescription = "email")
            }
        }


    ) { innerPadding->


        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {

            Text("Hello")

            LazyColumn(

                modifier = Modifier.fillMaxWidth()
            ) {

                items(tasks.size) { task->

                   listItem(tasks[task])
                }

            }





        }

    }
}


@Composable
fun listItem(task: Task) {
    Column {

        Text(task.id.toString())
        Text(task.name.toString())

        Divider()
    }
}