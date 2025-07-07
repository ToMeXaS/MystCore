#!/bin/bash
set -e

TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
COMMIT_URL="https://github.com/$REPO/commit/$GIT_HASH"
REPO_URL="https://github.com/$REPO"
BRANCH_URL="https://github.com/$REPO/tree/$BRANCH"
JAR_SIZE="unknown"
SIZE=""
EXTRA_FIELDS=""

# Print all vars for debug
echo "REPO=$REPO"
echo "GIT_HASH=$GIT_HASH"
echo "BRANCH=$BRANCH"
echo "JAR_NAME=$JAR_NAME"
echo "STATUS=$STATUS"
echo "AUTHOR=$AUTHOR"
echo "RUN_URL=$RUN_URL"
echo "BUILD_DURATION=$BUILD_DURATION"
echo "DISCORD_WEBHOOK_URL=$DISCORD_WEBHOOK_URL"

# Calculate JAR size and SIZE string if possible
if [[ -n "$JAR_NAME" && -f "artifacts/$JAR_NAME" ]]; then
  JAR_SIZE=$(stat -c%s "artifacts/$JAR_NAME")
  if (( JAR_SIZE < 1024 )); then
    SIZE="\`$JAR_SIZE bytes\`"
  elif (( JAR_SIZE < 1024 * 1024 )); then
    SIZE="\`$((JAR_SIZE / 1024)) KB\`"
  else
    SIZE="\`$(echo "scale=2; $JAR_SIZE / 1024 / 1024" | bc) MB\`"
  fi
fi

if [[ "$STATUS" == "build_failure" ]]; then
  BUILD_LOG=$(tail -n 20 build.log 2>/dev/null || echo "No build log found.")
  COLOR=15158332
  TITLE="❌ Build Failed"
  FOOTER="Failed Job via GitHub Actions"
  EXTRA_FIELDS='{"name": "Error Log", "value": "```
'"$BUILD_LOG"'```", "inline": false}'
elif [[ "$STATUS" == "upload_failure" ]]; then
  UPLOAD_LOG=$(tail -n 20 upload.log 2>/dev/null || echo "No upload log found.")
  COLOR=15158332
  TITLE="❌ Upload Failed"
  FOOTER="Failed Job via GitHub Actions"
  EXTRA_FIELDS='{"name": "Error Log", "value": "```
'"$UPLOAD_LOG"'```", "inline": false}'
elif [[ "$STATUS" == "upload_success" ]]; then
  COLOR=3447003
  TITLE="✅ Upload Successful"
  FOOTER="Upload Job via GitHub Actions"

  if [[ "$JAR_SIZE" != "unknown" && -n "$SIZE" ]]; then
    EXTRA_FIELDS='{"name": "Jar & Size", "value": "`'"$JAR_NAME"'` ('"$SIZE"')", "inline": true}, {"name": "From → To", "value": "`artifacts/` → `/plugins`", "inline": true}'
  fi
else
  COLOR=3066993
  TITLE="✅ Build Successful"
  FOOTER="Build Job via GitHub Actions"

  if [[ "$JAR_SIZE" != "unknown" && -n "$SIZE" ]]; then
    EXTRA_FIELDS='{"name": "Jar & Size", "value": "`'"$JAR_NAME"'` ('"$SIZE"')", "inline": true}, {"name": "Build Duration", "value": "`'"$BUILD_DURATION"' s`", "inline": true}'
  fi
fi

FIELDS_LIST=()

FIELDS_LIST+=("{\"name\": \"Repository\", \"value\": \"[$REPO]($REPO_URL)\", \"inline\": true}")
FIELDS_LIST+=("{\"name\": \"Branch\", \"value\": \"[$BRANCH]($BRANCH_URL)\", \"inline\": true}")
FIELDS_LIST+=("{\"name\": \"Commit\", \"value\": \"[$GIT_HASH]($COMMIT_URL)\", \"inline\": true}")

if [[ -n "$EXTRA_FIELDS" ]]; then
  # Split EXTRA_FIELDS in case it contains multiple fields
  IFS='},' read -ra ADDR <<< "$EXTRA_FIELDS"
  for i in "${ADDR[@]}"; do
    # Add back the closing } if it's missing
    [[ $i != *"}" ]] && i="$i}"
    # Remove leading comma and whitespace
    i="${i#, }"
    FIELDS_LIST+=("$i")
  done
fi

FIELDS_LIST+=("{\"name\": \" \", \"value\": \"[[View Run Action]]($RUN_URL)\", \"inline\": false }")

FIELDS_ARRAY="["
FIELDS_ARRAY+=$(IFS=, ; echo "${FIELDS_LIST[*]}")
FIELDS_ARRAY+="]"

read -r -d '' PAYLOAD <<EOF
{
  "embeds": [{
    "title": "$TITLE",
    "color": $COLOR,
    "timestamp": "$TIMESTAMP",
    "author": {
      "name": "$AUTHOR",
      "url": "https://github.com/$AUTHOR",
      "icon_url": "https://github.com/$AUTHOR.png"
    },
    "fields": $FIELDS_ARRAY,
    "footer": { "text": "$FOOTER" }
  }]
}
EOF

curl -s -w "%{http_code}" -H "Content-Type: application/json" -X POST -d "$PAYLOAD" "$DISCORD_WEBHOOK_URL"