package cn.iocoder.yudao.framework.mybatis.core.handler;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 通用参数填充实现类
 *
 * 如果没有显式的对通用参数进行赋值，这里会对通用参数进行填充、赋值
 *
 * @author hexiaowu
 */
public class DefaultDBFieldHandler implements MetaObjectHandler {

    /**
     * 系统用户编号
     *
     * 支付回调、定时任务、异步事件等场景没有登录上下文，此时 creator / updater 使用该值兜底。
     * 项目建表规范中 creator / updater 为 NOT NULL，而 MyBatis Plus 对带填充注解的字段
     * 在 INSERT / UPDATE 时会强制写入（不会因为 null 跳过），因此这里必须保证非空。
     */
    private static final String SYSTEM_USER_ID = "0";

    @Override
    @SuppressWarnings("PatternVariableCanBeUsed")
    public void insertFill(MetaObject metaObject) {
        if (Objects.nonNull(metaObject) && metaObject.getOriginalObject() instanceof BaseDO) {
            BaseDO baseDO = (BaseDO) metaObject.getOriginalObject();

            LocalDateTime current = LocalDateTime.now();
            // 创建时间为空，则以当前时间为插入时间
            if (Objects.isNull(baseDO.getCreateTime())) {
                baseDO.setCreateTime(current);
            }
            // 更新时间为空，则以当前时间为更新时间
            if (Objects.isNull(baseDO.getUpdateTime())) {
                baseDO.setUpdateTime(current);
            }

            // 创建人 / 更新人为空，则以当前登录用户填充；无登录上下文（支付回调、定时任务等）则使用系统用户
            String userId = getUserId();
            if (Objects.isNull(baseDO.getCreator())) {
                baseDO.setCreator(userId);
            }
            if (Objects.isNull(baseDO.getUpdater())) {
                baseDO.setUpdater(userId);
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时间为空，则以当前时间为更新时间
        Object modifyTime = getFieldValByName("updateTime", metaObject);
        if (Objects.isNull(modifyTime)) {
            setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        }

        // 更新人为空，则以当前登录用户填充；无登录上下文（支付回调、定时任务等）则使用系统用户
        Object modifier = getFieldValByName("updater", metaObject);
        if (Objects.isNull(modifier)) {
            setFieldValByName("updater", getUserId(), metaObject);
        }
    }

    /**
     * 获得当前登录用户编号；无登录上下文时返回系统用户编号
     */
    private static String getUserId() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return userId != null ? userId.toString() : SYSTEM_USER_ID;
    }
}
