package com.alibaba.bytekit.utils;
import java.lang.reflect.InvocationTargetException;

public class InstanceUtils {

    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
