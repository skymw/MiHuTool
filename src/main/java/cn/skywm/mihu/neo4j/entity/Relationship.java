package cn.skywm.mihu.neo4j.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 图数据库 - 关系
 */
public class Relationship {
    private String[] labels;
    private Map<String, Object> profiles = new HashMap<>();

    public Relationship(Map<String, Object> profiles, String... labels) {
        if (profiles != null)
            this.profiles = profiles;
        this.labels = labels;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public Map<String, Object> getProfiles() {
        return profiles;
    }

    public void setProfiles(Map<String, Object> profiles) {
        this.profiles = profiles;
    }
}
