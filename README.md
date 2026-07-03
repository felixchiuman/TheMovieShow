# The Movie Show

**The Movie Show** is a modern Android application built using Jetpack Compose that allows users to discover movies, view detailed information, read reviews, and watch trailers. The app fetches data from The Movie Database (TMDB) API.

## 🚀 Features

*   **Discover Movies**: Browse through a list of popular, top-rated, and upcoming movies.
*   **Detailed Information**: View movie descriptions, ratings, release dates, and cast.
*   **Reviews**: Read user reviews for each movie.
*   **Trailers**: Integrated YouTube player to watch movie trailers directly in the app.
*   **Clean UI**: Built entirely with Jetpack Compose for a smooth and modern user experience.

## 🛠 Tech Stack

*   **Language**: [Kotlin](https://kotlinlang.org/)
*   **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
*   **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
*   **Networking**: [Retrofit](https://square.github.io/retrofit/) & [Moshi](https://github.com/square/moshi)
*   **Image Loading**: [Coil](https://coil-kt.github.io/coil/)
*   **Navigation**: [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
*   **Data Persistence**: [Room](https://developer.android.com/training/data-storage/room?hl=id)
*   **Video Playback**: [Android YouTube Player](https://github.com/PierfrancescoSoffritti/android-youtube-player)
*   **Architecture**: MVVM (Model-View-ViewModel)

## 🔑 Setup

To run this project, you will need to obtain an API key from [The Movie Database (TMDB)](https://www.themoviedb.org/documentation/api).

1.  Clone the repository.
2.  Open the project in Android Studio.
3.  Create a `local.properties` file in the root directory if it doesn't exist.
4.  Add your TMDB API key to `local.properties`:
    ```properties
    TMDB_API_KEY=your_api_key_here
    ```
5.  Build and run the app.

## 📄 License

```text
Copyright 2024 Felix

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
