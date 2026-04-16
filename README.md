[![Build](https://img.shields.io/github/actions/workflow/status/ShadelessFox/odradek/build.yml?logo=githubactions&logoColor=white&label=Build)](https://github.com/ShadelessFox/odradek/actions/workflows/build.yml)
[![Discord](https://img.shields.io/discord/1012475585605414983?label=Chat&logo=discord&logoColor=white)](https://discord.gg/Gt4gkMwadB)
[![Support](https://img.shields.io/badge/Support-Ko--fi-blue?logo=kofi&logoColor=white)](https://ko-fi.com/shadelessfox)

# Odradek

This is a reincarnation of [Decima Workshop](https://github.com/ShadelessFox/decima), a modding tool
for Horizon Zero Dawn and Death Stranding.

Odradek targets **Horizon Forbidden West** and **Death Stranding 2** in an attempt to come up with something that could be useful for modders.

> [!IMPORTANT]
> 
> A separate build targeting **Death Stranding 2** is now available [here](https://github.com/ShadelessFox/odradek/releases/tag/v1.0).

### Features
- Preview for static and skinned models; export to `.cast`
- Preview for regular and animated textures; export to `.dds`, `.png`
- Audio playback; export to `.wav`
- Rich object inspector that utilizes type information
- Command-line interface with the export functionality fully exposed

### Building

> [!TIP]
> Prebuilt binaries can be downloaded from [Actions](https://github.com/ShadelessFox/odradek/actions/workflows/build.yml).

#### Prerequisites

- Make sure you have **JDK 25** installed; [Adoptium](https://adoptium.net/temurin/releases/?arch=x64&version=17&package=jdk) is recommended for use
- Make sure you have **Git** installed

Open the terminal and execute the following commands:
1. `git clone https://github.com/ShadelessFox/odradek --recursive`
2. `cd odradek`
3. `./mvnw clean package`

Ready-to-use distributions can be found under the `odradek-app/target/dist` directory.

#### Troubleshooting

<details>
<summary>Module sh.adelessfox.atrac9j not found</summary>
Make sure you cloned the repository with the `--recursive` flag, or run the following command in the root directory of the project:

```bash
git submodule update --init --recursive
```

Then refresh the Maven project in IntelliJ IDEA and make sure `sh.adelessfox:atrac9j` project is **not** marked as _ignored_.

</details>

<details>
<summary>Preview features are not enabled</summary>
You will need to supply a compiler flag  in IntelliJ IDEA's settings to make this error go away.

Go to _File | Settings | Build, Execution, Deployment | Compiler_
and set **___Shared VM options___** to `--enable-preview`.
</details>

<details>
<summary>Cannot resolve symbol 'HFW'</summary>
IntelliJ IDEA refuses to provide code analysis for generated RTTI classes due to the enormous size of the generated
code, and all references will be highlighted in _red_. The project will still compile and launch just fine.

To fix this, go to **Help -> Edit Custom Properties** and add the following lines:

```properties
idea.max.content.load.filesize=10000
idea.max.intellisense.filesize=10000
```

Then restart the IDE.
</details>

## License
This project is licensed under the GPL-3.0 license.

This project is not sponsored by or related to Guerrilla Games, Kojima Productions, Sony Interactive Entertainment, or others.

Source code and all software made with Decima engine belong to their developers.
