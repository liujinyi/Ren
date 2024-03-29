package org.calf.reader.novel.help;

import android.text.TextUtils;

import org.calf.reader.novel.utils.EncodingDetect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import static android.text.TextUtils.isEmpty;

public class EncodeConverter extends Converter.Factory {
    private String encode;

    private EncodeConverter() {

    }

    private EncodeConverter(String encode) {
        this.encode = encode;
    }

    public static EncodeConverter create() {
        return new EncodeConverter();
    }

    public static EncodeConverter create(String en) {
        return new EncodeConverter(en);
    }

    @Override
    public Converter<ResponseBody, String> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return value -> {
            byte[] responseBytes = UTF8BOMFighter.removeUTF8BOM(value.bytes());
            if (!TextUtils.isEmpty(encode)) {
                return new String((responseBytes), encode);
            }
            String charsetStr;
            MediaType mediaType = value.contentType();
            //根据http头判断
            if (mediaType != null) {
                Charset charset = mediaType.charset();
                if (charset != null) {
                    charsetStr = charset.displayName();
                    if (!isEmpty(charsetStr)) {
                        return new String(responseBytes, Charset.forName(charsetStr));
                    }
                }
            }
            //根据内容判断
            charsetStr = EncodingDetect.getEncodeInHtml(responseBytes);
            return new String(responseBytes, Charset.forName(charsetStr));
        };
    }
}
