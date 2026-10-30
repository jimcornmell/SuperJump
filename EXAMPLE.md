# Some examples of things that SuperJump can jump from:

* A URL:                   https://plugins.jetbrains.com/plugin/34717-superjump
* A URL:                   https://jimscosmos.com/superjump-a-jetbrains-plugin/
* A URL:                   [My Home Setup](https://jimscosmos.com/code/)
* A CVE:                   CVE-2021-44228
* A colour:                #ff0000
* A colour:                #FFCC00
* A GitHub repository:     jimcornmell/superjump
* A GitHub issue:          DJ-4843

# A Dockerfile

This will not be matched as it does not sit inside a Dockerfile or a Dockerfile markdown block.
FROM openjdk:11-jdk-slim

```Dockerfile
# This however will be matched.
FROM openjdk:11-jdk-slim
```

# Linux command examples.
Also see [EXAMPLE.sh](EXAMPLE.sh)

A Linux command:
```
# This line will not be matched as it does not recognize it as a Linux command in this markdown block.
grep Something *
```

```bash
#This identical command will be matched as this is a Bash markdown block.
grep Something *
ls -l
```

# Some dependency examples from a `build.gradle` file:
```
# These will not be matched.
implementation("org.eclipse.jdt:junit:3.3.0-v20070606-0010")
implementation("org.eclipse.jdt:junit")
```

```groovy
// These will be matched in this markdown block.
implementation("org.eclipse.jdt:junit:3.3.0-v20070606-0010")
implementation("org.eclipse.jdt:junit:3.2.1-r321_v20060810")
implementation("org.eclipse.jdt:junit")
```
