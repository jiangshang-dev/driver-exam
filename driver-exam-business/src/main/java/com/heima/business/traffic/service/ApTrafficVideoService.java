package com.heima.business.traffic.service;

import com.heima.business.traffic.dto.ApTrafficVideoDTO;
import com.heima.business.traffic.entity.ApTrafficVideoEntity;
import com.heima.common.page.PageData;
import com.heima.common.service.CrudService;

import java.util.Map;

/**
 * 交规视频表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-07
 */
public interface ApTrafficVideoService extends CrudService<ApTrafficVideoEntity, ApTrafficVideoDTO> {

    PageData<ApTrafficVideoDTO> pageApp(Map<String, Object> params);

    boolean liked(String id, String token);

    boolean videoPlayer(String id, String token);

    ApTrafficVideoDTO getInfoById(Long id, String token);
}