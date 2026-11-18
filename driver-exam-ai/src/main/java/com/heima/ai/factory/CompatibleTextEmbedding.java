package com.heima.ai.factory;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.heima.common.exception.RenException;
import io.agentscope.core.embedding.EmbeddingException;
import io.agentscope.core.embedding.EmbeddingModel;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.TextBlock;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CompatibleTextEmbedding implements EmbeddingModel {

    private final String apiKey;
    private final String embeddingsUrl;
    private final String modelName;
    private final Integer dimensions;
    private final HttpClient httpClient;
    private final AtomicInteger resolvedDimensions = new AtomicInteger(-1);

    public CompatibleTextEmbedding(String apiKey, String baseUrl, String modelName,
                                   Integer dimensions, Duration timeout) {
        this.apiKey = StrUtil.blankToDefault(apiKey, "EMPTY");
        this.embeddingsUrl = normalizeEmbeddingsUrl(baseUrl);
        this.modelName = modelName;
        this.dimensions = dimensions;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout != null ? timeout : Duration.ofSeconds(60))
                .build();
    }

    static String normalizeEmbeddingsUrl(String baseUrl) {
        String url = StrUtil.removeSuffix(baseUrl.trim(), "/");
        if (url.endsWith("/embeddings")) {
            return url;
        }
        if (url.endsWith("/v1")) {
            return url + "/embeddings";
        }
        return url + "/v1/embeddings";
    }

    @Override
    public Mono<double[]> embed(ContentBlock block) {
        if (block == null) {
            return Mono.error(new EmbeddingException("ContentBlock cannot be null", modelName, "openai-compatible"));
        }
        if (!(block instanceof TextBlock textBlock)) {
            return Mono.error(new EmbeddingException(
                    "OpenAiCompatibleTextEmbedding only supports TextBlock, but got: "
                            + block.getClass().getSimpleName(),
                    modelName, "openai-compatible"));
        }
        String text = textBlock.getText();
        if (StrUtil.isBlank(text)) {
            return Mono.error(new EmbeddingException(
                    "TextBlock text cannot be null or empty", modelName, "openai-compatible"));
        }
        return Mono.fromCallable(() -> embedText(text))
                .onErrorMap(e -> {
                    if (e instanceof EmbeddingException) {
                        return e;
                    }
                    if (e instanceof RenException se) {
                        return new EmbeddingException(se.getMessage(), se, modelName, "openai-compatible");
                    }
                    return new EmbeddingException(
                            "Failed to generate embedding: " + e.getMessage(), e, modelName, "openai-compatible");
                });
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public int getDimensions() {
        if (shouldSendDimensions() && dimensions != null && dimensions > 0) {
            return dimensions;
        }
        int cached = resolvedDimensions.get();
        if (cached > 0) {
            return cached;
        }
        embedText("ping");
        return Math.max(resolvedDimensions.get(), 1);
    }

    private double[] embedText(String text) {
        JSONObject body = new JSONObject();
        body.set("model", modelName);
        body.set("input", text);
        if (shouldSendDimensions()) {
            body.set("dimensions", dimensions);
        }

        log.debug("Embedding request: url={}, model={}", embeddingsUrl, modelName);
        String responseBody = doPost(body.toString());
        JSONArray data = extractDataArray(responseBody);

        JSONObject item = data.getJSONObject(0);
        JSONArray embeddingArray = item.getJSONArray("embedding");
        double[] vector = toDoubleArray(embeddingArray);
        resolvedDimensions.compareAndSet(-1, vector.length);
        return vector;
    }

    private boolean shouldSendDimensions() {
        return dimensions != null && dimensions > 0
                && StrUtil.containsIgnoreCase(modelName, "text-embedding-3");
    }

    private String doPost(String jsonBody) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(embeddingsUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));
            if (StrUtil.isNotBlank(apiKey) && !"EMPTY".equals(apiKey)) {
                builder.header("Authorization", "Bearer " + apiKey);
            }
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            if (response.statusCode() >= 400) {
                throw new RenException(parseErrorMessage(body, response.statusCode()));
            }
            return body;
        } catch (RenException e) {
            throw e;
        } catch (Exception e) {
            throw new RenException("调用向量模型失败: " + e.getMessage());
        }
    }

    private JSONArray extractDataArray(String responseBody) {
        JSONObject json = JSONUtil.parseObj(responseBody);
        if (json.containsKey("error")) {
            throw new RenException(parseErrorMessage(responseBody, 200));
        }
        JSONArray data = json.getJSONArray("data");
        if (data == null || data.isEmpty()) {
            throw new RenException(
                    "向量模型响应缺少 data 字段，请确认 baseUrl/modelName 正确且 embedding 模型已加载。响应: "
                            + StrUtil.sub(responseBody, 0, 500));
        }
        return data;
    }

    private String parseErrorMessage(String body, int statusCode) {
        try {
            JSONObject json = JSONUtil.parseObj(body);
            if (json.containsKey("error")) {
                Object error = json.get("error");
                if (error instanceof JSONObject errObj) {
                    return StrUtil.format("向量模型返回错误({}): {}", statusCode,
                            errObj.getStr("message", errObj.toString()));
                }
                return StrUtil.format("向量模型返回错误({}): {}", statusCode, error);
            }
        } catch (Exception ignored) {
            // fall through
        }
        return StrUtil.format("向量模型返回错误({}): {}", statusCode, StrUtil.sub(body, 0, 500));
    }

    private static double[] toDoubleArray(JSONArray array) {
        double[] result = new double[array.size()];
        for (int i = 0; i < array.size(); i++) {
            result[i] = array.getDouble(i);
        }
        return result;
    }
}
