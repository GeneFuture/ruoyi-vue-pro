// 发布前环境检查：避免把 HTTP / 本地地址打进微信小程序正式包
// 运行：npm run check:release-env
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const envPath = resolve(process.cwd(), '.env');
const env = Object.fromEntries(
  readFileSync(envPath, 'utf8')
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter((line) => line && !line.startsWith('#') && line.includes('='))
    .map((line) => {
      const index = line.indexOf('=');
      return [line.slice(0, index).trim(), line.slice(index + 1).trim()];
    }),
);

const useCallContainer = (env.SHOPRO_USE_CALL_CONTAINER || '') === 'true';
let failed = false;

function checkHttps(key, label, options = {}) {
  const { allowEmpty = false, allowLocal = false } = options;
  const value = env[key] || '';
  if (!value) {
    if (allowEmpty) {
      console.warn(`[WARN] ${label} ${key} 为空`);
      return;
    }
    failed = true;
    console.error(`[FAIL] ${label} ${key} 为空`);
    return;
  }
  if (allowLocal && value === 'local') {
    console.log(`[OK] ${label} ${key}=local`);
    return;
  }
  const isHttps = value.startsWith('https://');
  const isLocal = /127\.0\.0\.1|localhost/i.test(value);
  if (!isHttps || isLocal) {
    failed = true;
    console.error(`[FAIL] ${label} ${key}=${value}，必须是可公网访问的 HTTPS 地址`);
  } else {
    console.log(`[OK] ${label} ${key}`);
  }
}

checkHttps('SHOPRO_BASE_URL', '生产 API');
checkHttps('SHOPRO_TRIAL_BASE_URL', '体验版 API');
checkHttps('SHOPRO_STATIC_URL', '静态资源', { allowLocal: true });
checkHttps('SHOPRO_H5_URL', 'H5 分享域名', { allowEmpty: true });

if (useCallContainer) {
  for (const [key, label] of [
    ['SHOPRO_CLOUD_ENV', '云托管环境 ID'],
    ['SHOPRO_CLOUD_SERVICE', '云托管服务名'],
  ]) {
    if (env[key]) {
      console.log(`[OK] ${label} ${key}=${env[key]}`);
    } else {
      failed = true;
      console.error(`[FAIL] ${label} ${key} 为空`);
    }
  }
  console.log('\n[INFO] 已启用 callContainer 私有协议：小程序无需配置服务器域名');
} else {
  const baseUrl = env.SHOPRO_BASE_URL || '';
  if (/\.sh\.run\.tcloudbase\.com/i.test(baseUrl)) {
    failed = true;
    console.error(
      '[FAIL] 云托管默认域名不能配置为小程序服务器域名：请绑定已备案自定义域名，或启用 SHOPRO_USE_CALL_CONTAINER',
    );
  }
  console.log('\n[INFO] 使用普通 HTTPS 请求：需要在小程序后台配置服务器域名');
}

if (failed) {
  console.error('\n请在 .env 中修正上述配置后再发布。');
  process.exit(1);
}
console.log('\n发布环境检查通过');
