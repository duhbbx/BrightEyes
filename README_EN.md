# BrightEyes

<p align="center">
  <img src="docs/logo.png" alt="BrightEyes Logo" width="120" height="120">
</p>

<p align="center">
  <strong>An Open-Source Android App for Childhood Amblyopia Treatment</strong>
  <br>
  <strong>Dedicated to Lele, and all children who need help</strong>
</p>

<p align="center">
  <a href="#the-story">The Story</a> •
  <a href="#features">Features</a> •
  <a href="#quick-start">Quick Start</a> •
  <a href="#contributing">Contributing</a> •
  <a href="#license">License</a> •
  <a href="README.md">中文</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform">
  <img src="https://img.shields.io/badge/API-24%2B-brightgreen.svg" alt="API">
  <img src="https://img.shields.io/badge/Language-Java-orange.svg" alt="Language">
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License">
</p>

---

## The Story

> **Why did I create this project?**

My son Lele was diagnosed with strabismus and amblyopia when he was just over 1 year old. As a father, I took him to numerous hospitals. Doctors recommended that in addition to wearing corrective glasses, visual training would help improve his vision.

However, when I looked into commercial amblyopia training software, I found that **the prices were unaffordable for ordinary families** — often costing thousands of dollars, which is a significant burden for many families like mine.

I am a programmer. I thought, why not develop one myself?

And so, **BrightEyes** was born.

This project was initially developed for Lele, but I hope it can help more families facing the same challenges. Every child deserves bright eyes, and **healthcare should not be a privilege for the few**.

**If this project helps you, please give it a Star to help more people discover it.**

---

## Introduction

**BrightEyes** is an open-source amblyopia training app for children. It uses scientifically-designed visual stimulation training to help children with amblyopia improve their vision. The app offers multiple training modes, records training data, and helps parents and children maintain consistent daily training.

> **Disclaimer**: This app is only an auxiliary training tool and cannot replace professional medical diagnosis and treatment. Please consult an ophthalmologist before use.

## Features

### Core Training Modules

| Training Type | Description | Principle |
|--------------|-------------|-----------|
| **Grating Training** | Highly customizable grating visual stimulation | Stimulates visual cortex development, improves amblyopic eye function |
| **Video Grating** | Overlay grating effects on videos | Combines entertainment with training, improves compliance |
| **Maze Game** | Maze game with grating overlay | Gamified training, improves hand-eye coordination |
| **Reaction Training** | Visual attention and reaction time training | Enhances visual attention and reaction speed |
| **Dichoptic Training** | Puzzle, chase, and fusion modes | Promotes binocular coordination and fusion |
| **Contrast Sensitivity** | CSF adaptive testing and training | Improves contrast sensitivity |

### Grating System Features

**Pattern Types**
- Stripes (horizontal/vertical)
- Checkerboard blocks
- Concentric circles
- Radial patterns
- Sine waves

**Color Modes**
- Black/White, Red/Blue, Red/Cyan, Red/White, Blue/White
- Red/White/Black tricolor, Rainbow
- Green/Magenta, Yellow/Blue
- Red/Blue/Black transparent (monocular occlusion)

**Animation Effects**
- Scroll - Smooth movement
- Zoom - Breathing-style scaling
- Rotate - Continuous rotation (seamless full-screen coverage)
- Pulse - Smooth pulsing effect
- Random - Automatic animation switching every 10 seconds

**Adjustable Parameters**
- Stripe width (adjustable)
- Animation speed (adjustable)
- Transparency (adjustable)
- Direction switching (horizontal/vertical)
- Reverse scrolling

### Maze Game Features

- **Cute Vehicle Characters** - Car, excavator, water truck, crane randomly appear
- **Bug Enemies** - Randomly moving bugs add fun
- **Difficulty Levels** - Easy/Medium/Hard
- **Grating Overlay** - Configurable grating effects on maze
- **Scoring System** - Records completion time and steps

### Background Music

All training modes support background music, auto-playing when training starts:
- Jingle Bells
- ABC Song
- Edelweiss
- Twinkle Twinkle Little Star

Music can be manually switched with volume control.

### Multi-Language Support

The app supports the following languages:
- 简体中文 (Simplified Chinese)
- English
- Français (French)
- Español (Spanish)
- 日本語 (Japanese)
- 한국어 (Korean)

Language can be changed in Settings.

### Other Features

- Training duration recording and statistics
- Daily training goal setting
- Consecutive training days tracking
- Training history viewing
- User profile management
- Dark theme full-screen training interface

## Screenshots

| Home | Training | Settings |
|:---:|:---:|:---:|
| ![Home](docs/screenshots/home.png) | ![Training](docs/screenshots/training.png) | ![Settings](docs/screenshots/settings.png) |

## Quick Start

### Requirements

- Android Studio Hedgehog (2023.1.1) or higher
- JDK 17
- Android SDK 34
- Gradle 8.2

### Build Steps

1. **Clone the repository**

```bash
git clone https://github.com/duhbbx/BrightEyes.git
cd BrightEyes
```

2. **Open the project**

Open the project root directory with Android Studio

3. **Sync Gradle**

Wait for Android Studio to automatically sync Gradle dependencies

4. **Run the app**

Connect an Android device or start an emulator, click the Run button

### Minimum System Requirements

- Android 7.0 (API 24) or above

> 📖 **Detailed Guide**: If you encounter issues, please check the [Complete Running Guide](docs/RUNNING_GUIDE.md)

## Project Structure

```
BrightEyes/
├── app/
│   ├── src/main/
│   │   ├── java/com/brighteyes/app/
│   │   │   ├── BrightEyesApp.java       # Application entry
│   │   │   ├── database/                 # Room database
│   │   │   ├── model/                    # Data models
│   │   │   ├── repository/               # Repository layer
│   │   │   ├── ui/                       # UI layer
│   │   │   │   ├── training/             # Training modules
│   │   │   │   │   ├── GratingTrainingActivity.java
│   │   │   │   │   ├── VideoGratingActivity.java
│   │   │   │   │   ├── MazeTrainingActivity.java
│   │   │   │   │   ├── ReactionTrainingActivity.java
│   │   │   │   │   ├── DichopticTrainingActivity.java
│   │   │   │   │   └── CSFTrainingActivity.java
│   │   │   ├── viewmodel/                # ViewModel layer
│   │   │   ├── widget/                   # Custom views
│   │   │   │   ├── GratingView.java      # Grating view
│   │   │   │   ├── MazeTrainingView.java # Maze view
│   │   │   │   └── ...
│   │   │   └── utils/                    # Utilities
│   │   │       ├── BackgroundMusicManager.java
│   │   │       └── LocaleHelper.java     # Language switching
│   │   ├── res/
│   │   │   ├── raw/                      # Music resources
│   │   │   ├── values/                   # Chinese strings (default)
│   │   │   ├── values-en/                # English strings
│   │   │   ├── values-fr/                # French strings
│   │   │   ├── values-es/                # Spanish strings
│   │   │   ├── values-ja/                # Japanese strings
│   │   │   ├── values-ko/                # Korean strings
│   │   │   └── ...
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── gradle/
├── build.gradle
├── settings.gradle
└── LICENSE
```

## Tech Stack

- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room
- **Async Processing**: LiveData
- **UI Components**: Material Design Components
- **Navigation**: AndroidX Navigation
- **Image Loading**: Glide
- **Custom Drawing**: Canvas API

## Contributing

We welcome all forms of contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details.

### How to Contribute

1. **Fork** this repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a **Pull Request**

### Especially Welcome

- Ophthalmologists or optometrists providing professional advice
- UI/UX designers improving child-friendly interfaces
- Developers enhancing training modules
- Translators helping with internationalization

## Roadmap

- [x] Grating training module (multiple patterns, colors, animations)
- [x] Video grating overlay
- [x] Maze game training
- [x] Background music support
- [x] Dichoptic vision training
- [x] Contrast sensitivity training
- [x] Multi-language support (6 languages)
- [ ] Training data statistics charts
- [ ] Multi-user switching (multiple children)
- [ ] Training reminder notifications
- [ ] Training data export
- [ ] Parental monitoring mode
- [ ] iOS version

## FAQ

<details>
<summary>What age is this app suitable for?</summary>

This app is primarily designed for children aged 3-12 with amblyopia. Please consult an ophthalmologist for specific training plans.
</details>

<details>
<summary>How long should training be done each day?</summary>

It is recommended to train for 15-30 minutes per day, divided into 2-3 sessions. Please follow your doctor's advice for specific duration.
</details>

<details>
<summary>Will training data be uploaded to servers?</summary>

No. All data is stored locally on the device. We value user privacy.
</details>

<details>
<summary>Can this app cure amblyopia?</summary>

This app is an auxiliary training tool and should be used in conjunction with professional medical treatment. Amblyopia treatment is a long-term process that requires consistent training and regular check-ups.
</details>

<details>
<summary>What is the principle behind grating training?</summary>

Grating training uses specific visual stimuli (such as stripes, color contrasts, etc.) to stimulate the visual pathway of the amblyopic eye, promoting the development and functional improvement of the visual cortex. Different pattern and color combinations can target different types of visual function training.
</details>

## Acknowledgments

- Dedicated to my son Lele, you are my motivation
- Thanks to all medical professionals contributing to amblyopia treatment for children
- Thanks to all open-source project contributors
- Thanks to everyone who gives this project a Star

## Contact Us

- Submit an Issue: [GitHub Issues](https://github.com/duhbbx/BrightEyes/issues)
- Email: duhbbx@gmail.com

## Support the Project

If this project helps you:

- Give the project a **Star**
- Share it with families in need
- Submit Issues or PRs to help improve it
- Tell us your story

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details

```
Copyright 2024 BrightEyes Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

---

<p align="center">
  <strong>Every child deserves bright eyes</strong>
  <br>
  <sub>Made with love for Lele and all children who need help</sub>
</p>
