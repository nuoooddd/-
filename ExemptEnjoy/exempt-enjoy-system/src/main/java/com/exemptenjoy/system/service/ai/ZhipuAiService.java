package com.exemptenjoy.system.service.ai;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;

@Service
public class ZhipuAiService {

    private static final Logger log = LoggerFactory.getLogger(ZhipuAiService.class);

    @Value("${zhipu.api-key:}")
    private String apiKey;

    @Value("${zhipu.model:glm-4-flash}")
    private String model;

    @Value("${zhipu.base-url:https://open.bigmodel.cn/api/paas/v4}")
    private String baseUrl;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    private final Map<Long, List<Map<String, String>>> sessionHistory = new ConcurrentHashMap<>();

    public String chat(Long userId, String userMessage, String systemPrompt) {
        if (apiKey == null || apiKey.isEmpty() || "YOUR_ZHIPU_API_KEY_HERE".equals(apiKey)) {
            return "智能助手尚未配置API Key，请联系管理员在application.yml中配置zhipu.api-key。";
        }

        List<Map<String, String>> messages = sessionHistory.computeIfAbsent(userId, k -> new ArrayList<>());

        if (systemPrompt != null && !systemPrompt.isEmpty() && messages.isEmpty()) {
            Map<String, String> sys = new HashMap<>();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            messages.add(sys);
        }

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        trimMessages(messages);

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("stream", false);

            Request request = new Request.Builder()
                    .url(baseUrl + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errBody = response.body() != null ? response.body().string() : "unknown";
                    log.error("智谱AI调用失败: status={}, body={}", response.code(), errBody);
                    return "AI服务暂时不可用，请稍后再试。(错误码:" + response.code() + ")";
                }

                String responseBody = response.body().string();
                JSONObject result = JSON.parseObject(responseBody);
                JSONArray choices = result.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    String aiReply = choices.getJSONObject(0).getJSONObject("message").getString("content");
                    Map<String, String> assistantMsg = new HashMap<>();
                    assistantMsg.put("role", "assistant");
                    assistantMsg.put("content", aiReply);
                    messages.add(assistantMsg);
                    return aiReply;
                }
                return "AI未返回有效回复，请重试。";
            }
        } catch (Exception e) {
            log.error("智谱AI调用异常", e);
            return "AI服务连接异常：" + e.getMessage();
        }
    }

    public void chatStream(Long userId, String userMessage, String systemPrompt, Consumer<String> onToken, Consumer<String> onComplete, Consumer<Exception> onError) {
        if (apiKey == null || apiKey.isEmpty() || "YOUR_ZHIPU_API_KEY_HERE".equals(apiKey)) {
            onComplete.accept("智能助手尚未配置API Key，请联系管理员在application.yml中配置zhipu.api-key。");
            return;
        }

        List<Map<String, String>> messages = sessionHistory.computeIfAbsent(userId, k -> new ArrayList<>());

        if (systemPrompt != null && !systemPrompt.isEmpty() && messages.isEmpty()) {
            Map<String, String> sys = new HashMap<>();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            messages.add(sys);
        }

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        trimMessages(messages);

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("stream", true);

            Request request = new Request.Builder()
                    .url(baseUrl + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                    .build();

            StringBuilder fullReply = new StringBuilder();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    log.error("智谱AI流式调用失败", e);
                    onError.accept(e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try (ResponseBody body = response.body()) {
                        if (!response.isSuccessful()) {
                            String errBody = body != null ? body.string() : "unknown";
                            log.error("智谱AI流式调用失败: status={}, body={}", response.code(), errBody);
                            onError.accept(new RuntimeException("AI服务暂时不可用(错误码:" + response.code() + ")"));
                            return;
                        }

                        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(body.byteStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data:")) {
                                String data = line.substring(5).trim();
                                if ("[DONE]".equals(data)) {
                                    break;
                                }
                                try {
                                    JSONObject chunk = JSON.parseObject(data);
                                    JSONArray choices = chunk.getJSONArray("choices");
                                    if (choices != null && !choices.isEmpty()) {
                                        JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                                        if (delta != null && delta.containsKey("content")) {
                                            String content = delta.getString("content");
                                            if (content != null) {
                                                fullReply.append(content);
                                                onToken.accept(content);
                                            }
                                        }
                                    }
                                } catch (Exception parseEx) {
                                    log.warn("解析SSE数据异常: {}", data, parseEx);
                                }
                            }
                        }

                        Map<String, String> assistantMsg = new HashMap<>();
                        assistantMsg.put("role", "assistant");
                        assistantMsg.put("content", fullReply.toString());
                        messages.add(assistantMsg);
                        onComplete.accept(fullReply.toString());
                    } catch (Exception e) {
                        log.error("智谱AI流式读取异常", e);
                        onError.accept(e);
                    }
                }
            });
        } catch (Exception e) {
            log.error("智谱AI流式调用异常", e);
            onError.accept(e);
        }
    }

    private void trimMessages(List<Map<String, String>> messages) {
        if (messages.size() > 22) {
            List<Map<String, String>> trimmed = new ArrayList<>();
            if (!messages.isEmpty() && "system".equals(messages.get(0).get("role"))) {
                trimmed.add(messages.get(0));
                trimmed.addAll(messages.subList(messages.size() - 20, messages.size()));
            } else {
                trimmed.addAll(messages.subList(messages.size() - 20, messages.size()));
            }
            messages.clear();
            messages.addAll(trimmed);
        }
    }

    public void clearSession(Long userId) {
        sessionHistory.remove(userId);
    }
}
