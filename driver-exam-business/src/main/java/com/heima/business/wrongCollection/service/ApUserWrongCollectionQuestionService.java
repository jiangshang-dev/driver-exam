package com.heima.business.wrongCollection.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.business.wrongCollection.dto.ApUserWrongCollectionQuestionDTO;
import com.heima.business.wrongCollection.entity.ApUserWrongCollectionQuestionEntity;
import com.heima.common.page.PageData;
import com.heima.common.service.CrudService;

import java.util.Map;

/**
 * 用户收藏_错题本表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-06
 */
public interface ApUserWrongCollectionQuestionService extends CrudService<ApUserWrongCollectionQuestionEntity, ApUserWrongCollectionQuestionDTO> {
    
    /**
     * 根据条件查询单个记录
     * @param wrapper 查询条件
     * @return DTO对象
     */
    ApUserWrongCollectionQuestionDTO getOne(QueryWrapper<ApUserWrongCollectionQuestionEntity> wrapper);

    PageData<ApUserWrongCollectionQuestionDTO> myCollectPage(Map<String, Object> params);
}