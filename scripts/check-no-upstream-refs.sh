#!/usr/bin/env bash
# Bridge reuses ideas from private codebases. It must not carry their names.
# Fails the build if an upstream identifier leaks into the sources.
set -uo pipefail

cd "$(dirname "$0")/.."

PATTERN='eva[-_. ]?ai|evaai|com\.evaai|ifriend|com\.ifriend|io\.tessera'

MATCHES=$(grep -rInE "$PATTERN" \
  --include='*.kt' --include='*.kts' --include='*.xml' --include='*.md' \
  --include='*.toml' --include='*.pro' --include='*.swift' --include='*.yml' \
  --exclude-dir=build --exclude-dir=.git --exclude-dir=.gradle --exclude-dir=.kotlin \
  --exclude-dir=.idea --exclude-dir=scripts \
  . || true)

if [ -n "$MATCHES" ]; then
  echo "Upstream references found. Rename them before committing:"
  echo "$MATCHES"
  exit 1
fi

echo "No upstream references found."
