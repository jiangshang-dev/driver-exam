package com.heima.controller.vod;

import com.aliyuncs.vod.model.v20170321.GetPlayInfoResponse;
import com.heima.common.utils.Result;
import com.heima.vod.entity.VideoInfoEntity;
import com.heima.vod.service.AliyunVodService;
import com.heima.vod.service.VideoInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vod")
public class VodController {

    @Resource
    private AliyunVodService aliyunVodService;

    @Resource
    private VideoInfoService videoInfoService;

    /**
     * 获取视频播放地址
     * @param videoId 视频ID
     * @return 播放地址
     */
    @GetMapping("/play/info")
    public Result<Map<String, Object>> getPlayInfo(@RequestParam(value = "videoId") String videoId) {
        try {
            GetPlayInfoResponse response = aliyunVodService.getPlayInfo(videoId);

            List<GetPlayInfoResponse.PlayInfo> playInfoList = response.getPlayInfoList();
            Map<String, Object> result = new HashMap<>();
            result.put("videoBase", response.getVideoBase());

            if (!playInfoList.isEmpty()) {
                result.put("playUrl", playInfoList.getFirst().getPlayURL());
            }

            return new Result<Map<String, Object>>().ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<Map<String, Object>>().error(e.getMessage());
        }
    }

    /**
     * 获取视频详情
     * @param id 视频ID
     * @return 视频详情
     */
    @GetMapping("/playerUrl/{id}")
    public Result<VideoInfoEntity> getVideoDetail(@PathVariable Long id) {
        try {
            VideoInfoEntity video = videoInfoService.getById(id);
            if (video != null) {
                return new Result<VideoInfoEntity>().ok(video);
            } else {
                return new Result<VideoInfoEntity>().error("视频不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<VideoInfoEntity>().error(e.getMessage());
        }
    }
}
