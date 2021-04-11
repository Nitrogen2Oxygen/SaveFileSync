# Save File Sync
A Java application for syncing files between different computers through a mutual data server.

## Installation
### Production
**WARNING: This project is currently in an early alpha stage.
It's not recommended to use pre-release versions due to limited bug testing.**

1. Download the [latest jar release](https://github.com/Nitrogen2Oxygen/SaveFileSync/releases/latest) (SaveFileSync.jar)
2. Run the .jar file using the latest version of Java

### Development
#### Requirements
* The latest version IntelliJ IDEA (Used for IntelliJ's UI designer)
* JDK version 11

#### Instructions
1. Clone the repository: `git clone https://github.com/<Name>/SaveFileSync.git`
2. Open the containing `SaveFileSync` folder as an IntelliJ project.
   The settings from the `.idea` folder should load the environment properly.
3. Make your changes if necessary
4. To build, simply run `mvn install` from the terminal or sidebar
This will package all dependencies inside the `/target/SaveFileSync.jar` file.
   For guidelines for contributing, see [Contributing](#Contributing)


## Usage
The UI allows for you to set directories or individual files as save-files.
The files can be exported or imported manually to a data server. 
Other computers can import saves from the data server using the app and store them in the necessary locations.


## Roadmap
Currently, the first release is in development. The following major features need to be completed:
* Server functionality with Google Drive
* Theming and other custom settings
* Finalized UI design
* Infinity bug fixes!!!

## Contributing
Anyone is welcome to contribute to the project.
At the project's current state, there are no guidelines for contributions.
Just submit a pull request, and I'll work with you.

## License
[MIT](https://github.com/Nitrogen2Oxygen/SaveFileSync/blob/main/LICENSE)
