package com.o7solutions.android_compose.Screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
    FirstScreen()
}