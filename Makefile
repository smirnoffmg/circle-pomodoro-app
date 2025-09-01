# Makefile for Pomodoro Timer Project
# Provides convenient commands for development, testing, and code quality

.PHONY: help build test clean lint format check ktlint-check

help:
	@echo "🍅 Pomodoro Timer - Commands"
	@echo "  build         Build project"
	@echo "  test          Run tests"
	@echo "  clean         Clean build"
	@echo "  lint          Run Android lint"
	@echo "  format        Format code"
	@echo "  ktlint-check  Check code style"
	@echo "  check         Run all checks"

build:
	./gradlew build

clean:
	./gradlew clean

test:
	./gradlew test

lint:
	./gradlew lint

ktlint-check:
	ktlint "app/src/**/*.kt" "app/src/**/*.kts"

format:
	ktlint "app/src/**/*.kt" "app/src/**/*.kts" --format || true

check: ktlint-check test lint
