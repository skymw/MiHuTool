package cn.skywm.mihu.jdbc.spring;

import java.lang.reflect.Type;

public class ValueConvertor {

	public static Object convertValue(Object value, Type type) throws Exception {
		if (type.equals(Integer.class)) {
			if (value == null || value.equals(""))
				return null;
			return Integer.valueOf(value.toString());
		} else if (type.equals(int.class)) {
            if (value == null || value.equals(""))
                return 0;
            return Integer.valueOf(value.toString());
        }

		return value;
	}
}
