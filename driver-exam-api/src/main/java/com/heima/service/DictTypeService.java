package com.heima.service;

import com.heima.common.service.BaseService;
import com.heima.dto.SysDictDataDTO;
import com.heima.entity.SysDictTypeEntity;

import java.util.List;

/**
 * 数据字典
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface DictTypeService extends BaseService<SysDictTypeEntity> {

    List<SysDictDataDTO> getByDictName(String dictCode);
}