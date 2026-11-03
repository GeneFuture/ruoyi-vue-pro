// 交叉检查 .vue 的模板 class 与 scoped 样式定义，找出「用了没定义」和「定义了没用」
import { createRequire } from 'module';
import fs from 'fs';
const require = createRequire(import.meta.url);
const { parse } = require('@vue/compiler-sfc');

const GLOBAL = /^(ss-|ui-|cicon-|sicon-|_icon-|font-|bg-|text-|is-|tap-hover$|page-|u-page)/;

for (const f of process.argv.slice(2)) {
  const { descriptor } = parse(fs.readFileSync(f, 'utf-8'), { filename: f });
  const tpl = descriptor.template?.content || '';
  const sty = (descriptor.styles || []).map((s) => s.content).join('\n');

  const tplClasses = new Set();
  for (const m of tpl.matchAll(/class="([^"]*)"/g)) m[1].split(/\s+/).forEach((c) => c && tplClasses.add(c));
  // :class="{ 'x': cond }" 里的静态键
  for (const m of tpl.matchAll(/:class="\{([^}]*)\}"/g)) {
    for (const k of m[1].matchAll(/'([a-z][\w-]*)'\s*:/g)) tplClasses.add(k[1]);
  }

  const styClasses = new Set();
  for (const m of sty.matchAll(/\.([a-z][\w-]*)/g)) styClasses.add(m[1]);
  // SCSS 嵌套的 &-xxx 展开：从父选择器推导
  for (const m of sty.matchAll(/\.([a-z][\w-]*)\s*\{[^{}]*?&(-[\w-]+)/g)) {
    styClasses.add(m[1] + m[2]);
  }

  const local = (s) => [...s].filter((c) => c && !GLOBAL.test(c));
  const missing = local(tplClasses).filter((c) => !styClasses.has(c)).sort();
  const unused = local(styClasses).filter((c) => !tplClasses.has(c)).sort();

  console.log(`\n${f}`);
  console.log(`  模板类 ${local(tplClasses).length} / 样式类 ${local(styClasses).length}`);
  console.log(`  ❌ 模板用了未定义: ${missing.join(', ') || '无'}`);
  console.log(`  ⚠️  定义了未使用: ${unused.join(', ') || '无'}`);
}
