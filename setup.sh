#!/bin/sh
# Setup script to install git hooks from scripts/git-hooks to .git/hooks
cp scripts/git-hooks/* .git/hooks/
chmod +x .git/hooks/*
echo "Git hooks installed."
