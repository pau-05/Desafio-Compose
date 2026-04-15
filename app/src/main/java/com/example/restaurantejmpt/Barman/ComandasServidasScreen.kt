package com.example.restaurantejmpt.Barman

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.restaurantejmpt.Camarero.ComandaViewModel

@Composable
fun ComandasServidasScreen(
    comandaViewModel: ComandaViewModel
) {
    val todasLasComandas by comandaViewModel.comandas.collectAsState()
    val comandasServidas = todasLasComandas.filter { it.servido }

    val totalRecaudado = comandasServidas.sumOf { it.total }

    Column(modifier = Modifier.fillMaxSize()) {

        Text(
            text = "Total recaudado: $totalRecaudado €",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        if (comandasServidas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay comandas servidas")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(comandasServidas) { comanda ->
                    ComandaCard(
                        comanda = comanda,
                        onServirClick = {}
                    )
                }
            }
        }
    }
}