package cn.skywm.mihu.common;

public class SafException extends Exception {
    private String code;

    protected  SafException() {};
    public SafException(String code, String message) {
        super(message);

        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
