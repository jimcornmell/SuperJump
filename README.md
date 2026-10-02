# SuperJump

[plugin-repo]: https://plugins.jetbrains.com/plugin/12345-superjump
[plugin-version-svg]: https://img.shields.io/jetbrains/plugin/v/12345-superjump.svg
[plugin-downloads-svg]: https://img.shields.io/jetbrains/plugin/d/12345-superjump.svg
[plugin-rating-svg]: https://img.shields.io/jetbrains/plugin/r/rating/12345

![Java CI with Gradle](https://github.com/jimcornmell/SuperJump/workflows/Java%20CI%20with%20Gradle/badge.svg)
![Validate against IJ versions](https://github.com/jimcornmell/SuperJump/workflows/Validate%20against%20IJ%20versions/badge.svg)
[![JetBrains plugins][plugin-version-svg]][plugin-repo]
[![JetBrains Plugin Rating][plugin-rating-svg]][plugin-repo]
[![JetBrains plugins][plugin-downloads-svg]][plugin-repo]

![Build](https://github.com/jimcornmell/SuperJump/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

## Description
<!-- Plugin description -->

SuperJump is free and open-source IntelliJ Platform plugin that allows you to quickly jump to a specific line in your codebase. It provides a simple and efficient way to navigate through your code, making it easier to find and edit the lines you need.

## Example of usage:

### `SuperJumpGit`

This takes the current file and line number and jumps to the same file and line number in the git repository.

### `SuperJump`

SuperJump analyzes the text under the cursor and jumps to a specific location based on the text.  The following table shows some examples of what SuperJump can do:

| Text                                                           | Action                                                           | Default URL |
|----------------------------------------------------------------|------------------------------------------------------------------|-------------|
| `cve-2021-44228`                                               | Jump to the CVE-2021-44228 page on the configured website        |             |
| `http://someurl.com`                                           | Open the website in your default browser                         |             |
| `https://someurl.com`                                          | Open the website in your default browser                         |             |
| `dj-1234`                                                      | Jump to the Jira ticket DJ-1234 in your configured Jira instance |             |
| `ls`                                                           | This looks like a command so jump to documenation for it.        |             |
| `#ff0000`                                                      | This looks like a colour so jump to some information             |             |
| `implementation("org.eclipse.jdt:junit:3.3.0-v20070606-0010")` | Jump to the Maven dependency                                     |             |
| `org.eclipse.jdt:junit:3.3.0-v20070606-0010`                   | Jump to the Maven dependency                                     |             |
| `jimcornmell/superjump`                                        | Jump to the GitHub repository `jimcornmell/superjump`            |             |
| Any text which matches none of the above does a `SuperJumpGit` | Jump to the repository location for the current file and line    |             |

Please raise a pull request if you have any suggestions for new features or improvements.

## Requirements

Intellij IDEA 2026.1 or more recent (I **try** to support releases as they come out, but I can't guarantee that it will work on all versions)

* I let Toolbox automatically update my IDE, so I can test the plugin on the latest version.
* There is no special requirements for the plugin (so it **should** work with any version that the plugin installs in, but I make no guarantees).

## Contributing

This is an open source project open to anyone. Contributions are extremely welcome!  Just raise a pull request or open an issue if you have any questions or suggestions.

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "SuperJump"</kbd> >
  <kbd>Install</kbd>

- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/jimcornmell/SuperJump/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Configuration

SuperJump can be configured by going to:

<kbd>Settings/Preferences</kbd> > <kbd>Tools</kbd> > <kbd>SuperJump</kbd>

![configuration.png](docs/configuration.png)

The defaults should work for most users, but you can configure the plugin to your liking.

There is one exception, if you want to jump to a jira ticket you need to configure the url for your jira instance.
Then you can set the url for your jira instance in the <kbd>Jira URL</kbd> field.  Highlighted in the image above.

### Urls

You can use your own urls in the settings.  Just replace the defaults with your own urls.

### Keyboard shortcuts:

#### IntelliJ IDEA

Default keyboard shortcuts for SuperJump and SuperJumpGit are:
SuperJump: <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>U</kbd>
SuperJumpGit: <kbd>Ctrl</kbd> + <kbd>Alt</kbd> + <kbd>Shift</kbd> + <kbd>W</kbd>

You can overwrite the default keyboard shortcuts in IntelliJ IDEA by going to:

<kbd>Settings/Preferences</kbd> > <kbd>Keymap</kbd> and searching for "SuperJump" or "SuperJumpGit".

#### VIM

You can map the SuperJump and SuperJumpGit actions to your preferred key combinations in your `.ideavimrc` file.
For example, you can use the following mappings:

```vim
nmap gs <Action>(SuperJump)
nmap gw <Action>(SuperJumpGit)
```

## Building

Project is managed by Gradle. So building is quite easy.

### Building the plugin distribution file

Run the following command:

```sh
./gradlew buildPlugin
```
The plugin distribution file is located in ```build/distributions```.

### Testing

You can also easily test the plugin. Just run the following command:

```sh
./gradlew runIde
```

### Testing the CI builds

You can also download and install CI builds of the latest commits or a specific pull request:

- open the [`Build plugin zip` workflow](https://github.com/jimcornmell/superjump/actions/workflows/buildZip.yml)
- click on the build you are interested in
- scroll down and download the `SuperJump <version>.zip` file
- install `SuperJump <version>.zip` into IntelliJ IDEA by following these [instructions](https://www.jetbrains.com/help/idea/managing-plugins.html#install_plugin_from_disk).

### Testing nightly builds

You can easily install nightly builds from the nightly channel:

- in IntelliJ, open `Setting > Plugins > [Gear icon] > Manage Plugin Repositories...`
- Add `https://plugins.jetbrains.com/plugins/nightly/12345` and press `OK`
  <img alt="Nightly Channel Repository" src="docs/images/nightly-channel-repo.png" width="500px" />
- install the latest `SuperJump` version

Nightly builds are published once a day.

## Feedback

File a bug in [GitHub Issues](https://github.com/jimcornmell/superjump/issues).

## License

GNU GENERAL PUBLIC LICENSE - Version 3, 29 June 2007 - See [LICENSE](LICENSE.txt) file.

----

## Template ToDo list
- [x] Adjust the [group](./gradle.properties), as well as the [id](./src/main/resources/META-INF/plugin.xml), [name](./src/main/resources/META-INF/plugin.xml), and [sources package](./src/main/kotlin).
- [ ] Adjust the plugin [description](./src/main/resources/META-INF/plugin.xml) (see [Tips][docs:plugin-description]) and this README to describe what your plugin does.
- [x] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [ ] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [ ] Set the `MARKETPLACE_ID` in the above README badges. You can obtain it once the plugin is published to JetBrains Marketplace.
- [ ] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate) related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.
- [x] Change name to SuperJump
- [x] VIM mapping with `gj`:



