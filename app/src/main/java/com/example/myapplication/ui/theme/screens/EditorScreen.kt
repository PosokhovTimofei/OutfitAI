package com.example.myapplication.ui.theme.screens
import android.graphics.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.MyApp
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.graphics.Color


@Composable
fun EditorScreen(
    itemId: Long,
    navController: NavController,
    vm: ClosetViewModel = viewModel(
        factory = ClosetViewModelFactory(
            (LocalContext.current.applicationContext as MyApp).repo
        )
    )
) {
    val items by vm.items.collectAsState()
    val item = items.find { it.id == itemId } ?: return

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val imageFile = remember(item.imageUri) {
        File(item.imageUri)
    }

    Scaffold(
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // ❌ Cancel / Back button (outlined style)
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Назад")
                    }

                    // 💙 Primary Save button (Google Material style)
                    Button(
                        onClick = {

                            val finalBitmap = bitmap
                                ?: BitmapFactory.decodeFile(imageFile.absolutePath)

                            val outFile = File(
                                imageFile.parentFile,
                                "edited_${System.currentTimeMillis()}.png"
                            )

                            FileOutputStream(outFile).use { stream ->
                                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                            }

                            vm.updateItem(
                                item.copy(
                                    imageUri = outFile.absolutePath
                                )
                            )

                            navController.popBackStack()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Сохранить")
                    }
                }
            }
        }
    ) { padding ->

        Box(modifier = Modifier.padding(padding)) {

            EraserEditor(
                file = imageFile,
                bitmap = bitmap,
                onBitmapChange = { bitmap = it },
                onSave = {},
                onInteractionChange = {}
            )
        }
    }
}