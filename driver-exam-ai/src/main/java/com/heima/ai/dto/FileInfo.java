package com.heima.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 聊天附件信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String documentId;
    private String fileName;
    private String originalFileUrl;
    private String conversionFileUrl;
}
