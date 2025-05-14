# QuizCraft

**QuizCraft** is an Android application designed to facilitate the creation, sharing, and solving of quizzes. Built using **Kotlin** in **Android Studio** and integrated with **Firebase** for backend services, QuizCraft offers a streamlined experience for educators, students, and quiz enthusiasts.

## Features

* **Create Quizzes**: Users can create their own quizzes with multiple questions and answers.
* **Solve Quizzes**: Users can participate in quizzes shared with them or explore public quizzes.
* **Public & Private Quizzes**:

  * **Public Quizzes**: Accessible to all users.
  * **Private Quizzes**: Quizzes can be shared within custom-created classes.
* **Classroom System**:

  * Users can create or join classes using class codes.
  * Teachers can manage quizzes specific to their classes.
* **Firebase Integration**:

  * **Authentication**: User login and registration.
  * **Realtime Database / Firestore**: For storing quizzes, results, and class data.
  * **Storage**: For handling multimedia content (if any) in quizzes.

## Tech Stack

* **Frontend**: Android Studio with Kotlin
* **Backend**: Firebase (Authentication, Firestore / Realtime Database, Storage)

## Installation & Setup

1. Clone the repository:

   ```bash
   git clone https://github.com/your-username/QuizCraft.git
   ```
2. Open the project in **Android Studio**.
3. Connect your Firebase project:

   * Add the `google-services.json` file to the `app/` directory.
   * Ensure Firebase services (Auth, Firestore, etc.) are enabled in your Firebase console.
4. Build and run the app on an emulator or physical Android device.

## Future Enhancements

* Add timer and scoring system.
* Analytics dashboard for quiz results.
* Notifications for quiz deadlines and results.
* Support for multimedia questions (images, audio, video).

## License

MIT License. See `LICENSE` file for details.

---

Feel free to contribute or raise issues via pull requests or the issues tab!
