package relanto.jpn.nrf.utils

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Example usage of Common Components
 * This file demonstrates how to use the common button and text components
 */
@Composable
fun CommonComponentsExample() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Text Example
        CommonTitleText(
            text = "Common Components Demo",
            modifier = Modifier.fillMaxWidth()
        )
        
        // Subtitle Text Example
        CommonSubtitleText(
            text = "Reusable UI components for consistent design",
            modifier = Modifier.fillMaxWidth()
        )
        
        // Solid Button Examples
        CommonSolidButton(
            text = "Primary Action",
            onClick = { /* Handle click */ },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.Check
        )
        
        CommonSolidButton(
            text = "Secondary Action",
            onClick = { /* Handle click */ },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.Star
        )
        
        // Outlined Button Examples
        CommonOutlinedButton(
            text = "Outlined Action",
            onClick = { /* Handle click */ },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.Info
        )
        
        CommonOutlinedButton(
            text = "Another Outlined Action",
            onClick = { /* Handle click */ },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.Settings
        )
        
        // Text Examples
        CommonTextView(
            text = "This is a regular text component with custom styling",
            modifier = Modifier.fillMaxWidth()
        )
        
        CommonCaptionText(
            text = "This is a caption text component",
            modifier = Modifier.fillMaxWidth()
        )
        
        // Disabled Button Example
        CommonSolidButton(
            text = "Disabled Button",
            onClick = { /* Won't execute */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            icon = Icons.Default.Close
        )
    }
}
