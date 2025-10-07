package com.heima.business.wrongCollection.service.impl;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.business.enums.RecordTypeEnums;
import com.heima.common.page.PageData;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.business.wrongCollection.dao.ApUserWrongCollectionQuestionDao;
import com.heima.business.wrongCollection.dto.ApUserWrongCollectionQuestionDTO;
import com.heima.business.wrongCollection.entity.ApUserWrongCollectionQuestionEntity;
import com.heima.business.wrongCollection.service.ApUserWrongCollectionQuestionService;
import com.heima.common.service.impl.CrudServiceImpl;
import com.heima.common.utils.ConvertUtils;

import cn.hutool.core.util.StrUtil;

/**
 * 用户收藏_错题本表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-06
 */
@Service
public class ApUserWrongCollectionQuestionServiceImpl extends CrudServiceImpl<ApUserWrongCollectionQuestionDao, ApUserWrongCollectionQuestionEntity, ApUserWrongCollectionQuestionDTO> implements ApUserWrongCollectionQuestionService {

    @Override
    public QueryWrapper<ApUserWrongCollectionQuestionEntity> getWrapper(Map<String, Object> params){
        String userId = (String)params.get("userId");
        String questionId = (String)params.get("questionId");
        String subject = (String)params.get("subject");

        if (StrUtil.isBlank(userId)) {
            return null;
        }

        QueryWrapper<ApUserWrongCollectionQuestionEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(userId), "user_id", userId);
        wrapper.eq(StrUtil.isNotBlank(questionId), "question_id", questionId);
        wrapper.eq(StrUtil.isNotBlank(subject), "subject", questionId);

        return wrapper;
    }
    
    @Override
    public ApUserWrongCollectionQuestionDTO getOne(QueryWrapper<ApUserWrongCollectionQuestionEntity> wrapper) {
        ApUserWrongCollectionQuestionEntity entity = this.baseDao.selectOne(wrapper);
        if (entity == null) {
            return null;
        }
        return ConvertUtils.sourceToTarget(entity, ApUserWrongCollectionQuestionDTO.class);
    }

    @Override
    public PageData<ApUserWrongCollectionQuestionDTO> myCollectPage(Map<String, Object> params) {
        String userId = (String)params.get("userId");
        String subject = (String)params.get("subject");
        String pageNo = (String)params.get("pageNo");
        int pageNum = Integer.parseInt(pageNo);
        int offset = (pageNum - 1) * 20;
        List<ApUserWrongCollectionQuestionDTO> pageData = baseDao.myCollectPage(userId, subject, offset, 20);
        PageData page = new PageData(pageData, pageData.size());
        page.setList(pageData);
        return page;
    }

    @Override
    public PageData<ApUserWrongCollectionQuestionDTO> myWrongPage(Map<String, Object> params) {
        String userId = (String)params.get("userId");
        String subject = (String)params.get("subject");
        String pageNo = (String)params.get("pageNo");
        int pageNum = Integer.parseInt(pageNo);
        int offset = (pageNum - 1) * 20;
        List<ApUserWrongCollectionQuestionDTO> pageData = baseDao.myWrongPage(userId, subject, offset, 20);
        PageData page = new PageData(pageData, pageData.size());
        page.setList(pageData);
        return page;
    }

    @Override
    public boolean clearWrong(Long userId) {
        LambdaQueryWrapper<ApUserWrongCollectionQuestionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApUserWrongCollectionQuestionEntity::getUserId, userId);
        queryWrapper.eq(ApUserWrongCollectionQuestionEntity::getRecordType, RecordTypeEnums.WRONG);
        List<ApUserWrongCollectionQuestionEntity> selectedList = baseDao.selectList(queryWrapper);
        int delete = baseDao.deleteByIds(selectedList);
        return delete > 0;
    }
}