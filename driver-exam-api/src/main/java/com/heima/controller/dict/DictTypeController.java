package com.heima.controller.dict;

import com.heima.common.utils.Result;
import com.heima.dto.SysDictDataDTO;
import com.heima.service.DictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典类型
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("/dict/type")
@Tag(name = "字典类型")
@AllArgsConstructor
public class DictTypeController {
    private final DictTypeService sysDictTypeService;

    @GetMapping("getByDictName")
    @Operation(summary = "信息")
    public Result<List<SysDictDataDTO>> getByDictName(String dictCode) {
        List<SysDictDataDTO> data = sysDictTypeService.getByDictName(dictCode);
        return new Result<List<SysDictDataDTO>>().ok(data);
    }
}
