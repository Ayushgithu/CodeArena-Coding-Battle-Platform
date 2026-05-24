----------------+---------------------+--------------------+-----------------------+-------------------------+---------------------------+--------------+

⚔️ Code Arena: Coding Battle Platform
Code Arena is a real-time, competitive programming platform designed to facilitate coding and MCQ battles. It features a decoupled, full-stack architecture that supports high-concurrency, low-latency interaction for users participating in coding assessments and competitive rooms.

-------------------------------------------------------------------------------------------------------------------------------------------------------------

🚀 Tech Stack
Backend: Java (Spring Boot), Spring Security (JWT), Spring Data JPA, WebSockets (STOMP).

Frontend: React.js (Vite), Axios, Tailwind CSS.

Database & Cache: MySQL, Redis.

Authentication: JWT-based stateless authentication.

---------------------------------------------------------------------------------------------------------------------------------------------------------------

🧠 Core Features
-> Real-time Battle Rooms: Supports multiplayer coding and MCQ battles with WebSocket-based state synchronization.

-> Adaptive AI Engine: Dynamically selects questions based on difficulty levels and user skill tags.

-> Sandboxed Code Execution: Secure, integrated backend engine to evaluate user code against test cases.

-> Real-time Communication: Built-in live chat features for participants within a room.

-> Performance Analytics: Detailed post-battle feedback, including strength/weakness analysis and leaderboard rankings.

-----------------------------------------------------------------------------------------------------------------------------------------------------------------

📂 Project Structure
Backend (/cdac-project)
-> controller/: Manages all REST endpoints and WebSocket messaging (e.g., RoomWebSocketController for battle logic).

-> entity/: Database models representing core domain objects like User, Room, CodingQuestion, and Submission.

-> service/ & serviceImpl/: The "brain" of the application.

-> AdaptiveQuestionSelector: Algorithms to fetch questions tailored to user performance.

-> CodeExecutionService: Logic to handle code compilation and test case validation.

-> repository/: Data access layer interfacing with MySQL.

-> config/: System configuration, including SecurityConfig (JWT filters) and WebSocketConfig (messaging protocols).

-> dataInitializer/: Initializers to load foundational data (roles, questions) on startup.

----------------------------------------------------------------------------------------------------------------------

Frontend (/code-arena-fe)
-> pages/: View-level components for different routes (e.g., DashboardPage, CodingRoomPage).

-> components/: Modular UI elements:

-> editor/: The code editor environment.

-> chat/: The real-time messaging component.

-> room/: Modals for room creation and invitations.

-> services/: API abstraction layer using Axios to communicate with the Spring Boot backend.

-> config/: Global configurations, including ApiInterceptor for handling auth headers and session expiration.

------------------------------------------------------------------------------------------------------------------------------------------------------------

🛠️ Prerequisites
  Java 17+

  Node.js 18+

  MySQL Server

  Redis Server

--------------------------------------------------------------------------------------------------------------------------------------------------------------

Setup Environment
-> Configure your database and API settings in src/main/resources/application.properties:

  Properties
    spring.datasource.url=jdbc:mysql://localhost:3306/codearena
    spring.datasource.username=your_user
    spring.datasource.password=your_password

----------------------------------------------------------------------------------------------------------------------------------------------------------------

▶️ Run the Application
-> Start Backend
  Navigate to the cdac-project directory.

  Build and run:

  Bash
  ./mvnw clean install
  ./mvnw spring-boot:run

-------------------------------------------------------------------------
-> Start Frontend
  Navigate to the code-arena-fe directory.

  Install dependencies:

  Bash
  npm install
  
  Start the development server:

  Bash
  npm run dev

-> 🌐 Access
  Backend API: http://localhost:8080

  Frontend UI: http://localhost:5173

----------------------------------------------------------------------------------------------------------------------------------------------------------------

🔮 Future Feature Scope

-> Proctoring-Ready Interview Mode: A specialized interface that restricts window switching, monitors focus, and manages timed assessment constraints.

-> Collaborative Whiteboard: An integrated digital canvas allowing candidates to sketch algorithms, system designs, or data structures in real-time during an interview.

-> Live Screen Sharing: Peer-to-peer (P2P) screen-sharing capability built via WebRTC to allow interviewers or proctors to observe the coding process live.

-> Tournament Mode: Creating bracket-style tournament systems for larger community events.

-------------------+-----------------------+-------------------------+---------------------------+------------------------+-------------------+----------------+
