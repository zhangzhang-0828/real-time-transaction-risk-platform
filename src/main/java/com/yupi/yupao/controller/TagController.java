package com.yupi.yupao.controller;

import com.yupi.yupao.common.BaseResponse;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.common.ResultUtils;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.model.domain.Tag;
import com.yupi.yupao.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 标签接口
 *
 */
@RestController
@RequestMapping("/tag")
@CrossOrigin(origins = {"http://localhost:3000"})
@Slf4j
public class TagController {

    @Resource
    private TagService tagService;

    /**
     * 获取所有有效标签
     */
    @GetMapping("/list")
    public BaseResponse<List<Tag>> listTags() {
        List<Tag> tagList = tagService.listValidTags();
        return ResultUtils.success(tagList);
    }

    /**
     * 根据类型获取标签
     */
    @GetMapping("/list/type")
    public BaseResponse<List<Tag>> listTagsByType(@RequestParam Integer tagType) {
        if (tagType == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<Tag> tagList = tagService.listTagsByType(tagType);
        return ResultUtils.success(tagList);
    }

    /**
     * 获取所有标签名称（用于前端校验）
     */
    @GetMapping("/list/names")
    public BaseResponse<List<String>> listTagNames() {
        List<String> tagNames = tagService.listAllTagNames();
        return ResultUtils.success(tagNames);
    }
}
