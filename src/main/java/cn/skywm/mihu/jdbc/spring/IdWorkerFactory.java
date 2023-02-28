package cn.skywm.mihu.jdbc.spring;

public class IdWorkerFactory {
    public static IdWorker snowflake(long workerId, long datacenterId) {
        return new SnowflakeIdWorker(workerId, datacenterId);
    }
}
