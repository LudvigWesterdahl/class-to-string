package se.ludvigwesterdahl.lib;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class Main<T> {
    private Map<String, List<String>> field;
    private T field2;

    public static void main(String[] args) throws Exception {
        System.out.println(getString());
        final Field[] type = Main.class.getDeclaredFields();
        final Main<String> s = new Main<>();
        final Field[] f2 = s.getClass().getDeclaredFields();
        final Type t = type[0].getGenericType();

        System.out.println(type);
    }

    public static String getString() {
        return "Hello world";
    }
}
