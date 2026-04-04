# Address PR Review Comments

Read all unresolved inline review comments on the current branch's open PR and address them locally. Do NOT commit or push anything without explicit user approval.

## Setup

Before starting, resolve the repository owner and name from the git remote:

```bash
gh repo view --json owner,name
```

Get the PR number for the current branch:

```bash
gh pr view --json number,title,state
```

If no open PR exists for the current branch, inform the user and stop.

## Step 1 — Fetch Unresolved Review Threads

Use the GitHub GraphQL API to fetch all review threads. Replace OWNER, REPO, and PR_NUMBER with the values resolved above:

```bash
gh api graphql -f query='
query {
  repository(owner: "OWNER", name: "REPO") {
    pullRequest(number: PR_NUMBER) {
      reviewThreads(first: 100) {
        nodes {
          id
          isResolved
          path
          line
          startLine
          diffSide
          comments(first: 10) {
            nodes {
              id
              databaseId
              body
              author {
                login
                __typename
              }
              createdAt
            }
          }
        }
      }
    }
  }
}'
```

Filter results to only threads where `isResolved` is `false`.

Also filter out any threads where the comment author `__typename` is `Bot` and the login matches known noise bots (e.g. `github-actions`, `dependabot`, `renovate`). **Include** comments from `copilot-pull-request-reviewer` — treat Copilot review comments the same as human reviewer comments.

If there are no unresolved threads (from human reviewers or Copilot), inform the user and stop.

## Step 2 — Analyze Before Acting

Before making any edits, output a structured plan to the terminal listing every thread you intend to address:

```
Thread 1
  File: app/src/main/java/com/example/MyViewModel.kt
  Line: 42
  Reviewer: alice
  Comment: "This should use viewModelScope instead of GlobalScope"
  Planned action: Replace GlobalScope.launch with viewModelScope.launch

Thread 2
  File: app/src/main/java/com/example/Repository.kt
  Line: 87
  Reviewer: bob
  Comment: "Missing null check before accessing this field"
  Planned action: Add null safety check using Kotlin's ?. operator
```

Group threads that touch the same file so edits are batched — do not make multiple passes on the same file.

## Step 3 — Clarify Ambiguous Comments

Before editing anything, identify comments that are:
- Subjective or opinion-based ("I'd prefer X" without a clear directive)
- Contradictory to another comment on the same block
- Architectural in scope (suggesting a structural refactor vs. a local fix)
- Outside your confidence threshold to address correctly

For each ambiguous thread, use AskUserQuestion to ask the user how to proceed. Never silently skip or auto-resolve a comment you are unsure about.

## Step 4 — Make the Edits

For each unresolved, unambiguous thread:

1. Use the Read tool to load the full file and understand context around the flagged line
2. Make the minimal edit necessary to address the feedback — do not refactor beyond what was asked
3. Respect the project's existing conventions:
   - Kotlin idioms (prefer `?.let`, `?:`, extension functions over verbose null checks)
   - ViewModel / Repository / UseCase layering — do not blur layer boundaries
   - Coroutines: use `viewModelScope` in ViewModels, `withContext(Dispatchers.IO)` for I/O in repositories
   - Koin for dependency injection — do not introduce Hilt or manual construction
   - Follow existing naming conventions in the file being edited
4. If the fix requires changes in more than one file (e.g. an interface and its implementation), make all related changes before moving to the next thread

## Step 5 — Review Summary

After all edits are complete, output a summary to the terminal:

```
Summary of changes made:

✅ Thread 1 — MyViewModel.kt:42
   Fixed: Replaced GlobalScope.launch with viewModelScope.launch
   
✅ Thread 2 — Repository.kt:87
   Fixed: Added null safety check with ?.let block

⏭️ Thread 3 — UserAdapter.kt:15
   Skipped: Awaiting your decision on the architectural question above

Files modified:
  app/src/main/java/com/example/MyViewModel.kt
  app/src/main/java/com/example/Repository.kt

No commits have been made. Review the diff with:
  git diff

When ready to commit:
  git add -p
  git commit -m "fix: address PR review feedback"
```

## Step 6 — Do Not Commit or Push

Stop after the summary. Do not run `git add`, `git commit`, or `git push` unless the user explicitly asks you to in a follow-up message.

If the user approves and asks you to commit, use this format:

```bash
git commit -m "fix: address PR review feedback

- <brief description of each fix>

🤖 Generated with Claude Code"
```

## Notes

- Never resolve GitHub review threads via the API — leave that for the reviewer or the user to do after confirming the fix is correct
- Never post reply comments back to the PR threads — the user may want to word those themselves
- If a suggested fix would break existing tests or violate obvious correctness constraints, flag it in the summary rather than blindly applying it
- If the PR has more than 20 unresolved threads, inform the user of the count and ask if they want to proceed with all of them or a subset
