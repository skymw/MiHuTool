package cn.skywm.mihu.jdbc.spring;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.Map;

/**
 * GreenPlum、HashData数据访问辅助类
 */
@Component
public class HashDataHelper extends AbstractJdbcAccessor {

    public HashDataHelper(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public int replace(String tableName, Map<String, Object> entity) {
        throw new NotImplementedException();
    }

    @Override
    public int[] batchReplace(String tableName, List<Map<String, Object>> entities) {
        throw new NotImplementedException();
    }
}
