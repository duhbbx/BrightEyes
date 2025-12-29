# BrightEyes

<p align="center">
  <img src="docs/logo.png" alt="BrightEyes Logo" width="120" height="120">
</p>

<p align="center">
  <strong>An Open-Source Android App for Children's Amblyopia Treatment</strong>
  <br>
  <strong>Dedicated to Lele, and all children who need help</strong>
</p>

<p align="center">
  <a href="#the-story">The Story</a> •
  <a href="#features">Features</a> •
  <a href="#getting-started">Getting Started</a> •
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

> **Why did I build this?**

My son Lele was diagnosed with strabismus and amblyopia when he was just over 1 year old. As a father, I took him to numerous hospitals. Doctors recommended that in addition to wearing corrective glasses, he needed visual training to help improve his vision.

However, when I looked into the amblyopia training software available on the market, I found that **commercial software prices were unaffordable for ordinary families** — costing thousands or even tens of thousands of yuan, which is a significant burden for many families like mine.

I am a programmer. I thought, why not develop one myself?

And so, **BrightEyes** was born.

This project was initially developed for Lele, but I hope it can help more families facing the same challenges. Every child deserves bright eyes, and **healthcare should not be a privilege for the few**.

I chose to open-source this project, hoping to:
- Allow more families to access free, scientific amblyopia training tools
- Invite more developers to join and improve this project together
- Pass on this father's love to more children in need

**If this project helps you, please give it a Star to help more people discover it.**

---

## Introduction

**BrightEyes** is an open-source amblyopia training app for children. Through scientific visual stimulation training, it helps children with amblyopia improve their vision. The app provides multiple training modes, records training data, and helps parents and children maintain daily training routines.

> **Disclaimer**: This app is only an auxiliary training tool and cannot replace professional medical diagnosis and treatment. Please consult an ophthalmologist before use.

## Features

### Training Modules

| Training Type | Description | Principle |
|--------------|-------------|-----------|
| Red Flash | Red light flickering stimulation | Enhance retinal sensitivity |
| Grating | Stripe pattern visual stimulation | Stimulate visual cortex development |
| Tracking | Track moving targets | Exercise eye muscle coordination |
| Focus | Near-far focus switching | Improve accommodation ability |
| Color Recognition | Color identification games | Enhance color discrimination |
| Shape Matching | Shape matching memory | Improve pattern recognition |

### Core Features

- Multiple scientific training modes
- Training duration and score recording
- Daily training goal setting
- Consecutive training day statistics
- Training history viewing
- User profile management

## Screenshots

| Home | Training | Settings |
|:---:|:---:|:---:|
| ![Home](docs/screenshots/home.png) | ![Training](docs/screenshots/training.png) | ![Settings](docs/screenshots/settings.png) |

## Getting Started

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

Connect an Android device or start an emulator, then click the Run button

### Minimum System Requirements

- Android 7.0 (API 24) or above

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
│   │   │   ├── viewmodel/                # ViewModel layer
│   │   │   └── utils/                    # Utilities
│   │   ├── res/                          # Resources
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
- **Async**: LiveData
- **UI Components**: Material Design Components
- **Navigation**: AndroidX Navigation
- **Image Loading**: Glide

## Contributing

We welcome all forms of contributions! See [CONTRIBUTING.md](CONTRIBUTING.md) for details.

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

- [ ] Complete implementation of all training modules
- [ ] Add training data statistics charts
- [ ] Support multiple users (multiple children)
- [ ] Add training reminder functionality
- [ ] Support training data export
- [ ] Add parental monitoring mode
- [ ] iOS version
- [ ] More language support

## FAQ

<details>
<summary>What age is the app suitable for?</summary>

This app is mainly designed for children aged 3-12 with amblyopia. Please consult an ophthalmologist for specific training plans.
</details>

<details>
<summary>How long should training be each day?</summary>

It is recommended to train 15-30 minutes per day, divided into 2-3 sessions. Follow your doctor's advice for specific duration.
</details>

<details>
<summary>Will training data be uploaded to servers?</summary>

No. All data is stored locally on the device. We value user privacy.
</details>

<details>
<summary>Can this app cure amblyopia?</summary>

This app is an auxiliary training tool and should be used in conjunction with professional medical treatment. Amblyopia treatment is a long-term process that requires consistent training and regular check-ups.
</details>

## Acknowledgments

- Dedicated to my son Lele, you are my motivation
- Thanks to all medical workers contributing to the rehabilitation of children with amblyopia
- Thanks to all open-source contributors
- Thanks to everyone who stars this project

## Contact

- Submit Issues: [GitHub Issues](https://github.com/duhbbx/BrightEyes/issues)
- Email: duhbbx@gmail.com

## Support the Project

If this project has helped you:

- Give the project a **Star**
- Share it with families in need
- Submit Issues or PRs to help improve
- Tell us your story

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

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
