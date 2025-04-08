package com.vector.bnh.mealsonwheels.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vector.bnh.mealsonwheels.viewmodel.DeliveryViewModel
import com.vector.bnh.mealsonwheels.R
import com.vector.bnh.mealsonwheels.model.DeliveryItem
import com.vector.bnh.mealsonwheels.model.DeliveryStatus

@Composable
fun DeliveryScreen(viewModel: DeliveryViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val deliveries = if (uiState is DeliveryUiState.Recognized) {
        (uiState as DeliveryUiState.Recognized).deliveries
    } else {
        emptyList()
    }

    var selectedImage by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val stream = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(stream)
            selectedImage = bitmap
            viewModel.recognizeTextFromImage(context, bitmap)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { imagePicker.launch("image/*") }) {
            Text(stringResource(R.string.btn_pick_image))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { viewModel.sortByDistance(context) }) {
            Text(stringResource(R.string.btn_sort_route))
        }

        Spacer(modifier = Modifier.height(12.dp))

        selectedImage?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.height(150.dp))
        }

        if (deliveries.isEmpty()) {
            Text(stringResource(R.string.no_data))
        } else {
            LazyColumn {
                itemsIndexed(deliveries) { index, item: DeliveryItem ->
                    Card(modifier = Modifier.padding(8.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(stringResource(R.string.label_name, item.name))
                            Text(stringResource(R.string.label_address, item.address))
                            item.phone?.let { Text(stringResource(R.string.label_phone, it)) }
                            Text(stringResource(R.string.label_meal, item.mealContent))
                            item.specialNote?.let { Text(stringResource(R.string.label_note, it)) }

                            val statusText = stringResource(
                                if (item.status == DeliveryStatus.NotDelivered)
                                    R.string.status_not_delivered
                                else
                                    R.string.status_delivered
                            )
                            Text(stringResource(R.string.label_status, statusText))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(onClick = { viewModel.toggleStatus(index) }) {
                                    Text(stringResource(
                                        if (item.status == DeliveryStatus.NotDelivered)
                                            R.string.btn_mark_delivered
                                        else
                                            R.string.btn_mark_undelivered
                                    ))
                                }

                                Button(onClick = {
                                    val uri = Uri.parse("geo:0,0?q=${Uri.encode(item.address)}")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    intent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(intent)
                                }) {
                                    Text(stringResource(R.string.btn_navigate))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
