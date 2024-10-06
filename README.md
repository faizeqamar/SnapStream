# SnapStream

## Description
SnapStream is an Android application that captures images using the device's camera.
Upon opening the application, it requests camera permission and then captures an image.
The captured image is converted into a byte array and checked for internet connectivity.
If the internet is available, the image is uploaded to the ImgBB server.
If not, the image is stored in a local Room database with its upload flag set to false.
When the internet connection is restored, the application automatically uploads the 
stored images to the server in the background using WorkManager, while also uploading
newly captured images in parallel.

## Features
- **Live Image Capture**: Snap images via the device's camera.
- **Image Uploading**: Automatically upload images to the ImgBB server when connected to the internet.
- **Offline Storage**: Save images locally in a Room database if offline, and upload them when the connection becomes available.
- **Parallel Uploading**: Simultaneous uploading of both newly captured and stored images when the internet is available.

## Technologies, Frameworks, and Libraries Used
- **Kotlin**
- **Koin** for dependency injection
- **MVVM Repository Pattern**
- **Retrofit** for network requests
- **Coroutines** for asynchronous operations
- **Room Database** for offline storage
- **WorkManager** for background image uploading tasks

## How to Set Up and Run the Project
1. Clone the repository.
2. Create an account on ImgBB (it's free) and copy your API key.
3. Create a `local.properties` file in the root of your project with the following content:

    ```properties
    BASE_URL="https://yourapiurl.com"
    API_KEY="your_api_key_here"
    ```

