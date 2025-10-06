package com.heima.dao;

import com.heima.common.dao.BaseDao;
import com.heima.entity.SysDictTypeEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysDictTypeDao extends BaseDao<SysDictTypeEntity> {

    Long getDictTypeByDictName(String dictCode);
}