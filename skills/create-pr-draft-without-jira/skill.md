---
name: create-pr-draft-without-jira
description: Analyzes all commits on current branch (from divergence point with main), generates PR draft document based on GitHub PR template. Use when user asks to "정리해줘" (summarize work), "작업 내용을 PR로 만들어줘" (create PR from work), "PR문서를 생성해줘" (generate PR document), or similar requests about creating pull request drafts or summarizing branch changes.
allowed-tools: Bash, Read, Grep, Glob, Write
model: sonnet
---

# Create PR Draft Document

Analyzes all commits on your current branch (from the point where it diverged from main), then generates a Pull Request
draft document following the project's GitHub PR template format.

## Overview

This Skill automates PR draft creation by:

1. Extracting GitHub issue number from the current branch name (e.g., `feature/1` → issue #1)
2. **Fetching GitHub issue information using GitHub CLI** (title, description, labels, state, etc.)
3. **Analyzing ALL commits on the current branch** (from divergence point with main branch)
4. Running `gradlew build` to verify the code builds successfully
5. Generating PR content based on the project's GitHub PR template and issue context
6. Creating a draft document file (NOT creating the actual PR)

## Prerequisites

- Current branch follows naming convention: `feature/{숫자}`, `fix/{숫자}`, `hotfix/{숫자}`, `refactor/{숫자}`, or `chore/{숫자}`
    - Example: `feature/1`, `fix/2`
- Git repository is initialized
- Current branch has diverged from main branch
- All commits on the branch relate to the current issue
- Gradle build environment is ready
- GitHub CLI is installed and authenticated

## Workflow

### Step 1: Extract and Fetch GitHub Issue Information

**1.1 Extract Issue Number from Branch Name**

- Parse branch name to extract GitHub issue number
    - Pattern: `(feature|feat|fix|hotfix|refactor|chore)/{숫자}`
    - Example: `feature/1` → Extract issue number `1`
- If branch name doesn't match pattern, ask user to provide issue number manually

**1.2 Fetch GitHub Issue Information using GitHub CLI**
Use the GitHub CLI to retrieve issue details:

```bash
# Fetch issue information
gh issue view {issue_number} --json title,body,labels,assignees,state
```

Example response:

```json
{
  "title": "회원 관리 기능 구현",
  "body": "백엔드 기본 프로젝트 구조를 정의하고 회원 관리 기능을 구현해야 합니다.",
  "labels": [
    "backend",
    "api"
  ],
  "assignees": [
    "홍길동"
  ],
  "state": "OPEN"
}
```

Extract the following information:

- **Issue Title** (`title`): Use as context for understanding what this PR accomplishes
- **Issue Body** (`body`): Provides background on requirements and expected behavior (may be empty)
- **Labels** (`labels`): Tags that provide additional context (may be empty)
- **Assignees** (`assignees`): People responsible for the issue (may be empty)
- **State** (`state`): Current workflow state (OPEN, CLOSED, DRAFT)

**1.3 Display Issue Context**
Show the user:

```
📋 GitHub 이슈 정보:
- Issue Number: #1
- Title: 회원 관리 기능 구현
- State: OPEN
- Labels: backend, api
- Assignees: 홍길동
```

This context will be used to:

- Verify the branch work aligns with the issue
- Generate more accurate PR description
- Include relevant issue details in the PR draft

### Step 2: Analyze ALL Branch Changes Holistically

**CRITICAL: Analyze ALL commits on the current branch since divergence from main using GitHub CLI**

**2.1 Setup: Extract Branch Info and Repo**

```bash
# Get current branch name (local git operation — gh has no equivalent)
BRANCH=$(git branch --show-current)
ISSUE_NUMBER=$(echo $BRANCH | grep -oE '[0-9]+$')
echo "Branch: $BRANCH  |  Issue Number: $ISSUE_NUMBER"

# Get GitHub repo identifier (owner/repo)
REPO=$(gh repo view --json nameWithOwner --jq '.nameWithOwner')
echo "Repo: $REPO"
```

**2.2 Fetch Branch Comparison via GitHub CLI**
Use `gh api` to query the GitHub compare endpoint. This is the **primary data source** — it returns commits and changed
files from GitHub's perspective (requires branch to be pushed).

```bash
# Overall comparison stats (commits ahead, files changed)
gh api repos/$REPO/compare/main...$BRANCH \
  --jq '{ahead_by: .ahead_by, behind_by: .behind_by, total_commits: (.commits | length), files_changed: (.files | length)}'

# All commits on this branch vs main (hash + full commit message)
gh api repos/$REPO/compare/main...$BRANCH \
  --jq '.commits[] | {sha: .sha[:7], message: .commit.message}'

# All changed files with status and line counts
gh api repos/$REPO/compare/main...$BRANCH \
  --jq '.files[] | {filename: .filename, status: .status, additions: .additions, deletions: .deletions}'
```

**2.3 Analyze Fetched Data**

**Phase A: Commit Analysis**
From the commit list, identify:

- Subject line pattern (feat/fix/refactor/chore + 한국어 설명)
- Commit types present:
    - ✨ feat: New features/functionality
    - ♻️ refactor: Code improvements
    - 🧪 test: Test additions
    - 📝 docs: Documentation
    - 🔧 config/chore: Configuration changes
    - 🐛 fix: Bug fixes

**Phase B: File-Level Analysis**
From the changed files list (`gh api ... .files[]`):

- High-impact files (additions 500+)
- New files (`status: "added"`) vs modifications (`status: "modified"`) vs deletions (`status: "removed"`)
- Categorize by architecture layer:
    - **Infrastructure**: build.gradle, config/, gradle/, .gitignore
    - **Domain logic**: entity/, service/, repository/, converter/
    - **API layer**: controller/, dto/
    - **Common utilities**: common/, error/, response/, util/
    - **Tests**: test/
    - **Documentation**: *.md, templates/

**Phase C: Synthesis**
Combine commits + files into 2-4 high-level themes:

- Example: "Checkstyle 통합" (checkstyle rules + gradle config + test application)
- Example: "회원 관리 API 구현" (entity + repository + service + controller + dto + tests)

**DO NOT:**

- Focus only on the latest commit
- List individual file names excessively (max 2-3 critical files)
- Break down changes commit-by-commit
- Include implementation details that don't add value
- Copy commit messages verbatim

**DO:**

- Synthesize ALL commits into a coherent narrative
- Describe the overall functionality implemented across the entire branch
- Explain what the system can now do that it couldn't before
- Write concise, high-level summaries (2-4 bullet points)
- Focus on the end result and capabilities added by this branch
- Use professional, brief style (10-20 words per bullet)
- Mention test coverage if tests were added
- Example good summary: "Checkstyle 규칙 파일 추가 및 Gradle 빌드 통합 구성"
- Example good summary: "CQRS 패턴 기반 회원 관리 API 구현 (생성/조회 기능)"

**Important Notes:**

- Analyze all commits on the current branch
- If the branch has 3+ commits, synthesize them into 2-4 key themes
- If commits include refactoring + features, mention both
- If commits include tests, mention test coverage
- Display total commit count for transparency

### Step 3: Build Verification

Execute build to ensure code compiles (Windows 환경):

```bash
gradlew build
```

If build fails, report errors to the user and stop the process.

### Step 4: Generate PR Draft

Read the template from `.github/PULL_REQUEST_TEMPLATE.md` (located in project root) and generate PR content based on it.

**Template Sections to Fill:**

1. **⏫ 어떤 이슈와 관련된 작업인가요?**
    - Fill in the issue number: `* #{issue_number}` (e.g., `* #1`)
    - This is the first section in the template

2. **✨ 이 PR에서 핵심적으로 변경된 사항은 무엇일까요?**
    - KEEP the `>` instruction comment line exactly as-is from template
    - **Write CONCISE, HIGH-LEVEL summaries** directly:
        - Read ALL file changes, code modifications, and commit messages
        - Cross-reference with issue description to verify alignment
        - Understand the complete picture and distill into essence
        - Write 2-4 bullet points in brief, professional style
        - Each bullet: 10-20 words maximum, focus on WHAT was done
        - Style example: "CQRS 패턴 기반 회원 관리 API 구현 (생성/조회)"
    - Focus based on branch type:
        - **feature/feat**: What was built/added? (e.g., "XX 기능 구현", "XX 시스템 구축")
        - **refactor**: What was improved? (e.g., "XX 로직 개선", "XX 구조 리팩토링")
        - **fix**: What was fixed? (e.g., "XX 버그 수정", "XX 오류 해결")
        - **chore**: What was configured/maintained? (e.g., "XX 설정 추가", "XX 의존성 업데이트")
    - Describe functionality, not just file names
    - Use noun phrases: "XX 구축", "XX 설정", "XX 구현"

3. **🔖 핵심 변경 사항 외에 추가적으로 변경된 부분이 있나요?**
    - KEEP the `>` instruction comment line exactly as-is from template
    - If none: write `* 없음`
    - If exists: List with `*` bullets (each item on new line)

4. **💾 SQL 스키마 또는 쿼리 변경 사항이 있나요?**
    - KEEP BOTH `>` instruction comment lines exactly as-is from template
    - If no SQL changes: write `* 없음`
    - ALWAYS keep the SQL code block even if empty
    - If SQL changes exist: describe them with `*` bullets and fill the SQL code block

5. **🙏 Reviewer 분들이 이런 부분을 신경써서 봐 주시면 좋겠어요**
    - KEEP the `>` instruction comment line exactly as-is from template
    - Highlight areas needing review with `*` bullets
    - Mention architectural decisions or trade-offs
    - If none: write `* 특별히 검토가 필요한 부분 없음`

6. **🩺 이 PR에서 테스트 혹은 검증이 필요한 부분이 있을까요?**
    - KEEP the `>` instruction comment line exactly as-is from template
    - List testing requirements with `*` bullets
    - Mention added test cases
    - Include functional verification items
    - If none: write `* 특별한 검증 필요 없음`

**Footer Section:**

- KEEP entire "📌 PR 진행 시 이러한 점들을 참고해 주세요" section exactly as-is from template

**Checkboxes (📝 Assignee를 위한 CheckList):**

- `- [x] 빌드 및 실행 테스트완료` (auto-checked after successful build)
- If NO SQL changes: `- [x] SQL 변경사항 검토 완료 (해당하는 경우)` (auto-checked)
- If SQL changes exist: `- [ ] SQL 변경사항 검토 완료 (해당하는 경우)` (unchecked, user must verify)

**IMPORTANT: Template Location**

- The template is located at: `.github/PULL_REQUEST_TEMPLATE.md` (in project root)
- Always read the template from this location before generating PR draft

### Step 5: Output Draft Document

Create file: `.pr-drafts/PR_DRAFT_{branch-name}.md` with generated content.

Directory structure:

```
project-root/
  .pr-drafts/
    PR_DRAFT_feature-1.md
```

Show the user:

- File path
- Issue summary
- Preview of generated content
- Build result summary

## Important Rules

1. **GitHub Issue Integration**
    - Extract issue number from branch name: `feature/1` → `#1`
    - Fetch GitHub issue using GitHub CLI before generating PR draft
    - Fill in the first section "⏫ 어떤 이슈와 관련된 작업인가요?" with: `* #{issue_number}`
    - Cross-reference commit changes with issue requirements
    - Use issue context for reference only (do not embed as metadata)

2. **NEVER delete or omit any section from the template**
    - Keep ALL `>` instruction comment lines EXACTLY as they appear in template
    - NEVER modify or remove `>` lines

3. **Focus on FUNCTIONALITY with context**
    - **feature/feat**: Emphasize new capabilities, features, APIs added
    - **refactor**: Emphasize improvements, convention changes made
    - **fix**: Emphasize bug resolution achieved
    - **chore**: Emphasize configuration, setup, maintenance work done
    - Include relevant file/component names for context, but focus on what was achieved

4. **"없음" formatting rules**
    - ALL sections use `* 없음` format (WITH bullet)
    - Correct: `* 없음`
    - Incorrect: just `없음` without bullet

5. **SQL checkbox handling**
    - If NO SQL changes: Auto-check the SQL checkbox `- [x]`
    - If SQL changes exist: Leave unchecked `- [ ]` for user verification

6. **NEVER create the actual PR**
    - Only generate draft document in `.pr-drafts/` folder
    - User will manually create PR using the draft on GitHub

7. **Build must pass before generating draft**
    - Run `gradlew build` (Windows 환경)
    - If fails, stop and report errors

8. **Windows 환경 고려**
    - Use `gradlew` (not `./gradlew`)
    - Handle Windows path separators correctly

## Example Usage

**Branch**: `feature/1`
**All commits on branch**:

```
3eef1b4 🚚 rename: Feature.md 파일을 Default.md로 변경
385a7f1 📝 docs: CLAUDE.md 파일 추가
5c9d05f ♻️ refactor: Lombok 접근 제어자 수정
458c313 🧪 test: 회원 서비스 단위 테스트 추가
1a40289 ✨ feat: 가이드라인 예제를 위한 회원 관리 기능 추가
```

**GitHub Issue**: #1 - "백엔드 기본 프로젝트 구조 정의"

**Step 1: GitHub Issue Fetched**

```
📋 GitHub 이슈 정보:
- Issue Number: #1
- Title: 백엔드 기본 프로젝트 구조 정의
- State: OPEN
- Labels: backend, api
- Assignees: 신윤섭
```

**Step 2: All Commits Analyzed**

```
분석된 커밋 수: 5개
주요 변경사항:
- 회원 관리 도메인 구현 (Member entity, DTO, Service, Controller)
- 단위 테스트 코드 작성
- Lombok 접근 제어자 리팩토링
- 프로젝트 가이드라인 문서화 (CLAUDE.md)
- GitHub PR 템플릿 표준화
```

**Generated PR Draft**:

```markdown
## ⏫ 어떤 이슈와 관련된 작업인가요?

* #1

## ✨ 이 PR에서 핵심적으로 변경된 사항은 무엇일까요?

> 문제를 해결하면서 주요하게 변경된 사항들을 적어 주세요

* CQRS 패턴 기반 회원 관리 도메인 구현 (Entity, DTO, Service, Controller)
* 회원 서비스 단위 테스트 작성 (CommandService, QueryService, FacadeService)
* 프로젝트 가이드라인 문서화 및 PR 템플릿 표준화
* Lombok 접근 제어자 리팩토링 (보안 강화)

## 🔖 핵심 변경 사항 외에 추가적으로 변경된 부분이 있나요?

> 없으면 "없음" 이라고 기재해 주세요

* 없음

## 💾 SQL 스키마 또는 쿼리 변경 사항이 있나요?

> 없으면 "없음" 이라고 기재해 주세요
> DB 마이그레이션이 필요한 경우, 마이그레이션 스크립트를 함께 첨부해 주세요

* 없음

### SQL

```sql

```

## 🙏 Reviewer 분들이 이런 부분을 신경써서 봐 주시면 좋겠어요

> 개발 과정에서 다른 분들의 의견은 어떠한지 궁금했거나 크로스 체크가 필요하다고 느껴진 코드가 있다면 남겨주세요

* CQRS 패턴 적용이 프로젝트 가이드라인과 일치하는지 확인 부탁드립니다
* Converter 패턴 사용이 적절한지 검토 부탁드립니다

## 🩺 이 PR에서 테스트 혹은 검증이 필요한 부분이 있을까요?

> 테스트가 필요한 항목이나 테스트 코드가 추가되었다면 함께 적어주세요

* 회원 생성 API 정상 동작 확인 (POST /api/members)
* 회원 조회 API 정상 동작 확인 (GET /api/members/{id})
* 이메일 중복 검증 로직 동작 확인
* 단위 테스트 전체 통과 확인

### 📌 PR 진행 시 이러한 점들을 참고해 주세요

* Reviewer 분들은 코드 리뷰 시 좋은 코드의 방향을 제시하되, 코드 수정을 강제하지 말아 주세요.
* Reviewer 분들은 좋은 코드를 발견한 경우, 칭찬과 격려를 아끼지 말아 주세요.
* Review는 특수한 케이스가 아니면 Reviewer로 지정된 시점 기준으로 1일 이내에 진행해 주세요.
* Comment 작성 시 Prefix로 P1, P2, P3 를 적어 주시면 Assignee가 보다 명확하게 Comment에 대해 대응할 수 있어요
    * P1 : 꼭 반영해 주세요 (Request Changes) - 이슈가 발생하거나 취약점이 발견되는 케이스 등
    * P2 : 반영을 적극적으로 고려해 주시면 좋을 것 같아요 (Comment)
    * P3 : 이런 방법도 있을 것 같아요~ 등의 사소한 의견입니다 (Chore)

---

## 📝 Assignee를 위한 CheckList

- [x] 빌드 및 실행 테스트완료
- [x] SQL 변경사항 검토 완료 (해당하는 경우)

```

## Limitations

- Only works with branches following naming convention: `{type}/{숫자}`
- Cannot create actual PR (only draft document)
- Requires issue number to be in branch name
- Windows 환경 기준으로 작성됨 (gradlew 명령어)

## Error Handling

If any step fails:
1. **Branch name parsing fails**: Ask user to provide issue number manually
2. **GitHub CLI connection fails**:
   - Show error message
   - Continue with PR generation using only commit information (without issue context)
3. **Issue not found**:
   - Show error message with issue number
   - Ask user to verify the issue exists on GitHub
   - Continue with PR generation using only commit information (without issue context)
4. **Build fails**: Report build errors and stop process
5. **No changes detected**: Warn user and ask if they want to continue
6. **Invalid branch name format**: Show expected format and ask user to rename or provide issue number

## Notes

- This skill is adapted for GitHub Pull Requests
- Uses GitHub issue tracking system (#숫자 format)
- Follows Spring Boot backend project conventions
- Outputs to `.pr-drafts/` directory
- GitHub CLI commands use GitHub API as the source of truth for branch analysis

## GitHub CLI Command Reference

These `gh` CLI commands are the primary tool for branch analysis. Use local `git` only as fallback when the branch is not yet pushed.

### Setup
```bash
# Get current branch (local only — no gh equivalent)
BRANCH=$(git branch --show-current)
ISSUE_NUMBER=$(echo $BRANCH | grep -oE '[0-9]+$')

# Get repo identifier
REPO=$(gh repo view --json nameWithOwner --jq '.nameWithOwner')
```

### 1. Branch Comparison Summary

```bash
# Ahead/behind counts, total commits and files changed
gh api repos/$REPO/compare/main...$BRANCH \
  --jq '{ahead_by: .ahead_by, behind_by: .behind_by, commits: (.commits | length), files: (.files | length)}'
```

### 2. All Commits on Branch

```bash
# List every commit (sha + full message)
gh api repos/$REPO/compare/main...$BRANCH \
  --jq '.commits[] | "\(.sha[:7]) \(.commit.message | split("\n")[0])"'

# With author and date
gh api repos/$REPO/compare/main...$BRANCH \
  --jq '.commits[] | "\(.sha[:7]) [\(.commit.author.name)] \(.commit.message | split("\n")[0])"'
```

### 3. Filter Commits by Issue ID

```bash
# All commits (issue filtering based on branch is sufficient)
gh api repos/$REPO/compare/main...$BRANCH \
  --jq '.commits[] | "\(.sha[:7]) \(.commit.message | split("\n")[0])"'

# Count total commits
TOTAL=$(gh api repos/$REPO/compare/main...$BRANCH --jq '.commits | length')
echo "Total commits: $TOTAL"
```

### 4. Changed Files

```bash
# All files with status and line counts
gh api repos/$REPO/compare/main...$BRANCH \
  --jq '.files[] | "\(.status)\t+\(.additions)/-\(.deletions)\t\(.filename)"'

# Only added files
gh api repos/$REPO/compare/main...$BRANCH \
  --jq '.files[] | select(.status == "added") | .filename'

# Only modified files
gh api repos/$REPO/compare/main...$BRANCH \
  --jq '.files[] | select(.status == "modified") | .filename'

# Summary: files grouped by status
gh api repos/$REPO/compare/main...$BRANCH \
  --jq '[.files[] | .status] | group_by(.) | map({status: .[0], count: length})'
```

### 5. Patch Content (for deep analysis)

```bash
# Get diff patch for a specific file
gh api repos/$REPO/compare/main...$BRANCH \
  --jq --arg file "src/main/java/troublog/backend/SomeService.java" \
  '.files[] | select(.filename == $file) | .patch'
```

### 6. Fallback — Local Git (when branch is not pushed)

```bash
# Commit list
git log main..HEAD --format="%h %s" --no-merges

# Changed files
git diff main...HEAD --name-status

# File diff stats
git diff main...HEAD --stat
```

### Why gh CLI over git

- **GitHub API as source of truth**: returns exactly what GitHub sees (not local state)
- **`--jq` filtering**: extract only the fields needed without extra parsing
- **`.files[].status`**: explicitly labeled as `added/modified/removed` (vs git A/M/D flags)
- **No xargs chains**: single API call returns both commits and files together
