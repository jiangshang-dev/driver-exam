package com.heima;

import com.heima.business.common.aliyun.GreenTextScan;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class AliyunTest {

    @Resource
    private GreenTextScan greenTextScan;

    @Test
    public void test() {
        try {
            Map textScan = greenTextScan.greeTextScan("测试");
            System.out.println(textScan);
            System.out.println("-----------");
            Map textScan1 = greenTextScan.greeTextScan("白纸运动");
            System.out.println(textScan1);
            System.out.println("------------");
            Map textScan2 = greenTextScan.greeTextScan("冰毒");
            System.out.println(textScan2);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
