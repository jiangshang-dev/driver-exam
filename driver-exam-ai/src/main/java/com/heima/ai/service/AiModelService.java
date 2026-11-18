package com.heima.ai.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.heima.ai.dao.AiModelDao;
import com.heima.ai.entity.AiModelEntity;
import com.heima.common.exception.RenException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI 模型配置服务
 */
@Slf4j
@Service
public class AiModelService {

    @Resource
    private AiModelDao aiModelDao;

    public AiModelEntity getById(Long id) {
        return id == null ? null : aiModelDao.selectById(id);
    }

    public void save(AiModelEntity entity) {
        entity.setId(null);
        entity.setIsActive(0);
        entity.setIsDeleted(0);
        entity.setGmtCreate(LocalDateTime.now());
        aiModelDao.insert(entity);
    }

    public void update(AiModelEntity entity) {
        entity.setGmtModified(LocalDateTime.now());
        aiModelDao.updateById(entity);
    }

    public void delete(List<Long> ids) {
        aiModelDao.deleteBatchIds(ids);
    }

    /**
     * 获取指定类型下已启用的模型（同一类型只允许一个）
     */
    public AiModelEntity getActive(String modelType) {
        QueryWrapper<AiModelEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("model_type", modelType).eq("is_active", 1)
                .orderByAsc("sort_order").last("LIMIT 1");
        return aiModelDao.selectOne(wrapper);
    }

    /**
     * 启用模型：同一类型下的其它模型自动停用
     */
    @Transactional(rollbackFor = Exception.class)
    public void activate(Long id) {
        AiModelEntity entity = getById(id);
        if (entity == null) {
            throw new RenException("模型不存在");
        }
        if (!"LLM".equals(entity.getModelType())) {
            // 普通大模型同一时间只能启用一个，向量/TTS 也按类型各保留一个启用项
            log.debug("切换启用模型, type={}, id={}", entity.getModelType(), id);
        }
        UpdateWrapper<AiModelEntity> disable = new UpdateWrapper<>();
        disable.eq("model_type", entity.getModelType()).eq("is_active", 1).ne("id", id)
                .set("is_active", 0).set("gmt_modified", LocalDateTime.now());
        aiModelDao.update(null, disable);

        AiModelEntity update = new AiModelEntity();
        update.setId(id);
        update.setIsActive(1);
        update.setGmtModified(LocalDateTime.now());
        aiModelDao.updateById(update);
    }

    /**
     * 测试模型是否可用
     */
    public String test(Long id) {
        AiModelEntity model = getById(id);
        if (model == null) {
            throw new RenException("模型不存在");
        }
        if (StrUtil.isBlank(model.getBaseUrl()) || StrUtil.isBlank(model.getModelName())) {
            throw new RenException("api地址和模型名称不能为空");
        }
        String baseUrl = StrUtil.removeSuffix(model.getBaseUrl().trim(), "/");
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
        try {
            HttpRequest request;
            if ("EMBEDDING".equals(model.getModelType())) {
                String body = JSONUtil.toJsonStr(Map.of(
                        "model", model.getModelName(),
                        "input", "ping"
                ));
                request = buildJsonRequest(baseUrl + "/embeddings", model.getApiKey(), body);
            } else if ("TTS".equals(model.getModelType())) {
                request = HttpRequest.newBuilder(URI.create(baseUrl))
                        .timeout(Duration.ofSeconds(8))
                        .header("Authorization", "Bearer " + StrUtil.blankToDefault(model.getApiKey(), ""))
                        .GET().build();
            } else {
                String body = JSONUtil.toJsonStr(Map.of(
                        "model", model.getModelName(),
                        "messages", List.of(Map.of("role", "user", "content", "ping")),
                        "max_tokens", 1,
                        "stream", false
                ));
                request = buildJsonRequest(baseUrl + "/chat/completions", model.getApiKey(), body);
            }
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new RenException("模型不可用（HTTP " + response.statusCode() + "）："
                        + StrUtil.sub(response.body(), 0, 200));
            }
            return "模型可用（HTTP " + response.statusCode() + "）";
        } catch (RenException e) {
            throw e;
        } catch (Exception e) {
            log.warn("模型连通性测试失败, id={}", id, e);
            throw new RenException("模型不可用：" + e.getMessage());
        }
    }

    private HttpRequest buildJsonRequest(String url, String apiKey, String body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        if (StrUtil.isNotBlank(apiKey)) {
            builder.header("Authorization", "Bearer " + apiKey);
        }
        return builder.build();
    }
}
