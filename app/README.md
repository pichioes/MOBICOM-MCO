# AquaBuddy

A comprehensive water intake tracking application designed to help users monitor their daily hydration and achieve personalized hydration goals.

## Project Overview

**Course:** MOBICOM Major Course Output
**Development Team:**
- Balbastro, Lianne Maxene (S17)
- Lammoglia, Julianna (S17)
- Mansueto, Maria Alyssa (S17)

## Application Description

AquaBuddy is a mobile application that enables users to monitor their daily water consumption to achieve personalized hydration goals. The application promotes healthy water intake habits by providing customized recommendations based on individual user metrics such as weight and height.

### Home Page Functionality
- **Real-time Water Tracking:** Displays total daily water consumption with live updates
- **Manual Water Logging:** Allows users to input water intake with customizable portion sizes
- **Predefined Portion Options:** Includes standard measurements
- **Custom Measurement Input:** Supports custom water amounts in milliliters
- **Goal Progress Visualization:** Shows daily hydration goal achievement status
- **Hydration Timer:** Provides reminders for optimal water intake timing
- **Streak Tracking:** Monitors consecutive days of goal achievement

### Calendar and History Management
- **30-Day Historical Data:** Complete water intake records for the previous month
- **Interactive Date Selection:** Detailed intake records viewable for specific dates
- **Visual Status Indicators:** Color-coded calendar days indicating hydration achievement levels
- **Comprehensive Analytics:**
    - Weekly consumption averages
    - Monthly consumption trends
    - Goal completion percentages
    - Drinking frequency patterns
- **Detailed Water Reports:** In-depth hydration analysis and insights

### Profile and Account Management
- **Personal Metrics Configuration:** Input and update weight and height for personalized recommendations
- **Customizable Hydration Goals:** Set and modify daily water intake targets based on personal needs
- **Notification Preferences:** Configure reminder frequency and enable/disable notification system
- **Account Security:** Secure sign-out functionality and permanent account deletion options
- **Session Management:** Maintains user login state across application sessions

### Authentication System
- **Multi-Platform Login Support:**
    - Standard email and password authentication
    - Google Sign-In integration
    - Facebook Sign-In integration
- **Account Recovery:** Comprehensive forgot password functionality
- **User Registration:** New account creation with personal detail collection
- **Error Handling:** Robust authentication error management

### Notification System
- **Background Service Integration:** Continuous reminder notifications even when app is closed
- **Customizable Notification Frequency:** User-controlled reminder intervals
- **Achievement Notifications:** Congratulatory alerts for daily goal completion

### Database Management
- **Local Storage Solution:** SQLite database for user profiles and intake history
- **Database Identifier:** `Aquabuddy.db`
- **Application Path:** `com.mobdeve.s17.mco2.group88/databases/`
- **Data Persistence:** Maintains historical records and user preferences

### External API Integrations
- **Google API Services:** User authentication and profile management
- **Facebook SDK Integration:** Social media authentication services
- **Background Processing:** Continuous notification delivery services
- **System Event Handling:** Broadcast receiver implementation for system-level events

### System Requirements
- Android Studio IDE
- Android SDK (minimum API level as specified)
- Google Play Services
- Facebook SDK for Android

### Database Setup and Inspection
1. Access Android Studio Device Explorer (View → Tool Windows → Device Explorer)
2. Navigate to application directory: `com.mobdeve.s17.mco2.group88 → databases`
3. Download `Aquabuddy.db` file to local system
4. Import database file into SQLite Viewer (https://inloop.github.io/sqlite-viewer/)

### Application Deployment
1. Clone project repository to local development environment
2. Import project into Android Studio
3. Synchronize Gradle build files
4. Configure Google and Facebook API authentication keys
5. Build and deploy to Android device or emulator

### Analytics and Reporting
- Comprehensive consumption pattern analysis
- Statistical goal completion tracking
- Personalized hydration frequency insights
- Data-driven recommendation system

### Implemented Features
- Complete user authentication system (Google, Facebook, Email)
- Profile creation and management functionality
- Home page water tracking interface
- SQLite database integration
- Basic notification system implementation
- Calendar view with historical data
- Analytics dashboard with reporting capabilities

### Current Development Challenges
- Profile display banner optimization
- Height and weight update synchronization
- Notification frequency database persistence
- Goal update reflection in UI components
- Background notification consistency
- Analytics data synchronization accuracy
- Composable calendar component optimization

### Ongoing Development Tasks
- User interface refinement and optimization
- Notification system reliability improvements
- Database query performance optimization
- Application stability enhancements
- Comprehensive bug resolution and testing

## User Interface Design

The application implements a three-tier navigation structure:

1. **Home Interface:** Primary dashboard for daily water intake management
2. **Calendar/History View:** Historical data visualization and analytics
3. **Profile Management:** Personal settings and account configuration

## Project Classification

This project represents coursework completion for the MOBICOM Major Course Output requirement and demonstrates comprehensive mobile application development skills including database management, API integration, user interface design, and system architecture implementation.

## Disclaimer

This application is designed to support healthy hydration habits and is intended for educational and personal wellness purposes. Users should consult qualified healthcare professionals for specific medical advice regarding individual hydration requirements and health-related decisions.