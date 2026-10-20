#!/usr/bin/env python3
"""
بررسی سلامت مدل‌های صوتی Vosk در assets.

مدل‌ها با Git-LFS نگه‌داری می‌شوند. اگر کسی بدون `git lfs pull` کلون کند،
فایل‌های ZIP به‌جای آرشیو واقعی، یک «pointer» متنی هستند و اپ در زمان اجرا
میکروفون آفلاین را بی‌صدا رها می‌کند. این اسکریپت همان حالت را قبل از بیلد
گرفته و با پیام واضح متوقف می‌شود.

اجرا:  python3 check_voice_models.py
خروج:  0 = سالم، 1 = نیاز به git lfs pull
"""
import os
import sys

ASSETS = os.path.join("app", "src", "main", "assets")
EXPECTED = [
    "vosk-model-small-fa-0.4.zip",
    "vosk-model-small-en-us-0.15.zip",
    "vosk-model-small-ar-0.3.zip",
    "vosk-model-small-tr-0.3.zip",
]
# کوچک‌ترین مدل واقعی چند مگابایت است؛ pointer معمولاً ~۱۳۵ بایت است.
MIN_VALID_BYTES = 1024 * 1024
LFS_MAGIC = b"version https://git-lfs.github.com/spec/v1"


def main() -> int:
    problems = []
    for name in EXPECTED:
        path = os.path.join(ASSETS, name)
        if not os.path.exists(path):
            problems.append(f"missing: {name}")
            continue
        size = os.path.getsize(path)
        with open(path, "rb") as fh:
            head = fh.read(len(LFS_MAGIC))
        if head == LFS_MAGIC:
            problems.append(f"Git-LFS pointer (not downloaded): {name}")
        elif size < MIN_VALID_BYTES:
            problems.append(f"suspiciously small ({size} bytes): {name}")

    if problems:
        print("❌ Voice models are NOT usable:")
        for p in problems:
            print("   -", p)
        print()
        print("Fix: install Git LFS and fetch the models, then rebuild:")
        print("   git lfs install && git lfs pull")
        return 1

    print(f"✅ All {len(EXPECTED)} Vosk models look like real archives.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
