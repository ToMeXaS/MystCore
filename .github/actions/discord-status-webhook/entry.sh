#!/bin/bash
set -e

# Debug mode for CI troubleshooting. Remove or comment for production.
set -x

TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
COMMIT_URL="https://github.com/$REPO/commit/$GIT_HASH"
REPO_URL="https://github.com/$REPO"
BRANCH_URL="https://github.com/$REPO/tree/$BRANCH"

JAR_SIZE="unknown"
SIZE=""

for var in STATUS WEBHOOK GIT_HASH USERNAME REPO BRANCH RUN_URL JAR_NAME BUILD_DURATION; do
  echo "$var=${!var}"
done

# Calculate JAR size and human-readable value
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

# Build status-dependent fields
FIELDS_LIST=()
FIELDS_LIST+=("{\"name\": \"Repository\", \"value\": \"[$REPO]($REPO_URL)\", \"inline\": true}")
FIELDS_LIST+=("{\"name\": \"Branch\", \"value\": \"[$BRANCH]($BRANCH_URL)\", \"inline\": true}")
FIELDS_LIST+=("{\"name\": \"Commit\", \"value\": \"[$GIT_HASH]($COMMIT_URL)\", \"inline\": true}")

if [[ "$STATUS" == "build_failure" ]]; then
  COLOR=15158332
  TITLE="❌ Build Failed"
  FOOTER="Failed Job via GitHub Actions"
  BUILD_LOG=$(tail -n 20 build.log 2>/dev/null || echo "No build log found.")
  FIELDS_LIST+=("{\"name\": \"Error Log\", \"value\": \"\`\`\`$BUILD_LOG\`\`\`\", \"inline\": false}")
elif [[ "$STATUS" == "upload_failure" ]]; then
  COLOR=15158332
  TITLE="❌ Upload Failed"
  FOOTER="Failed Job via GitHub Actions"
  UPLOAD_LOG=$(tail -n 20 upload.log 2>/dev/null || echo "No upload log found.")
  FIELDS_LIST+=("{\"name\": \"Error Log\", \"value\": \"\`\`\`$UPLOAD_LOG\`\`\`\", \"inline\": false}")
elif [[ "$STATUS" == "upload_success" ]]; then
  COLOR=3447003
  TITLE="✅ Upload Successful"
  FOOTER="Upload Job via GitHub Actions"
  if [[ "$JAR_SIZE" != "unknown" && -n "$SIZE" ]]; then
    FIELDS_LIST+=("{\"name\": \"Jar & Size\", \"value\": \"\`$JAR_NAME\` ($SIZE)\", \"inline\": true}")
    FIELDS_LIST+=("{\"name\": \"From → To\", \"value\": \"\`artifacts/\` → \`/plugins\`\", \"inline\": true}")
  fi
else
  COLOR=3066993
  TITLE="✅ Build Successful"
  FOOTER="Build Job via GitHub Actions"
  if [[ "$JAR_SIZE" != "unknown" && -n "$SIZE" ]]; then
    FIELDS_LIST+=("{\"name\": \"Jar & Size\", \"value\": \"\`$JAR_NAME\` ($SIZE)\", \"inline\": true}")
    FIELDS_LIST+=("{\"name\": \"Build Duration\", \"value\": \"\`$BUILD_DURATION s\`\", \"inline\": true}")
  fi
fi

FIELDS_LIST+=("{\"name\": \" \", \"value\": \"[[View Run Action]]($RUN_URL)\", \"inline\": false }")
FIELDS_ARRAY="[$(IFS=,; echo "${FIELDS_LIST[*]}")]"

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

echo "---- PAYLOAD ----"
echo "$PAYLOAD"
echo "-----------------"

HTTP_RESPONSE=$(curl -s -w "%{http_code}" -H "Content-Type: application/json" -X POST -d "$PAYLOAD" "$DISCORD_WEBHOOK_URL")
CURL_EXIT_CODE=$?
echo "Curl exit code: $CURL_EXIT_CODE"
echo "Curl HTTP response: $HTTP_RESPONSE"
if [ $CURL_EXIT_CODE -ne 0 ] || [[ $HTTP_RESPONSE != 2* ]]; then
  echo "Discord webhook send failed!"
  exit 1
fi