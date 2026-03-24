package com.mtp.config.center.netty.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.netty.MessageContext;
import com.mtp.core.model.ThreadPoolStatus;
import com.mtp.core.netty.MessageRequest;
import com.mtp.core.netty.MessageType;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

/**
 * 上报状态处理器，处理客户端上报线程池状态的请求
 */
public class ReportStatusHandler extends AbstractMessageHandler {

    public ReportStatusHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public MessageType getType() {
        return MessageType.REPORT_STATUS;
    }

    @Override
    public String handle(ChannelHandlerContext ctx, MessageRequest request, MessageContext context) throws IOException {
        ThreadPoolStatus status = parsePayload(request.payload, ThreadPoolStatus.class);
        if (status != null) {
            context.getConfigCenterService().reportStatus(status);
            context.getConfigCenterService().updateClientReportTime(status.getInstanceId());
            return buildResponse(request.correlationId, MessageType.REPORT_STATUS, true);
        }
        return buildErrorResponse(request.correlationId, "Invalid status payload");
    }
}