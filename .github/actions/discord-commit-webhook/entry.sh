#!/usr/bin/env bash
set -e
set -x

: "${REPO:?REPO not set}"
: "${GIT_HASH:?GIT_HASH not set}"
: "${AUTHOR:?AUTHOR not set}"
: "${BRANCH:?BRANCH not set}"
: "${DISCORD_WEBHOOK_URL:?DISCORD_WEBHOOK_URL not set}"

command -v jq >/dev/null 2>&1 || { echo "jq not installed"; exit 1; }
command -v curl >/dev/null 2>&1 || { echo "curl not installed"; exit 1; }

TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
COMMIT_URL="https://github.com/$REPO/commit/$GIT_HASH"
COMMIT_MESSAGE="$(git log -1 --pretty=format:%B || echo "No commit message")"
REPO_URL="https://github.com/$REPO"
BRANCH_URL="https://github.com/$REPO/tree/$BRANCH"

echo "REPO: $REPO"
echo "GIT_HASH: $GIT_HASH"
echo "AUTHOR: $AUTHOR"
echo "BRANCH: $BRANCH"
echo "DISCORD_WEBHOOK_URL: $DISCORD_WEBHOOK_URL"
echo "COMMIT_MESSAGE: $COMMIT_MESSAGE"

CHANGED_FILES_LIST=""
file_count=0
current_length=0
max_files=5
MAX_LENGTH=900

while IFS= read -r f; do
  [ -z "$f" ] && continue
  fname=$(basename "$f")
  url="https://github.com/$REPO/blob/$GIT_HASH/$f"
  line="- [$fname]($url)\n"
  new_length=$((current_length + ${#line}))
  if (( new_length > MAX_LENGTH )) || ((file_count >= max_files)); then
    CHANGED_FILES_LIST="${CHANGED_FILES_LIST}...and more files not shown."
    break
  fi
  CHANGED_FILES_LIST="${CHANGED_FILES_LIST}${line}"
  current_length=$new_length
  ((file_count++))
done < <(git diff --name-only "$GIT_HASH"^ "$GIT_HASH")

if [[ -z "$CHANGED_FILES_LIST" ]]; then
  CHANGED_FILES_LIST="No files changed."
fi

if ! json=$(jq -n \
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
        { name: "Message", value: $commit_message, inline: false },
        { name: " ", value: "[[View Commit]](\($commit_url))", inline: false }
      ],
      footer: { text: "Commit detected by GitHub Actions" }
    }]
  }'
); then
  echo "jq failed to generate JSON payload" >&2
  exit 1
fi

echo "::group::Generated JSON"
echo "$json"
echo "::endgroup::"

response=$(curl -s -w "%{http_code}" -o /tmp/curl_output -H "Content-Type: application/json" -X POST -d "$json" "$DISCORD_WEBHOOK_URL")
cat /tmp/curl_output
echo "Discord webhook HTTP status: $response"

if [[ "$response" != "204" ]]; then
  echo "Discord webhook failed with HTTP status $response"
  exit 1
fi