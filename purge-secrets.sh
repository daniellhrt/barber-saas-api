#!/bin/sh
# Purge known secrets from files in working tree. Intended for use with git filter-branch --tree-filter

# Replace sensitive DB URL, username and password in application.yml if present
if [ -f src/main/resources/application.yml ]; then
  sed -i 's|jdbc:postgresql://ep-flat-voice-acnlajh4-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require&channelBinding=require|REMOVED_DB_URL|g' src/main/resources/application.yml || true
  sed -i 's|neondb_owner|REMOVED_DB_USER|g' src/main/resources/application.yml || true
  sed -i 's|npg_kMoGY1Rse4Iz|REMOVED_DB_PASSWORD|g' src/main/resources/application.yml || true
  sed -i 's|404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970|REMOVED_JWT_SECRET|g' src/main/resources/application.yml || true
fi

# Replace known password in docker-compose.yml if present
if [ -f docker-compose.yml ]; then
  sed -i 's|POSTGRES_PASSWORD: admin|POSTGRES_PASSWORD: REMOVED_COMPOSE_DB_PASSWORD|g' docker-compose.yml || true
fi

# Additionally, attempt to replace the long JWT secret wherever it appears in text files
# Use git ls-files to limit replacements to tracked files
for f in $(git ls-files); do
  # check if file contains the JWT secret
  if git grep -I --line-number -n "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970" -- "$f" >/dev/null 2>&1; then
    sed -i 's|404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970|REMOVED_JWT_SECRET|g' "$f" || true
  fi
done

exit 0

