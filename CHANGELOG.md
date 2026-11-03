<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# SuperJump Changelog

All notable changes to this project will be documented in this file.

## 1.1.1

### Fixed
* Settings: the "enabled" column of the jumpers table now shows a checkbox again (column class uses the boxed Boolean type).

## 1.1.0

### Added
* Support for code blocks in markdown files.
* New "Super Jump to Git source repository" action (Ctrl+Shift+0) that opens the current file and line on GitHub, GitLab, Bitbucket or Azure.
* New setting to choose whether Super Jump falls back to jumping to Git when no regular expression matches.
* Errors and missing URLs are reported through SuperJump notifications instead of failing silently.
* Settings state is now versioned to allow future migrations.
* Expanded the plugin description, README, EXAMPLE.md and added EXAMPLE.sh (including Linux command examples).
* Added unit tests for Git URL building, URL support and text-to-URL matching.

### Fixed
* Improves some of the regular expressions.
* Fixes some errors being reported, although plugin was in fact working.
* Jumper matching now uses the selection, word or line under the cursor, and skips jumpers with invalid regexes instead of breaking every jump.
* Settings: text edits to jumpers are picked up immediately so the Apply button appears.
* Removed the unused "What's New" action registration.

## 1.0.0 - Initial Public Release

### Added
* Added badges to the README file.
* Added a changelog file to document changes in the project.
* Updated the README file with more information about the project.
* Added github actions to build and test the project on every commit.

## 0.0.3 - Unreleased

### Added
* Allowed full user defined URL patterns in the settings.
* Added move up/down functionality to the jump list.
* Added reset to defaults functionality in the settings.
* Updated docs.

## 0.0.2 - Unreleased

### Added
* Bug fixes and improvements.
* Added support for more URL patterns and improved the jump functionality.

## 0.0.1 - Unreleased

### Added
* Initial project setup with Gradle and Kotlin DSL.
