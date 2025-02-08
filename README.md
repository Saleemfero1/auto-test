```markdown
# auto-test
Hackathon 2025

## Setup Guide

### Clone the Project

git clone <repository-url>
cd auto-test
```

### Build the Project
```sh
./gradlew build
```

### Run the Plugin
```sh
./gradlew runIde
```

This will open a new IntelliJ IDEA window. You can open any existing project or create a new project space where the plugin will place the generated feature files.

## Plugin Description

AutoTest Generator automates the creation of test scenarios from **user stories** and **design documents**. This IntelliJ plugin extracts structured content from Word documents, converts it into JSON, and leverages OpenAPI to generate `.feature` files with test cases for the specified feature.

### Key Features:
- Reads **User Stories** & **Design Docs** from Word files
- Converts structured content into **test scenarios**
- Sends data to OpenAPI for **automated test case generation**
- Generates **.feature files** for behavior-driven testing (BDD)
- Saves the feature file inside your project for easy access

### How to Use:
1. Install the plugin
2. Import the project
3. Go to Tools and click on "Generate Gherkin Test Scenarios"
4. It will open a dialog box; upload `input.doc` which is a combined user story and design document Word file
5. Wait until the "Test cases generated" popup is shown on the screen
6. You can find the test cases in the generated `feature` file folder
```
