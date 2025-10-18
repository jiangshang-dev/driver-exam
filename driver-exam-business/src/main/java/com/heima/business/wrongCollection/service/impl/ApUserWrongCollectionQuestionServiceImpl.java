package com.heima.business.wrongCollection.service.impl;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.business.auth.entity.ApUserEntity;
import com.heima.business.auth.service.user.ApUserService;
import com.heima.business.enums.RecordTypeEnums;
import com.heima.business.question.entity.ApDriveQuestionEntity;
import com.heima.business.question.service.ApDriveQuestionService;
import com.heima.business.uniad.dto.ApAdWatchRecordDTO;
import com.heima.business.uniad.entity.ApAdWatchRecordEntity;
import com.heima.common.page.PageData;
import jakarta.annotation.Resource;
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

    @Resource
    private ApUserService apUserService;

    @Resource
    private ApDriveQuestionService apDriveQuestionService;

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

    @Override
    public PageData<ApUserWrongCollectionQuestionDTO> pagesWrong(Map<String, Object> params) {
        String id = (String) params.get("id");
        String phone = (String) params.get("phone");
        String limitStr = (String) params.get("limit");
        String pageNoStr = (String) params.get("page");
        String recordType = (String) params.get("recordType");

        QueryWrapper<ApUserWrongCollectionQuestionEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("record_type", 1);

        // 解析分页参数
        int limit = StrUtil.isBlank(limitStr) ? 10 : Integer.parseInt(limitStr);  // 默认每页10条
        int pageNo = StrUtil.isBlank(pageNoStr) ? 1 : Integer.parseInt(pageNoStr); // 默认第一页

        // 使用 MyBatis Plus 分页查询
        Page<ApUserWrongCollectionQuestionEntity> page = new Page<>(pageNo, limit);
        page = baseDao.selectPage(page, wrapper);

        List<ApUserWrongCollectionQuestionEntity> records = page.getRecords();
        records.forEach(record -> {
            ApUserEntity user = apUserService.getById(record.getUserId());
            if (user != null) {
                record.setUserNickname(StrUtil.isBlank(user.getPhone())? user.getRealName() : user.getPhone());
            } else {
                record.setUserNickname("未知用户");
            }

            ApDriveQuestionEntity driveQuestion = apDriveQuestionService.selectById(record.getQuestionId());
            if (driveQuestion != null) {
                record.setQuestionContent(driveQuestion.getQuestionContent());
                record.setImageUrl(driveQuestion.getImageUrl());
                record.setCorrectAnswer(driveQuestion.getCorrectAnswer());
            }
        });

        if (StrUtil.isNotBlank(phone)) records = records.stream().filter(record -> record.getUserNickname().contains(phone)).toList();
        // 转换实体类到 DTO
        List<ApUserWrongCollectionQuestionDTO> list = ConvertUtils.sourceToTarget(records, ApUserWrongCollectionQuestionDTO.class);
        return new PageData<>(list, page.getTotal());
    }

    @Override
    public PageData<ApUserWrongCollectionQuestionDTO> pagesCollection(Map<String, Object> params) {
        String id = (String) params.get("id");
        String phone = (String) params.get("phone");
        String limitStr = (String) params.get("limit");
        String pageNoStr = (String) params.get("page");
        String recordType = (String) params.get("recordType");

        QueryWrapper<ApUserWrongCollectionQuestionEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("record_type", 0);

        // 解析分页参数
        int limit = StrUtil.isBlank(limitStr) ? 10 : Integer.parseInt(limitStr);  // 默认每页10条
        int pageNo = StrUtil.isBlank(pageNoStr) ? 1 : Integer.parseInt(pageNoStr); // 默认第一页

        // 使用 MyBatis Plus 分页查询
        Page<ApUserWrongCollectionQuestionEntity> page = new Page<>(pageNo, limit);
        page = baseDao.selectPage(page, wrapper);

        List<ApUserWrongCollectionQuestionEntity> records = page.getRecords();
        records.forEach(record -> {
            ApUserEntity user = apUserService.getById(record.getUserId());
            if (user != null) {
                record.setUserNickname(StrUtil.isBlank(user.getPhone())? user.getRealName() : user.getPhone());
            } else {
                record.setUserNickname("未知用户");
            }

            ApDriveQuestionEntity driveQuestion = apDriveQuestionService.selectById(record.getQuestionId());
            if (driveQuestion != null) {
                record.setQuestionContent(driveQuestion.getQuestionContent());
                record.setImageUrl(driveQuestion.getImageUrl());
                record.setCorrectAnswer(driveQuestion.getCorrectAnswer());
            }
        });

        if (StrUtil.isNotBlank(phone)) records = records.stream().filter(record -> record.getUserNickname().contains(phone)).toList();
        // 转换实体类到 DTO
        List<ApUserWrongCollectionQuestionDTO> list = ConvertUtils.sourceToTarget(records, ApUserWrongCollectionQuestionDTO.class);
        return new PageData<>(list, page.getTotal());
    }
}