package com.vector.bnh.mealsonwheels

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.vector.bnh.mealsonwheels.ui.DeliveryScreen
import com.vector.bnh.mealsonwheels.viewmodel.DeliveryViewModel
import com.vector.bnh.mealsonwheels.ui.theme.MealsOnWheelsTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DeliveryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MealsOnWheelsTheme {
                DeliveryScreen(viewModel)
            }
        }
    }
}
