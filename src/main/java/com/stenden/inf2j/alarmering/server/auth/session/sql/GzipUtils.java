package com.stenden.inf2j.alarmering.server.auth.session.sql;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class GzipUtils {

    private GzipUtils() {
        throw new UnsupportedOperationException("No instances");
    }

    private static final byte GZIP_SIGNATURE_BYTE1 = (byte) GZIPInputStream.GZIP_MAGIC;
    private static final byte GZIP_SIGNATURE_BYTE2 = (byte) (GZIPInputStream.GZIP_MAGIC >> 8);

    public static boolean isCompressed(byte[] bytes){
        if(bytes.length < 2){
            return false;
        }
        return bytes[0] == GZIP_SIGNATURE_BYTE1 && bytes[1] == GZIP_SIGNATURE_BYTE2;
    }

    public static byte[] compress(byte[] bytes){
        try{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gos = new GZIPOutputStream(bos);
            gos.write(bytes);
            gos.finish();
            byte[] ret = bos.toByteArray();
            gos.close();
            bos.close();
            return ret;
        }catch(IOException e){
            //Should never happen
            throw new RuntimeException(e);
        }
    }

    public static byte[] uncompress(byte[] bytes) {
        try{
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            GZIPInputStream gis = new GZIPInputStream(bis);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] tmp = new byte[4096];
            int len;
            do {
                len = gis.read(tmp);
                if(len > 0){
                    bos.write(tmp, 0, len);
                }
            }while (len > 0);
            byte[] res = bos.toByteArray();
            bos.close();

            gis.close();
            bis.close();
            return res;
        }catch(IOException e){
            //Should never happen
            throw new RuntimeException(e);
        }
    }

    public static byte[] mayUncompress(byte[] bytes) {
        if(isCompressed(bytes)){
            return uncompress(bytes);
        }else{
            return bytes;
        }
    }
}
