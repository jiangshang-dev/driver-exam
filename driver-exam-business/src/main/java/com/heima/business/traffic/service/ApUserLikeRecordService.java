package com.heima.business.traffic.service;

import com.heima.business.traffic.dto.ApTrafficVideoDTO;
import com.heima.business.traffic.dto.ApUserLikeRecordDTO;
import com.heima.business.traffic.entity.ApUserLikeRecordEntity;
import com.heima.common.page.PageData;
import com.heima.common.service.CrudService;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 用户点赞记录表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-11
 */
public interface ApUserLikeRecordService extends CrudService<ApUserLikeRecordEntity, ApUserLikeRecordDTO> {

    PageData<ApUserLikeRecordDTO> pages(Map<String, Object> params);
}