# AGENTS.md — Hushka

## 1. Mission

You are working on **Hushka**, an Android application for exploring, creating, storing, and managing AI prompts and interacting with multiple AI services.

Your primary objective is:

> Make the smallest correct change that solves the actual problem, verify it, and avoid changing unrelated behavior.

Correctness and stability are more important than architectural elegance or the amount of code changed.

---

## 2. Before Writing Code

DO NOT immediately modify files.

First:

1. Inspect the repository structure.
2. Identify the relevant module, package, and file.
3. Read the existing implementation around the problem.
4. Trace the actual execution and data flow.
5. Identify the root cause.
6. Check whether the requested behavior already exists partially.
7. Check existing tests before creating new ones.
8. State important assumptions when information is missing.

Do not invent architecture, APIs, classes, functions, dependencies, or behavior that have not been verified in the repository.

If the request is ambiguous and different interpretations could produce different implementations, ask before making a broad change.

---

## 3. Surgical Changes

Follow the principle:

> Change only what is necessary.

DO NOT:

- perform unrelated refactors;
- rename classes or files without necessity;
- reorganize packages for aesthetic reasons;
- upgrade dependencies without a concrete reason;
- replace working implementations with a preferred architecture;
- rewrite working code merely because another implementation looks cleaner;
- modify unrelated UI;
- change API behavior accidentally;
- delete apparently unused code without proving it is unused.

A bug fix should normally result in a small, focused diff.

If a larger change is genuinely required, explain why before doing it.

---

## 4. Existing Architecture Is the Default

Respect the architecture already present in Hushka.

Do not introduce a new architectural pattern merely because it is fashionable.

Before adding a Repository, ViewModel, UseCase, Manager, Service, Singleton, abstraction/interface, or dependency, first determine whether an existing component already performs the required responsibility.

Prefer extending an existing mechanism over creating a parallel one.

---

## 5. Android / Kotlin Rules

Hushka is an Android/Kotlin project.

Prefer:

- Kotlin idioms already used by the project;
- existing Compose patterns;
- existing state-management patterns;
- existing coroutine and suspend patterns;
- existing Room patterns;
- existing networking abstractions;
- existing dependency versions.

Do not introduce Java unless there is a demonstrated Android/platform requirement.

Do not introduce a new third-party dependency when the Android/Kotlin standard library or an existing project dependency can reasonably solve the problem.

Do not change Gradle configuration unless necessary.

---

## 6. AI Provider Changes

Hushka communicates with multiple AI providers.

When modifying an AI provider:

1. Inspect the existing provider abstraction.
2. Follow the existing request/response model.
3. Preserve existing providers.
4. Do not silently change provider-selection behavior.
5. Handle malformed, empty, partial, and error responses.
6. Do not expose API keys or credentials in source code.
7. Never log API keys, authorization headers, or sensitive request payloads.
8. Preserve streaming behavior where streaming is already supported.
9. Verify that UI state is correctly updated on success, failure, cancellation, and partial responses.

A provider-specific change must not accidentally alter another provider.

---

## 7. Networking

For network changes:

- inspect existing OkHttp/network code first;
- reuse existing clients and configuration;
- preserve timeouts unless the bug requires changing them;
- handle HTTP errors explicitly;
- handle malformed JSON;
- handle empty responses;
- handle connection failures;
- avoid blocking the main thread;
- do not leak credentials into logs;
- do not silently swallow exceptions.

Do not assume an HTTP 2xx response means the application-level operation succeeded.

---

## 8. Vosk / Voice Features

Hushka supports online and offline voice functionality.

Voice-related changes must consider:

- model availability;
- Git LFS-managed model files;
- initialization failures;
- missing or corrupt models;
- microphone permission;
- lifecycle;
- cancellation;
- partial recognition;
- final recognition;
- UI error states.

Never replace a meaningful error with silent failure.

If a voice model is expected to exist, verify how the repository currently validates it before changing that mechanism.

---

## 9. Room / Persistence

When changing database entities, DAOs, repositories, or persisted models:

1. Check existing schema/version handling.
2. Determine whether a migration is required.
3. Do not casually delete or rename persisted fields.
4. Preserve backward compatibility where existing user data matters.
5. Test insert/read/update/delete behavior affected by the change.

A successful compilation is NOT sufficient proof that a database change is safe.

---

## 10. Compose / UI

For UI changes:

- inspect the existing composable hierarchy first;
- reuse existing components;
- preserve existing theme behavior;
- preserve dark/AMOLED mode;
- preserve localization;
- preserve RTL behavior where applicable;
- avoid introducing state duplication;
- avoid unnecessary recomposition;
- do not redesign unrelated screens.

Do not fix a UI bug by hiding the underlying state problem.

When modifying animations, loading states, or streaming UI, verify behavior during:

- initial state;
- loading;
- success;
- empty result;
- error;
- cancellation;
- repeated execution.

---

## 11. Caching

Hushka uses RAM and disk caching.

When modifying caching:

- identify cache ownership;
- understand cache invalidation;
- preserve persistence semantics;
- avoid stale data;
- avoid memory leaks;
- do not introduce a second cache for the same data without justification.

A cache bug should be investigated through the complete read/write/invalidation path.

---

## 12. Prompt / Role / Variable Systems

Hushka contains prompt management, specialized roles, tags, and variable handling.

When changing these systems:

- preserve existing prompt data;
- preserve variable-detection behavior;
- preserve user-entered values;
- avoid destructive transformations;
- preserve category/tag relationships;
- consider imported or previously stored prompts.

Never assume prompt content is disposable test data.

---

## 13. Error Handling

Every failure path that can affect the user should have an intentional outcome.

Avoid:

```kotlin
catch (e: Exception) {
    // ignore
}
```

unless ignoring the exception is demonstrably intentional.

Prefer:

- meaningful fallback;
- user-visible error where appropriate;
- structured logging;
- retry only when retrying is safe;
- preserving current state when recovery fails.

Do not show technical stack traces directly to users.

---

## 14. Security

Never commit:

- API keys;
- passwords;
- tokens;
- private certificates;
- keystores;
- credentials;
- personal data.

Before committing a change involving configuration or networking, check that secrets are not being introduced.

Never print secrets through Logcat.

Treat user prompts, conversations, and generated content as potentially sensitive.

---

## 15. Tests

Before changing production code:

1. Find relevant existing tests.
2. Determine whether the bug can be reproduced by a unit test.
3. Add or modify the smallest useful test.

After changing code, run the narrowest relevant validation first.

Typical validation:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

For voice-model changes, also run:

```bash
python3 check_voice_models.py
```

when applicable.

Do not claim a build or test passed unless it was actually executed.

---

## 16. Verification Hierarchy

Use this order:

### Level 1 — Static inspection

Check:

- imports;
- types;
- nullability;
- state flow;
- threading;
- lifecycle;
- obvious regressions.

### Level 2 — Focused tests

Run the smallest relevant test.

### Level 3 — Full unit tests

```bash
./gradlew testDebugUnitTest
```

### Level 4 — APK build

```bash
./gradlew assembleDebug
```

### Level 5 — Device validation

When the problem concerns permissions, microphone, notifications, streaming, rendering, lifecycle, or Android-specific behavior, prefer actual device/emulator verification.

---

## 17. Build Failure Discipline

If the build fails:

1. Read the FIRST meaningful error.
2. Identify the actual root cause.
3. Fix that cause.
4. Re-run the smallest relevant command.
5. Do not randomly modify multiple files.

Do not respond to one compilation error by rewriting an entire subsystem.

---

## 18. No Fake Verification

Never say:

- "build successful"
- "tests passed"
- "fixed"
- "verified"

unless the corresponding verification actually happened.

Distinguish clearly between:

- inspected;
- reasoned;
- changed;
- built;
- tested;
- manually verified.

---

## 19. Git Discipline

Keep commits focused.

Preferred commit structure:

```text
fix: <specific problem>
feat: <specific feature>
refactor: <specific refactor>
test: <specific test change>
```

Do not mix a bug fix, dependency upgrade, formatting, and unrelated refactor in one change unless there is a concrete reason.

Before committing, inspect the diff.

Ask:

> Is every changed line necessary for this task?

If not, revert the unrelated change.

---

## 20. When Investigating a Bug

Use this sequence:

```text
REPRODUCE
   ↓
LOCATE
   ↓
TRACE
   ↓
IDENTIFY ROOT CAUSE
   ↓
DESIGN SMALLEST FIX
   ↓
IMPLEMENT
   ↓
TEST
   ↓
BUILD
   ↓
RECHECK REGRESSION
```

Do not jump directly from symptom to code modification.

---

## 21. When Adding a Feature

Use:

```text
UNDERSTAND EXISTING ARCHITECTURE
        ↓
DEFINE BEHAVIOR
        ↓
IDENTIFY EXISTING EXTENSION POINT
        ↓
IMPLEMENT MINIMAL CHANGE
        ↓
ADD TEST
        ↓
BUILD
        ↓
VERIFY USER FLOW
```

Do not create infrastructure before proving that the feature requires it.

---

## 22. Agent Communication

Before making a significant change, briefly state:

### Problem
What is actually wrong?

### Root cause
Why is it happening?

### Plan
What is the smallest safe change?

### Verification
How will success be measured?

After implementation, report:

- files changed;
- what changed;
- tests executed;
- build result;
- remaining uncertainty, if any.

Do not produce long explanations for trivial changes.

---

## 23. Success Criteria

A task is complete only when:

1. The requested behavior exists.
2. Existing behavior is preserved.
3. Relevant tests pass.
4. The project builds successfully when applicable.
5. No unrelated files were changed.
6. No secrets were introduced.
7. The result has been verified to the degree appropriate for the task.

The final question before declaring completion is:

> What evidence do I have that this actually works?

If the answer is "I only changed the code", the task is NOT fully verified.

---

## 24. Golden Rule

> THINK FIRST.
>
> UNDERSTAND THE EXISTING CODE.
>
> MAKE THE SMALLEST CORRECT CHANGE.
>
> VERIFY THE RESULT.
>
> DO NOT TOUCH WHAT YOU DO NOT NEED TO TOUCH.
