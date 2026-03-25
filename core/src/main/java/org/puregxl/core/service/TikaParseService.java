package org.puregxl.core.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.puregxl.core.dto.ParseResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class TikaParseService {

    /**
     * Tika 实例（用于简单操作，如 MIME 检测）
     */
    private final Tika tika = new Tika();

    /**
     * 自动检测解析器
     */
    private final Parser parser = new AutoDetectParser();

    /**
     * 最大文本长度限制（-1 表示无限制，但可能导致内存问题）
     * 这里设置为 10MB 字符
     */
    private static final int MAX_TEXT_LENGTH = 10 * 1024 * 1024;

    /**
     * 单个切块的最大字符数。
     */
    private static final int CHUNK_SIZE = 800;

    /**
     * 相邻切块的重叠字符数，便于保持上下文连续。
     */
    private static final int CHUNK_OVERLAP = 100;

    private static final Set<String> COMMON_HEADINGS = Set.of(
            "教育背景",
            "实习经验",
            "项目经历",
            "项目",
            "项目描述",
            "主要职责与技术贡献",
            "核心亮点",
            "技能",
            "专业技能",
            "自我评价",
            "求职意向",
            "个人信息",
            "工作经历"
    );

    /**
     * 解析文件，提取文本和元数据
     *
     * @param file 上传的文件
     * @return 解析结果
     */
    public ParseResult parseFile(MultipartFile file) {
        // 1. 基本校验
        if (file == null || file.isEmpty()) {
            return ParseResult.failure("文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        log.info("开始解析文件: {}, 大小: {} bytes", originalFilename, file.getSize());

        try (InputStream inputStream = file.getInputStream()) {

            // 2. 检测 MIME 类型
            // 注意：这里需要重新获取流，因为检测会消费流
            String mimeType;
            try (InputStream detectStream = file.getInputStream()) {
                mimeType = tika.detect(detectStream, originalFilename);
            }
            log.info("检测到 MIME 类型: {}", mimeType);

            // 3. 准备解析器组件
            // BodyContentHandler: 用于接收解析出的文本内容
            // 参数 MAX_TEXT_LENGTH 限制最大文本长度，防止内存溢出
            BodyContentHandler handler = new BodyContentHandler(MAX_TEXT_LENGTH);

            // Metadata: 用于存储元数据
            Metadata metadata = new Metadata();
            // 设置文件名，帮助解析器识别
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, originalFilename);

            // ParseContext: 解析上下文，可以配置额外选项
            ParseContext context = new ParseContext();

            // 4. 执行解析
            try (InputStream parseStream = file.getInputStream()) {
                parser.parse(parseStream, handler, metadata, context);
            }

            // 5. 获取解析结果
            String content = handler.toString();

            // 6. 清洗文本（去除多余空白）
            content = cleanText(content);

            // 7. 提取元数据
            Map<String, String> metadataMap = extractMetadata(metadata);

            // 8. 检查解析质量
            if (content.isEmpty()) {
                log.warn("文件 {} 解析结果为空，可能是扫描件或加密文档", originalFilename);
                return ParseResult.failure("解析结果为空，可能是扫描件或加密文档");
            }

            List<String> chunks = splitIntoChunks(content);

            log.info("文件 {} 解析成功，提取文本长度: {}, 切块数量: {}", originalFilename, content.length(), chunks.size());
            return ParseResult.success(mimeType, content, metadataMap, chunks);

        } catch (IOException e) {
            log.error("读取文件失败: {}", originalFilename, e);
            return ParseResult.failure("读取文件失败: " + e.getMessage());

        } catch (TikaException e) {
            log.error("Tika 解析失败: {}", originalFilename, e);
            return ParseResult.failure("文档解析失败: " + e.getMessage());

        } catch (SAXException e) {
            log.error("XML 解析失败: {}", originalFilename, e);
            return ParseResult.failure("文档结构解析失败: " + e.getMessage());

        } catch (Exception e) {
            log.error("未知错误: {}", originalFilename, e);
            return ParseResult.failure("解析过程中发生未知错误: " + e.getMessage());
        }
    }

    /**
     * 仅检测文件的 MIME 类型
     *
     * @param file 上传的文件
     * @return MIME 类型字符串
     */
    public String detectMimeType(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return tika.detect(inputStream, file.getOriginalFilename());
        }
    }

    /**
     * 清洗文本内容
     * - 将多个连续空白字符替换为单个空格
     * - 将多个连续换行替换为最多两个换行（保留段落）
     * - 去除首尾空白
     */
    private String cleanText(String text) {
        if (text == null) {
            return "";
        }

        return text
                // 将 \r\n 统一为 \n
                .replaceAll("\\r\\n", "\n")
                // 将 \r 统一为 \n
                .replaceAll("\\r", "\n")
                // 去除每行首尾的空格
                .replaceAll("(?m)^[ \\t]+|[ \\t]+$", "")
                // 将 3 个及以上连续换行替换为 2 个换行
                .replaceAll("\\n{3,}", "\n\n")
                // 将多个连续空格/制表符替换为单个空格
                .replaceAll("[ \\t]+", " ")
                // 去除首尾空白
                .trim();
    }

    /**
     * 从 Metadata 对象提取元数据为 Map
     */
    private Map<String, String> extractMetadata(Metadata metadata) {
        Map<String, String> result = new HashMap<>();

        for (String name : metadata.names()) {
            String value = metadata.get(name);
            if (value != null && !value.isEmpty()) {
                result.put(name, value);
            }
        }

        return result;
    }

    /**
     * 将解析后的文本按段落优先切块；如果段落过长，再按固定窗口拆分。
     */
    private List<String> splitIntoChunks(String content) {
        List<String> chunks = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return chunks;
        }

        List<Section> sections = splitSections(content);
        for (Section section : sections) {
            String block = section.toText();
            if (block.length() <= CHUNK_SIZE) {
                chunks.add(block);
            } else {
                splitLongText(block, chunks);
            }
        }

        return chunks;
    }

    private List<Section> splitSections(String content) {
        List<Section> sections = new ArrayList<>();
        String[] lines = content.split("\\n");
        String currentHeading = "正文";
        List<String> currentLines = new ArrayList<>();

        for (String line : lines) {
            String normalized = line == null ? "" : line.trim();
            if (normalized.isEmpty()) {
                continue;
            }

            if (isHeadingLine(normalized)) {
                addSection(sections, currentHeading, currentLines);
                currentHeading = normalized;
                currentLines = new ArrayList<>();
                continue;
            }

            currentLines.add(normalized);
        }

        addSection(sections, currentHeading, currentLines);
        return mergeSmallSections(sections);
    }

    private boolean isHeadingLine(String line) {
        if (COMMON_HEADINGS.contains(line)) {
            return true;
        }

        if (line.length() > 20) {
            return false;
        }

        if (line.contains("：") || line.contains(":") || line.contains("http")) {
            return false;
        }

        return line.matches("^[\\p{IsHan}A-Za-z0-9（）()\\- /]+$")
                && !line.matches(".*\\d{4}.*");
    }

    private void addSection(List<Section> sections, String heading, List<String> lines) {
        if (lines.isEmpty()) {
            return;
        }
        sections.add(new Section(heading, new ArrayList<>(lines)));
    }

    private List<Section> mergeSmallSections(List<Section> sections) {
        List<Section> merged = new ArrayList<>();
        Section pending = null;

        for (Section section : sections) {
            if (pending == null) {
                pending = section;
                continue;
            }

            if (pending.length() < CHUNK_SIZE / 3) {
                pending = pending.merge(section);
            } else {
                merged.add(pending);
                pending = section;
            }
        }

        if (pending != null) {
            merged.add(pending);
        }

        return merged;
    }

    private void splitLongText(String text, List<String> chunks) {
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            chunks.add(text.substring(start, end).trim());
            if (end == text.length()) {
                break;
            }
            start = Math.max(end - CHUNK_OVERLAP, start + 1);
        }
    }

    private void flushChunk(List<String> chunks, StringBuilder currentChunk) {
        if (currentChunk.length() == 0) {
            return;
        }
        chunks.add(currentChunk.toString().trim());
        currentChunk.setLength(0);
    }

    private record Section(String heading, List<String> lines) {
        String toText() {
            if ("正文".equals(heading)) {
                return String.join("\n", lines).trim();
            }
            return (heading + "\n" + String.join("\n", lines)).trim();
        }

        int length() {
            return toText().length();
        }

        Section merge(Section other) {
            List<String> merged = new ArrayList<>(this.lines);
            if (!"正文".equals(other.heading)) {
                merged.add(other.heading);
            }
            merged.addAll(other.lines);
            return new Section(this.heading, merged);
        }
    }
}
