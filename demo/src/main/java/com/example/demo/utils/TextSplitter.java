package com.example.demo.utils;

import java.util.ArrayList;
import java.util.List;

public class TextSplitter {

    /**
     * 简单的文本切分器
     * @param text 原始大文本
     * @param maxChunkSize 每个块的最大字符数 (推荐 500-1000)
     * @param overlap 重叠字符数 (推荐 100-200)
     * @return 切分后的文本列表
     */
    public static List<String> splitText(String text, int maxChunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return chunks;
        }

        // 1. 简单清洗：把连续的空格、换行符替换成单个空格，避免格式混乱
        // (这一步看需求，保留换行符有时候对代码类文档有意义，但对普通文本建议清洗)
        // text = text.replaceAll("\\s+", " ");

        int len = text.length();
        int start = 0;

        while (start < len) {
            // 计算当前块的结束位置
            int end = Math.min(start + maxChunkSize, len);

            // 优化：尽量不要切断单词或句子（简单做法是往回找最近的标点或空格）
            if (end < len) {
                // 往回找最近的换行符、句号或空格，防止把单词切两半
                int lastSpace = -1;
                // 尝试找换行符
                int lastNewLine = text.lastIndexOf('\n', end);
                if (lastNewLine > start) {
                    end = lastNewLine;
                } else {
                    // 没换行符，找句号
                    int lastPeriod = text.lastIndexOf('。', end);
                    if (lastPeriod > start) {
                        end = lastPeriod + 1; // 包含句号
                    } else {
                        // 实在不行找空格
                        lastSpace = text.lastIndexOf(' ', end);
                        if (lastSpace > start) {
                            end = lastSpace;
                        }
                    }
                }
            }

            // 截取
            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            // 下一块的起始位置 = 当前结束位置 - 重叠量
            // 如果遇到死循环（start没动），强制移动
            int nextStart = end - overlap;
            if (nextStart <= start) {
                nextStart = end; // 防止重叠过大导致死循环
            }
            start = nextStart;
        }

        return chunks;
    }
}