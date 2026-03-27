package com.mtp.config.center.server.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtp.config.center.server.MessageContext;
import com.mtp.core.model.ThreadPoolStatus;
import com.mtp.core.netty.MessageRequest;
import com.mtp.core.netty.MessageType;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.List;

/**
 * 上报状态处理器，处理客户端上报线程池状态的请求
 */
public class ReportStatusesHandler extends AbstractMessageHandler {

    public ReportStatusesHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public MessageType getType() {
        return MessageType.REPORT_STATUSES;
    }

    @Override
    public String handle(ChannelHandlerContext ctx, MessageRequest request, MessageContext context) throws IOException {
        List<ThreadPoolStatus> statuses = objectMapper.convertValue(request.payload, new TypeReference<List<ThreadPoolStatus>>() {
        });
        for (ThreadPoolStatus status : statuses) {
            if (status != null) {
                context.getConfigCenterService().reportStatus(status);
                context.getConfigCenterService().updateClientReportTime(status.getInstanceId());
            }
        }
        return buildResponse(request.correlationId, MessageType.REPORT_STATUS, true);
    }
}