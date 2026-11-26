package com.example.t212watch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import java.util.Locale

private val Gain = Color(0xFF4CAF50)
private val Loss = Color(0xFFF44336)

private fun money(v: Double): String = String.format(Locale.US, "%,.2f", v)
private fun signed(v: Double): String = (if (v >= 0) "+" else "") + money(v)
private fun pplColor(v: Double): Color = if (v >= 0) Gain else Loss

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PortfolioScreen()
            }
        }
    }
}

@Composable
fun PortfolioScreen(viewModel: PortfolioViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
    ) {
        when (val s = state) {
            is PortfolioUiState.Loading -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }

            is PortfolioUiState.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(s.message, textAlign = TextAlign.Center)
                Chip(
                    modifier = Modifier.padding(top = 12.dp),
                    label = { Text("Retry") },
                    onClick = { viewModel.refresh() },
                    colors = ChipDefaults.primaryChipColors(),
                )
            }

            is PortfolioUiState.Success -> ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 32.dp),
            ) {
                item { ListHeader { Text("Trading 212") } }

                s.cash?.let { cash ->
                    item { SummaryCard(cash) }
                }

                items(s.positions, key = { it.ticker }) { position ->
                    PositionChip(position)
                }

                item {
                    Chip(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        label = {
                            Text("Refresh", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        },
                        onClick = { viewModel.refresh() },
                        colors = ChipDefaults.secondaryChipColors(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(cash: AccountCash) {
    Card(onClick = {}, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            cash.total?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Total", style = MaterialTheme.typography.caption2)
                    Text(money(it), style = MaterialTheme.typography.caption1)
                }
            }
            cash.invested?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Invested", style = MaterialTheme.typography.caption2)
                    Text(money(it), style = MaterialTheme.typography.caption1)
                }
            }
            cash.ppl?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("P/L", style = MaterialTheme.typography.caption2)
                    Text(signed(it), style = MaterialTheme.typography.caption1, color = pplColor(it))
                }
            }
        }
    }
}

@Composable
private fun PositionChip(position: Position) {
    Chip(
        modifier = Modifier.fillMaxWidth(),
        onClick = {},
        colors = ChipDefaults.secondaryChipColors(),
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    position.displayTicker,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(money(position.currentPrice))
            }
        },
        secondaryLabel = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(String.format(Locale.US, "%.4f", position.quantity).trimEnd('0').trimEnd('.') + " sh")
                Text(signed(position.ppl), color = pplColor(position.ppl))
            }
        },
    )
}
