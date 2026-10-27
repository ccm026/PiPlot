# PiPlot

**Raspberry Pi Ultrasonic Distance Measurement and Real-Time Web Visualization**

PiPlot is a Java-based application designed to collect ultrasonic distance measurements from a sensor connected to a Raspberry Pi and visualize the measurements in real time through a web browser.

The project combines hardware communication using GPIO, backend processing using Spring Boot, and browser-based graphical visualization using JavaScript and CanvasJS.

> **Project Status: Legacy / Under Modernization**
>
> Originally developed in 2017, PiPlot is being reviewed for further independent development. The existing functionality, compatibility with current hardware, and build environment still require verification.

## 1. Project Overview

PiPlot demonstrates communication between physical sensor hardware and a web-based visualization application.

An ultrasonic sensor measures the distance to an object, and a Raspberry Pi processes the sensor signals through its GPIO interface. The backend calculates the measured distance and makes the result available to a web interface.

A browser-based graph is designed to display the incoming values against time, allowing users to observe changes in distance measurements.

### Project objectives

- Acquire distance measurements using an ultrasonic sensor.
- Communicate with Raspberry Pi GPIO through Java.
- Process measurements through a Spring Boot application.
- Transfer measurement data using HTTP and JSON.
- Display measurements graphically in a web browser.
- Explore real-time hardware monitoring through a web interface.

## 2. Existing Features

### Ultrasonic distance measurement

The existing Java implementation uses Pi4J to control the trigger and echo pins of an ultrasonic sensor.

The measurement duration is used to calculate an estimated distance in centimeters.

### Spring Boot backend

The Java backend handles requests for sensor measurements and prepares JSON responses.

### Web-based data visualization

The application includes a JSP page with a CanvasJS line chart for plotting distance measurements against time.

The graph is configured to periodically request updated measurement data.

### Basic web interface

The repository contains:

- A home page.
- A sensor measurement graph.
- Navigation between application pages.


## 3. Technology Stack

| Component | Technology |
|---|---|
| Programming language | Java |
| Backend framework | Spring Boot 1.5.6.RELEASE |
| Web framework | Spring MVC |
| Build tool | Apache Maven |
| GPIO interface | Pi4J 1.2-SNAPSHOT |
| Frontend | HTML, JSP, JavaScript |
| Data visualization | CanvasJS |
| JavaScript library | jQuery |
| Data exchange | HTTP and JSON |
| Hardware platform | Raspberry Pi |
| Measurement device | Ultrasonic distance sensor |

These versions describe the existing implementation. Several dependencies are outdated and will need modernization.

## 4. System Architecture

The application follows a hardware-to-web data flow:

```text
      Ultrasonic Sensor
              |
       Trigger / Echo
              |
              v
      Raspberry Pi GPIO
              |
              v
        Pi4J Library
              |
              v
     Java DistanceController
              |
      HTTP JSON Response
              |
              v
       Web Application
        (JSP / JavaScript)
              |
              v
       CanvasJS Line Chart
              |
              v
     Distance vs. Time Graph
```

### Data acquisition

The Raspberry Pi sends a trigger signal to the ultrasonic sensor and measures the duration of the returning echo signal.

The backend calculates the corresponding distance.

### Data processing

The controller converts the calculated distance into a JSON response that can be requested by the frontend.

### Data visualization

The browser periodically requests measurement data and adds the received values to a line chart.

The intended result is a continuously updating visualization of distance measurements.

## 5. Repository Structure

```text
PiPlot/
|
|-- pom.xml
|-- README.md
|
|-- src/
|   |-- main/
|       |-- java/com/piproject/main/
|       |   |-- Application.java
|       |   |
|       |   |-- Controller/
|       |   |   |-- DistanceController.java
|       |   |   |-- LoginController.java
|       |   |   |-- User.java
|       |   |
|       |   |-- webinit/
|       |       |-- RootConfig.java
|       |       |-- Webconfig.java
|       |       |-- Webinit.java
|       |
|       |-- resources/
|       |   |-- application.properties
|       |
|       |-- webapp/WEB-INF/
|           |-- graph.jsp
|           |-- home.jsp
|           |-- login.jsp
```

### Important files

**Application.java**

The main Spring Boot application entry point.

**DistanceController.java**

Implements sensor triggering, echo signal monitoring, distance calculation, and JSON response generation.

**LoginController.java**

Handles the demonstration login page and navigation.

**User.java**

Contains demonstration credential checking logic. This is not suitable for production use.

**graph.jsp**

Provides the browser-based distance visualization.

**pom.xml**

Contains the project's Maven configuration and dependencies.

## 6. Backend API

The current application declares the following endpoints.

| Method | Endpoint | Description |
|---|---|---|
| GET | `/` | Display login page |
| POST | `/` | Process login form |
| GET | `/home` | Display home page |
| GET | `/graph` | Display measurement graph |
| GET | `/run` | Request sensor measurement |
| GET | `/logout` | Return to login page |

### Example measurement response

The existing backend constructs a JSON structure similar to:

```json
{
  "measuredData": [
    [23.5]
  ]
}
```

This is an illustrative response, not a verified sensor reading.

The API response format is a candidate for simplification during future development.

## 7. Hardware Requirements

The current implementation is designed for a Raspberry Pi with an attached ultrasonic distance sensor.

### Raspberry Pi GPIO configuration

The source code uses the following Pi4J GPIO identifiers:

| Function | Pi4J identifier |
|---|---|
| Sensor trigger | `RaspiPin.GPIO_00` |
| Sensor echo | `RaspiPin.GPIO_02` |

These are Pi4J logical identifiers, not physical Raspberry Pi header-pin numbers.

**Electrical safety:** Verify your exact Raspberry Pi model and ultrasonic sensor before connecting the circuit. Raspberry Pi GPIO inputs are not 5 V tolerant. If the sensor produces a 5 V echo signal, suitable level shifting or a voltage divider is required.

A verified wiring diagram and supported hardware list will be added after hardware testing.

## 8. Installation and Setup

### Prerequisites

- A compatible Java Development Kit.
- Apache Maven.
- A Raspberry Pi with the required GPIO support.
- A compatible ultrasonic sensor.
- A web browser.

The exact working Java version, Raspberry Pi OS version, and hardware model have not yet been revalidated.

### Step 1 — Clone the repository

```bash
git clone https://github.com/ccm026/PiPlot.git
cd PiPlot
```

### Step 2 — Check the Java environment

```bash
java -version
mvn -version
```

### Step 3 — Attempt to build the application

```bash
mvn clean package
```

This is a diagnostic starting point. The legacy dependencies and JSP configuration may require adjustments before the project builds successfully.

### Step 4 — Attempt to start the application

```bash
mvn spring-boot:run
```

This startup command has not yet been verified for the current code and packaging.

The application configuration specifies:

```properties
server.port=9192
```

If startup succeeds, the intended local address is:

```text
http://localhost:9192/
```

### Important limitations

The current backend directly accesses Raspberry Pi GPIO and does not yet support simulated sensor input.

Therefore, successful sensor measurement on a normal PC is not currently supported.

Do not expose the demonstration application publicly, because its login implementation does not provide secure authentication.

## 9. Known Issues and Limitations

The following issues were identified through source-code inspection.

### GPIO initialization and cleanup

The application provisions GPIO pins during measurement requests and shuts down the GPIO controller afterward.

Repeated measurement requests require improved GPIO resource management.

### Missing measurement timeouts

The echo signal waiting loops do not contain timeout conditions.

A disconnected or unresponsive sensor could cause a request to remain blocked.

### Trigger signal timing

The current implementation contains a delay using `Thread.sleep((long) 0.01)`, which does not produce the intended microsecond delay.

### Measurement performance

The controller includes a two-second sleep before acquisition, while the browser requests measurements at approximately one-second intervals.

This needs to be redesigned for reliable updates.

### Frontend communication

The graph currently requests `run.html`, while the controller declares `/run`.

The existing code also uses `parseInt()` when processing readings, which discards fractional precision.

### Demonstration authentication

The application uses hardcoded demonstration credentials without proper session management.

Authentication should either be removed or replaced with an appropriate implementation, depending on future requirements.

### Legacy dependencies

Several dependencies are outdated and may have compatibility or security limitations.

These issues have not yet been confirmed through runtime testing.

## 10. Future Development Roadmap

PiPlot is planned for further independent development.

The following improvements represent prospective work rather than implemented functionality.

| Planned improvement | Estimated effort |
|---|---:|
| Establish reproducible build environment | 4–6 hours |
| Improve GPIO lifecycle and measurement reliability | 5–8 hours |
| Implement simulated sensor measurements | 4–6 hours |
| Improve the web graph and API | 4–6 hours |
| Modernize dependencies, security and automated testing | 6–10 hours |
| **Total estimated development work** | **23–36 hours** |

### Reliable sensor acquisition

- Improve GPIO initialization and cleanup.
- Implement measurement timeouts.
- Correct trigger and echo timing.
- Handle communication errors and sensor disconnections.
- Improve repeated-measurement stability.

### Simulated sensor input

- Separate measurement processing from physical hardware access.
- Generate simulated measurement values.
- Allow development without Raspberry Pi hardware.
- Support automated testing using simulated inputs.

### Improved data visualization

- Correct measurement endpoint communication.
- Preserve decimal measurement precision.
- Handle network and sensor failures gracefully.
- Improve graph responsiveness and update handling.
- Explore useful display controls.

### Application modernization

- Review and update legacy dependencies.
- Improve application structure and maintainability.
- Establish automated tests.
- Add CI build verification.
- Improve technical and deployment documentation.

Actual priorities and effort will be refined during development.

## 11. Testing and Validation

Automated tests are not yet included in the uploaded version of the project.

Future tests should cover:

- Distance calculation.
- Sensor timeout behavior.
- GPIO resource handling.
- Sensor error handling.
- Simulated measurements.
- JSON response generation.
- Repeated requests.
- Frontend measurement processing.

Hardware-based validation and software-only testing should be documented separately.

## 12. Screenshots and Demonstration

Real screenshots of the application will be added after the original application has been successfully built and run again.

Planned documentation includes:

- Application home page.
- Distance measurement graph.
- Raspberry Pi and sensor connection.
- Simulation demonstration when implemented.

Screenshots should represent actual application output rather than mockups presented as working functionality.

## 13. Project Background

PiPlot was originally developed by me during my student years.

The repository preserves the original implementation and its development history.

I plan to continue developing and maintaining the application independently, focusing on reliability, usability, modernization, and testing.

## 14. License

A top-level `LICENSE` file will be added after confirming the rights to license the complete codebase.

Until an applicable license has been included, no general open-source reuse license is granted by this repository. Third-party libraries remain subject to their own licensing terms.

## 15. Feedback and Contributions

Feedback, bug reports, and improvement suggestions are welcome through GitHub Issues when enabled.

Please avoid sharing passwords, API keys, or private information in issues.

---

**PiPlot — Connecting physical sensor measurements with browser-based visualization.**
