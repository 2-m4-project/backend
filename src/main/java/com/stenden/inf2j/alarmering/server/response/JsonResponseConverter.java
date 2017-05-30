package com.stenden.inf2j.alarmering.server.response;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.CharsetUtil;
import nl.jk5.http2server.api.RequestContext;
import nl.jk5.http2server.api.ResponseConverter;
import nl.jk5.jsonlibrary.JsonElement;

public class JsonResponseConverter implements ResponseConverter {

    @Override
    public ByteBuf convert(Object response, ByteBufAllocator alloc, RequestContext context) {
        if(response instanceof JsonElement){
            String json = response.toString();

            ByteBuf buffer = alloc.buffer(json.length());
            buffer.writeBytes(json.getBytes(CharsetUtil.UTF_8));

            if(!context.responseHeaders().contains(HttpHeaderNames.CONTENT_TYPE)){
                context.responseHeaders().add(HttpHeaderNames.CONTENT_TYPE, "application/json");
            }

            return buffer;
        }
        return null;
    }
}
