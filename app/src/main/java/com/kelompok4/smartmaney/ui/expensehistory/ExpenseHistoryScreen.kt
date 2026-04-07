package com.kelompok4.smartmaney.ui.expensehistory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kelompok4.smartmaney.R
import com.kelompok4.smartmaney.ui.theme.SmartManeyTheme

private val Bg = Color(0xFFF5F5F5)
private val Txt = Color(0xFF1E2430)
private val Muted = Color(0xFF738097)
private val Accent = Color(0xFF29C35A)
private val Line = Color(0xFFE8EAF0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseHistoryScreen(modifier: Modifier = Modifier, onBackClick: () -> Unit) {
    val now = remember { ExpenseHistoryDummyData.transactions.first().timestampMillis }
    var state by remember { mutableStateOf(buildExpenseHistoryState(ExpenseFilter.Daily, nowMillis = now)) }

    Scaffold(
        modifier = modifier,
        containerColor = Bg,
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.expense_history_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Txt) }, navigationIcon = {
                IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.expense_history_back)) }
            }, actions = {
                IconButton(onClick = {}) { Icon(Icons.Default.Search, stringResource(R.string.expense_history_search)) }
            })
        }
    ){ innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            Tabs(selected = state.selectedFilter) {
                state = reduceExpenseHistoryState(ExpenseHistoryAction.SelectFilter(it), nowMillis = now)
            }
            HorizontalDivider(color = Line)
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
                state.groups.forEach { group ->
                    item(group.headerLabel) {
                        Spacer(Modifier.height(14.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(group.headerLabel, style = MaterialTheme.typography.labelMedium, color = Muted, fontWeight = FontWeight.SemiBold)
                            Text("-${formatRupiah(group.totalAmount)}", style = MaterialTheme.typography.labelMedium, color = Muted)
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    items(group.items, key = { it.id }) { item ->
                        ExpenseRow(item)
                        HorizontalDivider(color = Line)
                    }
                }
                item { Spacer(Modifier.height(18.dp)) }
            }
        }
    }
}

@Composable
private fun Tabs(selected: ExpenseFilter, onSelect: (ExpenseFilter) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        TabItem(stringResource(R.string.expense_history_daily), selected == ExpenseFilter.Daily) { onSelect(ExpenseFilter.Daily) }
        TabItem(stringResource(R.string.expense_history_weekly), selected == ExpenseFilter.Weekly) { onSelect(ExpenseFilter.Weekly) }
        TabItem(stringResource(R.string.expense_history_monthly), selected == ExpenseFilter.Monthly) { onSelect(ExpenseFilter.Monthly) }
    }
}

@Composable
private fun TabItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Text(label, color = if (selected) Accent else Muted, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, modifier = Modifier.padding(vertical = 10.dp))
        Box(Modifier.height(2.dp).width(82.dp).background(if (selected) Accent else Color.Transparent))
    }
}

@Composable
private fun ExpenseRow(item: ExpenseTransaction) {
    val (icon, bg, tint) = categoryVisual(item.category)
    Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, item.categoryLabel, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyLarge, color = Txt, fontWeight = FontWeight.SemiBold)
            Text("${item.categoryLabel} · ${item.timeLabel}", style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        Text("-${formatRupiah(item.amount)}", color = Txt, fontWeight = FontWeight.Bold)
    }
}

private fun categoryVisual(category: ExpenseCategory): Triple<ImageVector, Color, Color> = when (category) {
    ExpenseCategory.Food -> Triple(Icons.Default.Restaurant, Color(0xFFFFF0DD), Color(0xFFF08726))
    ExpenseCategory.Transport -> Triple(Icons.Default.DirectionsCar, Color(0xFFE8F0FF), Color(0xFF3A73E8))
    ExpenseCategory.Shopping -> Triple(Icons.Default.ShoppingBag, Color(0xFFF2E9FF), Color(0xFF8A4BE8))
    ExpenseCategory.Health -> Triple(Icons.Default.Medication, Color(0xFFE1F7E9), Color(0xFF2FA55E))
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewExpenseHistoryScreen() {
    SmartManeyTheme { ExpenseHistoryScreen(onBackClick = {}) }
}
