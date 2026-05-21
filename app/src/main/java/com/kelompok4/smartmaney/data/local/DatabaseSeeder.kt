package com.kelompok4.smartmaney.data.local

import com.kelompok4.smartmaney.data.local.entity.BudgetCategoryEntity
import com.kelompok4.smartmaney.data.local.entity.BudgetMetaEntity
import com.kelompok4.smartmaney.data.local.entity.TransactionEntity
import com.kelompok4.smartmaney.data.local.entity.WalletMetaEntity
import java.util.Calendar

class DatabaseSeeder(private val database: SmartManeyDatabase) {

    suspend fun seedIfEmpty() {
        seedTransactions()
        seedBudget()
        seedWallet()
    }

    private suspend fun seedTransactions() {
        val dao = database.transactionDao()
        if (dao.countTransactions() > 0) return
        currentMonthTransactions().forEach { dao.insertTransaction(it) }
        previousMonthTransactions().forEach { dao.insertTransaction(it) }
    }

    private suspend fun seedBudget() {
        val dao = database.budgetDao()
        if (dao.countBudgetCategories() > 0) return
        dao.upsertBudgetMeta(BudgetMetaEntity(totalBudget = 5_000_000))
        dao.upsertBudgetCategories(
            listOf(
                BudgetCategoryEntity(id = "food",      name = "Food",      allocated = 1_500_000),
                BudgetCategoryEntity(id = "transport", name = "Transport", allocated = 500_000),
                BudgetCategoryEntity(id = "shopping",  name = "Shopping",  allocated = 800_000),
                BudgetCategoryEntity(id = "bills",     name = "Bills",     allocated = 600_000),
                BudgetCategoryEntity(id = "health",    name = "Health",    allocated = 400_000),
                BudgetCategoryEntity(id = "other",     name = "Lain-lain", allocated = 200_000),
            )
        )
    }

    private suspend fun seedWallet() {
        val dao = database.walletDao()
        if (dao.getWalletMeta() != null) return
        dao.upsertWalletMeta(WalletMetaEntity(initialBalance = 10_000_000))
    }

    // ~1,000,000 total — spread across 5 days so trend vs. last month shows clearly
    private fun currentMonthTransactions(): List<TransactionEntity> {
        val base = monthStart(offsetMonths = 0)
        return listOf(
            expense("Lunch Warung Padang",    35_000, "Food",      "Cash",        "Nasi padang + es teh",        base.day(1, 13)),
            expense("Grab Car",               45_000, "Transport", "GoPay",       "Pergi kantor",                base.day(1,  8)),
            expense("Indomaret",             120_000, "Food",      "Debit",       "Belanja mingguan",            base.day(2, 10)),
            expense("Listrik PLN",           350_000, "Bills",     "Transfer",    "Tagihan bulan ini",           base.day(2, 14)),
            expense("Dinner KFC",             75_000, "Food",      "Credit Card", "Makan malam keluarga",        base.day(3, 19)),
            expense("Gojek",                  28_000, "Transport", "GoPay",       "Pulang kerja",                base.day(3, 18)),
            expense("Apotek Kimia Farma",     85_000, "Health",    "Cash",        "Vitamin dan obat",            base.day(4, 11)),
            expense("Mie Gacoan",             42_000, "Food",      "Cash",        "Makan siang",                 base.day(4, 12)),
            expense("Grab Motor",             22_000, "Transport", "OVO",         "Ke mal",                      base.day(5, 15)),
            expense("H&M",                   180_000, "Shopping",  "Credit Card", "Kaos casual",                 base.day(5, 16)),
            expense("Kopi Kenangan",          48_000, "Food",      "GoPay",       "Kopi + sandwich x2",          base.day(5,  9)),
        )
    }

    // ~2,300,000 total last month — intentionally higher to show downward trend in suggestions
    private fun previousMonthTransactions(): List<TransactionEntity> {
        val base = monthStart(offsetMonths = -1)
        return listOf(
            // Food ~850k
            expense("Warteg Bu Sari",         25_000, "Food",      "Cash",        "Makan siang",                 base.day(1,  13)),
            expense("Alfamart",               95_000, "Food",      "Debit",       "Belanja mingguan",            base.day(3,  10)),
            expense("Pizza Hut",             120_000, "Food",      "Credit Card", "Dinner bareng teman",         base.day(7,  19)),
            expense("Warung Makan",           30_000, "Food",      "Cash",        "Sarapan",                     base.day(10,  8)),
            expense("Superindo",             210_000, "Food",      "Debit",       "Groceries bulanan",           base.day(15, 11)),
            expense("McDonalds",              65_000, "Food",      "GoPay",       "Lunch",                       base.day(20, 13)),
            expense("Bakso Pak Man",          35_000, "Food",      "Cash",        "Makan sore",                  base.day(25, 17)),
            expense("Kopi Kenangan",          38_000, "Food",      "GoPay",       "Kopi pagi",                   base.day(28,  8)),
            expense("Hokben",                 55_000, "Food",      "GoPay",       "Makan siang berdua",          base.day(12, 12)),
            expense("Starbucks",              72_000, "Food",      "Credit Card", "Kerja dari kafe",             base.day(18, 10)),
            expense("Pasar Tradisional",     105_000, "Food",      "Cash",        "Sayur dan lauk",              base.day(22,  7)),
            // Transport ~350k
            expense("Grab Car",               55_000, "Transport", "GoPay",       "Ke bandara",                  base.day(5,   6)),
            expense("Gojek",                  32_000, "Transport", "GoPay",       "Pergi kantor",                base.day(8,   8)),
            expense("Grab Motor",             18_000, "Transport", "OVO",         "Pulang kerja",                base.day(12, 18)),
            expense("Commuter Line",          12_000, "Transport", "E-Money",     "Naik KRL",                    base.day(18,  7)),
            expense("Parkir Mal",             15_000, "Transport", "Cash",        "Parkir",                      base.day(22, 14)),
            expense("Ojek Online",            28_000, "Transport", "GoPay",       "Antar jemput",                base.day(27,  8)),
            expense("Bus TransJakarta",        8_000, "Transport", "E-Money",     "Ke pusat kota",               base.day(29,  9)),
            // Shopping ~500k
            expense("Uniqlo",                250_000, "Shopping",  "Credit Card", "Kemeja baru",                 base.day(6,  15)),
            expense("Tokopedia",             180_000, "Shopping",  "Transfer",    "Aksesori gadget",             base.day(14, 10)),
            expense("Miniso",                 75_000, "Shopping",  "GoPay",       "Alat tulis",                  base.day(21, 16)),
            // Bills ~450k
            expense("PDAM",                   95_000, "Bills",     "Transfer",    "Tagihan air",                 base.day(2,   9)),
            expense("Listrik PLN",           320_000, "Bills",     "Transfer",    "Tagihan listrik",             base.day(3,  14)),
            // Health ~200k
            expense("RS Hermina",            150_000, "Health",    "Debit",       "Konsultasi dokter umum",      base.day(9,  10)),
            expense("Guardian",               55_000, "Health",    "GoPay",       "Skincare & vitamin",          base.day(16, 15)),
        )
    }

    private fun expense(
        title: String,
        amount: Int,
        category: String,
        paymentMethod: String,
        note: String,
        createdAtMillis: Long
    ) = TransactionEntity(
        title = title,
        amount = amount,
        type = EXPENSE,
        category = category,
        note = note,
        paymentMethod = paymentMethod,
        createdAtMillis = createdAtMillis
    )

    private fun monthStart(offsetMonths: Int): Calendar {
        return Calendar.getInstance().apply {
            add(Calendar.MONTH, offsetMonths)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun Calendar.day(dayOfMonth: Int, hour: Int): Long {
        return (clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, hour)
        }.timeInMillis
    }

    companion object {
        private const val EXPENSE = "EXPENSE"
    }
}