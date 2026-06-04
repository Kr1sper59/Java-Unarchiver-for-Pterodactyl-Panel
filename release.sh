#!/usr/bin/env bash
set -euo pipefail

#  Проверка аргумента 
if [[ $# -lt 1 ]]; then
    echo "Usage: ./release.sh <version>"
    echo "Example: ./release.sh 1.2"
    exit 1
fi

VERSION="$1"
TAG="v${VERSION}"
JAR="target/unarchiver-${VERSION}.jar"

#  Проверка зависимостей 
for cmd in mvn gh git; do
    if ! command -v "$cmd" &>/dev/null; then
        echo "Error: '$cmd' is not installed or not in PATH"
        exit 1
    fi
done

#  Проверка авторизации GitHub CLI 
if ! gh auth status &>/dev/null; then
    echo "Error: not logged in to GitHub CLI. Run: gh auth login"
    exit 1
fi

#  Сборка 
echo "Building version ${VERSION}..."
mvn versions:set -DnewVersion="${VERSION}" -DgenerateBackupPoms=false -q
mvn clean package -q

if [[ ! -f "$JAR" ]]; then
    echo "Error: JAR not found at ${JAR}"
    exit 1
fi

echo "Build successful: ${JAR}"

#  Git коммит и тег 
echo "Creating git tag ${TAG}..."
git add pom.xml
git commit -m "Release ${TAG}" || echo "Nothing to commit, continuing..."
git tag -a "${TAG}" -m "Release ${TAG}"
git push origin HEAD
git push origin "${TAG}"

#  Создание релиза на GitHub 
echo "Creating GitHub release ${TAG}..."
gh release create "${TAG}" "${JAR}" \
    --title "Release ${TAG}" \
    --notes "Release ${TAG}"

echo ""
echo "Done! Released ${TAG} → $(gh repo view --json url -q .url)/releases/tag/${TAG}"