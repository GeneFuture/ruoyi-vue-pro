package cn.iocoder.yudao.module.maritime.job;

import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.mq.event.BalanceReminderEvent;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BalanceReminderJobTest {

    @Mock
    private EnrollmentMapper enrollmentMapper;
    @Mock
    private CourseSessionMapper courseSessionMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BalanceReminderJob balanceReminderJob;

    @Test
    public void testExecute_skipRemindedWithin3Days() throws Exception {
        LocalDate today = LocalDate.now();
        CourseSessionDO session = CourseSessionDO.builder()
                .id(100L).sessionName("测试班期").startDate(today.plusDays(5)).build();
        EnrollmentDO remindedRecently = EnrollmentDO.builder()
                .id(1L).memberId(10L).sessionId(100L)
                .balanceAmount(new BigDecimal("100.00"))
                .balanceDueDate(today.plusDays(1))
                .lastBalanceRemindDate(today.minusDays(1))
                .build();
        EnrollmentDO neverReminded = EnrollmentDO.builder()
                .id(2L).memberId(20L).sessionId(100L)
                .balanceAmount(new BigDecimal("200.00"))
                .balanceDueDate(today.plusDays(2))
                .build();

        when(enrollmentMapper.selectDepositedWithBalanceDueBefore(any())).thenReturn(List.of(remindedRecently, neverReminded));
        when(courseSessionMapper.selectList(any(SFunction.class), anyCollection())).thenReturn(List.of(session));

        String result = balanceReminderJob.execute("");

        ArgumentCaptor<BalanceReminderEvent> eventCaptor = ArgumentCaptor.forClass(BalanceReminderEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        assertEquals(2L, eventCaptor.getValue().getEnrollmentId());

        ArgumentCaptor<EnrollmentDO> updateCaptor = ArgumentCaptor.forClass(EnrollmentDO.class);
        verify(enrollmentMapper, times(1)).updateById(updateCaptor.capture());
        assertEquals(2L, updateCaptor.getValue().getId());
        assertEquals(today, updateCaptor.getValue().getLastBalanceRemindDate());
        assertEquals("共扫描 2 条，成功发送 1 条", result);
    }

}
