package cn.skywm.mihu.common;

public class StaleObjectStateException extends SafException {
    public StaleObjectStateException() {
        super("10020", "记录不存在或版本不正确.");
    }
}
