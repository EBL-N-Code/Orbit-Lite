# Apex

**AI chat inside Minecraft, built with Java and Fabric.**

Apex adds an in-game chat interface for talking to an AI assistant without leaving Minecraft. The client includes streamed responses, conversation history, formatted messages, and an account/usage display.

![Apex chat interface](https://cdn.modrinth.com/data/cached_images/da52605777f20c87a72385aeb443c1e52952d8d1_0.webp)

[Website](https://apexmod.tech) · [Modrinth](https://modrinth.com/mod/apexmod) · [Report an issue](https://github.com/7azeemm/Apex/issues)

## Features

- A dedicated chat screen, opened with **Right Shift**, `/chat`, or `/apex chat`.
- Responses displayed as they stream from the backend.
- Conversation creation, history browsing, and deletion.
- Custom text rendering for headings, code blocks, blockquotes, tables, and other formatted content.
- Profile and usage information supplied by the backend.
- A Java client connected to separate Rust and Python services.

AI responses require the backend and model service to be available. The mod does not run a language model locally.

## Installation

1. Download a release matching your Minecraft version from [Modrinth](https://modrinth.com/mod/apexmod).
2. Install the matching Fabric Loader and Fabric API.
3. Place the Apex JAR in your Minecraft `mods/` folder.
4. Launch the game and press **Right Shift**, or enter `/chat`.

Use the compatibility information for the specific release instead of assuming that the newest Minecraft version is supported.

## Building from source

The current source build targets **Minecraft 1.21.11** and **Java 21**. Dependency versions are configured in [gradle.properties](gradle.properties).

```bash
git clone https://github.com/7azeemm/Apex.git
cd Apex
bash gradlew build
```

On Windows, use `gradlew.bat build`. The build writes JARs to `build/libs/`; use the remapped mod JAR, not the sources JAR.

To launch the development client:

```bash
bash gradlew runClient
```

> [!NOTE]
> The current Gradle target is 1.21.11, while [fabric.mod.json](src/main/resources/fabric.mod.json) still declares `~1.21.9`. These values should be reviewed together when preparing a release. A build target alone does not establish runtime compatibility.

## Architecture

| Repository | Responsibility |
| --- | --- |
| **Apex** — this repository | Java/Fabric integration, chat state, UI, and streamed-text rendering |
| [Apex Server](https://github.com/7azeemm/Apex-Server) | Rust API, player authentication, conversation persistence, and usage accounting |
| [Apex AI Server](https://github.com/7azeemm/Apex-AI-Server) | Python/FastAPI service for model responses and chat titles |

Within the client:

- [chat/](src/main/java/org/apex/chat) manages conversations and message state.
- [gui/](src/main/java/org/apex/gui) implements the chat screen, sidebar, profile view, and input editor.
- [text/](src/main/java/org/apex/text) parses, wraps, and renders formatted responses.
- [network/](src/main/java/org/apex/network) handles authentication and backend requests.
- [render/](src/main/java/org/apex/render) provides custom rendering support.

The backend URL is currently a source constant in [Constants.java](src/main/java/org/apex/Constants.java). Running a development client does not automatically create a local backend. Self-hosting requires the companion services and a suitable backend URL.

## Usage and data flow

Daily usage limits and available plans are controlled by the backend, rather than by the client alone. Messages, conversation context, and account information needed for the service pass through the backend; chat content is then sent to its configured model provider.

Avoid sending secrets or sensitive personal information. AI answers can be incorrect, so verify anything important before acting on it.

## Planned features

The project's existing roadmap includes custom API keys, web search, voice interaction, and image generation. These are future directions, not features claimed to be implemented in this client today.

## Feedback

Report reproducible bugs in [GitHub Issues](https://github.com/7azeemm/Apex/issues). Include your Minecraft, Fabric, and Apex versions, steps to reproduce, and relevant logs with tokens and personal information removed.

## License

[All rights reserved](LICENSE.txt).
