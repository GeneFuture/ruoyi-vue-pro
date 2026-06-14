package cn.iocoder.yudao.module.maritime.dal.mysql.memberSubscribe;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.memberSubscribe.MemberSubscribeDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberSubscribeMapper extends BaseMapperX<MemberSubscribeDO> {

    default MemberSubscribeDO selectByMemberAndTemplate(Long memberId, String templateTitle) {
        return selectOne(new LambdaQueryWrapperX<MemberSubscribeDO>()
                .eq(MemberSubscribeDO::getMemberId, memberId)
                .eq(MemberSubscribeDO::getTemplateTitle, templateTitle));
    }

    default boolean isSubscribed(Long memberId, String templateTitle) {
        MemberSubscribeDO record = selectByMemberAndTemplate(memberId, templateTitle);
        return record != null && Boolean.TRUE.equals(record.getSubscribed());
    }

    /** upsert：存在则更新 subscribed 字段，不存在则插入 */
    default void upsert(MemberSubscribeDO record) {
        MemberSubscribeDO existing = selectByMemberAndTemplate(record.getMemberId(), record.getTemplateTitle());
        if (existing == null) {
            insert(record);
        } else {
            MemberSubscribeDO update = new MemberSubscribeDO();
            update.setId(existing.getId());
            update.setSubscribed(record.getSubscribed());
            updateById(update);
        }
    }

}
