# Changelog

This file records notable project changes. GitHub Actions also writes the latest detected file and commit summary to `.github/generated/last-detected-changes.md` on push.

## Unreleased

- Fixed GitHub Actions AndroidLiquidGlass repository input normalization so full GitHub URLs are accepted.
- Changed manual Release versioning to auto-increment semantic tags such as `0.1.0` to `0.2.0`.
- Reset Android and Desktop default package versions to semantic baseline `0.1.0`.
- Updated Gradle wrapper to `9.5.0` to satisfy Android Gradle Plugin `9.3.0` in GitHub Actions.
- Fixed `desktopMain` unresolved source-set accessor in `desktopApp` and `shared` Gradle scripts.

- Added cross-platform Compose Multiplatform project scaffold.
- Added Miuix UI foundation and AndroidLiquidGlass-kmp backdrop integration.
- Added initial Bilibili API vertical slices for recommendation, popular feed, user nav, and search.
- Added unsigned GitHub Actions build and manual release workflow.
