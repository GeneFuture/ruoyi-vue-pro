// 用 @vue/compiler-sfc 校验 .vue 文件的模板/脚本语法（项目无 CLI 构建链，借此做静态检查）
import { createRequire } from 'module';
import fs from 'fs';
const require = createRequire(import.meta.url);
const { parse, compileScript, compileTemplate } = require('@vue/compiler-sfc');

const files = process.argv.slice(2);
let bad = 0;
for (const f of files) {
  const src = fs.readFileSync(f, 'utf-8');
  const { descriptor, errors } = parse(src, { filename: f });
  if (errors.length) {
    bad++; console.log(`❌ ${f}`);
    errors.forEach(e => console.log('   parse:', e.message));
    continue;
  }
  try {
    if (descriptor.scriptSetup || descriptor.script) {
      compileScript(descriptor, { id: f });
    }
    if (descriptor.template) {
      const r = compileTemplate({ source: descriptor.template.content, filename: f, id: f });
      if (r.errors.length) {
        bad++; console.log(`❌ ${f}`);
        r.errors.forEach(e => console.log('   template:', e.message || e));
        continue;
      }
    }
    console.log(`✅ ${f}`);
  } catch (e) {
    bad++; console.log(`❌ ${f}\n   script: ${e.message}`);
  }
}
console.log(bad ? `\n${bad} 个文件有问题` : '\n全部通过');
process.exit(bad ? 1 : 0);
