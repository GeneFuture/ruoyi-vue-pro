package cn.iocoder.yudao.module.maritime.dal.mysql.sessionVenue;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionVenue.SessionVenueDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 班期场地信息 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface SessionVenueMapper extends BaseMapperX<SessionVenueDO> {

    /** 按班期ID查询场地信息（一个班期只有一条） */
    default SessionVenueDO selectBySessionId(Long sessionId) {
        return selectOne(SessionVenueDO::getSessionId, sessionId);
    }

    /** 批量按班期ID查询场地信息（班期删除时级联清理用） */
    default List<SessionVenueDO> selectBySessionIds(Collection<Long> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SessionVenueDO>()
                .in(SessionVenueDO::getSessionId, sessionIds));
    }

    /**
     * 批量按班期ID查询场地信息，并按班期ID 建索引。
     * 列表接口要给每条班期补城市，用这个一次查完，避免逐条查库。
     */
    default Map<Long, SessionVenueDO> selectMapBySessionIds(Collection<Long> sessionIds) {
        return selectBySessionIds(sessionIds).stream()
                .collect(Collectors.toMap(SessionVenueDO::getSessionId, Function.identity(), (a, b) -> a));
    }

    /**
     * 按城市模糊查询班期ID：城市存在场地表上，班期列表按城市筛选时先走这里拿到范围。
     * 关键词用 like，前端传「青岛」能匹配到「青岛市」。
     */
    default List<Long> selectSessionIdsByCityLike(String city) {
        if (StrUtil.isBlank(city)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SessionVenueDO>()
                .like(SessionVenueDO::getCity, city)).stream()
                .map(SessionVenueDO::getSessionId)
                .collect(Collectors.toList());
    }

}
