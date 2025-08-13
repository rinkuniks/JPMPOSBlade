# WebSocket Communication Setup Guide

## Overview
This guide explains how to set up and test WebSocket communication in the JPMPOS Blade application. The WebSocket implementation has been completely rewritten as a unified service that handles both server and client functionality in a single codebase.

## What Was Fixed

### 1. **Unified WebSocket Service**
- Created a single `UnifiedWebSocketService` that handles both server and client functionality
- Eliminated the need for separate services and complex dependency injection
- Both server and client use the same local IP address detection

### 2. **Proper WebSocket Server Implementation**
- Uses the Java-WebSocket library for proper WebSocket protocol implementation
- Implements proper WebSocket handshake protocol
- Added proper connection state management

### 3. **Network Configuration**
- Added proper network security configuration for local network access
- Added required network permissions (ACCESS_WIFI_STATE, CHANGE_WIFI_STATE)
- Configured cleartext traffic for local WebSocket connections

### 4. **Error Handling**
- Improved error handling and user feedback
- Added comprehensive logging for debugging
- Better connection state management

## How to Use

### 1. **Start the WebSocket Server**
1. Open the app and navigate to the WebSocket Setup screen
2. Enable "Server Mode" by checking the checkbox
3. Click "Start Server" button
4. The app will automatically detect your local IP address and start the server
5. You'll see a toast message confirming the server started

### 2. **Connect as a Client**
1. Enable "Client Mode" by checking the checkbox
2. The server URL will be automatically populated with your local server address
3. Click "Connect" button
4. The client will attempt to connect to the server
5. Once connected, you'll see "Connected" status

### 3. **Test Communication**
- Send messages using the input field
- Use the "Send PING" button to test ping/pong functionality
- Use the "Send STATUS" button to get server status
- Messages will be echoed back from the server

## Testing Between Devices

### **Device 1 (Server)**
1. Install and run the app on the first device
2. Navigate to WebSocket Setup screen
3. Enable Server Mode and click "Start Server"
4. Note the displayed IP address (e.g., 192.168.1.100)

### **Device 2 (Client)**
1. Install and run the app on the second device
2. Navigate to WebSocket Setup screen
3. Enable Client Mode
4. Enter the server URL: `ws://192.168.1.100:8080` (use the IP from Device 1)
5. Click "Connect"

### **Verification**
- Both devices should show connection status
- Device 1 should show "1 client connected"
- Device 2 should show "Connected" status
- Messages sent from Device 2 should be echoed back

## Troubleshooting

### **Common Issues and Solutions**

#### 1. **Server Won't Start**
- **Problem**: "Failed to get local IP address" error
- **Solution**: Ensure your device is connected to a WiFi network
- **Check**: Look at the logs for network interface detection

#### 2. **Client Can't Connect**
- **Problem**: Connection refused or timeout
- **Solution**: 
  - Verify the server is running on Device 1
  - Check that the IP address is correct
  - Ensure both devices are on the same WiFi network
  - Check firewall settings

#### 3. **Permission Denied**
- **Problem**: Network access denied
- **Solution**: 
  - Grant all requested permissions
  - Check Android settings for network permissions
  - Restart the app after granting permissions

#### 4. **Connection Drops**
- **Problem**: WebSocket connection closes unexpectedly
- **Solution**: 
  - Check network stability
  - Verify server is still running
  - Use the reconnect button

### **Debug Information**
The app provides comprehensive logging. Check Logcat with these tags:
- `UnifiedWebSocketService`: All WebSocket-related logs
- `WebSocketSetupViewModel`: UI state logs

## Network Requirements

### **Local Network Setup**
- Both devices must be on the same WiFi network
- The server device must have a local IP address (192.168.x.x, 10.x.x.x, or 172.16.x.x)
- Port 8080 must be available and not blocked by firewall

### **Firewall Configuration**
- Windows: Allow Java/Android apps through Windows Firewall
- Router: Ensure port 8080 is not blocked
- Android: Grant network permissions to the app

## Security Considerations

### **Local Development Only**
- This implementation is for local network testing only
- Uses cleartext WebSocket (ws://) not secure (wss://)
- No authentication or encryption
- Do not use in production environments

### **Production Recommendations**
- Use secure WebSocket (wss://) with SSL/TLS
- Implement proper authentication
- Add rate limiting and connection validation
- Use a production-grade WebSocket server

## API Reference

### **Server Messages**
- `PING` → Responds with `PONG`
- `STATUS` → Responds with server status
- Any other message → Echoed back with "Echo: " prefix

### **Client Events**
- `onOpen`: Connection established
- `onMessage`: Message received
- `onClose`: Connection closed
- `onError`: Connection error

## Performance Notes

### **Connection Limits**
- Server supports multiple concurrent clients
- Each client connection runs in a separate thread
- Automatic cleanup of disconnected clients

### **Memory Management**
- WebSocket connections are properly managed
- Proper cleanup on app shutdown

## Support

If you encounter issues:
1. Check the logs for error messages
2. Verify network connectivity
3. Ensure both devices are on the same network
4. Ensure all permissions are granted
5. Restart the app if needed

## Future Enhancements

- Secure WebSocket support (wss://)
- Message encryption
- User authentication
- Connection pooling
- Message queuing
- Broadcast messaging
- Connection monitoring dashboard
