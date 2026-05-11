package com.kelompok4.smartmaney.ui.wallet

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

class CurrencyVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {

        val originalText = text.text

        if (originalText.isEmpty()) {
            return TransformedText(
                AnnotatedString(""),
                OffsetMapping.Identity
            )
        }

        val formatted = NumberFormat
            .getNumberInstance(Locale("in", "ID"))
            .format(originalText.toLong())

        val offsetMapping = object : OffsetMapping {

            override fun originalToTransformed(offset: Int): Int {
                return formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                return originalText.length
            }
        }

        return TransformedText(
            AnnotatedString(formatted),
            offsetMapping
        )
    }
}