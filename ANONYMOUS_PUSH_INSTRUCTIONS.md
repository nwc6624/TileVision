# Instructions for Pushing to GitHub Anonymously

Follow these steps to ensure no personal contributor information appears on GitHub:

## Step 1: Backup Your Repository
```bash
cd /Users/noahcaulfield/Desktop/AR\ Measure\ Android
cp -r . ../AR\ Measure\ Android\ Backup
```

## Step 2: Update Git Configuration (Temporary)
```bash
git config user.name "TileVision"
git config user.email "tilevision.app@gmail.com"
```

## Step 3: Rewrite Git History
```bash
chmod +x rewrite_history.sh
./rewrite_history.sh
```

Or manually run:
```bash
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
```

## Step 4: Verify History Was Rewritten
```bash
git log --pretty=format:"%an %ae" | sort | uniq
```
Should only show "TileVision tilevision.app@gmail.com"

## Step 5: Update Remote URL (if needed)
```bash
git remote set-url origin https://github.com/YOUR_REPO_URL.git
```

## Step 6: Force Push to GitHub
```bash
git push --force --all
git push --force --tags
```

## Step 7: Verify on GitHub
- Check that the Contributors tab only shows "TileVision"
- Verify commit history shows only generic author
- Confirm no personal email addresses are visible

## Step 8: GitHub Repository Settings
After pushing, go to your GitHub repository:
1. Settings → General
2. Scroll to "Contributors" section
3. If available, disable "Show contributors" or hide the contributors graph

## Important Notes

⚠️ **WARNING**: The `git push --force` command will overwrite all history on GitHub. Make sure:
- You have a backup (Step 1)
- You're ready to permanently change the history
- You understand this cannot be easily undone

⚠️ **NOTE**: Once you force push, anyone who has cloned the old repository will need to re-clone.

## Alternative: New Repository Approach

If you prefer not to rewrite history, you can:
1. Create a completely new repository
2. Copy all files (excluding .git folder)
3. Make an initial commit with the TileVision identity
4. Push to the new repository

This keeps the history cleaner but loses the commit history.
