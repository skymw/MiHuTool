package cn.skywm.mihu.jdbc.spring;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GreenPlum、HashData数据访问辅助类
 */
@Component(value = "gPHelper")
public class GPHelper extends AbstractJdbcAccessor {
    private static Map<String, GPHelper> caches = new ConcurrentHashMap();

    public GPHelper(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    /**
     * 构造GPHelper对象
     * @param url
     * @param username
     * @param password
     * @param poolProperties 连接池属性，包括：max-active、max-wait、min-idle、max-idle、initial-size，其他参数默认，参考tomcat-jdbc连接池
     * @return
     */
    synchronized
    public static GPHelper build(String url, String username, String password,
                                    Properties poolProperties) {

        String cacheKey = username + "@" + url;
        if (caches.containsKey(cacheKey))
            return caches.get(cacheKey);

        // 数据库中文乱码处理，允许批量执行sql语句：rewriteBatchedStatements
        if (url.indexOf("?") == -1)
            url += "?useSSL=false&characterEncoding=utf-8&autoReconnect=true&rewriteBatchedStatements=true";

        String driverClassName = "org.postgresql.Driver";

        DataSource dataSource = new DataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);

        dataSource.setTestOnBorrow(true);
        dataSource.setTestWhileIdle(true);
        dataSource.setValidationQuery("select 1");
        dataSource.setValidationInterval(30000);

        dataSource.setMaxActive((Integer) poolProperties.get("max-active"));
        dataSource.setMaxWait((Integer) poolProperties.get("max-wait"));
        dataSource.setMinIdle((Integer) poolProperties.get("min-idle"));
        dataSource.setMaxIdle((Integer) poolProperties.get("max-idle"));
        dataSource.setInitialSize((Integer) poolProperties.get("initial-size"));

        dataSource.setTimeBetweenEvictionRunsMillis(30000);         // 每30秒运行一次空闲连接回收器
        dataSource.setMinEvictableIdleTimeMillis(1800000);          // 池中的连接空闲30分钟后被回收
        dataSource.setNumTestsPerEvictionRun(3);                    // 在每次空闲连接回收器线程(如果有)运行时检查的连接数量

        dataSource.setRemoveAbandoned(false);       // 生产环境中关闭
        dataSource.setRemoveAbandonedTimeout(180);
        dataSource.setLogAbandoned(true);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        GPHelper sqlHelper = new GPHelper(jdbcTemplate);
        caches.put(cacheKey, sqlHelper);

        return sqlHelper;
    }

    @Override
    public int replace(String tableName, Map<String, Object> entity) {
        throw new NotImplementedException();
    }

    @Override
    public int[] batchReplace(String tableName, final List<Map<String, Object>> entities) {
        throw new NotImplementedException();
    }
}
