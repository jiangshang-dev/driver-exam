package com.heima.ai.dao;

import com.heima.ai.entity.AiChatMessage;
import com.heima.common.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiChatMessageDao extends BaseDao<AiChatMessage> {
}
