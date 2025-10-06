package com.heima.business.question.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.business.question.dao.ApDriveQuestionDao;
import com.heima.business.question.dto.ApDriveQuestionDTO;
import com.heima.business.question.dto.SyncDriveQuestionDTO;
import com.heima.business.question.entity.ApDriveQuestionEntity;
import com.heima.business.question.enums.ExamTypeEnums;
import com.heima.business.question.enums.SubjectEnums;
import com.heima.business.question.service.ApDriveQuestionService;
import com.heima.common.service.impl.CrudServiceImpl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 驾考题目表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-01
 */
@Slf4j
@Service
public class ApDriveQuestionServiceImpl extends CrudServiceImpl<ApDriveQuestionDao, ApDriveQuestionEntity, ApDriveQuestionDTO> implements ApDriveQuestionService {

    @Value("${drive.key}")
    private String apiKey;
    @Value("${drive.url}")
    private String apiUrl;

    @Override
    public QueryWrapper<ApDriveQuestionEntity> getWrapper(Map<String, Object> params){
        String id = (String)params.get("id");
        String subject = (String)params.get("subject");
        String examType = (String)params.get("examType");
        String questionType = (String)params.get("questionType");
        String questionTypeName = (String)params.get("questionTypeName");
        String questionContent = (String)params.get("questionContent");
        String tags = (String)params.get("tags");

        QueryWrapper<ApDriveQuestionEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);
        
        // 处理科目枚举查询条件
        if (StrUtil.isNotBlank(subject)) {
            try {
                // 尝试将字符串转换为枚举值
                SubjectEnums subjectEnum = SubjectEnums.getEnumByCode(Integer.valueOf(subject));
                wrapper.eq(subjectEnum != null, "subject", subjectEnum);
            } catch (NumberFormatException e) {
                // 如果不是数字，尝试按名称查找
                if (SubjectEnums.contains(subject)) {
                    wrapper.eq("subject", SubjectEnums.valueOf(subject));
                }
            }
        }
        
        // 处理考试类型枚举查询条件
        if (StrUtil.isNotBlank(examType) && ExamTypeEnums.contains(examType)) {
            wrapper.eq("exam_type", ExamTypeEnums.valueOf(examType));
        }
        
        wrapper.eq(StrUtil.isNotBlank(questionType), "question_type", questionType);
        wrapper.eq(StrUtil.isNotBlank(tags), "tags", tags);
        wrapper.eq(StrUtil.isNotBlank(questionTypeName), "question_type_name", questionTypeName);
        wrapper.like(StrUtil.isNotBlank(questionContent), "question_content", questionContent);

        return wrapper;
    }


    @Override
    public void syncQuestion(SubjectEnums subject, ExamTypeEnums examTypeEnums, String testType) {
        List<SyncDriveQuestionDTO> driveVos = syncDriveQuestion(subject, examTypeEnums, testType);

        // 确保 driveVos 中的对象是 DriveTopic 类型
        if (driveVos == null || driveVos.isEmpty()) {
            return;
        }

        // 如果 driveVos 中的对象不是 DriveTopic 类型，可以在这里进行转换
        List<ApDriveQuestionEntity> driveTopicList = driveVos.stream()
                .map(vo -> {
                    ApDriveQuestionEntity driveTopic = new ApDriveQuestionEntity();
                    driveTopic.setId(null);
                    // 直接使用枚举类型
                    driveTopic.setSubject(subject);
                    driveTopic.setExamType(examTypeEnums);
                    driveTopic.setQuestionContent(vo.getQuestion());
                    driveTopic.setOptionA(vo.getItem1());
                    driveTopic.setOptionB(vo.getItem2());
                    driveTopic.setOptionC(vo.getItem3());
                    driveTopic.setOptionD(vo.getItem4());
                    driveTopic.setCorrectAnswer(vo.getAnswer());
                    driveTopic.setQuestionExplain(vo.getExplains());
                    driveTopic.setImageUrl(vo.getUrl());
                    return driveTopic;
                })
                .toList();

        baseDao.insert(driveTopicList);
    }

    /**
     * 驾照题库获取题库
     *
     * @param subject  选择考试科目类型
     * @param examTypeEnums    驾照类型，可选择参数为：c1,c2,a1,a2,b1,b2；当subject=4时可省略
     * @param testType 测试类型，rand：随机测试（随机100个题目），order：顺序测试（所选科目全部题目）
     */
    public List<SyncDriveQuestionDTO> syncDriveQuestion(SubjectEnums subject, ExamTypeEnums examTypeEnums, String testType) {
        Map<String, String> map = new HashMap<>();
        map.put("key", apiKey);
        // 选择考试科目类型，1：科目1；4：科目4
        map.put("subject", subject.getValue().toString());
        // 驾照类型，可选择参数为：c1,c2,a1,a2,b1,b2；当subject=4时可省略
        map.put("model", examTypeEnums.getValue());
        // 测试类型，rand：随机测试（随机100个题目），order：顺序测试（所选科目全部题目）
        map.put("testType", testType);

        Map<String, String> headers = new HashMap<>();
        JSONObject request = request(apiUrl, map, headers);
        JSONArray jsonObject = (JSONArray) request.get("result");
        List<SyncDriveQuestionDTO> driveVos = JSONUtil.toList(jsonObject, SyncDriveQuestionDTO.class);
        return driveVos;
    }

    private static JSONObject request(String apiUrl, Map<String, String> params, Map<String, String> headers) {
        try {
            String formatUrl = String.format("%s?%s", apiUrl, params(params));
            String result = HttpRequest.post(formatUrl)
                    .headerMap(headers, true)// 头信息，多个头信息多次调用此方法即可
                    .timeout(20000)// 超时，毫秒
                    .execute().body();
            log.info("获取user_id:{}", result);
            return JSONUtil.parseObj(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String params(Map<String, String> map) {
        return map.entrySet().stream()
                .map(entry -> {
                    try {
                        return entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return entry.getKey() + "=" + entry.getValue();
                    }
                })
                .collect(Collectors.joining("&"));
    }
}