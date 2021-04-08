# Save File Sync
A Java application for syncing files between different computers through a mutual data server.

## Installation
### Production
On the first major release, a JAR file will be uploaded to GitHub.
Downloading and running with an instance of Java will work.

Here is a link to the [latest release](https://github.com/Nitrogen2Oxygen/SaveFileSync/releases/latest)

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
The goal is to have a full GUI where many save files can be managed.
The 3 target servers are WebDAV, Dropbox and Google Drive.
A fully functional program has yet to be complete, but v0.2.0-alpha is working well with WebDAV.

TL;DR I'll work on this part later

## Roadmap
Currently, the first release is in development. The following major features need to be completed:
* Server functionality with Google Drive
* Server functionality with Dropbox
* Automatic sync logic
* Finalized UI design
* Infinity bug fixes!!!

## Contributing
Anyone is welcome to contribute to the project.
At the project's current state, there are no guidelines for contributions.

## License
[MIT](https://github.com/Nitrogen2Oxygen/SaveFileSync/blob/main/LICENSE)
