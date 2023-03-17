package cn.skywm.mihu.jdbc.spring;

import cn.skywm.mihu.common.DbConfig;
import cn.skywm.mihu.common.ShortcutUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JdbcHelper {
    private static Map<String, JdbcHelper> caches = new ConcurrentHashMap();
    private final JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public JdbcHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    synchronized
    public static JdbcHelper build(String url, String username, String password) {
        String cacheKey = username + "@" + url;
        if (caches.containsKey(cacheKey))
            return caches.get(cacheKey);

        DbConfig gseConfig = new DbConfig();
        Properties jdbcPoolProperties = gseConfig.getJdbcPoolProperties();

        String driverClassName;
        String validationQuery;
        if (url.startsWith("jdbc:oracle")) {
            validationQuery = "select 1 from dual";
        } else if (url.startsWith("jdbc:postgresql")) {
            url += "?characterEncoding=utf-8&autoReconnect=true&generateSimpleParameterMetadata=true";
            validationQuery = "select 1";
        } else if (url.startsWith("jdbc:pivotal:greenplum")) {
            validationQuery = "select 1";
        } else {
            validationQuery = "select 1";
            // 数据库中文乱码处理，允许批量执行sql语句：rewriteBatchedStatements
            if (!url.contains("?")) {
                url += "?useSSL=false&characterEncoding=utf-8&autoReconnect=true&rewriteBatchedStatements=true&generateSimpleParameterMetadata=true";
            }
        }
        // 根据url获取驱动
        driverClassName = getDriveClassFromUrl(url);

//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        DataSource dataSource = new DataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);

        dataSource.setTestOnBorrow(true);
        dataSource.setTestWhileIdle(true);
        dataSource.setValidationQuery(validationQuery);
        dataSource.setValidationInterval(0000);

        dataSource.setMaxActive((Integer) jdbcPoolProperties.get("max-active"));
        dataSource.setMaxWait((Integer) jdbcPoolProperties.get("max-wait"));
        dataSource.setMinIdle((Integer) jdbcPoolProperties.get("min-idle"));
        dataSource.setMaxIdle((Integer) jdbcPoolProperties.get("max-idle"));
        dataSource.setInitialSize((Integer) jdbcPoolProperties.get("initial-size"));

        dataSource.setTimeBetweenEvictionRunsMillis(30000);         // 每30秒运行一次空闲连接回收器
        dataSource.setMinEvictableIdleTimeMillis(600000);           // 池中的连接空闲10分钟后被回收
        dataSource.setNumTestsPerEvictionRun(3);                    // 在每次空闲连接回收器线程(如果有)运行时检查的连接数量

        dataSource.setRemoveAbandoned(false);                       // 生产环境中关闭
        dataSource.setRemoveAbandonedTimeout(180);
        dataSource.setLogAbandoned(true);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        JdbcHelper helper = new JdbcHelper(jdbcTemplate);
        caches.put(cacheKey, helper);

        return helper;
    }

    /**
     * 根据URL获取数据库驱动
     */
    public static String getDriveClassFromUrl (String url) {

        String driverClassName = "";
        if (url.startsWith("jdbc:oracle")) {
            driverClassName = "oracle.jdbc.driver.OracleDriver";
        } else if (url.startsWith("jdbc:postgresql")) {
            driverClassName = "org.postgresql.Driver";
        } else if (url.startsWith("jdbc:pivotal:greenplum")) {
            driverClassName = "com.pivotal.jdbc.GreenplumDriver";
        } else {
            driverClassName = "com.mysql.jdbc.Driver";
        }
        return driverClassName;
    }

    public List<Map<String, Object>> query(String sql) {
        List<Map<String, Object>> result = jdbcTemplate.query(sql, new ColumnMapRowMapper());
        return result;
    }

    public List<Object[]> query(String sql, Object[] args) {
        List<Object[]> result = jdbcTemplate.query(sql, args, new RowMapper<Object[]>() {
            @Override
            public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
                Object[] object = new Object[rs.getMetaData().getColumnCount()];
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    object[i-1] = rs.getObject(i);
                }
                return object;
            }
        });
        return result;
    }


    /**
     * 插入一条记录
     * @param entityClass
     * @param entity
     */
    public int insert(String entityClass, Map<String, Object> entity) {

        String insertClause = "", valuesClause = "";
        Object[] args = new Object[entity.keySet().size()];
        int i = 0;
        for (Map.Entry<String, Object> entry : entity.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            insertClause += ", " + key;
            valuesClause += ", ?";
            args[i++] = value;
        }

        String sql = "insert into " + entityClass + " (" + insertClause.substring(2) + ") " +
                "values (" + valuesClause.substring(2) + ") ";

        return jdbcTemplate.update(sql, args);
    }

    /**
     * 批量插入记录
     * @param entityClass
     * @param entities
     * @return
     */
    public int[] batchInsert(String entityClass, List<Map<String, Object>> entities, boolean ignoreFlag) {

        if (entities == null || entities.size() == 0)
            return new int[]{0};

        String option = ignoreFlag ? "ignore " : "";

        String[] keys = entities.get(0).keySet().toArray(new String[0]);
        String sql = "insert " + option + "into " + entityClass + " (" + StringUtils.join(keys, ", ") + ") " +
                "values (" + StringUtils.repeat("?", ", ", keys.length) + ") ";


        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map entity = entities.get(i);
                Map<Integer, Integer> colTypeMap = JdbcHelper.getParColTypeMap(ps);

                for (int m = 1; m <= keys.length; m++) {
                    int colType = ps.getParameterMetaData().getParameterType(m);
                    String valueStr = entity.get(keys[m-1]).toString();
                    JdbcHelper.setValue(ps, colType, m, valueStr);
                }
            }

            @Override
            public int getBatchSize() {
                return entities.size();
            }
        });
    }
    /**
     * 更新一条记录
     * @param entityClass
     * @param entity
     * @return
     */
    public int update(String entityClass, Map<String, Object> entity){
        String id = entity.get("id").toString();

        String setClause = "";
        Object[] args = new Object[entity.keySet().size() - 1];
        int i = 0;
        for (Map.Entry<String, Object> entry : entity.entrySet()) {
            String key = entry.getKey();
            if (key.equals("id"))
                continue;

            Object value = entry.getValue();

            setClause += ", " + key + " = ?";
            args[i++] = value;
        }

        String sql = "update " + entityClass + " set " + setClause.substring(2) + " where id = " + id;
        return jdbcTemplate.update(sql, args);
    }

    /**
     * 批量更新记录
     * @param entityClass
     * @param entities
     * @return
     */
    public int[] batchUpdate(String entityClass, List<Map<String, Object>> entities) {
        if (entities == null || entities.size() == 0)
            return new int[]{0};

        return batchUpdateN(entityClass, entities, null);
    }

    /**
     * 批量更新记录
     * @param entityClass
     * @param entities
     * @param uniqueKeyArr 判断数据是否重复的字段名
     * @return
     */
    public int[] batchUpdateN(String entityClass, List<Map<String, Object>> entities, String[] uniqueKeyArr) {

        if (entities == null || entities.size() == 0)
            return new int[]{0};

//        if (StringUtils.isEmpty(uniqueKey))
//            uniqueKey = "id";
//        String[] keys = entities.get(0).keySet().toArray(new String[0]);
//        String setClause = "";
//        for (String key : keys) {
//            if (key.equals("id") || key.equals(uniqueKey))
//                continue;
//            setClause += ", " + key + " = ?";
//        }
//        String sql = "update " + entityClass + " set " + setClause.substring(2) + " where " + uniqueKey + " = ? ";
//        String finalUniqueKey = uniqueKey;
        /*return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map entity = entities.get(i);
                int m = 1;
                Object pk = null;
                for (String key : keys) {
                    if (key.equals("id")
                            || key.equals(finalUniqueKey))
                        continue;
                    ps.setObject(m++, entity.get(key));
                }
                ps.setObject(m, entity.get(finalUniqueKey));
            }
            @Override
            public int getBatchSize() {
                return entities.size();
            }
        });*/

        String[] keys = entities.get(0).keySet().toArray(new String[0]);
        StringBuilder setClause = new StringBuilder();
        for (String key : keys) {
            if (!Arrays.asList(uniqueKeyArr).contains(key)) {
                setClause.append(", ").append(key).append(" = ?");
            }
        }
        StringBuilder setKey = new StringBuilder();
        for (String singleKsy : uniqueKeyArr) {
            setKey.append(" AND ").append(singleKsy).append(" = ?");
        }
        String sql = "update " + entityClass + " set " + setClause.toString().substring(2) + " where " + setKey.toString().substring(5);
        String[] finalUniqueKeyArr = uniqueKeyArr;
        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {

                // 获取字段类型对照表
                Map<Integer, Integer> colTypeMap = JdbcHelper.getParColTypeMap(ps);
                Map entity = entities.get(i);
                int m = 1;
                Object pk = null;
                // 插入 set 项
                for (String key : keys) {
                    if (!Arrays.asList(uniqueKeyArr).contains(key)) {
                        JdbcHelper.setValue(ps, colTypeMap.get(m), m ++, entity.get(key).toString());
                    }
                }
                // 插入 where 项
                for (String singleUniqueKey : finalUniqueKeyArr) {
                    JdbcHelper.setValue(ps, colTypeMap.get(m), m ++, entity.get(singleUniqueKey).toString());
                }
            }
            @Override
            public int getBatchSize() {
                return entities.size();
            }
        });
    }

    /**
     * 插入或更新一条记录
     * @param entityClass
     * @param entity
     * @return
     */
    public int replace(String entityClass, Map<String, Object> entity){

        String setClause = "";
        Object[] args = new Object[entity.keySet().size()];
        int i = 0;
        /*for (Map.Entry<String, Object> entry : entity.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            setClause += ", " + key + " = ?";
            args[i++] = value;
        }

        String sql = "replace into " + entityClass + " set " + setClause.substring(2);*/

        //-----------------------------------

        String str_value = "";
        String str_setClause = "";
        String str_insert = "";
        for (Map.Entry<String, Object> entry : entity.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            str_insert += ", " + key;
            str_value += ", " + "?";
            str_setClause += ", " + key + "= ?";
            args[ i++ ] = value;
        }
        // 创建新的args
        args = ShortcutUtil.array_doubling(args);

        String sql = "INSERT INTO " + entityClass + " (" + str_insert.substring(2) + ")"
                + " VALUE " + "(" + str_value.substring(2) + ")" + " ON DUPLICATE KEY UPDATE " + str_setClause.substring(2) + ";";

        //-----------------------------------
        return jdbcTemplate.update(sql, args);
    }

    /**
     * 批量插入或更新记录，只适用MySQL
     * @param entityClass
     * @param entities
     * @return
     */
    public int[] batchReplace(String entityClass, List<Map<String, Object>> entities, boolean ignoreFlag) {
        if (entities == null || entities.size() == 0)
            return new int[]{0};

        String option = ignoreFlag ? "ignore " : "";

        String[] keys = entities.get(0).keySet().toArray(new String[0]);
        String setClause = "";
        //--------------------------------
        String str_value = "";
        String str_setClause = "";
        String str_insert = "";
        for (String key : keys) {
            str_insert += ", " + key;
            str_value += ", " + "?";
            str_setClause += ", " + key + "= ?";
        }
        //-------------------------------------------------

        String sql = "INSERT " + option + "INTO " + entityClass + " (" + str_insert.substring(2) + ")"
                + " VALUE " + "(" + str_value.substring(2) + ")" + " ON DUPLICATE KEY UPDATE " + str_setClause.substring(2);
//        String sql = "replace into " + entityClass + " set " + setClause.substring(2);

        /*return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
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

        //--------------------------------------
        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map entity = entities.get(i);

                for (int m = 1; m <= keys.length * 2; m++) {

                    if (m <= keys.length) {
                        ps.setObject(m, entity.get(keys[m-1]));
                    } else {
                        ps.setObject(m, entity.get(keys[m - keys.length - 1]));
                    }
                }
            }

            @Override
            public int getBatchSize() {
                return entities.size();
            }
        });
        //----------------------------------------
    }

    /**
     * 批量插入或更新记录，通用方式。先尝试update，判断受影响行数，如果为0，则进行插入
     * @param entityClass
     * @param entities
     * @param uniqueKeyArr 判断数据是否重复的字段名
     * @return
     */
    public int[] batchReplaceN(String entityClass, List<Map<String, Object>> entities, String[] uniqueKeyArr, boolean ignoreFlag) {
        if (entities == null || entities.size() == 0)
            return new int[]{0};

        // 先执行批量更新
        int[] affects = batchUpdateN(entityClass, entities, uniqueKeyArr);

        //
        List<Map<String, Object>> newEntities = new ArrayList<>();
        for (int i = 0; i < affects.length; i++) {
            if (affects[i] > 0)
                continue;

            newEntities.add(entities.get(i));
        }

        batchInsert(entityClass, newEntities, ignoreFlag);

        return null;
    }

    /**
     * SQL参数解析
     */
    public static String[] getParam (String sql) {

        String paramStr = "";
        if (sql.contains("select ") && sql.contains(" from")) {
            paramStr = sql.split("select ")[1].split(" from")[0];
        } else if (sql.contains("SELECT ") && sql.contains(" FROM")) {
            paramStr = sql.split("SELECT ")[1].split(" FROM")[0];
        }
        return paramStr.replaceAll(" ", "").split(",");
    }

    /**
     * SQL 语句批量执行
     */
    public int[] batchExecute (List<String> sqlList) {
        if (sqlList == null || sqlList.size() == 0)
            return new int[]{0};

        // 批量执行
        return jdbcTemplate.batchUpdate(sqlList.toArray(new String[sqlList.size()]));
    }

    /**
     * 获取参数列表数据类型
     */
    public static Map<Integer, Integer> getParColTypeMap (PreparedStatement ps) throws SQLException {
        Map<Integer, Integer> colMap = new HashMap<>();
        for (int m = 1; m <= ps.getParameterMetaData().getParameterCount(); m ++) {
            int colType = ps.getParameterMetaData().getParameterType(m);
            colMap.put(m, colType);
        }
        return colMap;
    }


    /**
     * SetValue
     */
    public static void setValue (PreparedStatement ps, int colType, int m, String valueStr) throws SQLException {

        if (colType == Types.INTEGER) {
            // GP : int4
            ps.setInt(m, Integer.parseInt(valueStr));
        } else if (colType == Types.BIGINT) {
            // GP : int8
            ps.setLong(m, Long.parseLong(valueStr));
        } else if (colType == Types.SMALLINT) {
            // GP : int2
            ps.setShort(m, Short.parseShort(valueStr));
        } else if (colType == Types.REAL) {
            // GP : Float
            ps.setFloat(m, Float.valueOf(valueStr));
        } else if (colType == Types.DOUBLE) {
            // GP : Double
            ps.setDouble(m, Double.parseDouble(valueStr));
        } else if (colType == Types.NUMERIC) {
            // GP : Decimal / Numeric
            ps.setBigDecimal(m, BigDecimal.valueOf(Double.parseDouble(valueStr)));
        } else if (colType == Types.TIMESTAMP) {
            // GP : TimeStamp --> MySql : dateTime
            ps.setTimestamp(m, Timestamp.valueOf(valueStr));
        } else if (colType == Types.DATE) {
            // GP : Date
            ps.setDate(m, Date.valueOf(valueStr));
        } else if (colType == Types.TIME) {
            // GP : Time
            ps.setTime(m, Time.valueOf(valueStr));
        } else {
            ps.setObject(m, valueStr);
        }
    }

    /**
     * 生成一个数据库连接
     */
    public static Connection getConnection(String className, String url, String username, String password) {
        try {
            Class.forName(className);
            return DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
