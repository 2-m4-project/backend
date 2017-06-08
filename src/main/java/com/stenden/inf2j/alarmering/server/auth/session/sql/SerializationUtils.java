package com.stenden.inf2j.alarmering.server.auth.session.sql;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.base64.Base64Dialect;
import io.netty.util.CharsetUtil;

import java.util.Optional;

public final class SerializationUtils {

    private SerializationUtils() {
        throw new UnsupportedOperationException("No instances");
    }

    public static Optional<byte[]> bytesFromUrlSafeBase64(String input){
        input = addUrlSafeBase64Padding(input);
        ByteBuf buffer = Unpooled.copiedBuffer(input, CharsetUtil.UTF_8);
        try {
            ByteBuf decoded = Base64.decode(buffer, Base64Dialect.URL_SAFE);
            byte[] bytes = toBytes(decoded);
            decoded.release();
            return Optional.of(bytes);
        }catch (Exception e){
            return Optional.empty();
        }finally {
            buffer.release();
        }
    }

    public static String bytesToUrlSafeBase64(byte[] input){
        ByteBuf inBuffer = Unpooled.wrappedBuffer(input);
        ByteBuf outBuffer = Base64.encode(inBuffer, false, Base64Dialect.URL_SAFE);
        String base64 = outBuffer.toString(CharsetUtil.UTF_8);
        outBuffer.release();
        inBuffer.release();
        return base64.replace("=", "");
    }

    private static String addUrlSafeBase64Padding(String input){
        int mod = input.length() % 4;
        if(mod == 1){
            return input + "===";
        }else if(mod == 2){
            return input + "==";
        }else if(mod == 3){
            return input + "=";
        }else {
            return input;
        }
    }

    private static byte[] toBytes(ByteBuf buf){
        int len = buf.readableBytes();
        if(len == 0){
            return new byte[0];
        }

        byte[] ret = new byte[len];
        if(buf.hasArray()){
            System.arraycopy(buf.array(), 0, ret, 0, len);
        }else{
            buf.markReaderIndex().readBytes(ret).resetReaderIndex();
        }
        return ret;
    }
}
