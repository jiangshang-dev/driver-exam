package com.heima.ai.factory;

import com.heima.ai.dto.AiModelOptionDto;
import com.heima.common.exception.RenException;
import io.agentscope.core.embedding.EmbeddingModel;
import io.agentscope.core.message.TextBlock;

import java.lang.reflect.Array;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;

/**
 * AI模型工厂
 */
public class AiModelFactory {

    private static final int DEFAULT_EMBEDDING_DIMENSIONS = 1024;

    public static EmbeddingModel createAgentScopeEmbeddingModel(AiModelOptionDto options) {
        assertNotEmpty("请设置模型参数", options);
        String apiKey = options.getApiKey();
        String baseUrl = options.getBaseUrl();
        String modelName = options.getModelName();

        assertNotEmpty("modelName不能为空", modelName);
        assertNotEmpty("baseUrl不能为空", baseUrl);

        Duration timeout = Duration.ofSeconds(60);
        if (options.getTimeout() != null && options.getTimeout() > 0) {
            timeout = Duration.ofSeconds(options.getTimeout());
        }

        return new CompatibleTextEmbedding(apiKey, baseUrl, modelName, resolveDimensions(modelName), timeout);
    }

    public static double[] embedText(EmbeddingModel model, String text) {
        TextBlock block = TextBlock.builder().text(text).build();
        return model.embed(block).blockOptional().orElseThrow(() -> new RenException("向量模型返回空结果"));
    }

    private static int resolveDimensions(String modelName) {
        return DEFAULT_EMBEDDING_DIMENSIONS;
    }

    public static void assertNotEmpty(String msg, Object obj) {
        if (isObjectEmpty(obj)) {
            throw new RenException(msg);
        }
    }

    public static boolean isObjectEmpty(Object obj) {
        if (null == obj) {
            return true;
        }

        if (obj instanceof CharSequence) {
            return isEmpty(obj);
        } else if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        } else if (obj instanceof Iterable) {
            return isObjectEmpty(((Iterable<?>) obj).iterator());
        } else if (obj instanceof Iterator) {
            return !((Iterator<?>) obj).hasNext();
        } else if (isArray(obj)) {
            return 0 == Array.getLength(obj);
        }
        return false;
    }

    public static boolean isArray(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj.getClass().isArray();
    }

    public static boolean isEmpty(Object object) {
        if (object == null) {
            return true;
        }
        if ("".equals(object)) {
            return true;
        }
        return "null".equals(object);
    }
}
