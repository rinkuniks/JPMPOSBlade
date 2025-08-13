# Running JPMPOS Blade on Connected Devices

## Prerequisites

1. **Two Android devices** (phones, tablets, or emulators)
2. **Same WiFi network** for both devices
3. **Android Studio** installed on your development machine
4. **USB cables** or **ADB over WiFi** setup

## Step 1: Build the App

```bash
# In your project directory
./gradlew assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

## Step 2: Install on Device 1 (Server)

1. **Enable Developer Options** on Device 1:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings → Developer Options
   - Enable "USB Debugging"

2. **Connect Device 1** to your computer via USB

3. **Install the APK**:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

4. **Run the app** on Device 1

## Step 3: Install on Device 2 (Client)

1. **Enable Developer Options** on Device 2 (same steps as above)

2. **Connect Device 2** to your computer via USB

3. **Install the APK**:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

4. **Run the app** on Device 2

## Step 4: Test WebSocket Communication

### On Device 1 (Server):
1. Open the app
2. Navigate to "WebSocket Setup" screen
3. Enable "Server Mode" checkbox
4. Click "Start Server"
5. Note the displayed IP address (e.g., 192.168.1.100)

### On Device 2 (Client):
1. Open the app
2. Navigate to "WebSocket Setup" screen
3. Enable "Client Mode" checkbox
4. Enter server URL: `ws://192.168.1.100:8080` (use IP from Device 1)
5. Click "Connect"

## Step 5: Verify Connection

- **Device 1** should show: "Server Running" with "1 client connected"
- **Device 2** should show: "Connected" status
- Both devices should display the connection status in the overview section

## Step 6: Test Message Exchange

1. On Device 2, use the message input field to send a message
2. The message should be echoed back from the server
3. Try sending "PING" to test ping/pong functionality
4. Try sending "STATUS" to get server information

## Troubleshooting

### **Connection Issues**
- Ensure both devices are on the same WiFi network
- Check that the IP address is correct
- Verify the server is running on Device 1
- Check firewall settings

### **Permission Issues**
- Grant all requested permissions when prompted
- Check Android settings for network permissions
- Restart the app after granting permissions

### **Build Issues**
- Clean and rebuild: `./gradlew clean assembleDebug`
- Check Android Studio for any compilation errors
- Ensure all dependencies are properly synced

## Alternative: Using Emulators

If you don't have physical devices:

1. **Start two Android emulators** in Android Studio
2. **Install the APK** on both emulators:
   ```bash
   adb -s emulator-5554 install app/build/outputs/apk/debug/app-debug.apk
   adb -s emulator-5556 install app/build/outputs/apk/debug/app-debug.apk
   ```
3. **Use emulator IP addresses** (usually 10.0.2.2 for localhost)

## Network Configuration

The app automatically:
- Detects your local IP address
- Configures network security for local connections
- Handles WebSocket protocol implementation
- Manages connection states and error handling

## Logs and Debugging

To view logs from both devices:

```bash
# Device 1 logs
adb -s <device1-id> logcat | grep "UnifiedWebSocketService"

# Device 2 logs  
adb -s <device2-id> logcat | grep "UnifiedWebSocketService"
```

## Success Indicators

✅ **Server Running**: Device 1 shows "Server Running" status
✅ **Client Connected**: Device 2 shows "Connected" status  
✅ **Message Echo**: Messages sent from Device 2 are echoed back
✅ **Ping/Pong**: PING messages receive PONG responses
✅ **Status Updates**: STATUS requests return server information

## Next Steps

Once WebSocket communication is working:
1. Test with more complex message types
2. Implement custom message handling
3. Add message persistence
4. Implement reconnection logic
5. Add security features for production use
