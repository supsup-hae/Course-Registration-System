---
name: troubleshooting-doc
description: Document AI troubleshooting conversations into structured Markdown notes for personal reference. Use when user requests to summarize or document a conversation session with phrases like "해당 세션 내용을 정리하자", "해당 세션을 정리해줘", "지금까지 질문한 내용을 정리해줘", or similar requests to create documentation from the current chat.
---

# Troubleshooting Documentation

## Overview

Convert AI troubleshooting conversations into structured Markdown documentation that captures the problem-solving
journey, questions asked, solutions found, and technical concepts discussed.

## Output Structure

Create a Markdown document with the following sections:

### 1. 문제 상황 요약 (Problem Summary)

Provide a concise summary of the initial problem or challenge that triggered the conversation. Include:

- What wasn't working or what needed to be solved
- The context or environment (tech stack, tools, project type if relevant)
- The desired outcome

### 2. 질문 흐름 (Question Flow)

Document the conversation chronologically as a Q&A format:

- **User questions**: Include verbatim (exact original text)
- **AI responses**: Summarize the key points, insights, and solutions provided
- Maintain chronological order to show the problem-solving progression
- Number each Q&A pair for easy reference

**Code handling rules**:

- Review each code snippet from AI responses
- Check if the code was actually applied to the user's project (look for follow-up messages indicating implementation)
- Only include code blocks that were confirmed to be applied to the project
- Wrap included code in proper markdown code blocks with language identifier

### 3. 핵심 해결책 (Key Solutions)

Synthesize the main solutions, insights, or approaches that resolved the issue:

- List actionable solutions that worked
- Include critical configuration changes or commands
- Note any important caveats or considerations
- Reference the Q&A pair number where each solution was discussed

### 4. 기술 키워드 (Technical Keywords)

Extract and list technical concepts, technologies, tools, or methodologies discussed:

- Programming languages, frameworks, libraries
- Technical concepts or patterns
- Tools, services, or platforms
- Error messages or specific technical terms
- Format as comma-separated tags for easy scanning

## File Management

**Location**: Save the document in a `/docs` directory separate from any project folder

- If `/docs` doesn't exist, create it in the user's working directory
- Use descriptive filename: `troubleshooting-YYYY-MM-DD-brief-topic.md`
- Example: `troubleshooting-2025-01-06-redis-persistence.md`

**File creation**: Use the `create_file` tool to generate the markdown document, then present it to the user with
`present_files`.

## Workflow

1. Review the entire conversation from start to current point
2. Identify the core problem from initial messages
3. Extract all user questions verbatim
4. Summarize AI responses focusing on solutions and insights
5. Identify which code snippets were actually implemented
6. Synthesize key takeaways and solutions
7. Extract technical keywords from the discussion
8. Generate the structured Markdown document
9. Save to `/docs` directory
10. Present the file to the user

## Example Output Format

```markdown
# [문제 간단 설명]

## 문제 상황 요약
[Initial problem description and context]

## 질문 흐름

### Q1
[User's exact question]

**A1**
[Summarized response with key insights]

### Q2
[User's exact question]

**A2**
[Summarized response]
```python
# Code that was applied to project
[code here]
```

[Continue for all Q&A pairs...]

## 핵심 해결책

1. [Main solution 1] (참고: Q&A #X)
2. [Main solution 2] (참고: Q&A #Y)
3. [Important insight or approach]

## 기술 키워드

Redis, AOF, RDB, Spring Boot, Persistence, Docker, Configuration

```

## Important Notes

- Focus on clarity and brevity in summaries
- Preserve technical accuracy in all content
- Only include code that was confirmed to be applied
- Make the document immediately useful for future reference
- Use Korean for section headers and content as shown in the example
