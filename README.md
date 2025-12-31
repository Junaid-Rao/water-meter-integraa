# Water Meter Integraa - Android Application

**Made by Junaid Aslam**

A comprehensive Android application for managing water meters through Bluetooth Low Energy (BLE) communication. This application allows users to send commands to water meter devices, configure network settings, and manage device connections.

## 📱 Application Overview

Water Meter Integraa is an Android application designed to interact with water meter devices via Bluetooth. The application provides a user-friendly interface for:

- User authentication with location-based login
- Dynamic command rendering based on user permissions
- Bluetooth device selection and management
- Sending hexadecimal payloads to water meter devices
- Real-time connection status monitoring

## 🎥 Video Demonstration

A comprehensive video demonstration of the application is available in the repository:

**[Video Link: App Demo](https://github.com/Junaid-Rao/water-meter-integraa/blob/main/Water-Meter-App-Demo.mp4)**

The video covers:
- Application overview and features
- Login flow with location-based authentication
- Bluetooth device selection and scanning
- Command execution with parameter validation
- Error handling and user feedback
- Connection status monitoring

## 🏗️ Architecture

The application follows **MVVM (Model-View-ViewModel)** architecture pattern with **Hilt** for dependency injection, ensuring clean code separation and testability.

### Architecture Components

- **Model Layer**: Data models, API services, and local storage
- **View Layer**: Activities, Fragments, and UI components
- **ViewModel Layer**: Business logic and state management
- **Repository Layer**: Data source abstraction
- **Use Cases**: Domain-specific business logic

### Key Technologies

- **Language**: Java
- **Architecture**: MVVM with Hilt Dependency Injection
- **Networking**: Retrofit 2 + OkHttp
- **Reactive Programming**: LiveData
- **Background Tasks**: WorkManager
- **Bluetooth**: Android BLE API
- **UI**: Material Design Components

## 📋 Features

### 1. Authentication
- Secure login with username and password
- Location-based authentication (requires GPS coordinates)
- Token-based session management
- Automatic session expiration handling (3-hour refresh)
- Manual logout functionality
- User-friendly error messages for invalid credentials

### 2. Permissions Management
- Dynamic permission fetching from API
- Automatic refresh every 3 hours
- Session expiration detection
- Automatic logout on session expiry
- Error handling for network failures

### 3. Command Management
- Dynamic command rendering based on user permissions
- Tab-based navigation for command groups (Common, Network, etc.)
- Parameter validation (text, integer, IP address)
- Automatic checksum calculation (CheckSum8 Modulo 256)
- Value transformations (equal, IP, int4)
- Real-time validation feedback

### 4. Bluetooth Integration
- BLE device scanning
- Device selection and pairing
- One-time connection for command transmission
- Connection status indicator in app bar
- Automatic reconnection on command send
- Comprehensive error handling for connection failures

### 5. User Interface
- Material Design 3 components
- Responsive layouts with safe area handling
- Enhanced login screen with branding
- Error handling with user-friendly messages
- Loading states and progress indicators
- Dark mode support

## 🔧 Setup Instructions

### Prerequisites

- Android Studio (latest version recommended)
- Android SDK (API Level 24+)
- Java Development Kit (JDK 11 or higher)
- Physical Android device with Bluetooth Low Energy support (for testing)

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd integraaandroidjunaid
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle dependencies
   - Wait for the sync to complete

4. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

5. **Run on device**
   - Connect an Android device via USB
   - Enable USB debugging
   - Click "Run" in Android Studio

## 📱 Usage Guide

### Login
1. Launch the application
2. Enter your username and password
3. Grant location permission when prompted
4. Tap "Login" button
5. Upon successful login, you'll be redirected to the main screen

### Selecting Bluetooth Device
1. Tap the floating action button (FAB) in the bottom right
2. Grant Bluetooth permissions if prompted
3. View paired devices or scan for new devices
4. Select a device from the list
5. The device will be saved and used for command transmission

### Sending Commands
1. Navigate to the desired command tab (Common, Network, etc.)
2. Tap on a command card
3. Fill in required parameters (if any)
4. Tap "Send Command"
5. The app will automatically connect to the selected device and send the command
6. Wait for success/error confirmation

## 🔐 Permissions

The application requires the following permissions:

- **Internet**: For API communication
- **Location (Fine/Coarse)**: Required for login authentication
- **Bluetooth**: For device scanning and connection
- **Bluetooth Scan**: For discovering BLE devices (Android 12+)
- **Bluetooth Connect**: For connecting to BLE devices (Android 12+)
- **Bluetooth Advertise**: For BLE advertising (Android 12+)

## 🌐 API Endpoints

### Login Endpoint
- **URL**: `http://apm.integraaposta.com/gestione/api/login`
- **Method**: POST
- **Content-Type**: application/x-www-form-urlencoded
- **Parameters**: 
  - `u`: Username
  - `p`: Password
  - `lat`: Latitude
  - `lng`: Longitude
- **Response**: JSON with Token, TrackingToken, userId, etc.

### Permissions Endpoint
- **URL**: `http://apm.integraaposta.com/gestione/api/waterPermissions`
- **Method**: GET
- **Headers**: 
  - `token`: Authentication token from login
- **Response**: JSON with actions (command groups) and commands

## 📊 Command Parameter Types

### Text Parameters
- Renders a text input field
- Supports regex validation via `required` field
- Example: Serial number validation `^[0-9]{8}$`

### Integer Parameters
- Renders a numeric input field
- Supports min/max value constraints
- Example: Port number (1-65535)

### Checksum Parameters
- Automatically calculated (CheckSum8 Modulo 256)
- Not displayed in UI
- Calculated on all bytes before the checksum placeholder

## 🔄 Value Transformations

### Equal
- Value is inserted as-is into the payload

### IP Address
- Converts IP address to hexadecimal format
- Example: `192.168.1.1` → `C0A80101`

### Int4
- Converts integer to 4-byte little-endian hexadecimal
- Example: `5013` → `00001395`

## 🧪 Testing

### Testing Without Physical Device

**Important Note**: Due to the lack of a physical BLE water meter device, the following aspects have been implemented according to specifications but could not be fully tested end-to-end:

1. **Bluetooth Connection**: The connection logic is fully implemented with proper error handling, but requires a physical device for end-to-end testing
2. **Payload Transmission**: Hex payload generation is tested and verified, but actual transmission requires a compatible water meter device
3. **Device Communication**: All communication protocols are implemented per specifications, including:
   - BLE scanning and device discovery
   - GATT connection and service discovery
   - Characteristic write operations
   - Connection timeout handling
   - Error recovery mechanisms

### Implementation Status

✅ **Fully Implemented and Tested:**
- Login flow with location-based authentication
- Permission fetching and caching
- Dynamic command rendering
- Bluetooth device scanning and selection
- Command parameter validation
- Payload generation with checksum calculation
- Value transformations (IP, int4, equal)
- Connection status indicator
- Error handling and user feedback
- Safe area handling for modern devices
- Material Design UI components
- Session management and auto-logout

⚠️ **Implemented but Requires Physical Device for Final Testing:**
- End-to-end Bluetooth communication
- Actual payload transmission to water meter
- Device response handling
- Connection stability with real hardware
- BLE service discovery with actual device

### Testing Recommendations

1. **Unit Tests**: Test payload generation, checksum calculation, and value transformations
2. **Integration Tests**: Test API calls with mock responses
3. **UI Tests**: Test user flows and error handling
4. **Device Testing**: Test with actual water meter device when available

### Known Limitations

- Final Bluetooth communication testing requires a physical water meter device
- Some edge cases in device connection may need refinement with actual hardware
- Network timeout scenarios may need adjustment based on device response times

## 📹 Video Demonstration

A video demonstration of the application will be available in the repository. The video covers:

- Application overview and features
- Login flow
- Bluetooth device selection
- Command execution
- Error handling

**Video Link**: [To be added - Video will be uploaded to repository]

## 📁 Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/integraa_android_junaid/
│   │   │   ├── data/
│   │   │   │   ├── api/          # API models and services
│   │   │   │   ├── bluetooth/    # Bluetooth management
│   │   │   │   ├── local/        # Local storage (SharedPreferences)
│   │   │   │   └── repository/   # Data repositories
│   │   │   ├── domain/
│   │   │   │   ├── model/        # Domain models
│   │   │   │   └── usecase/      # Business logic use cases
│   │   │   ├── di/               # Hilt dependency injection modules
│   │   │   ├── ui/
│   │   │   │   ├── login/        # Login screen
│   │   │   │   ├── main/         # Main activity and fragments
│   │   │   │   ├── command/      # Command dialog
│   │   │   │   └── settings/     # Bluetooth settings
│   │   │   ├── util/             # Utility classes
│   │   │   └── worker/           # WorkManager workers
│   │   ├── res/                  # Resources (layouts, strings, etc.)
│   │   └── AndroidManifest.xml
│   └── test/                     # Unit tests
└── build.gradle.kts
```

## 🐛 Error Handling

The application includes comprehensive error handling with user-friendly messages:

### Authentication Errors
- **Invalid Credentials**: "Invalid username or password. Please check your credentials and try again."
- **Network Errors**: "Unable to connect to server. Please check your internet connection."
- **Token Missing**: "Authentication token not received. Please try again."
- **Location Permission**: "Location permission is required for login. Please grant location access."

### Bluetooth Errors
- **Bluetooth Not Enabled**: "Bluetooth is not enabled. Please enable Bluetooth in settings."
- **Permission Denied**: "Bluetooth permissions are required. Please grant permissions in settings."
- **No Device Selected**: "No Bluetooth device selected. Please select a device from settings first."
- **Connection Failed**: "Failed to connect to device. Please ensure the device is nearby and try again."
- **Device Disconnected**: "Device disconnected. Please reconnect and try again."

### Command Errors
- **Invalid Format**: "Invalid command format. Please try again or contact support."
- **Parameter Validation**: "Please check all required fields and try again."
- **Payload Generation**: "Error processing command. Please try again."

### Session Errors
- **Session Expired**: Automatic logout with user notification
- **Unauthorized**: "You are not authorized to perform this action."

## 🔒 Security Considerations

- Tokens are stored securely in SharedPreferences
- HTTPS should be used in production (currently HTTP for development)
- Location data is only sent during login
- Bluetooth permissions are requested at runtime
- Session tokens are automatically refreshed
- User credentials are never stored locally

## 📝 Development Notes

### Implementation Status

✅ **Completed Features:**
- Login flow with location-based authentication
- Permission fetching and caching
- Dynamic command rendering
- Bluetooth device scanning and selection
- Command parameter validation
- Payload generation with checksum
- Value transformations (IP, int4, equal)
- Connection status indicator
- Error handling and user feedback
- Safe area handling for modern devices
- Material Design UI components
- Enhanced login UI with branding
- Comprehensive error messages

⚠️ **Pending Testing (Requires Physical Device):**
- End-to-end Bluetooth communication
- Actual payload transmission to water meter
- Device response handling
- Connection stability with real hardware

## 🎨 UI/UX Enhancements

- **Login Screen**: Enhanced with company logo, app name, and developer credit
- **Error Messages**: User-friendly, actionable error messages throughout the app
- **Loading States**: Clear feedback during async operations
- **Connection Status**: Visual indicator in app bar showing device connection status
- **Material Design**: Consistent use of Material Design 3 components

## 🤝 Contributing

This is a technical test project. For any questions or issues, please contact:

**Developer**: Junaid Aslam

## 📄 License

This project is developed as a technical assessment. All rights reserved.

## 🙏 Acknowledgments

- Integraa for providing the project requirements
- Android Material Design team for UI components
- Open source community for libraries and tools

---

## ⚠️ Important Note

This application has been **fully implemented** according to the provided specifications. However, due to the **lack of a physical BLE water meter device**, final end-to-end testing of Bluetooth communication could not be completed. 

All code is production-ready and follows Android best practices:
- Proper error handling
- User-friendly error messages
- Clean architecture (MVVM with Hilt)
- Material Design UI
- Comprehensive logging
- Safe area handling
- Permission management

The application is ready for testing once a compatible water meter device is available.

---

**Made with ❤️ by Junaid Aslam**
