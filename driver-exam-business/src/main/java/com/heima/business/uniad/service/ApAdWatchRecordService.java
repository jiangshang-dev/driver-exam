package com.heima.business.uniad.service;

import com.heima.business.auth.dto.LoginResult;
import com.heima.business.uniad.dto.ApAdWatchRecordDTO;
import com.heima.business.uniad.entity.ApAdWatchRecordEntity;
import com.heima.common.page.PageData;
import com.heima.common.service.CrudService;

import java.util.Map;

/**
 * 广告观看记录表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-08
 */
public interface ApAdWatchRecordService extends CrudService<ApAdWatchRecordEntity, ApAdWatchRecordDTO> {

    LoginResult adWatchRecord(String token);

    PageData<ApAdWatchRecordDTO> pages(Map<String, Object> params);
}