# Odradek

This is a reincarnation of [Decima Workshop](https://github.com/ShadelessFox/decima), a modding tool
for Horizon Zero Dawn and Death Stranding.

Odradek, however, despite its name, targets exclusively
Horizon Forbidden West in an attempt to come up with something that could be useful for modders.

### Building

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
<summary>Preview features are not enabled</summary>
You will need to supply a compiler flag  in IntelliJ IDEA's settings to make this error go away.

Go to _File | Settings | Build, Execution, Deployment | Compiler_
and set **___Shared VM options___** to `--enable-preview`.
</details>

<details>
<summary>Cannot resolve symbol 'HorizonForbiddenWest'</summary>
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
