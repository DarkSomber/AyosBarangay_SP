# Application README

## Project Overview

This application is a web-based system developed using **Java, Vaadin, and MySQL**. It provides an interface for viewing submitted reports and complaints.

### Implemented Features

- **View Reports** – Allows users to view reports stored in the database.
- **View Complaints** – Allows users to view complaints stored in the database.
- **Data Models** – Includes the models used to represent and manage application data.
- **Dummy SQL Data** – Includes sample SQL data for testing and development purposes.

## Technologies Used

- **Java** – Primary programming language.
- **Vaadin** – Framework for building the web-based user interface.
- **MySQL** – Relational database management system.
- **Maven** – Project dependency management and build tool.
- **IntelliJ IDEA** – Integrated development environment (IDE).

## Prerequisites

Before running the application, make sure you have installed:

1. **Java JDK** – Use the version required by the project configuration.
2. **IntelliJ IDEA** – Used to open, configure, and run the project.
3. **Vaadin Plugin for IntelliJ IDEA** – Install the Vaadin plugin to support Vaadin development within the IDE.
4. **MySQL Server** – Required to store and retrieve application data.
5. **Maven** – The project includes a Maven Wrapper (`mvnw`), so a separate Maven installation may not be necessary.

## Getting Started

### 1. Clone or Download the Repository

Clone the project repository or download its source code to your computer.

### 2. Open the Project in IntelliJ IDEA

1. Launch IntelliJ IDEA.
2. Select **Open** and choose the project's root directory.
3. Allow IntelliJ IDEA to import the Maven project and download its dependencies.
4. Install the **Vaadin plugin** if it is not already installed. You can find it under **Settings → Plugins**.

### 3. Configure MySQL

1. Start your MySQL server.
2. Create the database required by the application.
3. Import the provided dummy SQL file into the appropriate database.
4. Configure the application's database connection settings with your MySQL hostname, port, database name, username, and password.

Check the project's configuration files, such as `application.properties`, for the expected database settings. Update them to match your local MySQL configuration.

**Note:** The database must be running, and the required tables and sample data must be available before testing the report and complaint viewing features.

### 4. Run the Application

Locate the `Application` class in IntelliJ IDEA and run it.

Alternatively, open a terminal in the project's root directory and execute:

```bash
./mvnw
```

On Windows, use:

```powershell
.\mvnw.cmd
```

Once the application starts, open the local URL displayed in the run console to access the application.

## Groq Report Analysis

Administrators can request an AI-assisted assessment from the Analyze report button in a report dialog.
The assessment is advisory only: it does not change report status, and a suspicious-report flag is a
human-review signal, not proof. Only the report description is sent to Groq; report address, user
identity, coordinates, and media are not included.
The controlled report categories are infrastructure, water and drainage, waste management, public
safety, animal control, public health, noise and disturbance, and other.

Set `GROQ_API_KEY` in the application environment before starting the app. `GROQ_MODEL` optionally
selects a Groq chat-completions model; its default is `openai/gpt-oss-20b`, which supports strict
JSON-schema outputs according to the [Groq structured outputs documentation](https://console.groq.com/docs/structured-outputs).
If you override the model, choose one currently listed by Groq as supporting strict structured outputs.
For example, in PowerShell:

```powershell
$env:GROQ_API_KEY = "your-groq-api-key"
$env:GROQ_MODEL = "openai/gpt-oss-20b"
.\mvnw.cmd
```

The key is read from the environment and is not stored in the repository. Analysis results are
cached in memory for the current application runtime; they are not persisted and are cleared on restart.

## Attachment review checks

The admin report dialog checks stored PNG/JPEG files by decoding them, checks MP4 files for a
recognizable container signature (not full video playback), and flags missing, empty, unsupported,
unreadable, oversized, or out-of-upload-folder files. Image inspection is limited to 12 megapixels;
the upload UI allows at most five files of 20 MiB each. Findings are advisory only. A report with
no attachments is shown as having no attachment provided, not as invalid.

To compile and run tests:

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd test
```

## Building the Application

To compile and package the application using Maven, run:

```bash
./mvnw package
```

On Windows:

```powershell
.\mvnw.cmd package
```

The generated build artifacts will be placed in the `target/` directory.

## Current Development Status

The current implementation includes the data models, dummy SQL data, and user interface features for viewing reports and complaints.

Additional functionality may be added as development progresses.

## Notes for Developers

- Ensure that the MySQL connection settings are correct before running the application.
- Keep the database schema consistent with the application's data models.
- Use the provided dummy SQL data for development and testing.
- Install and enable the Vaadin plugin in IntelliJ IDEA for a smoother development workflow.
- Do not commit database passwords or other sensitive credentials to version control.
