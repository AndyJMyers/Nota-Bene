package com.notabene.app

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
internal fun displayPreference(key: String): String {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("nota-bene-display", Context.MODE_PRIVATE) }
    var value by remember(key) { mutableStateOf(preferences.getString(key, "").orEmpty()) }
    DisposableEffect(preferences, key) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, changed ->
            if (changed == key) value = prefs.getString(key, "").orEmpty()
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        value = preferences.getString(key, "").orEmpty()
        onDispose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return value
}

internal fun saveDisplayPreference(context: Context, key: String, value: String) {
    context.getSharedPreferences("nota-bene-display", Context.MODE_PRIVATE).edit().putString(key, value).apply()
}

@Composable
internal fun OrderControls(position: Int, count: Int, onChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { onChange(position - 1) }, enabled = position > 0) { Text("EARLIER") }
        Text("${position + 1} of $count", color = MaterialTheme.colorScheme.onSurface)
        TextButton(onClick = { onChange(position + 1) }, enabled = position < count - 1) { Text("LATER") }
    }
}
