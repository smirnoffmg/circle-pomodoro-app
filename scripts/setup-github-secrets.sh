#!/bin/bash

# GitHub Actions Secrets Setup Script
# This script helps encode files for GitHub Secrets

set -e

echo "🔧 GitHub Actions Secrets Setup"
echo "================================"

# Check if google-services.json exists
if [ -f "app/google-services.json" ]; then
    echo "📱 Found google-services.json"
    echo "Encoding for GOOGLE_SERVICES_JSON secret..."
    
    # Store the encoded value
    GOOGLE_SERVICES_ENCODED=$(base64 -i app/google-services.json)
    
    echo "✅ google-services.json encoded"
    echo "   Copy this as the GOOGLE_SERVICES_JSON secret in GitHub:"
    echo "----------------------------------------"
    echo "$GOOGLE_SERVICES_ENCODED"
    echo "----------------------------------------"
else
    echo "❌ google-services.json not found in app/ directory"
    echo "   Please ensure the file exists before running this script"
fi

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

echo ""
echo "🎉 Setup complete!"
echo "Next steps:"
echo "1. Go to your GitHub repository"
echo "2. Navigate to Settings > Secrets and variables > Actions"
echo "3. Add the secrets mentioned above"
echo "4. Push to main or create a PR to trigger the workflow"

echo ""
echo "📋 Summary of Required Secrets:"
echo "================================"
if [ -f "app/google-services.json" ]; then
    echo "✅ GOOGLE_SERVICES_JSON: [see above]"
else
    echo "❌ GOOGLE_SERVICES_JSON: Missing google-services.json file"
fi

if [[ $REPLY =~ ^[Yy]$ ]] && [ -f "my-release-key.jks" ]; then
    echo "✅ KEYSTORE_BASE64: [see above]"
    echo "✅ KEYSTORE_PASSWORD: $KEYSTORE_PASSWORD"
    echo "✅ KEY_ALIAS: $KEY_ALIAS"
    echo "✅ KEY_PASSWORD: $KEY_PASSWORD"
else
    echo "⚠️  Signing secrets: Not configured (optional for debug builds)"
fi
