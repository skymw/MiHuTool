package cn.skywm.mihu.common;

import java.util.Map;

public class QueryModel {
    private String search;
    private Map filter;
    private Map sort;
    private int page;
    private int pageSize;

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Map getFilter() {
        return filter;
    }

    public void setFilter(Map filter) {
        this.filter = filter;
    }

    public Map getSort() {
        return sort;
    }

    public void setSort(Map sort) {
        this.sort = sort;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
