package elec332.isdrt.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by Elec332 on 5-10-2019
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ClassProperties {

    public static <T> T readProperties(Class<T> clazz, File file) {
        if (file == null) {
            throw new IllegalArgumentException();
        }
        T ret;
        try {
            ret = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (!file.exists()) {
            return ret;
        }
        Properties properties = new Properties();
        try {
            FileInputStream fis = new FileInputStream(file);
            properties.load(fis);
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getValidFields(clazz).forEach(f -> {
            f.setAccessible(true);
            String name = getPropertyName(f);
            if (properties.containsKey(name)) {
                try {
                    f.set(ret, properties.getProperty(name));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return ret;
    }

    public static void importProperties(Object o, File file){
        if (file == null) {
            throw new IllegalArgumentException();
        }
        if (!file.exists()) {
            return;
        }
        Properties properties = new Properties();
        try {
            FileInputStream fis = new FileInputStream(file);
            properties.load(fis);
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getValidFields(o.getClass())
                .filter(f -> f.isAnnotationPresent(PropertyData.class) && f.getAnnotation(PropertyData.class).canBeImported())
                .forEach(f -> {
                    f.setAccessible(true);
                    String name = getPropertyName(f);
                    if (properties.containsKey(name)) {
                        try {
                            f.set(o, properties.getProperty(name));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    public static void writeProperties(Object o, File file, String comment) {
        if (file == null) {
            throw new IllegalArgumentException();
        }
        Properties properties = new Properties();
        getValidFields(o.getClass()).forEach(f -> {
            f.setAccessible(true);
            try {
                properties.setProperty(getPropertyName(f), (String) f.get(o));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            FileOutputStream fos = new FileOutputStream(file);
            properties.store(fos, comment);
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<Field> getValidFields(Class clazz) {
        Field[] fields = clazz.getDeclaredFields();
        return Arrays.stream(fields)
                .filter(f -> f.getType() == String.class)
                .filter(f -> (f.getModifiers() & Modifier.TRANSIENT) == 0);
    }

    public static String getPropertyName(Field field) {
        String ret = null;
        if (field.isAnnotationPresent(PropertyData.class)) {
            ret = field.getAnnotation(PropertyData.class).value();
        }
        if (ret == null || ret.equals("")) {
            ret = field.getName();
        }
        return ret;
    }

    public static String[] getValidValues(Field field) {
        String[] ret = null;
        if (field.isAnnotationPresent(PropertyData.class)) {
            PropertyData p = field.getAnnotation(PropertyData.class);
            ret = p.validValues();
            if (ret.length == 0) {
                if (p.dynamicValidValues() == Dummy.class){
                    return null;
                }
                try {
                    p.dynamicValidValues().getConstructor().setAccessible(true);
                    ret = p.dynamicValidValues().newInstance().get();
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (ret == null || ret.length == 0) {
            return null;
        }
        return ret;
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PropertyData {

        String value() default "";

        String[] validValues() default {};

        Class<? extends Supplier<String[]>> dynamicValidValues() default Dummy.class;

        Class<? extends Number> number() default Number.class;

        boolean canBeImported() default false;

    }

    private static class Dummy implements Supplier<String[]> {

        @Override
        public String[] get() {
            throw new UnsupportedOperationException();
        }

    }

}
