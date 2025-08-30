#!/bin/bash

# GitHub Actions Secrets Setup Script for Pomodoro Timer
# This script helps you encode and prepare secrets for GitHub Actions

set -e

echo "🔧 GitHub Actions Secrets Setup for Pomodoro Timer"
echo "=================================================="

# Check if google-services.json exists
if [ ! -f "app/google-services.json" ]; then
    echo "❌ Error: app/google-services.json not found!"
    echo "Please ensure you have the google-services.json file in the app/ directory."
    exit 1
fi

echo ""
echo "📋 Required GitHub Secrets:"
echo "=========================="

# Encode google-services.json
echo "1. GOOGLE_SERVICES_JSON:"
GOOGLE_SERVICES_BASE64=$(base64 -i app/google-services.json)
echo "$GOOGLE_SERVICES_BASE64"
echo ""

echo ""
echo "🔐 Keystore Setup (Optional)"
echo "============================"

read -p "Do you want to create a new keystore for signing? (y/N): " -n 1 -r
echo

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Creating new keystore..."
    
    read -p "Enter keystore password: " -s KEYSTORE_PASSWORD
    echo
    read -p "Enter key alias: " KEY_ALIAS
    read -p "Enter key password (press Enter if same as keystore): " -s KEY_PASSWORD
    echo
    
    if [ -z "$KEY_PASSWORD" ]; then
        KEY_PASSWORD="$KEYSTORE_PASSWORD"
    fi
    
    echo "Generating keystore..."
    keytool -genkey -v \
        -keystore my-release-key.jks \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -alias "$KEY_ALIAS" \
        -storepass "$KEYSTORE_PASSWORD" \
        -keypass "$KEY_PASSWORD" \
        -dname "CN=Pomodoro Timer, OU=Development, O=Your Company, L=City, S=State, C=US"
    
    echo "✅ Keystore created: my-release-key.jks"
    echo ""
    echo "🔐 Encoding keystore for GitHub Secrets..."
    
    # Store the encoded keystore
    KEYSTORE_ENCODED=$(base64 -i my-release-key.jks)
    
    # Copy keystore to clipboard if available
    if command -v pbcopy &> /dev/null; then
        echo "$KEYSTORE_ENCODED" | pbcopy
        echo "✅ Keystore encoded and copied to clipboard"
    elif command -v clip &> /dev/null; then
        echo "$KEYSTORE_ENCODED" | clip
        echo "✅ Keystore encoded and copied to clipboard"
    elif command -v xclip &> /dev/null; then
        echo "$KEYSTORE_ENCODED" | xclip -selection clipboard
        echo "✅ Keystore encoded and copied to clipboard"
    else
        echo "⚠️  No clipboard tool found. Here's the encoded keystore:"
        echo "----------------------------------------"
        echo "$KEYSTORE_ENCODED"
        echo "----------------------------------------"
    fi
    
    echo ""
    echo "📋 GitHub Secrets to set:"
    echo "KEYSTORE_BASE64: [paste the encoded keystore above]"
    echo "KEYSTORE_PASSWORD: $KEYSTORE_PASSWORD"
    echo "KEY_ALIAS: $KEY_ALIAS"
    echo "KEY_PASSWORD: $KEY_PASSWORD"
    
    echo ""
    echo "💾 Keep these credentials safe! You'll need them for future builds."
    echo "   Store them securely - they cannot be recovered if lost."
    
else
    echo "Skipping keystore creation."
    echo "You can create one manually using:"
    echo "keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias"
fi

# Check if keystore exists (either created above or already present)
if [ -f "my-release-key.jks" ]; then
    echo ""
    echo "🔐 Existing Keystore Found"
    echo "=========================="
    echo "2. KEYSTORE_BASE64:"
    KEYSTORE_BASE64=$(base64 -i my-release-key.jks)
    echo "$KEYSTORE_BASE64"
    echo ""
    echo "3. Additional keystore secrets (set these manually):"
    echo "   - KEYSTORE_PASSWORD: Your keystore password"
    echo "   - KEY_ALIAS: Your key alias (e.g., 'my-key-alias')"
    echo "   - KEY_PASSWORD: Your key password"
fi

echo ""
echo "📝 Instructions:"
echo "==============="
echo "1. Go to your GitHub repository"
echo "2. Navigate to Settings > Secrets and variables > Actions"
echo "3. Add the secrets above with their corresponding values"
echo "4. For GOOGLE_SERVICES_JSON, paste the base64 encoded string"
echo "5. For keystore secrets, enter the actual passwords/aliases"
echo ""
echo "🎉 Setup complete! Your GitHub Actions workflow should now work properly."
echo ""
echo "📋 Summary of Required Secrets:"
echo "================================"
echo "✅ GOOGLE_SERVICES_JSON: [see above]"

if [ -f "my-release-key.jks" ]; then
    echo "✅ KEYSTORE_BASE64: [see above]"
    if [ -n "$KEYSTORE_PASSWORD" ]; then
        echo "✅ KEYSTORE_PASSWORD: $KEYSTORE_PASSWORD"
        echo "✅ KEY_ALIAS: $KEY_ALIAS"
        echo "✅ KEY_PASSWORD: $KEY_PASSWORD"
    else
        echo "⚠️  Signing secrets: Set manually (KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD)"
    fi
else
    echo "⚠️  Signing secrets: Not configured (optional for debug builds)"
fi
