#!/usr/bin/env bash
set -e

TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
COMMIT_URL="https://github.com/$REPO/commit/$GIT_HASH"
COMMIT_MESSAGE="$(git log -1 --pretty=format:%B || echo "No commit message")"
REPO_URL="https://github.com/$REPO"
BRANCH_URL="https://github.com/$REPO/tree/$BRANCH"
CHANGED_FILES=($(git diff --name-only "$GIT_HASH^" "$GIT_HASH"))

if [ ${#CHANGED_FILES[@]} -eq 0 ]; then
  CHANGED_FILES_LIST="No files changed."
else
  CHANGED_FILES_LIST=$(for f in "${CHANGED_FILES[@]}"; do
    fname=$(basename "$f")
    url="https://github.com/$REPO/blob/$GIT_HASH/$f"
    printf -- "- [%s](%s)\n" "$fname" "$url"
  done)
fi

MAX_LENGTH=900
if [ ${#CHANGED_FILES_LIST} -gt $MAX_LENGTH ]; then
  CHANGED_FILES_LIST="${CHANGED_FILES_LIST:0:$MAX_LENGTH}\n...and more files not shown."
fi

json=$(jq -n \
--arg title "📦 New Commit Pushed" \
--arg repo "$REPO" \
--arg repo_url "$REPO_URL" \
--arg branch "$BRANCH" \
--arg branch_url "$BRANCH_URL" \
--arg commit "$GIT_HASH" \
--arg commit_url "$COMMIT_URL" \
--arg commit_message "$COMMIT_MESSAGE" \
--arg author "$AUTHOR" \
--arg timestamp "$TIMESTAMP" \
--arg changed "$CHANGED_FILES_LIST" \
'{
  embeds: [{
    title: $title,
    color: 3447003,
    timestamp: $timestamp,
    author: {
      name: $author,
      url: "https://github.com/\($author)",
      icon_url: "https://github.com/\($author).png"
    },
    fields: [
      { name: "Repository", value: "[\($repo)](\($repo_url))", inline: true },
      { name: "Branch", value: "[\($branch)](\($branch_url))", inline: true },
      { name: "Commit", value: "[\($commit)](\($commit_url))", inline: true },
      { name: "Changed Files", value: $changed, inline: false },
      { name: "Message", value: "```\($commit_message)```", inline: false },
      { name: " ", value: "[[View Commit]](" + $commit_url + ")", inline: false }
    ],
    footer: { text: "Commit detected by GitHub Actions" }
  }]
}')

curl -H "Content-Type: application/json" -X POST -d "$json" "$DISCORD_WEBHOOK_URL"
