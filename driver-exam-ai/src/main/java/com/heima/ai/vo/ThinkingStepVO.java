package com.heima.ai.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 思考步骤
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThinkingStepVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private String content;
}
