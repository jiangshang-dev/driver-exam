package com.heima.business.traffic.service;

import com.heima.business.traffic.dto.ApTrafficSignDTO;
import com.heima.business.traffic.entity.ApTrafficSignEntity;
import com.heima.common.page.PageData;
import com.heima.common.service.CrudService;

import java.util.Map;

/**
 * 交通标志表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-07
 */
public interface ApTrafficSignService extends CrudService<ApTrafficSignEntity, ApTrafficSignDTO> {

    PageData<ApTrafficSignDTO> pageApp(Map<String, Object> params);
}