package com.kelompok4.smartmaney.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kelompok4.smartmaney.R
import com.kelompok4.smartmaney.ui.theme.SmartManeyTheme
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.withStyle

private val ScreenBackground = Color(0xFFE2E2E2)
private val CardBackground = Color(0xFFF3F3F3)
private val LoginGreen = Color(0xFF13D340)
private val SecondaryButton = Color(0xFFE9ECEF)
private val TermsGray = Color(0xFF9AA3B2)
private val DividerGray = Color(0xFFB1BACC)
private val Heading = Color(0xFF243047)

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .background(ScreenBackground)
            .padding(horizontal = 24.dp, vertical = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp),
            shape = RoundedCornerShape(34.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.login_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Heading,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(26.dp))

                ActionButton(
                    label = stringResource(R.string.login_action),
                    containerColor = LoginGreen,
                    contentColor = Color.Black,
                    onClick = onLoginClick
                )
                Spacer(modifier = Modifier.height(16.dp))
                ActionButton(
                    label = stringResource(R.string.register_action),
                    containerColor = SecondaryButton,
                    contentColor = Heading,
                    onClick = onRegisterClick
                )
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier  = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = DividerGray
                    )
                    Text(
                        text = stringResource(R.string.or_separator),
                        color = DividerGray,
                        style = MaterialTheme.typography.labelLarge,
                        letterSpacing = 2.sp
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = DividerGray
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))

                ActionButton(
                    label = stringResource(R.string.continue_with_google),
                    containerColor = Color.White,
                    contentColor = Heading,
                    prefix = stringResource(R.string.google_icon_placeholder),
                    onClick = onGoogleClick
                )

                Spacer(modifier = Modifier.height(26.dp))
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.terms_prefix))
                        append("\n")
                        withStyle(style = SpanStyle(color = LoginGreen)) {
                            append(stringResource(R.string.terms_of_service))
                        }
                        append(" ")
                        append(stringResource(R.string.and_separator))
                        append(" ")
                        withStyle(style = SpanStyle(color = LoginGreen)) {
                            append(stringResource(R.string.privacy_policy))
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TermsGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    prefix: String? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        if (prefix != null) {
            Text(text = prefix, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(text = label, style = MaterialTheme.typography.titleMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    SmartManeyTheme {
        LoginScreen(modifier = Modifier.fillMaxSize())
    }
}


