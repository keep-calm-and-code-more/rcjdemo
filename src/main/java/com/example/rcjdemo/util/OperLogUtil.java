package com.example.rcjdemo.util;


import com.twitter.chill.KryoInjection;
import org.json4s.Extraction;
import org.json4s.Formats;
import org.json4s.jackson.JsonMethods;
import org.json4s.jackson.Serialization;

/**
 * @author lhc
 * @version 1.0
 * @className OperLogUtil
 * @date 2022年06月15日 11:11
 * @description 描述
 */
public class OperLogUtil {

    private static JsonMethods jsonMethods = org.json4s.jackson.JsonMethods$.MODULE$;
    private static Formats formats = (Formats) Serialization.formats(org.json4s.NoTypeHints$.MODULE$);

    /**
     * 反序列化为Json
     *
     * @param bytes
     * @return
     */
    public static String toJson(byte[] bytes) {
        Object object = KryoInjection.invert(bytes).getOrElse(null);
        return jsonMethods.compact(jsonMethods.render(Extraction.decompose(object, formats), formats));
    }

    /**
     * 反序列化为具体的实例
     *
     * @param bytes
     * @return
     */
    public static Object toInstance(byte[] bytes) {
        return KryoInjection.invert(bytes).getOrElse(null);
    }
}