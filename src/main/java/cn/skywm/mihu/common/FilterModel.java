package cn.skywm.mihu.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilterModel {
    private String search;
    private List<Filter> filters = new ArrayList<>();
    private List<Group> groups = new ArrayList<>();
    private List<Sort> sorts = new ArrayList<>();
    private int page = -1;
    private int pageSize = 0;

    public FilterModel createFilter(String field, String oper, Object[] values) {
        Filter filter = new Filter(field, oper, values);
        filters.add(filter);
        return this;
    }

    public FilterModel createFilter(String field, String oper, int neg, Object[] values) {
        Filter filter = new Filter(field, oper, neg, values);
        filters.add(filter);
        return this;
    }

    public Filter Or() throws Exception {
        Filter filter = new Filter(Opers.Or);
        filters.add(filter);
        return filter;
    }

    public Filter last(int pos) {
        return filters.get(filters.size() - 1);
    }

    public FilterModel createGroup(String field) {
        Group group = new Group(field);
        groups.add(group);
        return this;
    }

    public FilterModel createSort(String field, String dir) {
        Sort sort = new Sort(field, dir);
        sorts.add(sort);
        return this;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filter) {
        this.filters = filter;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<Sort> getSorts() {
        return sorts;
    }

    public void setSorts(List<Sort> sort) {
        this.sorts = sort;
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

    public static class Filter {
        private String field;
        private String oper;
        private List<? super Serializable> values = new ArrayList<>();
        private String type;
        private int neg = 0;
        private List<Filter> filters = new ArrayList<>();

        public Filter() {}

        public Filter(String oper) throws Exception {
            if (oper.equals(Opers.And) || oper.equals(Opers.Or)) {
                this.oper = oper;
            } else
                throw new Exception("oper must be 'and' or 'or'");
        }
        public Filter(String field, String oper, Object[] values) {
            this(field, oper, 0, values);
        }

        public Filter(String field, String oper, int neg, Object[] values) {
            this.field = field;
            this.oper = oper;
            this.values = Arrays.asList(values);
            this.neg = neg;
        }

        public Filter createFilter(String field, String oper, Object[] values) {
            Filter filter = new Filter(field, oper, values);
            filters.add(filter);
            return this;
        }

        public Filter createFilter(String field, String oper, int neg, Object[] values) {
            Filter filter = new Filter(field, oper, neg, values);
            filters.add(filter);
            return this;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getOper() {
            return oper;
        }

        public void setOper(String oper) {
            this.oper = oper;
        }

        public List<? super Serializable> getValues() {
            return values;
        }

        public void setValues(List<? super Serializable> values) {
            this.values = values;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getNeg() {
            return neg;
        }

        public void setNeg(int neg) {
            this.neg = neg;
        }

        public List<Filter> getFilters() {
            return filters;
        }

        public void setFilters(List<Filter> filters) {
            this.filters = filters;
        }
    }

    public static class Group {
        private String field;

        public Group() {}

        public Group(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }
    }
    public static class Sort {
        private String field;
        private String dir;

        public Sort() {}

        public Sort(String field, String dir) {
            this.field = field;
            this.dir = dir;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }
    }

    public static class Opers {
        public static final String Eq = "=";
        public static final String NEq = "<>";
        public static final String GT = ">";
        public static final String LT = "<";
        public static final String GE = ">=";
        public static final String LE = "<=";
        public static final String Like = "like";
        public static final String In = "in";
        public static final String Between = "between";
        public static final String InSet = "inset";
        public static final String CONTAIN = "contain";
        protected static final String And = "and";
        protected static final String Or = "or";
    }
    public static class SortDir {
        public static final String ASC = "asc";
        public static final String DESC = "desc";
    }
}
