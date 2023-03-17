package cn.skywm.mihu.common;

import java.util.Map;
import java.util.Properties;

/**
 * @ProjectName: BsQaModel
 * @Package: cn.april.netcommander.qa.config
 * @ClassName:
 * @Author: maowei
 * @CreateDate: 2022/9/29 11:33
 * @Version: 1.0
 * Copyright: Copyright (c) 2022
 */
public class DbConfig {
    private Map<String, Object> jdbc;

    public Map<String, Object> getJdbc() {
        return jdbc;
    }

    public void setJdbc(Map<String, Object> jdbc) {
        this.jdbc = jdbc;
    }

    public Properties getJdbcPoolProperties() {
        Properties props = new Properties();
        // default properties
        props.put("max-wait", 10000);
        props.put("max-active", 500);
        props.put("min-idle", 5000);
        props.put("max-idle", 10000);
        props.put("initial-size", 5);


        Map jdbcProperties = getJdbc();
        if (jdbcProperties == null)
            return props;

        Map<String, Object> jdbcPoolProperties = (Map<String, Object>) jdbcProperties.get("pool");
        if (jdbcPoolProperties == null)
            return props;

        String propName = "max-wait";
        if (jdbcPoolProperties.get(propName) != null)
            props.put(propName, jdbcPoolProperties.get(propName));

        propName = "max-active";
        if (jdbcPoolProperties.get(propName) != null)
            props.put(propName, jdbcPoolProperties.get(propName));

        propName = "min-idle";
        if (jdbcPoolProperties.get(propName) != null)
            props.put(propName, jdbcPoolProperties.get(propName));

        propName = "max-idle";
        if (jdbcPoolProperties.get(propName) != null)
            props.put(propName, jdbcPoolProperties.get(propName));

        propName = "initial-size";
        if (jdbcPoolProperties.get(propName) != null)
            props.put(propName, jdbcPoolProperties.get(propName));

        return props;
    }
}
