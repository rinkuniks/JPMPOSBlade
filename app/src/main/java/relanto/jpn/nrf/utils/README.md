# Utils Package - Common UI Components

This package contains reusable UI components that provide consistent styling and behavior across the JPMPOS Blade application.

## Components Overview

### 🎯 Common Buttons

#### `CommonSolidButton`
A solid button component with customizable text, icon, and styling.

**Parameters:**
- `text: String` - Button text
- `onClick: () -> Unit` - Click handler
- `modifier: Modifier` - Compose modifier (optional)
- `enabled: Boolean` - Whether button is enabled (default: true)
- `icon: ImageVector?` - Optional icon (default: null)
- `iconSpacing: Dp` - Spacing between icon and text (default: 8.dp)

**Usage:**
```kotlin
CommonSolidButton(
    text = "Save",
    onClick = { /* Handle save */ },
    icon = Icons.Default.Save,
    modifier = Modifier.fillMaxWidth()
)
```

#### `CommonOutlinedButton`
An outlined button component with customizable text, icon, and styling.

**Parameters:**
- Same as `CommonSolidButton`

**Usage:**
```kotlin
CommonOutlinedButton(
    text = "Cancel",
    onClick = { /* Handle cancel */ },
    icon = Icons.Default.Close,
    modifier = Modifier.fillMaxWidth()
)
```

### 📝 Common Text Components

#### `CommonTextView`
A base text component with customizable styling and alignment.

**Parameters:**
- `text: String` - Text content
- `modifier: Modifier` - Compose modifier (optional)
- `style: TextStyle` - Text style (default: MaterialTheme.typography.bodyMedium)
- `color: Color` - Text color (default: MaterialTheme.colorScheme.onSurface)
- `fontWeight: FontWeight?` - Font weight (optional)
- `textAlign: TextAlign` - Text alignment (default: TextAlign.Start)
- `maxLines: Int` - Maximum lines (default: Int.MAX_VALUE)

#### `CommonTitleText`
A title text component with consistent styling.

**Usage:**
```kotlin
CommonTitleText(
    text = "Welcome to JPMPOS Blade",
    modifier = Modifier.fillMaxWidth()
)
```

#### `CommonSubtitleText`
A subtitle text component with consistent styling.

**Usage:**
```kotlin
CommonSubtitleText(
    text = "Your POS solution",
    modifier = Modifier.fillMaxWidth()
)
```

#### `CommonCaptionText`
A caption text component with consistent styling.

**Usage:**
```kotlin
CommonCaptionText(
    text = "Additional information",
    modifier = Modifier.fillMaxWidth()
)
```

## 🎨 Design Principles

- **Consistency**: All components follow Material3 design guidelines
- **Customizability**: Components accept standard Compose modifiers and styling
- **Accessibility**: Built-in support for content descriptions and proper contrast
- **Performance**: Lightweight components with minimal overhead

## 📱 Usage Examples

### Basic Button Usage
```kotlin
Column {
    CommonSolidButton(
        text = "Primary Action",
        onClick = { /* Handle action */ }
    )
    
    CommonOutlinedButton(
        text = "Secondary Action",
        onClick = { /* Handle action */ }
    )
}
```

### Text Component Usage
```kotlin
Column {
    CommonTitleText(text = "Page Title")
    CommonSubtitleText(text = "Page description")
    CommonTextView(text = "Regular content")
    CommonCaptionText(text = "Footer information")
}
```

### With Icons
```kotlin
CommonSolidButton(
    text = "Save Changes",
    onClick = { /* Save */ },
    icon = Icons.Default.Save,
    modifier = Modifier.fillMaxWidth()
)
```

### Custom Styling
```kotlin
CommonTextView(
    text = "Custom styled text",
    style = MaterialTheme.typography.headlineSmall,
    color = MaterialTheme.colorScheme.primary,
    fontWeight = FontWeight.Bold,
    textAlign = TextAlign.Center
)
```

## 🔧 Integration

To use these components in your screens:

1. **Import the components:**
```kotlin
import relanto.jpn.nrf.utils.*
```

2. **Use them in your Composable:**
```kotlin
@Composable
fun MyScreen() {
    Column {
        CommonTitleText(text = "My Screen")
        CommonSolidButton(
            text = "Action",
            onClick = { /* Handle action */ }
        )
    }
}
```

## 🚀 Benefits

- **DRY Principle**: No more duplicating button and text code
- **Consistent UI**: All screens will have uniform appearance
- **Easy Maintenance**: Update styling in one place
- **Better UX**: Consistent interaction patterns across the app
- **Developer Experience**: Faster development with pre-built components

## 📋 Future Enhancements

- [ ] Add more button variants (text buttons, icon buttons)
- [ ] Include loading states for buttons
- [ ] Add animation support
- [ ] Create theme-aware color schemes
- [ ] Add more text variants (code, quote, etc.)
