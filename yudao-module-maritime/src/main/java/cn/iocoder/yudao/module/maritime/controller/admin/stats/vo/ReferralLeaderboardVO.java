package cn.iocoder.yudao.module.maritime.controller.admin.stats.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 推荐人排行榜 VO")
@Data
public class ReferralLeaderboardVO {

    @Schema(description = "推荐人会员ID")
    private Long memberId;

    @Schema(description = "推荐人姓名（脱敏）")
    private String memberName;

    @Schema(description = "推荐人手机（脱敏）")
    private String memberPhone;

    @Schema(description = "推荐成功数")
    private Long referralCount;

    @Schema(description = "排名")
    private Integer rank;

}
