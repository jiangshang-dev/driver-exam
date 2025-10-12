package com.heima.business.constants;

public class MqContants {

    /**
     * 交换机
     */
    public final static String DRIVER_EXCHANGE = "driver.topic";
    /**
     * 新增和修改的routingKey
     */
    public final static String DRIVER_INSERT_KEY = "driver.insert.key";
    /**
     * 删除的routingKey
     */
    public final static String DRIVER_DELETE_KEY = "driver.delete.key";
    /**
     * 监听新增和修改的队列
     */
    public final static String DRIVER_INSERT_QUEUE = "driver.insert.queue";
    /**
     * 监听删除的队列
     */
    public final static String DRIVER_DELETE_QUEUE = "driver.delete.queue";

}
