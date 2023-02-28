package cn.skywm.mihu.neo4j;


import cn.skywm.mihu.neo4j.entity.Node;
import cn.skywm.mihu.neo4j.entity.Relationship;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Neo4jHelper {
    private static Map<String, Neo4jHelper> drivers = new ConcurrentHashMap<>();

    private Driver _driver;

    private Neo4jHelper(String uri, String user, String password) {
        _driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    synchronized
    public static Neo4jHelper build(String uri, String user, String password) {
        String key = user + "@" + uri;
        if (drivers.containsKey(key))
            return drivers.get(key);

        Neo4jHelper helper = new Neo4jHelper(uri, user, password);
        drivers.put(key, helper);

        return helper;
    }

    public void close() {
        _driver.close();
    }

    /**
     * 创建节点，包括属性。
     * id和code由调用方提供
     *
     * @param nodes
     */
    public void createNode(List<Node> nodes) {
        try (Session session = _driver.session()) {
            for (Node node : nodes) {
                String labels = StringUtils.join(node.getLabels(), ':');

                String profiles = "";
                for (Map.Entry entry : node.getProfiles().entrySet()) {
                    profiles += "," + entry.getKey() + ":\"" + entry.getValue() + "\"";
                }
                profiles = profiles.substring(1);

                String cql = "create (:" + labels + " {" + profiles + "})";
                session.run(cql, TransactionConfig.empty());
            }

            session.close();
        }
    }

    /**
     * 更新已存在节点的属性。
     * Node中必须提供id属性以及label，通过id和label匹配到节点
     *
     * @param nodes
     */
    public void updateNode(List<Node> nodes) {
        try (Session session = _driver.session()) {
            for (Node node : nodes) {
                String labels = StringUtils.join(node.getLabels(), ':'),
                        id = node.getProfiles().get("id").toString();

                String profiles = "";
                for (Map.Entry entry : node.getProfiles().entrySet()) {
                    profiles += ",e." + entry.getKey() + "=\"" + entry.getValue() + "\"";
                }
                profiles = profiles.substring(1);

                String cql = "match (e:" + labels + ") where e.id = \"" + id + "\" set " + profiles;
                session.run(cql, TransactionConfig.empty());
            }

            session.close();
        }
    }

    /**
     * 删除节点，同时删除与节点关联的关系。
     * Node中必须提供id属性以及label，通过id和label匹配到节点
     *
     * @param nodes
     */
    public void deleteNode(List<Node> nodes) {
        try (Session session = _driver.session()) {
            for (Node node : nodes) {
                String labels = StringUtils.join(node.getLabels(), ':'),
                        id = (String) node.getProfiles().get("id");

                String cql = StringUtils.isEmpty(id) ? "match (e:" + labels + ") detach delete e" :
                        "match (e:" + labels + ") where e.id = \"" + id + "\" detach delete e";
                session.run(cql, TransactionConfig.empty());
            }

            session.close();
        }
    }

    /**
     * 创建从现有节点node1到node2的关系。
     * 根据id和label匹配到node1和node2
     *
     * @param node1
     * @param node2
     * @param relationship
     */
    public void createRelationship(Node node1, Node node2, Relationship relationship) {
        String labels_1 = StringUtils.join(node1.getLabels(), ':'),
                id_1 = (String) node1.getProfiles().get("id"),
                labels_2 = StringUtils.join(node2.getLabels(), ':'),
                id_2 = (String) node2.getProfiles().get("id");

        // 关系的标签和属性
        String labels = StringUtils.join(relationship.getLabels(), ':');

        String profiles = "";
        for (Map.Entry entry : relationship.getProfiles().entrySet()) {
            profiles += "," + entry.getKey() + ":\"" + entry.getValue() + "\"";
        }
        profiles = profiles.substring(1);

        try (Session session = _driver.session()) {
            String cql = "match (e1:" + labels_1 + "), (e2:" + labels_2 + ") " +
                    "where e1.id = \"" + id_1 + "\" and e2.id = \"" + id_2 + "\" " +
                    "create (e1)-[:" + labels + " {" + profiles + "}]->(e2) ";
            session.run(cql, TransactionConfig.empty());

            session.close();
        }
    }

    /**
     * 更新从现有节点node1到node2的所有关系的属性。
     * Relationship中必须提供id属性以及label，通过id和label匹配到关系
     *
     * @param relationships
     */
    public void updateRelationship(List<Relationship> relationships) {
        try (Session session = _driver.session()) {
            for (Relationship relationship : relationships) {
                String labels = StringUtils.join(relationship.getLabels(), ':'),
                        id = relationship.getProfiles().get("id").toString();

                String profiles = "";
                for (Map.Entry entry : relationship.getProfiles().entrySet()) {
                    profiles += ",e." + entry.getKey() + "=\"" + entry.getValue() + "\"";
                }
                profiles = profiles.substring(1);

                String cql = "match (e1)-[r:" + labels + "]->(e2) where r.id=\"" + id + "\" set " + profiles;
                session.run(cql, TransactionConfig.empty());
            }

            session.close();
        }
    }

    /**
     * 删除关系，不影响现有节点。
     * Relationship中必须提供id属性以及label，通过id和label匹配到关系
     *
     * @param relationships
     */
    public void deleteRelationship(List<Relationship> relationships) {
        try (Session session = _driver.session()) {
            for (Relationship relationship : relationships) {
                String labels = StringUtils.join(relationship.getLabels(), ':'),
                        id = relationship.getProfiles().get("id").toString();

                String cql = "match (e1)-[r:" + labels + "]->(e2) where r.id=\"" + id + "\" delete r";
                session.run(cql, TransactionConfig.empty());
            }

            session.close();
        }
    }

    public static void main(String[] args) {
        Neo4jHelper neo4jHelper = Neo4jHelper.build("bolt://127.0.0.1:7687", "neo4j", "");

        long beginTime = System.currentTimeMillis();

        System.out.println("clear all data...");
        clearAllData(neo4jHelper);

//        System.out.println("simple create a node...");
//        simpleTest(neo4jHelper);

        System.out.println("load test, 1w rows...");
        loadTest(neo4jHelper);

        System.out.println("simple create a relationship...");
        simpleRelationship(neo4jHelper);

        long endTime = System.currentTimeMillis();

        System.out.println(String.format("test finished.duration:%s", endTime - beginTime));
    }

    private static void simpleRelationship(Neo4jHelper neo4jHelper) {
        Node node1 = new Node(null, "label1");
        node1.getProfiles().put("id", "1");

        Node node2 = new Node(null, "label1");
        node2.getProfiles().put("id", "2");

        Relationship relationship = new Relationship(null, "RE");
        relationship.getProfiles().put("name", "测试关系");

        neo4jHelper.createRelationship(node1, node2, relationship);
    }

    public static void clearAllData(Neo4jHelper neo4jHelper) {
        Node node = new Node(null, "label1", "label2");

        List<Node> nodes = new ArrayList<>();
        nodes.add(node);
        neo4jHelper.deleteNode(nodes);
    }

    public static void simpleTest(Neo4jHelper neo4jHelper) {
        Map profiles = new HashMap();
        profiles.put("id", "1");
        profiles.put("code", "01");
        profiles.put("name", "A01");
        Node node = new Node(profiles, "label1", "label2");

        List<Node> nodes = new ArrayList<>();
        nodes.add(node);
        neo4jHelper.createNode(nodes);
    }

    public static void loadTest(Neo4jHelper neo4jHelper) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Map profiles = new HashMap();
            profiles.put("id", i);

            String code = "00000" + i;
            code = code.substring(code.length() - 4);
            profiles.put("code", code);
            profiles.put("name", "A" + code);
            Node node = new Node(profiles, "label1", "label2");
            nodes.add(node);
        }
        neo4jHelper.createNode(nodes);
    }
}

