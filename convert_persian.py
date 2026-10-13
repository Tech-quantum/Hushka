#!/usr/bin/env python3
import json
import re
import sys

LIMIT = 0  # همه پرامپت‌ها

SOURCE = '/sdcard/hf_all.json'
TARGET = 'app/src/main/assets/prompts_persian_v0.1.0.json'

# نگاشت tools به provider
TOOL_TO_PROVIDER = {
    'chatgpt': 'openai', 'gpt': 'openai', 'dalle': 'openai', 'sora': 'openai',
    'claude': 'anthropic',
    'gemini': 'google', 'veo': 'google',
    'deepseek': 'deepseek',
    'perplexity': 'perplexity',
    'copilot': 'microsoft',
    'cursor': 'cursor',
    'midjourney': 'midjourney',
    'leonardo': 'leonardo',
    'flux': 'flux',
    'runway': 'runway',
    'kling': 'kling',
    'pika': 'pika',
}

def extract_variables(text):
    """استخراج [VARIABLE] ها از متن"""
    return list(dict.fromkeys(re.findall(r'\[([^\]]+)\]', text)))

def convert_prompt(p):
    tools = p.get('tools', [])
    provider = TOOL_TO_PROVIDER.get(tools[0], 'universal') if tools else 'universal'
    
    category = p.get('category', 'other')
    sub = p.get('sub', '')
    full_category = f"{category}.{sub}" if sub else category
    
    fa_text = p.get('fa', '')
    variables = extract_variables(fa_text)
    
    # tags: فارسی + tools
    tags = list(p.get('tags', []))
    
    return {
        "id": f"fa-{category}-{p.get('slug', '')}",
        "provider": provider,
        "model": ", ".join(tools) if tools else "universal",
        "modality": "text-to-text",
        "category": full_category,
        "title": p.get('title', ''),
        "prompt": fa_text,
        "variables": variables,
        "tags": tags,
        "source_ids": ["diba7star_persian_prompts"]
    }

def main():
    with open(SOURCE, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    source_prompts = data.get('prompts', [])
    total = len(source_prompts)
    
    if LIMIT > 0:
        source_prompts = source_prompts[:LIMIT]
        print(f"🔸 تست با {LIMIT} پرامپت از {total}")
    else:
        print(f"🔸 تبدیل همه {total} پرامپت")
    
    converted = [convert_prompt(p) for p in source_prompts]
    
    # حذف تکراری‌ها بر اساس id
    seen = set()
    unique = []
    for c in converted:
        if c['id'] not in seen:
            seen.add(c['id'])
            unique.append(c)
    
    output = {
        "schema_version": "1.0",
        "dataset_version": "0.1.0",
        "collected_at": "2026-10-13",
        "description": "Persian prompts from diba7star/awesome-persian-prompts (CC0)",
        "sources": [
            {
                "id": "diba7star_persian_prompts",
                "provider": "diba7star",
                "url": "https://github.com/diba7star/awesome-persian-prompts",
                "note": "Awesome Persian Prompts — CC0 licensed, short AI prompts in Persian/English/Arabic"
            }
        ],
        "prompts": unique
    }
    
    with open(TARGET, 'w', encoding='utf-8') as f:
        json.dump(output, f, ensure_ascii=False, indent=2)
    
    print(f"✅ تبدیل شد: {len(unique)} پرامپت → {TARGET}")
    print(f"📦 حجم فایل: {len(json.dumps(output, ensure_ascii=False))} بایت")

if __name__ == '__main__':
    main()
