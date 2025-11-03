# FindMyFriend

## Description

FindMyFriend is a mobile app that allows users to request and share their real-time location via SMS. This Android application enables location sharing without requiring internet connectivity, making it perfect for scenarios where data connection is limited or unavailable.

## How It Works

The workflow is simple and straightforward:

1. **User1** sends an SMS to User2 with the message: `"send me your location"`

2. **User2's phone** receives the SMS, automatically reads it, launches a background service to determine their current location (longitude and latitude), and sends the coordinates back to User1 via SMS in the format: `"my position is longitude: ..., latitude: ..."`

3. **User1** receives the SMS containing the coordinates and is notified to open Google Maps at the provided location to view User2's position.

## Features

- 📱 SMS-based location sharing (no internet required)
- 🗺️ Automatic Google Maps integration
- 🔄 Background service for location detection
- 📍 Real-time GPS coordinates (longitude and latitude)
- 🚀 Simple and intuitive workflow

## Usage Instructions

### For the Sender (User1)

1. Open your default SMS/messaging app
2. Send an SMS to the contact you want to locate with the text: `"send me your location"`
3. Wait for the response SMS containing the location coordinates
4. When you receive the response, tap the notification to open Google Maps with the location
5. View your friend's position on the map

### For the Receiver (User2)

1. Ensure the FindMyFriend app is installed and has the necessary permissions:
   - SMS read/receive permissions
   - Location access (GPS)
   - SMS send permissions
2. The app runs in the background and automatically:
   - Detects incoming SMS with the trigger phrase
   - Activates the location service
   - Retrieves your current GPS coordinates
   - Sends your location back to the requester
3. No manual intervention required once set up!

## Technical Details

- **Platform**: Android
- **Programming Language**: Java
- **Target Devices**: Android smartphones with SMS and GPS capabilities
- **Minimum Requirements**: 
  - SMS functionality
  - GPS/Location services
  - Android OS (version TBD)

## Permissions Required

The app requires the following permissions:
- `READ_SMS` - To read incoming location requests
- `RECEIVE_SMS` - To detect incoming SMS messages
- `SEND_SMS` - To send location coordinates back
- `ACCESS_FINE_LOCATION` - To get precise GPS coordinates
- `ACCESS_COARSE_LOCATION` - For approximate location
- `FOREGROUND_SERVICE` - To run location service in background

## Installation

1. Clone this repository
2. Open the project in Android Studio
3. Build and run the project on your Android device
4. Grant all necessary permissions when prompted

## Privacy & Security

- Location is only shared when explicitly requested via SMS
- No data is stored on external servers
- Direct peer-to-peer communication via SMS
- User maintains full control over location sharing

## Contributing

Contributions are welcome! Feel free to submit issues and pull requests.

## License

This project is open source and available under the [MIT License](LICENSE).

---

**Note**: This app is designed for personal use and trusted contacts. Always be mindful of privacy when sharing your location.
