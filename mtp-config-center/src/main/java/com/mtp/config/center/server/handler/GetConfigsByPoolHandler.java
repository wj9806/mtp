package com.mtp.config.center.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.server.MessageContext;
import com.mtp.config.center.service.ConfigCenterService;
import com.mtp.core.model.ThreadPoolConfig;
import com.mtp.core.netty.MessageRequest;
import com.mtp.core.netty.MessageType;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 获取线程池配置处理器，处理客户端查询线程池配置的请求
 */
public class GetConfigsByPoolHandler extends AbstractMessageHandler {

    public GetConfigsByPoolHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public MessageType getType() {
        return MessageType.GET_CONFIGS_BY_POOL;
    }

    @Override
    public String handle(ChannelHandlerContext ctx, MessageRequest request, MessageContext context) throws IOException {
        Map<String, Object> params = toParams(request.payload);
        if (params == null) {
            return buildErrorResponse(request.correlationId, "Invalid payload");
        }

        String instanceId = (String) params.get("instanceId");
        Object name = params.get("poolName");
        String poolName = name == null ? null : (String) name;

        if (instanceId == null) {
            return buildErrorResponse(request.correlationId, "Missing required parameters");
        }

        ConfigCenterService configCenterService = context.getConfigCenterService();
        List<ThreadPoolConfig> configs = poolName == null
                ? configCenterService.findConfigListByInstanceId(instanceId)
                : Collections.singletonList(configCenterService.findConfig(instanceId, poolName));
        return buildResponse(request.correlationId, MessageType.GET_CONFIGS_BY_POOL, configs);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toParams(Object payload) {
        if (payload instanceof Map) {
            return (Map<String, Object>) payload;
        }
        return null;
    }

}