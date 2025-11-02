package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                YouTubeUIScreen()
            } }
    }
}

@Composable
fun YouTubeUIScreen() {
    Image(
        painter = painterResource(id = R.drawable.youtube_ui), // Make sure your file is named youtube_ui.png
        contentDescription = "YouTube UI design",
        modifier = Modifier.fillMaxSize()
    )
}

@Preview(showBackground = true)
@Composable
fun YouTubeUIScreenPreview() {
    MyApplicationTheme {
        YouTubeUIScreen()
    }
}


