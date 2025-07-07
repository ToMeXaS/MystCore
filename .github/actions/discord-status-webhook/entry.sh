#!/bin/bash
set -e
set -x

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

FIELDS='
{"name": "Repository", "value": "['"$REPO"']('"$REPO_URL"')", "inline": true},
{"name": "Branch", "value": "['"$BRANCH"']('"$BRANCH_URL"')", "inline": true},
{"name": "Commit", "value": "['"$GIT_HASH"']('"$COMMIT_URL"')", "inline": true}
'

# Build the fields array safely
FIELDS_ARRAY="$FIELDS"
if [[ -n "$EXTRA_FIELDS" ]]; then
  FIELDS_ARRAY="$FIELDS_ARRAY, $EXTRA_FIELDS"
fi
FIELDS_ARRAY="$FIELDS_ARRAY, {\"name\": \" \", \"value\": \"[[View Run Action]]($RUN_URL)\", \"inline\": false }"
FIELDS_ARRAY="[$FIELDS_ARRAY]"

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

echo "Payload to Discord:"
echo "$PAYLOAD"

HTTP_RESPONSE=$(curl -s -w "%{http_code}" -H "Content-Type: application/json" -X POST -d "$PAYLOAD" "$DISCORD_WEBHOOK_URL")
CURL_EXIT_CODE=$?
echo "Curl exit code: $CURL_EXIT_CODE"
echo "Curl HTTP response: $HTTP_RESPONSE"
if [ $CURL_EXIT_CODE -ne 0 ] || [[ $HTTP_RESPONSE != 2* ]]; then
  exit 1
fi