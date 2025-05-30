#!/bin/bash

export PRODUCTION_DIR=$HOME/IdeaProjects/meiSearchBackend-prod

# Clear it all out
rm -rf $PRODUCTION_DIR/*

# Copy everything over initially
cp -r ~/IdeaProjects/meiSearchBackend/* $PRODUCTION_DIR

# Delete all unnecessary files
rm -rf $PRODUCTION_DIR/.git*
rm -rf $PRODUCTION_DIR/.idea
rm -rf $PRODUCTION_DIR/tests
rm -rf $PRODUCTION_DIR/meiSearchBackend.iml
rm -rf $PRODUCTION_DIR/meiSearchBackend-prod.iml
rm -rf $PRODUCTION_DIR/src/Test.java
rm -rf $PRODUCTION_DIR/src/Scratch.java
rm -rf $PRODUCTION_DIR/metadataInfo/
rm -rf $PRODUCTION_DIR/src/txtFiles/TODO.txt
rm -rf $PRODUCTION_DIR/src/txtFiles/brainstorm.txt
rm -rf $PRODUCTION_DIR/src/txtFiles/pushToProd.txt
rm -rf $PRODUCTION_DIR/src/scripts/pushToProd.sh

# Sanitize files
sanitize_file() {
  local input="$1"
  shift
  local tmpfile
  tmpfile=$(mktemp)
  sed "$@" "$input" > "$tmpfile" && mv "$tmpfile" "$input"
}

# Define paths
INPUT1="$PRODUCTION_DIR/src/scripts/clearAndCopyToEc2.sh"
INPUT2="$PRODUCTION_DIR/src/scripts/indexDatabase.sh"
INPUT3="$PRODUCTION_DIR/docker-compose.yml"
INPUT4="$PRODUCTION_DIR/Dockerfile"

# Sanitize clearAndCopyToEc2.sh
sanitize_file "$INPUT1" \
  -e 's|export INSTANCE_URL=.*|export INSTANCE_URL="${INSTANCE_URL}"|' \
  -e 's|export HOME_URL=.*|export HOME_URL="${HOME_URL}"|' \
  -e 's|export PEM_PATH=.*|export PEM_PATH="${PEM_PATH}"|'

# Sanitize indexDatabase.sh
sanitize_file "$INPUT2" \
  -e 's|jdbc:postgresql://[^" ]*|${JDBC_URL}|g' \
  -e 's|"postgres"|"${DB_USER}"|g' \
  -e 's|"ScouredElmContempt8"|"${DB_PASS}"|g' \
  -e 's|"localhost"|"${HOST}"|g' \
  -e 's|"5000"|"${PORT}"|g'

# Sanitize docker-compose.yml
sanitize_file "$INPUT3" \
  -e 's|ELASTIC_USERNAME=.*|ELASTIC_USERNAME=${E_USER}|g' \
  -e 's|ELASTIC_PASSWORD=.*|ELASTIC_PASSWORD=${E_PASS}|g' \
  -e 's|ELASTICSEARCH_HOST=.*|ELASTICSEARCH_HOST=${E_HOST}|g' \
  -e 's|ELASTICSEARCH_PORT=.*|ELASTICSEARCH_PORT=${E_PORT}|g' \
  -e 's|"5000:5000"|"${PORT}:${PORT}"|g' \
  -e 's|"9200:9200"|"${PORT2}:${PORT2}"|g' \
  -e 's|"9300:9300"|"${PORT3}:${PORT3}"|g'

# Sanitize Dockerfile
sanitize_file "$INPUT4" \
  -e 's|5000|${PORT}|g'

echo "✅ All files sanitized safely."

cd $PRODUCTION_DIR || exit 1

# Configurable values
REPO_NAME=${1:-"meiSearchBackend-prod"}
VISIBILITY=${2:-"public"} # or "private"

# Check if GitHub CLI is authenticated
if ! gh auth status &>/dev/null; then
  echo "❌ GitHub CLI not authenticated. Run 'gh auth login' first."
  exit 1
fi

# If it's not a git repo yet, initialize it
if [ ! -d .git ]; then
  echo "📁 Initializing git..."
  git init
  git add .
  git commit -m "Release"
  git branch -M main
else
  echo "⚠️ Git repo already exists. Delete it for a fresh start or skip initialization."
fi

# Create GitHub repo
echo "🚀 Creating GitHub repo '$REPO_NAME' ($VISIBILITY)..."
gh repo create "$REPO_NAME" --"$VISIBILITY" --source=. --remote=origin --push

# Push to GitHub (just in case push didn't happen)
echo "📤 Pushing code to GitHub..."
git push -u origin main

echo "✅ Done! Repo: https://github.com/$(gh api user --jq .login)/$REPO_NAME"
