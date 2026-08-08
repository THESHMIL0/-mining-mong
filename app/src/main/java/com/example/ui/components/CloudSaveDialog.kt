package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.GameStateEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SapphireBlue
import kotlinx.coroutines.launch

@Composable
fun CloudSaveDialog(
    state: GameStateEntity,
    onExportSave: suspend () -> String,
    onImportSave: suspend (String) -> Boolean,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var exportedCode by remember { mutableStateOf("") }
    var inputCode by remember { mutableStateOf("") }
    var importMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(SapphireBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Cloud Save",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Cloud Save Sync", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        },
        text = {
            Column {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A2B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Cloud Passkey Code:", color = Color.Gray, fontSize = 12.sp)
                            Text(state.cloudSyncCode, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Synced", tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Auto-Synced", color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        scope.launch {
                            exportedCode = onExportSave()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_save_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue, contentColor = Color.White)
                ) {
                    Text("Generate Backup String", fontWeight = FontWeight.Bold)
                }

                if (exportedCode.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportedCode,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Backup JSON String") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Restore Save Data", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { inputCode = it },
                    placeholder = { Text("Paste Save Code Here") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restore_code_input")
                )

                if (importMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = importMessage!!,
                        fontSize = 12.sp,
                        color = if (importMessage!!.contains("Success")) EmeraldGreen else Color(0xFFFF5252)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val success = onImportSave(inputCode)
                            importMessage = if (success) "Successfully Restored Save Data!" else "Invalid Save Code!"
                        }
                    },
                    enabled = inputCode.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_save_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.Black)
                ) {
                    Text("Restore Game Save", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}
