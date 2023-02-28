package cn.skywm.mihu.jdbc.spring;

import cn.skywm.mihu.common.StaleObjectStateException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.persistence.Table;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.Date;
import java.util.*;

@Component
public abstract class AbstractJdbcAccessor {
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate jdbcTemplateN;

    public AbstractJdbcAccessor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcTemplateN = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    public List<Map<String, Object>> query(String sql) {
        List<Map<String, Object>> result = jdbcTemplate.query(sql, new ColumnMapRowMapper());
        return result;
    }

    public List<Map<String, Object>> query(String sql, Object[] args) {
        List<Map<String, Object>> result = jdbcTemplate.query(sql, args, new ColumnMapRowMapper());
        return result;
    }

    public int execute(String sql) {
        return jdbcTemplate.update(sql);
    }

    public int execute(String sql, Object[] args) {
        return jdbcTemplate.update(sql, args);
    }
    public int executeN(String sql, SqlParameterSource args) {
        return jdbcTemplateN.update(sql, args);
    }

    public Object executeScalar(String sql, Object[] args) {
        List<Object> result = jdbcTemplate.query(sql, args, new RowMapper<Object>() {
            @Nullable
            @Override
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                if (i > 0)
                    return null;

                return resultSet.getObject(1);
            }
        });

        if (result == null || result.size() == 0)
            return null;

        return result.get(0);
    }
    public Object executeScalarN(String sql, SqlParameterSource args) {
        List<Object> result = jdbcTemplateN.query(sql, args, new RowMapper<Object>() {
            @Nullable
            @Override
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                if (i > 0)
                    return null;

                return resultSet.getObject(1);
            }
        });

        if (result == null || result.size() == 0)
            return null;

        return result.get(0);
    }

    /**
     * 插入一条记录
     * @param tableName
     * @param entity
     */
    public int insert(String tableName, Map<String, Object> entity) {
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

        String sql = "insert into " + tableName + " (" + insertClause.substring(2) + ") " +
                "values (" + valuesClause.substring(2) + ") ";

        return jdbcTemplate.update(sql, args);
    }

    /**
     * 批量插入记录
     * @param tableName
     * @param entities
     * @return
     */
    public int[] batchInsert(String tableName, final List<Map<String, Object>> entities) {
        if (entities == null || entities.size() == 0)
            return new int[]{0};

        final String[] keys = entities.get(0).keySet().toArray(new String[0]);
        String sql = "insert into " + tableName + " (" + StringUtils.join(keys, ", ") + ") " +
                "values (" + StringUtils.repeat("?", ", ", keys.length) + ") ";

        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map entity = entities.get(i);

                for (int m = 1; m <= keys.length; m++) {
                    ps.setObject(m, entity.get(keys[m - 1]));
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
     * @param tableName
     * @param entity
     * @return
     */
    public int update(String tableName, Map<String, Object> entity) {
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

        String sql = "update " + tableName + " set " + setClause.substring(2) + " where id = " + id;
        return jdbcTemplate.update(sql, args);
    }

    /**
     * 批量更新记录
     * @param tableName
     * @param entities
     * @return
     */
    public int[] batchUpdate(String tableName, final List<Map<String, Object>> entities) {
        if (entities == null || entities.size() == 0)
            return new int[]{0};

        final String[] keys = entities.get(0).keySet().toArray(new String[0]);
        String setClause = "";
        for (String key : keys) {
            if (key.equals("id"))
                continue;
            setClause += ", " + key + " = ?";
        }

        String sql = "update " + tableName + " set " + setClause.substring(2) + " where id = ? ";

        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map entity = entities.get(i);

                int m = 1;
                Object pk = null;
                for (String key : keys) {
                    if (key.equals("id")) {
                        pk = entity.get(key);
                        continue;
                    }
                    ps.setObject(m++, entity.get(key));
                }
                ps.setObject(m, pk);
            }

            @Override
            public int getBatchSize() {
                return entities.size();
            }
        });
    }

    /**
     * 插入或更新一条记录
     * @param tableName
     * @param entity
     * @return
     */
    public abstract int replace(String tableName, Map<String, Object> entity);

    /**
     * 批量插入或更新记录
     *
     * @param tableName
     * @param entities
     * @return
     */
    public abstract int[] batchReplace(String tableName, List<Map<String, Object>> entities);

    /**
     * 查询对象列表，该方法依赖于Hibernate生成的实体对象
     * @param sql
     * @param args
     * @param entityClazz
     * @param <T>
     * @return
     */
    public <T> List<T> query(String sql, Object[] args, final Class<T> entityClazz) throws Exception {
        final ResultSetMetaData[] metaData = {null};
        final Map<String, EntityPropertyAccessor> propertyAccessors = new HashMap<>();
        final EntityPropertyAccessor.EntityPropertyAccessorBuilder builder = EntityPropertyAccessor.builder(entityClazz);

        List<T> result = jdbcTemplate.query(sql, args, new RowMapper<T>() {
            @Nullable
            @Override
            public T mapRow(ResultSet resultSet, int rowNum) throws SQLException {
                T entity = null;
                try {
                    if (metaData[0] == null)
                        metaData[0] = resultSet.getMetaData();

                    entity = entityClazz.newInstance();

                    int columnCount = metaData[0].getColumnCount();
                    for (int i = 0; i < columnCount; i++) {
                        String columnName = metaData[0].getColumnLabel(i+1);

                        EntityPropertyAccessor propertyAccessor = propertyAccessors.get(columnName);
                        if (propertyAccessor == null) {
                            propertyAccessor = builder.buildByColumnName(columnName);
                            propertyAccessors.put(columnName, propertyAccessor);
                        }

                        if (propertyAccessor.getSetter() != null) {
                            propertyAccessor.getSetter().invoke(entity,
                                    ValueConvertor.convertValue(resultSet.getObject(i + 1),
                                            propertyAccessor.getGetter().getReturnType()));
                        }
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                    throw new SQLException(e);
                }

                return entity;
            }
        });

        return result;
    }

    /**
     * 查询对象列表，该方法依赖于Hibernate生成的实体对象
     * @param sql
     * @param args
     * @param entityClazz
     * @param <T>
     * @return
     */
    public <T> List<T> queryN(String sql, SqlParameterSource args, final Class<T> entityClazz) throws Exception {
        final ResultSetMetaData[] metaData = {null};
        final Map<String, EntityPropertyAccessor> propertyAccessors = new HashMap<>();
        final EntityPropertyAccessor.EntityPropertyAccessorBuilder builder = EntityPropertyAccessor.builder(entityClazz);

        List<T> result = null;
        try {
            result = jdbcTemplateN.query(sql, args, new RowMapper<T>() {
                @Nullable
                @Override
                public T mapRow(ResultSet resultSet, int rowNum) throws SQLException {
                    T entity = null;
                    try {
                        if (metaData[0] == null)
                            metaData[0] = resultSet.getMetaData();

                        entity = entityClazz.newInstance();

                        int columnCount = metaData[0].getColumnCount();
                        for (int i = 0; i < columnCount; i++) {
                            String columnName = metaData[0].getColumnLabel(i + 1);

                            EntityPropertyAccessor propertyAccessor = propertyAccessors.get(columnName);
                            if (propertyAccessor == null) {
                                propertyAccessor = builder.buildByColumnName(columnName);
                                propertyAccessors.put(columnName, propertyAccessor);
                            }

                            if (propertyAccessor.getSetter() != null) {
                                propertyAccessor.getSetter().invoke(entity,
                                        ValueConvertor.convertValue(resultSet.getObject(i + 1),
                                                propertyAccessor.getGetter().getReturnType()));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new SQLException(e);
                    }

                    return entity;
                }
            });
        } catch(Exception e) {
            System.out.println("sql : "+sql.toString());
            for (String argName : args.getParameterNames()){
                System.out.println("argName : "+argName);
                System.out.println("value : "+args.getValue(argName));
            }
            throw e;
        }
        return result;
    }

    /**
     * 查询对象列表，返回Map集合
     * @param sql
     * @param args
     * @return
     */
    public List<Map<String, Object>> queryNForMap(String sql, SqlParameterSource args) throws Exception {
        final ResultSetMetaData[] metaData = {null};

        List<Map<String, Object>> result = jdbcTemplateN.queryForList(sql, args);

        return result;
    }

    /**
     * 查询，记录以数组返回，同时返回列信息
     * @param sql
     * @param args
     * @return 返回数组，0为rows，1为columns
     * @throws Exception
     */
    public Object[] queryNForArray(String sql, SqlParameterSource args) throws Exception {
        final ResultSetMetaData[] metaData = {null};

        List<Object[]> rows = jdbcTemplateN.query(sql, args, new RowMapper<Object[]>() {
            @Nullable
            @Override
            public Object[] mapRow(ResultSet resultSet, int rowNum) throws SQLException {

                try {
                    if (metaData[0] == null)
                        metaData[0] = resultSet.getMetaData();

                    int columnCount = metaData[0].getColumnCount();
                    Object[] row = new Object[columnCount];

                    for (int i = 0; i < columnCount; i++) {
                        row[i] = resultSet.getObject(i + 1);
                    }
                    return row;
                } catch (Exception e) {
                    throw new SQLException(e);
                }
            }
        });

        String[] columns = null;
        if (metaData[0] != null) {
            columns = new String[metaData[0].getColumnCount()];
            for (int i = 0; i < columns.length; i++)
                columns[i] = metaData[0].getColumnLabel(i+1);
        }

        return new Object[] { rows, columns };
    }

    /**
     * 删除实体对象
     * @param entity
     * @param <T>
     * @throws Exception
     */
    public <T> void delete(T entity) throws Exception {
        List<T> entities = new ArrayList<>();
        entities.add(entity);
        delete(entities);
    }

    /**
     * 批量删除实体对象
     * @param entities
     * @param <T>
     * @throws Exception
     */
    public <T> void delete(List<T> entities) throws Exception {
        if (entities == null || entities.size() == 0)
            throw new Exception("parameter 'entities' is empty.");

        T entity0 = entities.get(0);
        Class<?> entityClazz = entity0.getClass();
        entityClazz = EntityPropertyAccessor.getEntityClass(entityClazz);

        String tableName = entityClazz.getSimpleName();
        Table tableAnnotation = entityClazz.getDeclaredAnnotation(Table.class);
        if (tableAnnotation != null && !StringUtils.isEmpty(tableAnnotation.name()))
            tableName = tableAnnotation.name();

        boolean bConcurrent = false;    // 是否需要并发控制

        Field field_ver = null;
        try {
            field_ver = entityClazz.getDeclaredField("ver");
        } catch (Exception e) { }

        if (field_ver != null)
            bConcurrent = true;

        final String sql = "delete from " + tableName + " where id = ?" + (bConcurrent ? " and ver = ?" : "");

        Method getId = entityClazz.getMethod("getId");
        Method getVer = null;
        if (bConcurrent)
            getVer = entityClazz.getMethod("getVer");

        final List<Object> args = new ArrayList<>();

        long[] keys = new long[entities.size()];
        int k = 0;
        for (T entity : entities) {
            args.clear();
            args.add(getId.invoke(entity));
            if (bConcurrent)
                args.add(getVer.invoke(entity));

            int affected = jdbcTemplate.update(sql, args.toArray());
            if (affected == 0)
                throw new StaleObjectStateException();
        }

    }

    /**
     * 保存对象，并返回自增长主键。该方法依赖于Hibernate生成的实体对象
     * @param entity
     * @param affectedFields 更新时受影响的属性，仅对修改操作有效
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> long save(T entity, String[] affectedFields) throws Exception {
        List<T> entities = new ArrayList<>();
        entities.add(entity);
        return save(entities, affectedFields)[0];
    }

    /**
     * 保存对象，并返回自增长主键。该方法依赖于Hibernate生成的实体对象
     * @param entity
     * @param affectedFields 更新时受影响的属性，仅对修改操作有效
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> long save(T entity, String[] affectedFields, IdWorker idWorker) throws Exception {
        List<T> entities = new ArrayList<>();
        entities.add(entity);
        return save(entities, affectedFields, idWorker)[0];
    }

    /**
     * 保存对象，并返回自增长主键。该方法依赖于Hibernate生成的实体对象
     * @param entity
     * @return
     */
    public <T> long save(T entity) throws Exception {
        List<T> entities = new ArrayList<>();
        entities.add(entity);
        return save(entities, (String[]) null)[0];
    }

    /**
     * 保存对象，并返回自增长主键。该方法依赖于Hibernate生成的实体对象
     * @param entity
     * @return
     */
    public <T> long save(T entity, IdWorker idWorker) throws Exception {
        List<T> entities = new ArrayList<>();
        entities.add(entity);
        return save(entities, null, idWorker)[0];
    }

    /**
     * 批量保存对象，并返回自增长主键。该方法依赖于Hibernate生成的实体对象
     * @param entities 实体对象集合
     * @param affectedFields 更新时受影响的属性，仅对修改操作有效
     * @return
     */
    public <T> long[] save(List<T> entities, String[] affectedFields) throws Exception {
        return save(entities, affectedFields, (IdWorker) null);
    }

    /**
     * 批量保存对象，并返回自增长主键。该方法依赖于Hibernate生成的实体对象
     * @param entities 实体对象集合
     * @param affectedFields 更新时受影响的属性，仅对修改操作有效
     * @return
     */
    public <T> long[] save(List<T> entities, String[] affectedFields, IdWorker idWorker) throws Exception {
        // 修改时不受影响的属性
        String[] excludedFields = {"id", "creatorId", "createTime"};

        return save(entities, affectedFields, excludedFields, idWorker);
    }

    /**
     * 批量保存对象，并返回自增长主键。该方法依赖于Hibernate生成的实体对象
     * @param entities 实体对象集合
     * @param affectedFields 更新时受影响的属性，仅对修改操作有效
     * @param excludedFields 更新时不受影响的属性，仅对修改操作有效
     * @return
     */
    public <T> long[] save(List<T> entities, String[] affectedFields, String[] excludedFields) throws Exception {
        return save(entities, affectedFields, excludedFields, (IdWorker) null);
    }

    /**
     * 批量保存对象，并返回自增长主键。该方法依赖于Hibernate生成的实体对象
     * @param entities 实体对象集合
     * @param affectedFields 更新时受影响的属性，仅对修改操作有效
     * @param excludedFields 更新时不受影响的属性，仅对修改操作有效
     * @param idWorker 如果为null表示主键数据库自增长
     * @return
     */
    public <T> long[] save(List<T> entities, String[] affectedFields, String[] excludedFields, IdWorker idWorker) throws Exception {
        if (entities == null || entities.size() == 0)
            throw new Exception("parameter 'entities' is empty.");

        T entity0 = entities.get(0);
        Class<?> entityClazz = entity0.getClass();
        entityClazz = EntityPropertyAccessor.getEntityClass(entityClazz);

        final EntityPropertyAccessor.EntityPropertyAccessorBuilder builder = EntityPropertyAccessor.builder(entityClazz);

        String tableName = entityClazz.getSimpleName();
        Table tableAnnotation = entityClazz.getDeclaredAnnotation(Table.class);
        if (tableAnnotation != null && !StringUtils.isEmpty(tableAnnotation.name()))
            tableName = tableAnnotation.name();

        final Map<String, EntityPropertyAccessor> propertyAccessors = new HashMap<>();


        Timestamp now = new Timestamp(new Date().getTime());
        boolean bConcurrent = false;    // 是否需要并发控制
        Field[] fields = EntityPropertyAccessor.getAllFields(entityClazz);  // entityClazz.getDeclaredFields();

        List<String> affectedFieldsList = null;
        if (affectedFields != null)
            affectedFieldsList = Arrays.asList(affectedFields);

        Field field_createTime = null;
        try {
            field_createTime = entityClazz.getDeclaredField("createTime");
            if (affectedFieldsList != null)
                affectedFieldsList.add("modifyTime");
        } catch (Exception e) { }

        Field field_creatorId = null;
        try {
            field_creatorId = entityClazz.getDeclaredField("creatorId");
            if (affectedFieldsList != null)
                affectedFieldsList.add("modifierId");
        } catch (Exception e) { }

        Field field_ver = null;
        try {
            field_ver = entityClazz.getDeclaredField("ver");
        } catch (Exception e) { }

        if (field_ver != null)
            bConcurrent = true;

        if (affectedFieldsList != null)
            affectedFields = affectedFieldsList.toArray(new String[0]);

        String insertClause = "", valuesClause = "";
        String setClause = "";
        final String insert_sql, update_sql;

        for (Field field : fields) {
            String fieldName = field.getName();

            EntityPropertyAccessor propertyAccessor = propertyAccessors.get(fieldName);
            if (propertyAccessor == null) {
                propertyAccessor = builder.buildByFieldName(fieldName);
                propertyAccessors.put(fieldName, propertyAccessor);
            }

            String columnName = propertyAccessor.getColumnName();

            if (!(idWorker == null && fieldName.equals("id"))) {
                insertClause += ", " + columnName;
                valuesClause += ", ?";
            }

            if (!fieldName.equals("id") && !fieldName.equals("ver")
                    && !ArrayUtils.contains(excludedFields, fieldName)
                    && (affectedFields == null || ArrayUtils.contains(affectedFields, fieldName)))
                setClause += ", " + columnName + " = ?";
        }

        if (bConcurrent) {
            setClause += ", ver = ver + 1";
        }

        if (StringUtils.isEmpty(insertClause) || StringUtils.isEmpty(valuesClause) || StringUtils.isEmpty(setClause))
            throw new Exception("insertClause,valuesClause or setClause is empty.");

        insert_sql = "insert into " + tableName + " (" + insertClause.substring(2) + ") " +
                "values (" + valuesClause.substring(2) + ") ";
        update_sql = "update " + tableName + " set " + setClause.substring(2)
                + " where id = ?" + (bConcurrent ? " and ver = ?" : "");


        Method getId = entityClazz.getMethod("getId");
        Method setId = entityClazz.getMethod("setId", getId.getReturnType());

        Method getCreatorId = null, getModifierId = null, setModifierId = null, setCreateTime = null, setModifyTime = null, getVer = null;
        if (field_creatorId != null) {
            getCreatorId = entityClazz.getMethod("getCreatorId");
            getModifierId = entityClazz.getMethod("getModifierId");
            setModifierId = entityClazz.getMethod("setModifierId", field_creatorId.getType());
        }
        if (field_createTime != null) {
            setCreateTime = entityClazz.getMethod("setCreateTime", field_createTime.getType());
            setModifyTime = entityClazz.getMethod("setModifyTime", field_createTime.getType());
        }
        if (bConcurrent)
            getVer = entityClazz.getMethod("getVer");

        long[] keys = new long[entities.size()];
        int k = 0;
        for (T entity : entities) {
            Object id = getId.invoke(entity);
            boolean bNew = (id == null || Long.parseLong(id.toString()) == 0) ? true : false;
            Object ver = null;

            if (bNew) {
                if (idWorker != null) {
                    id = idWorker.nextId();
                    setId.invoke(entity, id);
                }

                if (field_createTime != null) {
                    setCreateTime.invoke(entity, now);
                    setModifyTime.invoke(entity, now);
                }

                if (field_creatorId != null) {
                    Object creatorId = getCreatorId.invoke(entity);
                    if (creatorId == null)
                        throw new Exception("creatorId is null.");

                    setModifierId.invoke(entity, creatorId);
                }

            } else {
                if (field_createTime != null) {
                    setModifyTime.invoke(entity, now);
                }

                if (field_creatorId != null) {
                    Object modifierId = getModifierId.invoke(entity);
                    if (modifierId == null)
                        throw new Exception("modifierId is null.");
                }

                if (bConcurrent) {
                    ver = getVer.invoke(entity);
                    if (ver == null)
                        throw new Exception("field 'ver' is null.");
                }
            }

            final List<Object> args = new ArrayList<>();
            int i = 0;
            for (Field field : fields) {
                String fieldName = field.getName();

                EntityPropertyAccessor propertyAccessor = propertyAccessors.get(fieldName);
                Object fieldValue = propertyAccessor.getGetter().invoke(entity);

                if (bNew) {
                    if (!(idWorker == null && fieldName.equals("id")))
                        args.add(fieldValue);
                } else {
                    if (!fieldName.equals("id") && !fieldName.equals("ver")
                            && !ArrayUtils.contains(excludedFields, fieldName)
                            && (affectedFields == null || ArrayUtils.contains(affectedFields, fieldName)))
                        args.add(fieldValue);
                }
            }

            if (!bNew)
                args.add(id);

            if (!bNew && bConcurrent)
                args.add(ver);

            final String sql = bNew ? insert_sql : update_sql;
            final KeyHolder keyHolder = new GeneratedKeyHolder();

            int affected = -1;
            if (idWorker == null) {
                affected = jdbcTemplate.update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        for (int i = 0; i < args.size(); i++) {
                            ps.setObject(i + 1, args.get(i));
                        }
                        return ps;
                    }
                }, keyHolder);
            } else {
                affected = jdbcTemplate.update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps = connection.prepareStatement(sql);
                        for (int i = 0; i < args.size(); i++) {
                            ps.setObject(i + 1, args.get(i));
                        }
                        return ps;
                    }
                });
            }

            if (affected == 0)
                throw new StaleObjectStateException();

            keys[k++] = keyHolder.getKey() == null ? Long.parseLong(id.toString()) : keyHolder.getKey().longValue();
        }

        return keys;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
    public NamedParameterJdbcTemplate getJdbcTemplateN() {
        return jdbcTemplateN;
    }

}
