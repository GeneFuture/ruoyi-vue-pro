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

const checks = [
  ['SHOPRO_BASE_URL', '生产 API'],
  ['SHOPRO_TRIAL_BASE_URL', '体验版 API'],
  ['SHOPRO_STATIC_URL', '远程静态资源'],
  ['SHOPRO_H5_URL', 'H5 分享域名'],
];

let failed = false;
for (const [key, label] of checks) {
  const value = env[key] || '';
  const isHttps = value.startsWith('https://');
  const isLocal = /127\.0\.0\.1|localhost/i.test(value);
  if (!isHttps || isLocal) {
    failed = true;
    console.error(`[FAIL] ${label} ${key}=${value || '(空)'}，必须是可公网访问的 HTTPS 地址`);
  } else {
    console.log(`[OK] ${label} ${key}`);
  }
}

if (failed) {
  console.error('\n微信小程序正式环境不允许 HTTP / localhost，请在 .env 中配置正式域名，并在微信后台配置合法域名。');
  process.exit(1);
}
console.log('\n发布环境检查通过');
