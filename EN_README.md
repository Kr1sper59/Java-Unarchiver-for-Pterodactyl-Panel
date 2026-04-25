# Java Unarchiver for Pterodactyl Panel

In the panel, there are restrictions on unpacking archives through the built-in file manager, which can make it difficult to transfer large assemblies.

This project solves the problem by providing a JAR utility for unpacking archives right at server startup.

---

## Features
- Support.`zip`
- Support.`tar.gz `
- Deleting the archive after unpacking

---

## Installation

1. Download the latest build from [Releases](https://github.com/Kr1sper59/Java-Unarchiver-for-Pterodactyl-Panel/releases)
2. Upload to the server
3. Install the `server.jar` (or another name of the kernel in the startup settings)
4. Make sure that Java 21 is used.
5. Place the archive next to the JAR file.
6. Create a `config.yml` using the template below
7. Start the server

---

## Configuration
Working example of configuration [here](src/main/resources/config.yml)
```yaml
archive: archive_name.zip
deleteAfter: false
```

### Parameters:
archive - archive name (.zip or .tar.gz )
deleteAfter - delete the archive after unpacking

⚠️ The config is not created automatically - create it manually.

---

## Bugs and suggestions

If you find a problem or want to suggest an improvement, create an Issue.