package com.kelompok4.smartmaney.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.kelompok4.smartmaney.R
import com.kelompok4.smartmaney.ui.theme.SmPrimary
import com.kelompok4.smartmaney.ui.theme.SmTerms
import com.kelompok4.smartmaney.ui.theme.SmTextHeading
import com.kelompok4.smartmaney.ui.theme.SmartManeyTheme

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onGoogleClick: () -> Unit = {},
    isGoogleSigningIn: Boolean = false,
    googleSignInError: String? = null
) {
    Scaffold(

    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 36.dp)
                .padding(horizontal = 28.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally, // Cross-axis (Horizontal)
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineMedium,
                color = SmTextHeading,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.revenue_anim)
            )

            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(180.dp)
            )
            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(26.dp))

            ActionButton(
                label = if (isGoogleSigningIn) {
                    stringResource(R.string.google_signing_in)
                } else {
                    stringResource(R.string.continue_with_google)
                },
                containerColor = Color.White,
                contentColor = SmTextHeading,
                prefix = stringResource(R.string.google_icon_placeholder),
                onClick = onGoogleClick,
                enabled = !isGoogleSigningIn
            )

            if (!googleSignInError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = googleSignInError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(26.dp))
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.terms_prefix))
                    append("\n")
                    withStyle(style = SpanStyle(color = SmPrimary)) {
                        append(stringResource(R.string.terms_of_service))
                    }
                    append(" ")
                    append(stringResource(R.string.and_separator))
                    append(" ")
                    withStyle(style = SpanStyle(color = SmPrimary)) {
                        append(stringResource(R.string.privacy_policy))
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = SmTerms,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
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
    prefix: String? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
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
            Image(
                painterResource(R.drawable.g_logo),
                contentDescription = "Google Logo"
            )
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


