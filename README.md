**NOTICE: As of 10/31/2021 this project will cease recieving any updates. The official rewrite and rebranding will be available under [SaveDataSync](https://github.com/Nitrogen2Oxygen/SaveDataSync). Furthermore, this repo will be permanently archived and eventually privated in roughly a month. No one actually ended up using this app so this shouldn't matter, but eh.**

# Save File Sync
A Java application for syncing files between different computers through a mutual data servers.

## Features
* Import and export save files/directories
* WebDAV, Dropbox & OneDrive data server support
* Create and restore backups for your save files
* Various settings and themes


## Download
1. Download the [latest Java SE](https://java.com/en/download/) 
2. Download the [latest jar release](https://github.com/Nitrogen2Oxygen/SaveFileSync/releases/latest) (SaveFileSync.jar)
3. Run the .jar file using Java

## Build
### Requirements
* The latest version IntelliJ IDEA (Used for IntelliJ's UI designer)
* OpenJDK version 8

#### Instructions
1. Clone the repository: `git clone https://github.com/<Name>/SaveFileSync.git`
2. Open the containing `SaveFileSync` folder as an IntelliJ project.
   The settings from the `.idea` folder should load the environment properly.
3. Make your changes if necessary
4. To build, simply run `mvn install` from the terminal or sidebar
This will package all dependencies inside the `/target/SaveFileSync.jar` file.
   For guidelines for contributing, see [Contributing](#Contributing)


## Contributing
Anyone is welcome to contribute to the project.
There are no major guidelines for contributions.
Just submit a pull request, and I'll work with you.

## License
[MIT](https://github.com/Nitrogen2Oxygen/SaveFileSync/blob/main/LICENSE)
