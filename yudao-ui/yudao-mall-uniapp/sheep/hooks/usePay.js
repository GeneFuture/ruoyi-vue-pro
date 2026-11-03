import dayjs from 'dayjs';

/**
 * 倒计时
 * @param toTime   截止时间
 * @param fromTime 起始时间，默认当前时间
 * @return {{s: string, ms: number, h: string, m: string}} 持续时间
 */
export function useDurationTime(toTime, fromTime = '') {
  const end = getDayjsTime(toTime);
  const start = fromTime === '' ? dayjs() : getDayjsTime(fromTime);
  const duration = end && start ? end.diff(start) : 0;
  const durationTime = dayjs.duration(duration);
  return {
    h: (durationTime.months() * 30 * 24 + durationTime.days() * 24 + durationTime.hours())
      .toString()
      .padStart(2, '0'),
    m: durationTime.minutes().toString().padStart(2, '0'),
    s: durationTime.seconds().toString().padStart(2, '0'),
    ms: duration,
  };
}

/**
 * 转换为 Dayjs
 * @param {any} time 时间
 * @return {dayjs.Dayjs}
 */
function getDayjsTime(time) {
  if (time === null || time === undefined || time === '') {
    return undefined;
  }
  time = time.toString();
  if (time.indexOf('-') > 0) {
    // 'date'
    return dayjs(time);
  }
  if (time.length > 10) {
    // 'timestamp'
    return dayjs(parseInt(time));
  }
  if (time.length === 10) {
    // 'unixTime'
    return dayjs.unix(parseInt(time));
  }
}

/**
 * 将分转成元
 *
 * @param price 分，例如说 100 分
 * @returns {string} 元，例如说 1.00 元
 */
export function fen2yuan(price) {
  return (price / 100.0).toFixed(2);
}
