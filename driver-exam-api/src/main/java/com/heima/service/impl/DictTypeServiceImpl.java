package com.heima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.common.service.impl.BaseServiceImpl;
import com.heima.common.utils.ConvertUtils;
import com.heima.dao.SysDictDataDao;
import com.heima.dao.SysDictTypeDao;
import com.heima.dto.SysDictDataDTO;
import com.heima.entity.DictDataEntity;
import com.heima.entity.SysDictTypeEntity;
import com.heima.service.DictTypeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DictTypeServiceImpl extends BaseServiceImpl<SysDictTypeDao, SysDictTypeEntity> implements DictTypeService {
    private final SysDictDataDao sysDictDataDao;

    @Override
    public List<SysDictDataDTO> getByDictName(String dictCode) {
        Long typeId = baseDao.getDictTypeByDictName(dictCode);
        QueryWrapper<DictDataEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_type_id", typeId);
        List<DictDataEntity> selectedList = sysDictDataDao.selectList(wrapper);
        List<SysDictDataDTO> list = ConvertUtils.sourceToTarget(selectedList, SysDictDataDTO.class);
        return list;
    }

}
