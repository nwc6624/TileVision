#!/bin/bash

# This script rewrites the entire git history to remove personal contributor information
# WARNING: This will rewrite all commits. Make sure to backup first.

echo "Rewriting git history to remove personal contributor information..."

# Rewrite all commits with anonymous author
git filter-branch --force --env-filter '
if [ "$GIT_COMMITTER_EMAIL" != "" ]; then
    export GIT_COMMITTER_NAME="TileVision"
    export GIT_COMMITTER_EMAIL="tilevision.app@gmail.com"
fi
if [ "$GIT_AUTHOR_EMAIL" != "" ]; then
    export GIT_AUTHOR_NAME="TileVision"
    export GIT_AUTHOR_EMAIL="tilevision.app@gmail.com"
fi
' --tag-name-filter cat -- --branches --tags

echo "History rewritten. Run 'git push --force' to push to remote."
