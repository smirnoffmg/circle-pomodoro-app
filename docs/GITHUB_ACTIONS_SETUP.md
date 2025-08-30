# GitHub Actions Setup for APK Building

This document explains how to set up GitHub Actions to automatically build APKs for your Android project.

## Overview

The GitHub Actions workflow provides three build scenarios:

1. **CI Job**: Runs on PRs and pushes to main, builds debug APK
2. **Release Build**: Runs on pushes to main, builds unsigned release APK
3. **Signed Release Build**: Runs manually, builds signed release APK

## Required Secrets

Add these secrets to your GitHub repository (Settings > Secrets and variables > Actions):

### Firebase Configuration
- `GOOGLE_SERVICES_JSON`: Base64 encoded content of your `google-services.json` file

### Signing Configuration (for signed releases)
- `KEYSTORE_BASE64`: Base64 encoded keystore file
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias
- `KEY_PASSWORD`: Key password

## Setting Up Secrets

### 1. Firebase Configuration

```bash
# Encode your google-services.json file
base64 -i app/google-services.json | pbcopy  # macOS
# or
base64 -i app/google-services.json | clip    # Windows
# or
base64 -i app/google-services.json | xclip -selection clipboard  # Linux
```

Paste the output as the `GOOGLE_SERVICES_JSON` secret.

### 2. Keystore for Signing

If you don't have a keystore, create one:

```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

Then encode it:

```bash
base64 -i my-release-key.jks | pbcopy
```

Set the secrets:
- `KEYSTORE_BASE64`: The encoded keystore
- `KEYSTORE_PASSWORD`: The password you used when creating the keystore
- `KEY_ALIAS`: The alias you used (e.g., "my-key-alias")
- `KEY_PASSWORD`: The key password (usually same as keystore password)

## Workflow Triggers

- **Pull Requests**: Runs tests and debug build
- **Push to main**: Runs tests, release APK, and release AAB
- **Manual trigger**: Runs selected build type
  - `debug`: Debug APK only
  - `release`: Release APK and AAB (unsigned)
  - `signed-release`: Signed release APK and AAB
  - `all`: All build types (debug, release, signed)

## Build Outputs

The workflow generates the following artifacts with date-based naming:

### Debug APK
- **Path**: `app/build/outputs/apk/debug/app-debug.apk`
- **Retention**: 30 days
- **Signing**: Debug keystore
- **Naming**: `YYYY-MM-DD - RepositoryName - Debug APK`

### Release APK (Unsigned)
- **Path**: `app/build/outputs/apk/release/app-release.apk`
- **Retention**: 90 days
- **Signing**: Debug keystore (for testing)
- **Naming**: `YYYY-MM-DD - RepositoryName - Release APK`

### Release AAB (Unsigned)
- **Path**: `app/build/outputs/bundle/release/app-release.aab`
- **Retention**: 90 days
- **Signing**: Debug keystore (for testing)
- **Naming**: `YYYY-MM-DD - RepositoryName - Release AAB`
- **Usage**: Android App Bundle for Play Store

### Release APK (Signed)
- **Path**: `app/build/outputs/apk/release/app-release.apk`
- **Retention**: 90 days
- **Signing**: Release keystore
- **Naming**: `YYYY-MM-DD - RepositoryName - Signed Release APK`

### Release AAB (Signed)
- **Path**: `app/build/outputs/bundle/release/app-release.aab`
- **Retention**: 90 days
- **Signing**: Release keystore
- **Naming**: `YYYY-MM-DD - RepositoryName - Signed Release AAB`
- **Usage**: Production App Bundle for Play Store

## Manual Release Build

To create a release APK:

1. Go to your repository on GitHub
2. Navigate to Actions tab
3. Select "CI/CD" workflow
4. Click "Run workflow"
5. Select "main" branch
6. Choose build type:
   - `debug`: Debug APK (for testing)
   - `release`: Unsigned release APK (for testing)
   - `signed-release`: Signed release APK (for production)
7. Click "Run workflow"

The APK will be available as an artifact once the workflow completes.

## Local Development

For local development, you can build APKs using:

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (unsigned)
./gradlew assembleRelease

# Release APK (signed - requires keystore setup)
./gradlew assembleRelease
```

## Troubleshooting

### Build Failures
- Check that all secrets are properly set
- Verify `google-services.json` is correctly encoded
- Ensure keystore passwords match

### Signing Issues
- Verify keystore file is properly encoded
- Check that alias and passwords match the keystore
- Ensure keystore file is valid

### Firebase Issues
- Verify `google-services.json` contains correct project configuration
- Check that Firebase project is properly set up
- Ensure API keys are valid

## Security Features

- ✅ No hardcoded secrets in workflow files
- ✅ Secure secret management with GitHub Secrets
- ✅ Input validation for all secrets
- ✅ JSON validation for google-services.json
- ✅ Keystore file integrity checks
- ✅ APK output validation using `aapt`
- ✅ Minimal permissions (read-only for contents)
- ✅ Timeout protection for long-running tasks
- ✅ APK structure validation before upload

## Security Notes

- Never commit keystore files or passwords to the repository
- Use GitHub Secrets for all sensitive information
- Regularly rotate keystore passwords
- Keep keystore files secure and backed up
- Monitor workflow logs for any sensitive data exposure
