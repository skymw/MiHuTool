package cn.skywm.mihu.jdbc.spring;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MySQL数据访问辅助类
 */
@Component
public class MySQLHelper extends AbstractJdbcAccessor {
    private static Map<String, MySQLHelper> caches = new ConcurrentHashMap();

    public MySQLHelper(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    /**
     * 构造MySQLHelper对象
     * @param url
     * @param username
     * @param password
     * @param poolProperties 连接池属性，包括：max-active、max-wait、min-idle、max-idle、initial-size，其他参数默认，参考tomcat-jdbc连接池
     * @return
     */
    synchronized
    public static MySQLHelper build(String url, String username, String password,
                                    Properties poolProperties) {

        String cacheKey = username + "@" + url;
        if (caches.containsKey(cacheKey))
            return caches.get(cacheKey);

        // 数据库中文乱码处理，允许批量执行sql语句：rewriteBatchedStatements
        if (url.indexOf("?") == -1)
            url += "?useSSL=false&characterEncoding=utf-8&autoReconnect=true&rewriteBatchedStatements=true";

        String driverClassName = "com.mysql.jdbc.Driver";

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
        MySQLHelper sqlHelper = new MySQLHelper(jdbcTemplate);
        caches.put(cacheKey, sqlHelper);

        return sqlHelper;
    }

    @Override
    public int replace(String tableName, Map<String, Object> entity) {
        /* replace方法会先删除再新增，在有自增主键的情况下，每次的id会不一样
        String setClause = "";
        Object[] args = new Object[entity.keySet().size()];
        int i = 0;
        for (Map.Entry<String, Object> entry : entity.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            setClause += ", " + key + " = ?";
            args[i++] = value;
        }

        String sql = "replace into " + tableName + " set " + setClause.substring(2);
        return getJdbcTemplate().update(sql, args);*/

        String insertClause = "", insertValue = "", setClause = "";
        Object[] args = new Object[entity.keySet().size()];
        int i = 0;
        for (Map.Entry<String, Object> entry : entity.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            insertClause += ", " + key;
            insertValue += ", ?";
            setClause += ", " + key + " = ?";
            args[i++] = value;
        }

        String sql = "INSERT INTO " + tableName + " (" + insertClause.substring(2) + ")"
                + " VALUE " + "(" + insertValue.substring(2) + ")" + " ON DUPLICATE KEY UPDATE " + setClause.substring(2) + ";";

        Object[] args2 = new Object[args.length * 2];
        System.arraycopy(args, 0, args2, 0, args.length);
        System.arraycopy(args, 0, args2, args.length + 1, args.length);

        return getJdbcTemplate().update(sql, args);
    }

    @Override
    public int[] batchReplace(String tableName, final List<Map<String, Object>> entities) {
        /* replace方法会先删除再新增，在有自增主键的情况下，每次的id会不一样
        if (entities == null || entities.size() == 0)
            return new int[]{0};

        final String[] keys = entities.get(0).keySet().toArray(new String[0]);
        String setClause = "";
        for (String key : keys) {
            setClause += ", " + key + " = ?";
        }

        String sql = "replace into " + tableName + " set " + setClause.substring(2);

        return getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map entity = entities.get(i);

                for (int m = 1; m <= keys.length; m++) {
                    ps.setObject(m, entity.get(keys[m-1]));
                }
            }

            @Override
            public int getBatchSize() {
                return entities.size();
            }
        });*/

        if (entities == null || entities.size() == 0)
            return new int[]{0};

        final String[] keys = entities.get(0).keySet().toArray(new String[0]);
        String insertClause = "", insertValue = "", setClause = "";
        for (String key : keys) {
            insertClause += ", " + key;
            insertValue += ", ?";
            setClause += ", " + key + " = ?";
        }

        String sql = "INSERT INTO " + tableName + " (" + insertClause.substring(2) + ")"
                + " VALUE " + "(" + insertValue.substring(2) + ")" + " ON DUPLICATE KEY UPDATE " + setClause.substring(2);

        return getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map entity = entities.get(i);

                for (int m = 1; m <= keys.length; m++) {
                    ps.setObject(m, entity.get(keys[m-1]));
                }
                for (int m = 1; m <= keys.length; m++) {
                    ps.setObject(keys.length + m, entity.get(keys[m-1]));
                }
            }

            @Override
            public int getBatchSize() {
                return entities.size();
            }
        });
    }
}
