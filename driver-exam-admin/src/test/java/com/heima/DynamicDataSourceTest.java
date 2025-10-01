/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package com.heima;

import com.heima.modules.sys.dto.SysDictDataDTO;
import com.heima.modules.sys.service.SysDictTypeService;
import com.heima.service.DynamicDataSourceTestService;
import jakarta.annotation.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;


/**
 * 多数据源测试
 *
 * @author Mark sunlightcs@gmail.com
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DynamicDataSourceTest {
    @Resource
    private DynamicDataSourceTestService dynamicDataSourceTestService;

    @Resource
    private SysDictTypeService sysDictTypeService;

    @Test
    public void test() {
        Long id = 1067246875800000001L;

        dynamicDataSourceTestService.updateUser(id);
        dynamicDataSourceTestService.updateUserBySlave1(id);
        //dynamicDataSourceTestService.updateUserBySlave2(id);
    }

    @Test
    public void test1() {
        List<SysDictDataDTO> knowledge = sysDictTypeService.getByDictName("knowledge");
        System.out.println(knowledge);
    }


}
