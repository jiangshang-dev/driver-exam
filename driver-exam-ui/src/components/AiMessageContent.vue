<template>
  <div class="ai-message-content" :class="{ dark }" v-html="html"></div>
</template>

<script lang="ts" setup>
import { computed } from "vue";

const props = defineProps<{
  content?: string;
  dark?: boolean;
}>();

const escapeHtml = (text: string) =>
  text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");

const renderMarkdown = (raw: string) => {
  if (!raw) {
    return "";
  }

  // 先提取代码块，避免内部内容被后续规则处理
  const codeBlocks: string[] = [];
  let text = raw.replace(/```[a-zA-Z0-9_-]*\n?([\s\S]*?)```/g, (_match, code: string) => {
    const index = codeBlocks.length;
    codeBlocks.push(`<pre class="md-code"><code>${escapeHtml(code.replace(/\n$/, ""))}</code></pre>`);
    return `\u0000CODE${index}\u0000`;
  });

  text = escapeHtml(text);

  // 行内代码
  text = text.replace(/`([^`\n]+)`/g, '<code class="md-inline-code">$1</code>');
  // 标题
  text = text.replace(/^#### (.*)$/gm, "<h4>$1</h4>");
  text = text.replace(/^### (.*)$/gm, "<h4>$1</h4>");
  text = text.replace(/^## (.*)$/gm, "<h3>$1</h3>");
  text = text.replace(/^# (.*)$/gm, "<h2>$1</h2>");
  // 粗体、斜体
  text = text.replace(/\*\*([^*\n]+)\*\*/g, "<strong>$1</strong>");
  text = text.replace(/(^|[^*])\*([^*\n]+)\*/g, "$1<em>$2</em>");
  // 链接
  text = text.replace(/\[([^\]]+)\]\((https?:\/\/[^\s)]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>');

  // 列表与段落
  const lines = text.split("\n");
  const result: string[] = [];
  let inUl = false;
  let inOl = false;

  const closeLists = () => {
    if (inUl) {
      result.push("</ul>");
      inUl = false;
    }
    if (inOl) {
      result.push("</ol>");
      inOl = false;
    }
  };

  for (const line of lines) {
    if (/^\s*[-*]\s+/.test(line)) {
      if (inOl) {
        result.push("</ol>");
        inOl = false;
      }
      if (!inUl) {
        result.push("<ul>");
        inUl = true;
      }
      result.push(`<li>${line.replace(/^\s*[-*]\s+/, "")}</li>`);
    } else if (/^\s*\d+\.\s+/.test(line)) {
      if (inUl) {
        result.push("</ul>");
        inUl = false;
      }
      if (!inOl) {
        result.push("<ol>");
        inOl = true;
      }
      result.push(`<li>${line.replace(/^\s*\d+\.\s+/, "")}</li>`);
    } else if (/^\s*<(h2|h3|h4|pre)/.test(line)) {
      closeLists();
      result.push(line);
    } else if (line.trim() === "") {
      closeLists();
    } else {
      closeLists();
      result.push(`<p>${line}</p>`);
    }
  }
  closeLists();

  // 还原代码块
  return result.join("").replace(/\u0000CODE(\d+)\u0000/g, (_match, index: string) => codeBlocks[Number(index)] || "");
};

const html = computed(() => renderMarkdown(props.content || ""));
</script>

<style scoped>
.ai-message-content {
  line-height: 1.7;
  word-break: break-word;
  color: #303133;
}
.ai-message-content.dark {
  color: #e5e7eb;
}
.ai-message-content :deep(p) {
  margin: 0 0 8px;
}
.ai-message-content :deep(p:last-child) {
  margin-bottom: 0;
}
.ai-message-content :deep(h2),
.ai-message-content :deep(h3),
.ai-message-content :deep(h4) {
  margin: 12px 0 8px;
  font-weight: 600;
}
.ai-message-content :deep(ul),
.ai-message-content :deep(ol) {
  margin: 4px 0 8px;
  padding-left: 22px;
}
.ai-message-content :deep(code) {
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
}
.ai-message-content :deep(.md-inline-code) {
  background: rgba(127, 127, 127, 0.15);
  border-radius: 3px;
  padding: 1px 5px;
}
.ai-message-content :deep(.md-code) {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 10px 12px;
  overflow-x: auto;
  margin: 8px 0;
  white-space: pre-wrap;
}
.ai-message-content.dark :deep(.md-code) {
  background: #1f2937;
}
.ai-message-content :deep(a) {
  color: #409eff;
  text-decoration: none;
}
</style>
