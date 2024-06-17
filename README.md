
# PortableChunkLoader Plugin

PortableChunkLoader is a Minecraft plugin designed to keep chunks loaded even when players are not nearby. This can be particularly useful for farms, redstone machines, and other automated systems that need to keep running continuously. Additionally, the chunk loader can be moved, making it ideal for moving structures such as flying machines.

## Features

- **Chunk Loading**: Keep specific chunks loaded to ensure your machines and farms keep running.
- **Portable**: Chunk loaders can be moved even if a player isn't present, allowing moving structures to stay loaded.
- **Easy Management**: Simple commands to give chunk loaders, list active chunk loaders, and manage configurations.
- **Player Ownership**: Track and manage chunk loaders based on player ownership.
- **GUI Interface**: An intuitive graphical user interface to manage and view chunk loaders.

## Installation
1. Download the latest release of the PortableChunkLoader plugin from SpigotMC.
2. Place the downloaded JAR file into the `plugins` directory of your Minecraft server.
3. Restart the server to load the plugin.

## Commands
- `/cl give <player> <hours> <minutes> <seconds>`: Give a chunk loader to a player for a specified duration.
- `/cl list [player]`: List all active chunk loaders. If a player name is specified, only chunk loaders owned by that player will be listed.
- `/cl reload`: Reload the plugin configuration.
### Permissions
- `chunkloader.give`: Permission to give chunk loaders to players.
- `chunkloader.list`: Permission to list all active chunk loaders.
- `chunkloader.reload`: Permission to reload the plugin configuration.


## Contributing

Contributions are welcome! Please follow these steps to contribute:

1. Fork the repository.
2. Create a new branch for your feature or bugfix.
3. Make your changes and commit them with descriptive commit messages.
4. Submit a pull request with a description of your changes.

### License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.

## Contact

For support, questions, or suggestions, please open an issue on the [GitHub repository](https://github.com/OkinawaBoss/portablechunkloader/issues).
