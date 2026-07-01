package com.codecraft.subvault.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.codecraft.subvault.ui.theme.SubVaultTheme

@Composable
fun ConfirmationDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmButtonText: String = "Confirm",
    dismissButtonText: String = "Cancel"
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title) },
            text = { Text(text = message) },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    }
                ) {
                    Text(text = confirmButtonText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = dismissButtonText)
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConfirmationDialogPreview() {
    SubVaultTheme {
        ConfirmationDialog(
            show = true,
            onDismiss = {},
            onConfirm = {},
            title = "Delete Subscription",
            message = "Are you sure you want to delete this subscription?"
        )
    }
}
