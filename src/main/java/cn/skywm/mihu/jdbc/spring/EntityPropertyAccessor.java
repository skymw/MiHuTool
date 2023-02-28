package cn.skywm.mihu.jdbc.spring;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class EntityPropertyAccessor {
    private Class<?> clazz;
    private String fieldName;
    private String columnName;
    private Method getter;
    private Method setter;
    private Class<?> fieldType;

    private EntityPropertyAccessor(Class<?> clazz, String fieldName, String columnName) {
        this.clazz = clazz;
        this.fieldName = fieldName;
        this.columnName = columnName;

        try {
            getter = clazz.getMethod("get" + javaName(fieldName));
            fieldType = getter.getReturnType();
        } catch (Exception e) {}

        try {
            if (getter != null) {
                Class<?> fieldType = getter.getReturnType();
                setter = clazz.getMethod("set" + javaName(fieldName), fieldType);
            }
        } catch (Exception e) {}
    }

    public static EntityPropertyAccessorBuilder builder(Class<?> clazz) throws Exception {
        return new EntityPropertyAccessorBuilder(clazz);
    }

    @Deprecated
    public static EntityPropertyAccessor buildByFieldName(Class<?> clazz, String fieldName) throws Exception {
        Method getMethod = clazz.getMethod("get" + javaName(fieldName));
        Column columnAnnotation = getMethod.getDeclaredAnnotation(Column.class);
        if (columnAnnotation == null)
            throw new Exception("fieldName:" + fieldName + " is not a entity column.");

        String columnName = columnAnnotation.name();

        return new EntityPropertyAccessor(clazz, fieldName, columnName);
    }

    @Deprecated
    public static EntityPropertyAccessor buildByColumnName(Class<?> clazz, String columnName) throws Exception {
        for (Field field : getAllFields(clazz)) {
            Method getMethod = clazz.getMethod("get" + javaName(field.getName()));
            Column columnAnnotation = getMethod.getDeclaredAnnotation(Column.class);
            if (columnAnnotation != null && columnAnnotation.name().equals(columnName))
                return new EntityPropertyAccessor(clazz, field.getName(), columnName);
        }

//        throw new Exception("columnName not found.");
        // 如果没找到Column注解对应关系，尝试使用kebab-case命名转换为camelCase
        String fieldName = camelCasedName(columnName);
        Method getMethod = clazz.getMethod("get" + javaName(fieldName));  // 如果没有该属性会抛出异常

        return new EntityPropertyAccessor(clazz, fieldName, columnName);
    }

    public Method getGetter() {
        return getter;
    }

    public Method getSetter() {
        return setter;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getColumnName() throws NoSuchMethodException {
        /*if (columnName != null)
            return columnName;

        Method getMethod = clazz.getMethod("get" + javaName(fieldName));
        Column columnAnnotation = getMethod.getDeclaredAnnotation(Column.class);
        columnName = columnAnnotation.name();*/
        return columnName;
    }

    public Class<?> getFieldType() {
        return fieldType;
    }

    public static Field[] getAllFields(Class<?> clazz) {
        final List<Field> allFields = new ArrayList<>();
        Class<?> currentClazz = clazz;
        while (currentClazz != null) {
            final Field[] declaredFields = currentClazz.getDeclaredFields();
            allFields.addAll(Arrays.asList(declaredFields));
            currentClazz = currentClazz.getSuperclass();
        }
        return allFields.toArray(new Field[0]);
    }

    public static Class<?> getEntityClass(Class<?> clazz) {
        Class<?> entityClazz = clazz;
        while (true) {
            if (entityClazz.getSuperclass() == Object.class)
                break;

            entityClazz = entityClazz.getSuperclass();
        }
        return entityClazz;
    }

    private static String javaName(String s) {
        if (s.length() > 1) {
            char[] chars = s.toCharArray();
            if (Character.isLowerCase(chars[0]) && Character.isLowerCase(chars[1]))
                return s.substring(0, 1).toUpperCase() + s.substring(1);
        }

        return s;
    }

    private static String camelCasedName(String s) {
        String[] ss = s.split("_");

        String newName = ss[0];
        for (int i = 1; i < ss.length; i++) {
            newName += javaName(ss[i]);
        }

        return newName;
    }

    public static class EntityPropertyAccessorBuilder {
        private Class<?> clazz;

        private Map<String, String> field_column = new HashMap<>();
        private Map<String, String> column_field = new HashMap<>();

        public EntityPropertyAccessorBuilder(Class<?> clazz) throws Exception {
            this.clazz = clazz;

            for (Field field : getAllFields(clazz)) {
                String fieldName = field.getName(),
                        columnName = "";

                Method getMethod = clazz.getMethod("get" + javaName(field.getName()));
                Column columnAnnotation = getMethod.getDeclaredAnnotation(Column.class);
                if (columnAnnotation != null) {
                    columnName = columnAnnotation.name();
                    field_column.put(fieldName, columnName);
                    column_field.put(columnName, fieldName);
                }
            }
        }

        public EntityPropertyAccessor buildByFieldName(String fieldName) throws Exception {
            String columnName = field_column.get(fieldName);
            if (StringUtils.isEmpty(columnName))
                throw new Exception("fieldName:" + fieldName + " is not a entity column.");

            return new EntityPropertyAccessor(clazz, fieldName, columnName);
        }

        public EntityPropertyAccessor buildByColumnName(String columnName) throws Exception {
            String fieldName = field_column.get(columnName);
            if (StringUtils.isEmpty(fieldName)) {
                fieldName = camelCasedName(columnName);
            }
            return new EntityPropertyAccessor(clazz, fieldName, columnName);
        }
    }
}
