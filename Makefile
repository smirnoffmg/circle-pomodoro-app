# Makefile for Pomodoro Timer Project
# Provides convenient commands for development, testing, and code quality

.PHONY: help install-ktlint ktlint-check ktlint-format build test clean lint install-deps

# Default target
help:
	@echo "🍅 Pomodoro Timer - Development Commands"
	@echo ""
	@echo "📦 Setup Commands:"
	@echo "  install-deps     Install all project dependencies"
	@echo "  install-ktlint   Install ktlint CLI tool"
	@echo ""
	@echo "🔧 Build Commands:"
	@echo "  build           Build the project"
	@echo "  clean           Clean build artifacts"
	@echo "  test            Run all tests"
	@echo ""
	@echo "✨ Code Quality:"
	@echo "  ktlint-check    Check Kotlin code style"
	@echo "  ktlint-format   Format Kotlin code automatically"
	@echo "  lint            Run Android lint"
	@echo ""
	@echo "🚀 Quick Commands:"
	@echo "  check           Run all checks (ktlint + tests + lint)"
	@echo "  format          Format code and run checks"

# Setup Commands
install-deps:
	@echo "📦 Installing project dependencies..."
	./gradlew build --refresh-dependencies

install-ktlint:
	@echo "🔧 Installing ktlint..."
	@mkdir -p ~/.local/bin
	@curl -sSL "https://github.com/pinterest/ktlint/releases/download/1.7.1/ktlint" -o ~/.local/bin/ktlint
	@chmod +x ~/.local/bin/ktlint
	@echo "✅ ktlint installed to ~/.local/bin/ktlint"
	@echo "💡 Make sure ~/.local/bin is in your PATH"

# Build Commands
build:
	@echo "🏗️ Building project..."
	./gradlew build

clean:
	@echo "🧹 Cleaning build artifacts..."
	./gradlew clean

test:
	@echo "🧪 Running tests..."
	./gradlew test

# Code Quality Commands
ktlint-check:
	@echo "🔍 Checking Kotlin code style..."
	@if command -v ktlint >/dev/null 2>&1; then \
		ktlint "app/src/**/*.kt" "app/src/**/*.kts"; \
	else \
		echo "❌ ktlint not found. Run 'make install-ktlint' first."; \
		exit 1; \
	fi

ktlint-format:
	@echo "✨ Formatting Kotlin code..."
	@if command -v ktlint >/dev/null 2>&1; then \
		ktlint "app/src/**/*.kt" "app/src/**/*.kts" --format; \
		echo "✅ Code formatted successfully!"; \
	else \
		echo "❌ ktlint not found. Run 'make install-ktlint' first."; \
		exit 1; \
	fi

lint:
	@echo "🔎 Running Android lint..."
	./gradlew lint

# Quick Commands
check: ktlint-check test lint
	@echo "✅ All checks passed!"

format: ktlint-format ktlint-check
	@echo "✅ Code formatted and verified!"

# Development workflow
dev-setup: install-ktlint install-deps
	@echo "🎯 Development environment setup complete!"
	@echo ""
	@echo "Next steps:"
	@echo "  make format     # Format and check code"
	@echo "  make build      # Build the project"
	@echo "  make test       # Run tests"

# Pre-commit checks
pre-commit: ktlint-check test
	@echo "✅ Pre-commit checks passed!"

# CI/CD commands
ci: ktlint-check test lint build
	@echo "✅ CI pipeline completed successfully!"
