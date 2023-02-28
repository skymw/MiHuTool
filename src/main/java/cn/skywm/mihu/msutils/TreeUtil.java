package cn.skywm.mihu.msutils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeUtil {
    /**
     * 转换业务对象列表为树型结构
     * @param entities 业务对象列表
     * @param primaryKey 业务对象的主键字段名称，如id
     * @param parentKey 业务对象的上级字段名称，如parentId
     * @param childContainerName 构造生成的树包含下级对象的节点名称，如children
     * @return
     */
    public static List<Map> getEntitiesTreeList(List entities, String primaryKey, String parentKey, String childContainerName) throws Exception {
        if (entities == null || entities.size() == 0) return null;
        List<Map> result = new ArrayList<>();

        List<Map> data = new ArrayList<>();
        Map<Object, Map> dataIndex = new HashMap<>();

        // 将业务对象转换成Map对象，并建立索引
        ObjectMapper om = new ObjectMapper();
        for (int i = 0; i < entities.size(); i++) {
            Map obj = om.readValue(om.writeValueAsString(entities.get(i)), Map.class);

            data.add(obj);
            dataIndex.put(obj.get(primaryKey), obj);
        }

        // 构建树
        for (int i = 0; i < data.size(); i++) {
            Map obj = data.get(i);

            Object pv = obj.get(parentKey);
            if (pv == null)
                result.add(obj);        // 将根节点数据添加到返回结果
            else {
                Map pObj = dataIndex.get(pv);
                if (pObj == null)
                    throw new Exception("缺少主键为" + pv + "的数据");

                if (!pObj.containsKey(childContainerName))
                    pObj.put(childContainerName, new ArrayList<>());

                List subset = (List) pObj.get(childContainerName);
                subset.add(obj);
            }
        }

        return result;
    }
}
