# 🎵 Melody Search Backend Server

This project is the backend server for the Melody Search application. It is designed to run in a Docker container on an
AWS EC2 instance.

---

## 📁 Project Structure Overview

<pre> <code> 
project-root/
├── lib/ # Contains all required JAR files 
├── META-INF/ # Contains manifest.txt for identifying the main class 
├── out/ 
│ ├── artifacts/ # Contains the compiled JAR file 
│ ├── javadoc/ # Auto-generated documentation 
│ └── production/ # Compiled production code 
├── src/ 
│ ├── exceptions/ # Custom internal exceptions 
│ ├── music/ # Core music-related Java objects 
│ ├── parser/ # Specialized music parsers 
│ ├── scripts/ # Scripts for deployment and server management 
│ ├── serverCode/ # Full HTTP server stack 
│ ├── txtFiles/ # Documentation links and setup instructions 
│ └── workers/ # Core indexing/searching logic 
├── docker-compose.yml # Docker configuration 
└── Dockerfile # Dockerfile for containerizing the backend </code> </pre>
---

## 📦 Core Components

### 🔧 `lib/`

- Contains all required `.jar` libraries.
- Used by `src/scripts/jarAndDockServer`.

### 🗂️ `META-INF/`

- Contains `manifest.txt` to identify the main class for the server.
- Used in `jarAndDockServer`.

### 🧾 `out/`

- `javadoc/`: Auto-generated Java documentation.
- `artifacts/meiSearchBackend.jar`: Compiled executable JAR.
- `production/`: Built production code.
- **Note:** Always rebuild the JAR after changes—this is what the Docker container runs.

---

## 🧑‍💻 Source Code Overview (`src/`)

### `exceptions/`

- Contains a single internal exception: `Empty`, used for parsing-related logic.

### `music/`

- Core music domain objects:
    - `Chord`, `Document`, `KeySig`, `Measure`, `Note`

### `parser/`

- Specialized parsers for each music object.
- Also includes:
    - `AccidentalTracker`: For proper interval logic
    - `MetaDataParser`: Parsing MEI metadata

### `scripts/`

Automation and deployment scripts. Recommended execution order:

1. `clearAndCopyToEc2` (in local): Copies code, JARs, and scripts to the EC2 instance
2. `rmAllDocker` (in ec2): Stops and removes mei-search-backend* containers on the EC2 
3. `jarAndDockServer`(in ec2): Packages everything, runs Docker, creates the Elasticsearch index
4. `indexDatabase` (in ec2): Indexes music data from a given database. Uses `src/workers/Indexer`

**Configuration Notes:**

- **`clearAndCopyToEc2`:** Update the `export` paths (EC2 target, backend directory, PEM file)
- **Other scripts:** Only modify if you're confident with the setup

### `serverCode/`

Handles HTTP request/response lifecycle:

1. `HttpServer` attaches endpoint handlers
2. Handlers (in `Handlers/`) deserialize requests (`Requests/`), process with `Services/`
3. `Services` return a `Response` object (`Responses/`), serialized and sent back by handler

**Example flow:**

Endpoint: /addMusic → HanAddMusic → ReqAddMusic → AddMusic → ResAddMusic → response sent

- `HttpServerProxy`: Client-side version (used in `/workers/Indexer`)

### `txtFiles/`

Contains:

- Elasticsearch documentation links
- Setup instructions for a clean EC2

### `workers/`

Key backend logic lives here:

- `ElasticProcessor`: Core indexing and search logic
- `Indexer`: Indexes database via `HttpServerProxy`. Used in the script `indexDatabase`
- `Record`: Core document structure indexed and retrieved by Elasticsearch

---

## 🐳 Docker Setup

- `docker-compose.yml` and `Dockerfile` configure the container environment.
- Only modify these if you're familiar with Docker.

---

## 📄 Documentation

- **Generated Javadocs**: Found in `out/javadoc`
- **Additional Notes**: See `src/txtFiles/documentationLinks`
- **Inline Comments**: The codebase is well-commented for clarity

---

## 📬 Questions?

For deeper configuration changes or technical questions, please reach out directly. Ensure you understand all
dependencies before modifying core components.

---

## ✅ Final Notes

> Good luck, and happy searching!