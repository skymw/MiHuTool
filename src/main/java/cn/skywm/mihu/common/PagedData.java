package cn.skywm.mihu.common;

import java.util.List;
import java.util.Map;

public class PagedData {
    private List data;
    private long totalSize;
    private Map ref;

    public PagedData(List data) {
        this.data = data;
    }
    public PagedData(List data, int totalSize) {
        this.data = data;
        this.totalSize = totalSize;
    }

    public List getData() {
        return data;
    }

    public void setData(List data) {
        this.data = data;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public Map getRef() {
        return ref;
    }

    public void setRef(Map ref) {
        this.ref = ref;
    }
}
