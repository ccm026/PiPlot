# PiPlot

**Raspberry Pi ultrasonic distance measurement and live browser-based visualization**

PiPlot is a Java application that reads distance measurements from an ultrasonic sensor connected to a Raspberry Pi and displays them as a time-series chart in a web browser. It brings together GPIO-based hardware input, a Spring MVC backend, and a JavaScript graph.

> **Project status: Legacy personal project / under review.** I developed and used PiPlot during my student years in 2017. This repository preserves the original implementation. The build, hardware compatibility, and security have **not** yet been revalidated on current systems. Features described as *planned* below are not implemented in the reviewed source snapshot.

## What the project contains

- **Ultrasonic distance acquisition:** A Java controller triggers the sensor and times the returning echo using Pi4J GPIO APIs.
- **HTTP measurement endpoint:** The backend exposes a measurement request at `GET /run` and returns JSON.
- **Browser-based distance graph:** A JSP page uses JavaScript and CanvasJS to request measurements and plot recent values over time.
- **Single-account trial login and navigation:** Created while I was a student to experiment with web-form handling. The project currently has one demo account (`admin` / `password`), a home page, logout navigation, and a measurement graph. The login remains **unchanged** and is not production-ready authentication.

### Technology stack

| Layer | Technology in the existing source |
| --- | --- |
| Language | Java |
| Application framework | Spring Boot `1.5.6.RELEASE`, Spring MVC |
| Build tool | Apache Maven |
| Raspberry Pi GPIO | Pi4J `1.2-SNAPSHOT` |
| Web views | JSP, HTML, JavaScript, jQuery |
| Plotting | CanvasJS |
| Data format | JSON |

These are **existing dependency versions**, not recommendations for a new deployment. Some are obsolete and need review.

## How it works

### User-facing flow

```text
Login page (/) → Home page (/home) → Distance graph (/graph)
         ↳ Logout (/logout) returns to the login page
```

### Trial login page (student-era demonstration)

I added the login page when I was a student as a **personal trial feature**. The purpose was to learn how a Java/Spring MVC application accepts a login form, checks supplied values, and navigates to another page. It was not designed as a complete user-management or production authentication system.

**The current implementation supports one demo account only.** Its credentials are defined directly in `User.java`:

| Login field | Value |
| --- | --- |
| **Username** | `admin` |
| **Password** | `password` |

#### How to try the login

1. Start PiPlot using the legacy setup instructions below, after confirming that the application can run in your environment.
2. Open **`http://localhost:9192/`** in your web browser (port `9192` is configured in `application.properties`).
3. Enter **`admin`** as the username and **`password`** as the password.
4. Click **Login**. When the credentials match, the application redirects to **`/home`**, where you can navigate to the distance graph. When they do not match, it displays **`Error logging in`** on the login page.
5. Use **Logout** to return to the login page.

#### Scope and future extension

The single username/password pair was sufficient for this initial experiment. **The design could be extended to support multiple user accounts in a future version**, but that feature does **not** exist today. A proper extension would use a user database or another identity provider, hashed passwords, session handling, route authorization, and a real logout mechanism instead of adding further hardcoded credentials.

> **Demo only — not a security feature:** The username and password above are intentionally published for testing. The existing application does not create a secure authenticated session, does not prevent direct access to `/home` or `/graph`, and its logout route does not invalidate a session. Do not use the demo credentials for any other service or expose this application on the public internet without implementing proper security.

### Sensor data flow

```text
Ultrasonic sensor
    │ trigger / echo signal
    ▼
Raspberry Pi GPIO (Pi4J)
    │
    ▼
DistanceController.java
    │ GET /run → JSON measurement
    ▼
Browser: graph.jsp + JavaScript
    │
    ▼
Distance-versus-time plot
```

The controller estimates distance from the duration of the echo pulse. The web page periodically requests the result and appends it to a chart showing the most recent readings.

### HTTP routes in the source

| Method | Route | Intended behavior |
| --- | --- | --- |
| `GET` | `/` | Display the demo login page |
| `POST` | `/` | Process the demo login form |
| `GET` | `/home` | Display the home page |
| `GET` | `/graph` | Display the distance chart |
| `GET` | `/run` | Trigger a sensor reading and return JSON |
| `GET` | `/logout` | Return to the login page (does not invalidate a session) |

The current code builds a nested JSON structure similar to this **illustrative response**:

```json
{
  "measuredData": [[23.5]]
}
```

The value above is an example, **not a recorded or verified measurement**. The graph currently requests `run.html` rather than `/run`; that route must be checked before claiming the graph works as expected.

## Repository layout

```text
PiPlot/
├── pom.xml
├── README.md
├── src/
│   └── main/
│       ├── java/com/piproject/main/
│       │   ├── Application.java
│       │   ├── Controller/
│       │   │   ├── DistanceController.java
│       │   │   ├── LoginController.java
│       │   │   └── User.java
│       │   └── webinit/
│       │       ├── RootConfig.java
│       │       ├── Webconfig.java
│       │       └── Webinit.java
│       ├── resources/application.properties
│       └── webapp/WEB-INF/
│           ├── graph.jsp
│           ├── home.jsp
│           └── login.jsp
└── ...
```

The structure above shows the relevant handwritten files, not generated build outputs. Maven's `target/` directory and IDE settings should not be committed.

## Getting started (unverified legacy setup)

### Prerequisites

- A Raspberry Pi and an **electrically compatible** ultrasonic distance sensor for real measurements.
- A compatible Java Development Kit and Maven installation. **Exact working versions still need to be established.**
- GPIO access and any operating-system support required by the older Pi4J integration.
- A browser able to load the frontend's JavaScript dependencies.

**GPIO safety:** The source uses `RaspiPin.GPIO_00` as the trigger and `RaspiPin.GPIO_02` as the echo input, using **Pi4J's numbering scheme**, not physical header-pin numbers. Verify the sensor's supply and signal levels against your exact Raspberry Pi and sensor. Raspberry Pi GPIO inputs must not receive a 5 V echo signal directly; use appropriate level shifting or voltage division where required. Do not connect hardware based solely on the source-code pin labels.

### Check out the source

```bash
git clone https://github.com/ccm026/PiPlot.git
cd PiPlot
java -version
mvn -version
```

### Attempt a baseline build

```bash
mvn clean package
```

This is a **diagnostic first step**, not a claim that the current application builds successfully. The old Maven dependencies, snapshot repositories, servlet configuration, and JSP packaging may require changes. Record the Java/Maven versions and any errors before modernizing dependencies.

### Attempt local startup

```bash
mvn spring-boot:run
```

This command is **not yet verified** against the legacy code and its JSP configuration. If startup succeeds, `src/main/resources/application.properties` specifies port `9192`, so the intended root page is:

```text
http://localhost:9192/
```

**Important:** The current measurement endpoint directly accesses Raspberry Pi GPIO and there is no simulation mode yet. A normal desktop PC cannot be expected to produce sensor readings. Do not expose the application publicly: its demo login uses hardcoded credentials and does not enforce access control.

## Known limitations

The following items were identified by source inspection and have **not yet been validated through runtime tests**:

1. **GPIO lifecycle:** Pins are provisioned for each request and the GPIO controller is shut down after the measurement; repeated requests need safer resource management.
2. **Missing echo timeouts:** Busy-wait loops can block indefinitely if the sensor does not respond.
3. **Trigger timing:** `Thread.sleep((long) 0.01)` does not produce the intended 10-microsecond pause; the value is converted to zero milliseconds.
4. **Request performance:** A two-second sleep occurs in the measurement handler while the page attempts to poll every second.
5. **Frontend integration:** `graph.jsp` requests `run.html`, while the Java controller declares `/run`. Its numeric handling also truncates fractional values using `parseInt()`.
6. **Demo authentication:** Only the documented `admin` / `password` trial account is accepted; there is no authenticated session or protected-route enforcement. The logout route merely returns a view. Supporting multiple users would require proper authentication infrastructure.
7. **Legacy dependencies:** Spring Boot, Pi4J, JavaScript libraries, and multiple overlapping Tomcat dependencies need compatibility and security review.
8. **Tests and reproducibility:** No automated tests or verified build procedure were present in the uploaded source snapshot.

## Planned development

This is a proposed roadmap for independent improvement; **none of these tasks should be presented as completed until implemented and verified**.

| Workstream | Planned work | Initial estimate |
| --- | --- | ---: |
| Build baseline | Establish reproducible local build, document compatible toolchain | 4–6 h |
| Reliable measurements | Correct GPIO initialization/cleanup, timing, timeouts, and error handling | 5–8 h |
| Offline development | Introduce simulated sensor readings without Raspberry Pi hardware | 4–6 h |
| Visualization and API | Stabilize polling, preserve decimal measurements, improve JSON format and charts | 4–6 h |
| Quality and security | Document demo-login limitations, review dependencies, add automated tests and CI | 6–10 h |
| **Estimated total** | **Subject to actual implementation and validation** | **23–36 h** |

Once a separate `ROADMAP.md` is added, development tasks can be tracked there and linked to GitHub Issues. The estimates represent possible future work, not hours already worked.

## Running PiPlot on Windows (Simulation Mode)

PiPlot supports a simulation mode that allows the application to run on a Windows PC without Raspberry Pi hardware.

This mode generates sample distance measurements and displays them through the existing web-based visualization.

### Requirements

- Java JDK 8
- Apache Maven 3.9.x
- Web browser

### Configuration

Open `src/main/resources/application.properties` and configure:

```properties
server.port=9192
piplot.simulation=true
```

Setting `piplot.simulation=true` enables simulated measurements without initializing Raspberry Pi GPIO hardware.

### Build and Start

```bash
mvn clean test
mvn clean package
mvn spring-boot:run
```

### Access the Application

Open `http://localhost:9192/` in your browser.

Log in using the predefined demonstration credentials documented in the Login section.

After logging in, select **Start Measuring** to view the live graph.

### Simulation API

The measurement endpoint is available at:

`http://localhost:9192/run`

Example response:

```json
{
  "measuredData": [[24.02]]
}
```

The simulation generates changing distance values to demonstrate the application's data visualization functionality.

**Note:** Simulation mode has been tested on Windows. Measurement acquisition using physical Raspberry Pi GPIO hardware still requires testing on compatible hardware.

## Project background

I originally created PiPlot during my student years in 2017 as a personal trial and learning project. I developed and used the application to explore Raspberry Pi sensor measurements, Java backend development, web forms (including a simple one-account login demonstration), and live data visualization in a browser.

I have kept the original implementation and development history and intend to continue improving the project independently. The planned improvements are separate from the features already present in the repository.

## License

A license must be added as a top-level `LICENSE` file **after confirming the rights to license the complete codebase**. Until such a license is present, this repository does not grant a general open-source reuse license. Third-party dependencies have their own licenses.

## Feedback

Bug reports and suggestions can be discussed through the repository's GitHub Issues once issues are enabled. Please do not post real passwords, API keys, private keys, or private sensor data in an issue.
