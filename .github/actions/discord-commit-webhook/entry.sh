#!/usr/bin/env bash
set -e
set -x

command -v jq >/dev/null 2>&1 || { echo "jq is required but not installed"; exit 1; }
command -v curl >/dev/null 2>&1 || { echo "curl is required but not installed"; exit 1; }

TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
COMMIT_URL="https://github.com/$REPO/commit/$GIT_HASH"
COMMIT_MESSAGE="$(git log -1 --pretty=format:%B || echo "No commit message")"
REPO_URL="https://github.com/$REPO"
BRANCH_URL="https://github.com/$REPO/tree/$BRANCH"
COMPARE_URL="https://github.com/$REPO/compare/$BRANCH"

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
  --arg compare_url "$COMPARE_URL" \
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
        { name: " ", value: "[[View Commit]](\($commit_url))", inline: false }
      ],
      footer: { text: "Commit detected by GitHub Actions" }
    }]
  }'
); then
  echo "jq failed to generate JSON payload" >&2
  exit 1
fi

echo "::group::JSON Payload"
echo "$json"
echo "::endgroup::"

curl_response=$(curl -s -w "%{http_code}" -o /tmp/curl_output -H "Content-Type: application/json" -X POST -d "$json" "$DISCORD_WEBHOOK_URL")
cat /tmp/curl_output
if [[ "$curl_response" != "204" ]]; then
  echo "Discord webhook failed with HTTP status $curl_response"
  exit 1
fi
