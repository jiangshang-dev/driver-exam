package com.heima.business.wrongCollection.dao;

import com.heima.business.wrongCollection.dto.ApUserWrongCollectionQuestionDTO;
import com.heima.business.wrongCollection.entity.ApUserWrongCollectionQuestionEntity;
import com.heima.common.dao.BaseDao;
import com.heima.common.page.PageData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户收藏_错题本表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-06
 */
@Mapper
public interface ApUserWrongCollectionQuestionDao extends BaseDao<ApUserWrongCollectionQuestionEntity> {

    List<ApUserWrongCollectionQuestionDTO> myCollectPage(@Param("userId") String userId, @Param("subject") String subject,
                                                         @Param("pageNo") int pageNo, @Param("pageSize") int pageSize);
}