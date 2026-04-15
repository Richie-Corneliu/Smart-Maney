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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
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
import com.kelompok4.smartmaney.ui.theme.SmBackgroundAlt
import com.kelompok4.smartmaney.ui.theme.SmCategoryFood
import com.kelompok4.smartmaney.ui.theme.SmCategoryFoodBg
import com.kelompok4.smartmaney.ui.theme.SmCategoryHealth
import com.kelompok4.smartmaney.ui.theme.SmCategoryHealthBg
import com.kelompok4.smartmaney.ui.theme.SmCategoryRent
import com.kelompok4.smartmaney.ui.theme.SmCategoryShopping
import com.kelompok4.smartmaney.ui.theme.SmCategoryShoppingBg
import com.kelompok4.smartmaney.ui.theme.SmCategoryTransportBg
import com.kelompok4.smartmaney.ui.theme.SmDivider
import com.kelompok4.smartmaney.ui.theme.SmMuted
import com.kelompok4.smartmaney.ui.theme.SmPrimary
import com.kelompok4.smartmaney.ui.theme.SmTextPrimary
import com.kelompok4.smartmaney.ui.theme.SmartManeyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseHistoryScreen(modifier: Modifier = Modifier, onBackClick: () -> Unit) {
    val now = remember { ExpenseHistoryDummyData.transactions.first().timestampMillis }
    var state by remember { mutableStateOf(buildExpenseHistoryState(ExpenseFilter.Daily, nowMillis = now)) }

    Scaffold(
        modifier = modifier,
        containerColor = SmBackgroundAlt,
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.expense_history_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SmTextPrimary) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Search,
                            stringResource(R.string.expense_history_search)
                        )
                    }
                })
        }
    ){ innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            Tabs(selected = state.selectedFilter) {
                state = reduceExpenseHistoryState(ExpenseHistoryAction.SelectFilter(it), nowMillis = now)
            }
            HorizontalDivider(color = SmDivider)
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
                state.groups.forEach { group ->
                    item(group.headerLabel) {
                        Spacer(Modifier.height(14.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(group.headerLabel, style = MaterialTheme.typography.labelMedium, color = SmMuted, fontWeight = FontWeight.SemiBold)
                            Text("-${formatRupiah(group.totalAmount)}", style = MaterialTheme.typography.labelMedium, color = SmMuted)
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    items(group.items, key = { it.id }) { item ->
                        ExpenseRow(item)
                        HorizontalDivider(color = SmDivider)
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
        Text(label, color = if (selected) SmPrimary else SmMuted, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, modifier = Modifier.padding(vertical = 10.dp))
        Box(Modifier.height(2.dp).width(82.dp).background(if (selected) SmPrimary else Color.Transparent))
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
            Text(item.title, style = MaterialTheme.typography.bodyLarge, color = SmTextPrimary, fontWeight = FontWeight.SemiBold)
            Text("${item.categoryLabel} · ${item.timeLabel}", style = MaterialTheme.typography.bodySmall, color = SmMuted)
        }
        Text("-${formatRupiah(item.amount)}", color = SmTextPrimary, fontWeight = FontWeight.Bold)
    }
}

private fun categoryVisual(category: ExpenseCategory): Triple<ImageVector, Color, Color> = when (category) {
    ExpenseCategory.Food -> Triple(Icons.Default.Restaurant, SmCategoryFoodBg, SmCategoryFood)
    ExpenseCategory.Transport -> Triple(Icons.Default.DirectionsCar, SmCategoryTransportBg, SmCategoryRent)
    ExpenseCategory.Shopping -> Triple(Icons.Default.ShoppingBag, SmCategoryShoppingBg, SmCategoryShopping)
    ExpenseCategory.Health -> Triple(Icons.Default.Medication, SmCategoryHealthBg, SmCategoryHealth)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewExpenseHistoryScreen() {
    SmartManeyTheme { ExpenseHistoryScreen(onBackClick = {}) }
}
