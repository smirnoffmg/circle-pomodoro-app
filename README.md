# 🍅 Circle - Minimalistic Pomodoro Timer

A minimalistic Android Pomodoro timer designed to maximize user retention through simplicity and reliability. Addresses the 77% abandonment rate of productivity apps by delivering immediate value with zero cognitive overhead.

## 🎯 Core Objectives

- **Bulletproof timer functionality** with zero cognitive overhead
- **25%+ day-one retention** through immediate value delivery
- **Sub-3-tap access** to all primary functions
- **Consistent background operation** despite Android battery optimization
- **Meaningful customization** without undermining structured methodology

## 📱 Features

### Priority 1: Foundation (MVP)

- ✅ **Reliable Timer System** - Foreground service with AlarmManager backup
- ✅ **One-Tap Controls** - Start/pause/stop with 48dp touch targets
- ✅ **Visual Countdown** - Large circular progress indicator as primary interface
- ✅ **Automatic Transitions** - Seamless work/break cycling with notifications
- ✅ **Session Tracking** - Basic completed pomodoro count with timestamps


### Priority 2: Differentiation

- 🔄 **Flexible Intervals** - 15-90 minute work sessions, 5-30 minute breaks
- 🔄 **Task Association** - Simple work categorization
- 🔄 **Progress Visualization** - Clean streak tracking and weekly charts
- 🔄 **Cross-Device Sync** - Hybrid local/cloud architecture

### Priority 3: Integration

- 📅 **Google Assistant** - Voice commands for hands-free operation
- 📅 **Do Not Disturb** - Automatic focus mode activation
- 📅 **Calendar Integration** - Session scheduling around commitments
- 📅 **Wear OS Companion** - Discrete wrist-based controls

## 🛠️ Tech Stack

- **Language:** Kotlin 1.9.22 + Java 17
- **UI Framework:** Jetpack Compose 2024.02.00 + Material You
- **Architecture:** MVVM + Clean Architecture
- **Dependency Injection:** Hilt 2.48
- **Database:** Room 2.6.1 (local-first)
- **Background Tasks:** WorkManager 2.9.0 + Foreground Service + AlarmManager

- **Testing:** JUnit 4, Mockito, Turbine, Truth

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.3.1) or later
- Android SDK API 26+ (Android 8.0)
- Kotlin 1.9.22+
- Java 17 (JDK 17)

### Setup
```bash
git clone https://github.com/yourusername/circle-pomodoro.git
cd circle-pomodoro
./gradlew clean build
```

### Testing
```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Integration tests
./gradlew check                   # All tests
```

### CI/CD
The project uses GitHub Actions for automated builds and testing. See [GitHub Actions Setup](docs/GITHUB_ACTIONS_SETUP.md) for detailed configuration.

```bash
# Setup GitHub Actions secrets
./scripts/setup-github-secrets.sh
```

## 📁 Project Structure

```
app/src/main/java/com/smirnoffmg/pomodorotimer/
├── data/                   # Data layer (local-first)
├── domain/                 # Business logic
├── presentation/           # UI layer (circular design)
├── service/               # Timer foreground service

├── di/                    # Dependency injection
└── worker/                # WorkManager workers
```

## 🎨 Design Principles

### Minimalistic Interface

- **Single-Focus Design:** One primary action per screen state
- **Progressive Disclosure:** Advanced features revealed through engagement
- **Visual Hierarchy:** Timer display dominant, controls secondary
- **White Space Utilization:** Breathing room preventing cognitive overload

### Circular Design Language

- **Timer Visualization:** Completing circle metaphor for countdown
- **Consistent Iconography:** Circular elements throughout interface
- **Smooth Animations:** Subtle transitions maintaining focus
- **Color Psychology:** Calming blues/greens for focus, warm tones for breaks

## 🤝 Contributing

Follow [Android's coding standards](https://developer.android.com/kotlin/style-guide) and write tests for new features. Maintain 80%+ test coverage and follow SOLID, DRY, and KISS principles.

## 📄 License

MIT License - see [LICENSE](LICENSE) file for details.

---

**Built with ❤️ for productivity enthusiasts who value simplicity over complexity**

