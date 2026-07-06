#!/usr/bin/env node
'use strict';
/*
 * Project discovery script for MoviesTheater (Java Servlet/JSP/JSTL Ant web project).
 * Usage: node ua-project-scan.js <projectRoot> <outputPath>
 */
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

function toPosix(p) {
  return p.split(path.sep).join('/');
}

function safeReadFile(absPath) {
  try {
    return fs.readFileSync(absPath, 'utf8');
  } catch (e) {
    return null;
  }
}

// ---------------------------------------------------------------------------
// Step 1: File discovery
// ---------------------------------------------------------------------------
function gitLsFiles(root) {
  const out = execSync('git ls-files', {
    cwd: root,
    maxBuffer: 1024 * 1024 * 64,
  }).toString('utf8');
  return out.split(/\r?\n/).filter(Boolean).map((line) => {
    // git quotes paths containing unusual characters; strip surrounding quotes
    // and unescape common octal/backslash escapes if present.
    if (line.length >= 2 && line[0] === '"' && line[line.length - 1] === '"') {
      try {
        return JSON.parse(line);
      } catch (e) {
        return line.slice(1, -1);
      }
    }
    return line;
  });
}

const RECURSIVE_SKIP_DIRS = new Set([
  'node_modules', '.git', 'vendor', 'venv', '.venv', '__pycache__',
]);

function recursiveList(root) {
  const results = [];
  function walk(dir) {
    let entries;
    try {
      entries = fs.readdirSync(dir, { withFileTypes: true });
    } catch (e) {
      return;
    }
    for (const entry of entries) {
      if (RECURSIVE_SKIP_DIRS.has(entry.name)) continue;
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) {
        walk(full);
      } else if (entry.isFile()) {
        results.push(toPosix(path.relative(root, full)));
      }
    }
  }
  walk(root);
  return results;
}

function discoverFiles(root) {
  let files;
  try {
    files = gitLsFiles(root);
  } catch (e) {
    files = recursiveList(root);
  }
  files = files.map(toPosix);
  // Safety net: every reported path MUST exist on disk (handles unstaged
  // deletions still present in the git index, stale entries, etc.)
  files = files.filter((relPath) => {
    try {
      return fs.statSync(path.join(root, relPath)).isFile();
    } catch (e) {
      return false;
    }
  });
  // de-dupe just in case
  return Array.from(new Set(files)).sort();
}

// ---------------------------------------------------------------------------
// Step 2 / 2.5: Exclusion filtering (hardcoded defaults + .understandignore)
// ---------------------------------------------------------------------------

// Expressed as gitignore-style patterns so hardcoded defaults and
// user-supplied .understandignore patterns can be merged into one ordered
// list (later patterns, including "!" negations, win -- matching the
// semantics of a combined .gitignore-compatible matcher).
const DEFAULT_IGNORE_PATTERNS = [
  // dependency directories
  'node_modules/', '.git/', 'vendor/', 'venv/', '.venv/', '__pycache__/',
  // build output directories (full segment match only)
  'dist/', 'build/', 'out/', 'coverage/', '.next/', '.cache/', '.turbo/', 'target/', 'obj/',
  // IDE/editor config directories
  '.idea/', '.vscode/',
  // lock files
  '*.lock', 'package-lock.json', 'yarn.lock', 'pnpm-lock.yaml',
  // binary / asset files
  '*.png', '*.jpg', '*.jpeg', '*.gif', '*.svg', '*.ico', '*.woff', '*.woff2',
  '*.ttf', '*.eot', '*.mp3', '*.mp4', '*.pdf', '*.zip', '*.tar', '*.gz',
  // compiled/packaged binaries (not explicitly enumerated in the base spec,
  // but covered by the general "only exclude binaries..." principle -- a
  // vendored .jar/.war/.class is a compiled artifact, not a source file, and
  // treating it as text would produce meaningless line counts)
  '*.jar', '*.war', '*.class', '*.exe', '*.dll', '*.so',
  // generated files
  '*.min.js', '*.min.css', '*.map', '*.generated.*',
  // misc non-source
  'LICENSE', 'LICENSE.md', 'LICENSE.txt', '.gitignore', '.editorconfig',
  '.prettierrc', '.prettierrc*', '.eslintrc', '.eslintrc*', '*.log',
];

function compileIgnorePattern(rawPattern) {
  let pattern = rawPattern.trim();
  let negate = false;
  if (pattern.startsWith('!')) {
    negate = true;
    pattern = pattern.slice(1);
  }
  let dirOnly = false;
  if (pattern.endsWith('/')) {
    dirOnly = true;
    pattern = pattern.slice(0, -1);
  }
  const isAnchored = pattern.includes('/'); // slash anywhere -> rooted pattern
  let core = pattern.startsWith('/') ? pattern.slice(1) : pattern;

  let regexStr = '';
  for (let i = 0; i < core.length; i++) {
    const c = core[i];
    if (c === '*') {
      if (core[i + 1] === '*') {
        regexStr += '.*';
        i++;
      } else {
        regexStr += '[^/]*';
      }
    } else if (c === '?') {
      regexStr += '[^/]';
    } else if ('.+^${}()|[]\\'.includes(c)) {
      regexStr += '\\' + c;
    } else {
      regexStr += c;
    }
  }

  const anchoredPrefix = isAnchored ? '^' : '(^|.*/)';
  const regex = new RegExp(anchoredPrefix + regexStr + '($|/.*)');
  return { regex, negate, dirOnly, raw: rawPattern };
}

function parseIgnoreFileContent(content) {
  return content
    .split(/\r?\n/)
    .map((l) => l.trim())
    .filter((l) => l && !l.startsWith('#'));
}

function buildMatcher(patterns) {
  const compiled = patterns.map(compileIgnorePattern);
  return function isIgnored(relPath) {
    let ignored = false;
    for (const rule of compiled) {
      if (rule.regex.test(relPath)) {
        ignored = !rule.negate;
      }
    }
    return ignored;
  };
}

function loadUnderstandIgnoreFiles(root) {
  const candidatePaths = [
    path.join(root, '.understand-anything', '.understandignore'),
    path.join(root, '.understandignore'),
  ];
  const patterns = [];
  let anyFound = false;
  for (const p of candidatePaths) {
    const content = safeReadFile(p);
    if (content !== null) {
      anyFound = true;
      patterns.push(...parseIgnoreFileContent(content));
    }
  }
  return { anyFound, patterns };
}

function applyExclusionFiltering(root, files) {
  const defaultMatcher = buildMatcher(DEFAULT_IGNORE_PATTERNS);
  const baselineKept = files.filter((f) => !defaultMatcher(f));

  const { anyFound, patterns: userPatterns } = loadUnderstandIgnoreFiles(root);
  if (!anyFound) {
    return { kept: baselineKept, filteredByIgnore: 0 };
  }

  const combinedMatcher = buildMatcher(DEFAULT_IGNORE_PATTERNS.concat(userPatterns));
  const unifiedKept = files.filter((f) => !combinedMatcher(f));
  const filteredByIgnore = baselineKept.length - unifiedKept.length;
  return { kept: unifiedKept, filteredByIgnore };
}

// ---------------------------------------------------------------------------
// Step 3: Language detection
// ---------------------------------------------------------------------------
const EXT_LANGUAGE_MAP = {
  '.ts': 'typescript', '.tsx': 'typescript',
  '.js': 'javascript', '.jsx': 'javascript',
  '.py': 'python',
  '.go': 'go',
  '.rs': 'rust',
  '.java': 'java',
  '.rb': 'ruby',
  '.cpp': 'cpp', '.cc': 'cpp', '.cxx': 'cpp', '.h': 'cpp', '.hpp': 'cpp',
  '.c': 'c',
  '.cs': 'csharp',
  '.swift': 'swift',
  '.kt': 'kotlin',
  '.php': 'php',
  '.vue': 'vue',
  '.svelte': 'svelte',
  '.sh': 'shell', '.bash': 'shell',
  '.ps1': 'powershell',
  '.bat': 'batch', '.cmd': 'batch',
  '.md': 'markdown', '.rst': 'markdown',
  '.yaml': 'yaml', '.yml': 'yaml',
  '.json': 'json',
  '.jsonc': 'jsonc',
  '.toml': 'toml',
  '.sql': 'sql',
  '.graphql': 'graphql', '.gql': 'graphql',
  '.proto': 'protobuf',
  '.tf': 'terraform', '.tfvars': 'terraform',
  '.html': 'html', '.htm': 'html',
  '.css': 'css', '.scss': 'css', '.sass': 'css', '.less': 'css',
  '.xml': 'xml',
  '.cfg': 'config', '.ini': 'config', '.env': 'config',
};

const BASENAME_LANGUAGE_MAP = {
  'Dockerfile': 'dockerfile',
  'Makefile': 'makefile',
  'Jenkinsfile': 'jenkinsfile',
};

function detectLanguage(relPath) {
  const base = path.basename(relPath);
  if (BASENAME_LANGUAGE_MAP[base]) return BASENAME_LANGUAGE_MAP[base];
  const ext = path.extname(base).toLowerCase();
  if (ext && EXT_LANGUAGE_MAP[ext]) return EXT_LANGUAGE_MAP[ext];
  if (ext) return ext.slice(1).toLowerCase();
  return 'unknown';
}

// ---------------------------------------------------------------------------
// Step 4: File category detection
// ---------------------------------------------------------------------------
function detectFileCategory(relPath) {
  const base = path.basename(relPath);
  const ext = path.extname(base).toLowerCase();
  const lower = relPath.toLowerCase();

  // infra (checked before config/docs since it is the most specific)
  if (base === 'Dockerfile' || /^docker-compose\..+$/.test(base)) return 'infra';
  if (ext === '.tf' || ext === '.tfvars') return 'infra';
  if (base === 'Makefile' || base === 'Jenkinsfile' || base === 'Procfile' || base === 'Vagrantfile') return 'infra';
  if (lower.includes('.github/workflows/')) return 'infra';
  if (base === '.gitlab-ci.yml') return 'infra';
  if (lower.includes('.circleci/')) return 'infra';
  if (/\.k8s\.ya?ml$/.test(lower)) return 'infra';
  if (lower.split('/').includes('k8s') || lower.split('/').includes('kubernetes')) return 'infra';

  // data
  if (['.sql', '.graphql', '.gql', '.proto', '.prisma', '.csv'].includes(ext)) return 'data';
  if (/\.schema\.json$/.test(lower)) return 'data';

  // docs
  if (['.md', '.rst', '.txt'].includes(ext) && base !== 'LICENSE') return 'docs';

  // config
  if (['.yaml', '.yml', '.json', '.jsonc', '.toml', '.xml', '.cfg', '.ini', '.env'].includes(ext)) return 'config';
  if (['tsconfig.json', 'package.json', 'pyproject.toml', 'Cargo.toml', 'go.mod'].includes(base)) return 'config';

  // script
  if (['.sh', '.bash', '.ps1', '.bat'].includes(ext)) return 'script';

  // markup
  if (['.html', '.htm', '.css', '.scss', '.sass', '.less'].includes(ext)) return 'markup';

  return 'code';
}

// ---------------------------------------------------------------------------
// Step 5: Line counting
// ---------------------------------------------------------------------------
function countLines(absPath) {
  let content;
  try {
    content = fs.readFileSync(absPath);
  } catch (e) {
    return 0;
  }
  // crude binary sniff: NUL byte present -> don't attempt to count "lines"
  if (content.includes(0)) return 0;
  const text = content.toString('utf8');
  if (text.length === 0) return 0;
  const lines = text.split(/\r\n|\r|\n/);
  // trailing empty string from a final newline shouldn't inflate the count
  if (lines.length > 0 && lines[lines.length - 1] === '') lines.pop();
  return lines.length;
}

// ---------------------------------------------------------------------------
// Step 6: Framework detection
// ---------------------------------------------------------------------------
const NPM_FRAMEWORK_DEPS = [
  'react', 'vue', 'svelte', '@angular/core', 'express', 'fastify', 'koa',
  'next', 'nuxt', 'vite', 'vitest', 'jest', 'mocha', 'tailwindcss', 'prisma',
  'typeorm', 'sequelize', 'mongoose', 'redux', 'zustand', 'mobx',
];
const PY_FRAMEWORK_KEYWORDS = [
  'django', 'djangorestframework', 'fastapi', 'flask', 'sqlalchemy', 'alembic',
  'celery', 'pydantic', 'uvicorn', 'gunicorn', 'aiohttp', 'tornado',
  'starlette', 'pytest', 'hypothesis', 'channels',
];
const RUBY_FRAMEWORK_GEMS = [
  'rails', 'railties', 'sinatra', 'grape', 'rspec', 'sidekiq', 'activerecord',
  'actionpack', 'devise', 'pundit',
];
const GO_FRAMEWORK_MODULES = [
  'github.com/gin-gonic/gin', 'github.com/labstack/echo',
  'github.com/gofiber/fiber', 'github.com/go-chi/chi', 'gorm.io/gorm',
];
const RUST_FRAMEWORK_CRATES = [
  'actix-web', 'axum', 'rocket', 'diesel', 'tokio', 'serde', 'warp',
];
const JVM_FRAMEWORK_DEPS = [
  'spring-boot', 'spring-web', 'spring-data', 'quarkus', 'micronaut',
  'hibernate', 'jakarta', 'junit', 'ktor',
];

function titleCaseFrameworkName(name) {
  // present known dependency identifiers with a readable display name
  const displayNames = {
    '@angular/core': 'Angular',
    react: 'React', vue: 'Vue', svelte: 'Svelte', express: 'Express',
    fastify: 'Fastify', koa: 'Koa', next: 'Next.js', nuxt: 'Nuxt',
    vite: 'Vite', vitest: 'Vitest', jest: 'Jest', mocha: 'Mocha',
    tailwindcss: 'Tailwind CSS', prisma: 'Prisma', typeorm: 'TypeORM',
    sequelize: 'Sequelize', mongoose: 'Mongoose', redux: 'Redux',
    zustand: 'Zustand', mobx: 'MobX',
    django: 'Django', djangorestframework: 'Django REST Framework',
    fastapi: 'FastAPI', flask: 'Flask', sqlalchemy: 'SQLAlchemy',
    alembic: 'Alembic', celery: 'Celery', pydantic: 'Pydantic',
    uvicorn: 'Uvicorn', gunicorn: 'Gunicorn', aiohttp: 'aiohttp',
    tornado: 'Tornado', starlette: 'Starlette', pytest: 'pytest',
    hypothesis: 'Hypothesis', channels: 'Django Channels',
    rails: 'Ruby on Rails', railties: 'Ruby on Rails', sinatra: 'Sinatra',
    grape: 'Grape', rspec: 'RSpec', sidekiq: 'Sidekiq',
    activerecord: 'ActiveRecord', actionpack: 'ActionPack',
    devise: 'Devise', pundit: 'Pundit',
    'github.com/gin-gonic/gin': 'Gin', 'github.com/labstack/echo': 'Echo',
    'github.com/gofiber/fiber': 'Fiber', 'github.com/go-chi/chi': 'Chi',
    'gorm.io/gorm': 'GORM',
    'actix-web': 'Actix Web', axum: 'Axum', rocket: 'Rocket',
    diesel: 'Diesel', tokio: 'Tokio', serde: 'Serde', warp: 'Warp',
    'spring-boot': 'Spring Boot', 'spring-web': 'Spring Web',
    'spring-data': 'Spring Data', quarkus: 'Quarkus', micronaut: 'Micronaut',
    hibernate: 'Hibernate', jakarta: 'Jakarta EE', junit: 'JUnit', ktor: 'Ktor',
  };
  return displayNames[name] || name;
}

function detectFrameworks(root, files) {
  const frameworks = new Set();
  let projectName = null;
  let rawDescription = '';

  const has = (relPath) => files.includes(relPath);
  const fileSet = new Set(files);

  // package.json
  const pkgJsonPath = path.join(root, 'package.json');
  const pkgJsonContent = safeReadFile(pkgJsonPath);
  if (pkgJsonContent) {
    try {
      const pkg = JSON.parse(pkgJsonContent);
      if (pkg.name) projectName = projectName || pkg.name;
      if (pkg.description) rawDescription = rawDescription || pkg.description;
      const deps = Object.assign({}, pkg.dependencies || {}, pkg.devDependencies || {});
      for (const dep of NPM_FRAMEWORK_DEPS) {
        if (deps[dep]) frameworks.add(titleCaseFrameworkName(dep));
      }
    } catch (e) { /* ignore malformed json */ }
  }

  // tsconfig.json
  if (safeReadFile(path.join(root, 'tsconfig.json')) !== null) {
    frameworks.add('TypeScript');
  }

  // Cargo.toml
  const cargoContent = safeReadFile(path.join(root, 'Cargo.toml'));
  if (cargoContent) {
    const nameMatch = cargoContent.match(/\[package\][^[]*name\s*=\s*"([^"]+)"/);
    if (nameMatch) projectName = projectName || nameMatch[1];
    for (const crate of RUST_FRAMEWORK_CRATES) {
      const re = new RegExp('^' + crate.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + '\\s*=', 'm');
      if (re.test(cargoContent)) frameworks.add(titleCaseFrameworkName(crate));
    }
  }

  // go.mod
  const goModContent = safeReadFile(path.join(root, 'go.mod'));
  if (goModContent) {
    const moduleMatch = goModContent.match(/^module\s+(\S+)/m);
    if (moduleMatch) {
      const segments = moduleMatch[1].split('/');
      projectName = projectName || segments[segments.length - 1];
    }
    for (const mod of GO_FRAMEWORK_MODULES) {
      if (goModContent.includes(mod)) frameworks.add(titleCaseFrameworkName(mod));
    }
  }

  // requirements.txt
  const reqContent = safeReadFile(path.join(root, 'requirements.txt'));
  if (reqContent) {
    const lines = reqContent.split(/\r?\n/);
    for (const line of lines) {
      const pkgName = line.trim().split(/[=<>~!;\s]/)[0].toLowerCase();
      if (PY_FRAMEWORK_KEYWORDS.includes(pkgName)) frameworks.add(titleCaseFrameworkName(pkgName));
    }
  }

  // pyproject.toml
  const pyprojectContent = safeReadFile(path.join(root, 'pyproject.toml'));
  if (pyprojectContent) {
    const nameMatch = pyprojectContent.match(/name\s*=\s*"([^"]+)"/);
    if (nameMatch) projectName = projectName || nameMatch[1];
    const descMatch = pyprojectContent.match(/description\s*=\s*"([^"]*)"/);
    if (descMatch) rawDescription = rawDescription || descMatch[1];
    for (const kw of PY_FRAMEWORK_KEYWORDS) {
      if (new RegExp('\\b' + kw + '\\b', 'i').test(pyprojectContent)) frameworks.add(titleCaseFrameworkName(kw));
    }
    if (/\[tool\.pytest\.ini_options\]/.test(pyprojectContent)) frameworks.add('pytest');
    if (/\[tool\.django\]/.test(pyprojectContent)) frameworks.add('Django');
  }

  // setup.py / setup.cfg / Pipfile
  for (const fname of ['setup.py', 'setup.cfg', 'Pipfile']) {
    const content = safeReadFile(path.join(root, fname));
    if (content) {
      for (const kw of PY_FRAMEWORK_KEYWORDS) {
        if (new RegExp('\\b' + kw + '\\b', 'i').test(content)) frameworks.add(titleCaseFrameworkName(kw));
      }
    }
  }

  // Gemfile
  const gemfileContent = safeReadFile(path.join(root, 'Gemfile'));
  if (gemfileContent) {
    for (const gem of RUBY_FRAMEWORK_GEMS) {
      if (new RegExp("gem\\s+['\"]" + gem + "['\"]").test(gemfileContent)) frameworks.add(titleCaseFrameworkName(gem));
    }
  }

  // pom.xml / build.gradle / build.gradle.kts
  for (const fname of ['pom.xml', 'build.gradle', 'build.gradle.kts']) {
    const content = safeReadFile(path.join(root, fname));
    if (content) {
      for (const dep of JVM_FRAMEWORK_DEPS) {
        if (content.includes(dep)) frameworks.add(titleCaseFrameworkName(dep));
      }
    }
  }

  // --- Infra tooling from discovered files ---
  if (fileSet.has('Dockerfile') || files.some((f) => path.basename(f) === 'Dockerfile')) frameworks.add('Docker');
  if (has('docker-compose.yml') || has('docker-compose.yaml')) frameworks.add('Docker Compose');
  if (files.some((f) => f.endsWith('.tf'))) frameworks.add('Terraform');
  if (files.some((f) => /^\.github\/workflows\/.+\.ya?ml$/.test(f))) frameworks.add('GitHub Actions');
  if (has('.gitlab-ci.yml')) frameworks.add('GitLab CI');
  if (files.some((f) => path.basename(f) === 'Jenkinsfile')) frameworks.add('Jenkins');

  // --- NetBeans / Ant / Jakarta EE web project detection ---
  // This project has no package.json/pom.xml/Cargo.toml/go.mod manifest --
  // it is a plain Ant-based NetBeans web project (nbproject/project.xml)
  // referencing jars directly. Detect its real stack deterministically from
  // the project manifest, web.xml, and the vendored library jar names.
  const nbProjectXml = safeReadFile(path.join(root, 'nbproject', 'project.xml'));
  if (nbProjectXml) {
    frameworks.add('Apache Ant');
    const nameMatch = nbProjectXml.match(/<name>([^<]+)<\/name>/);
    if (nameMatch) projectName = projectName || nameMatch[1].trim();
  }
  if (safeReadFile(path.join(root, 'build.xml')) !== null) {
    frameworks.add('Apache Ant');
  }
  const webXml = safeReadFile(path.join(root, 'web', 'WEB-INF', 'web.xml'));
  if (webXml || files.some((f) => f.endsWith('.jsp'))) {
    frameworks.add('Jakarta Servlet');
  }
  if (files.some((f) => f.endsWith('.jsp'))) {
    frameworks.add('JSP');
  }
  // Vendored library jars (read from disk directly, independent of whether
  // the jar itself made it into the final filtered file list) signal JSTL /
  // Gson / JDBC driver usage.
  const libDirsToScan = ['allowedlib', path.join('web', 'WEB-INF', 'lib')];
  const libBasenames = [];
  for (const dir of libDirsToScan) {
    try {
      const entries = fs.readdirSync(path.join(root, dir));
      libBasenames.push(...entries);
    } catch (e) { /* dir may not exist */ }
  }
  // also inspect the nbproject library references themselves
  if (nbProjectXml) {
    const refMatches = nbProjectXml.match(/file\.reference\.([^}]+)\}/g) || [];
    for (const ref of refMatches) libBasenames.push(ref);
  }
  const libBlob = libBasenames.join(' ').toLowerCase();
  if (libBlob.includes('jstl')) frameworks.add('JSTL');
  if (libBlob.includes('gson')) frameworks.add('Gson');
  if (libBlob.includes('mssql-jdbc') || libBlob.includes('mssql_jdbc')) frameworks.add('JDBC (SQL Server)');

  return { frameworks: Array.from(frameworks).sort(), projectName, rawDescription };
}

// ---------------------------------------------------------------------------
// Step 7: Complexity estimation
// ---------------------------------------------------------------------------
function estimateComplexity(totalFiles) {
  if (totalFiles <= 30) return 'small';
  if (totalFiles <= 150) return 'moderate';
  if (totalFiles <= 500) return 'large';
  return 'very-large';
}

// ---------------------------------------------------------------------------
// Step 8: Project name
// ---------------------------------------------------------------------------
function resolveProjectName(root, detectedName) {
  if (detectedName) return detectedName;
  return path.basename(root);
}

// ---------------------------------------------------------------------------
// Step 9: Import resolution
// ---------------------------------------------------------------------------
const RESOLVABLE_EXTENSIONLESS_PROBES = [
  '.ts', '.tsx', '.js', '.jsx', '/index.ts', '/index.js', '/index.tsx', '/index.jsx',
  '.py', '.go', '.rs', '.rb',
];

function buildFileIndex(files) {
  const set = new Set(files);
  return {
    has: (p) => set.has(p),
  };
}

function resolveExtensionless(baseNoExt, index) {
  for (const suffix of RESOLVABLE_EXTENSIONLESS_PROBES) {
    const candidate = suffix.startsWith('/') ? baseNoExt + suffix : baseNoExt + suffix;
    if (index.has(candidate)) return candidate;
  }
  return null;
}

function normalizeRelative(fromDir, importPath) {
  return toPosix(path.normalize(path.join(fromDir, importPath)));
}

function resolveJsTsImport(importPath, fromDir, index, tsAliases) {
  const resolved = [];
  let candidatePaths = [];

  if (importPath.startsWith('.')) {
    candidatePaths.push(normalizeRelative(fromDir, importPath));
  } else if (tsAliases && tsAliases.length) {
    for (const alias of tsAliases) {
      if (importPath === alias.prefix || importPath.startsWith(alias.prefix + '/')) {
        const rest = importPath.slice(alias.prefix.length).replace(/^\//, '');
        const base = rest ? toPosix(path.join(alias.target, rest)) : alias.target;
        candidatePaths.push(base);
      }
    }
  }

  for (const base of candidatePaths) {
    if (index.has(base)) {
      resolved.push(base);
      continue;
    }
    const ext = path.extname(base);
    if (ext) continue; // has explicit extension but not found -> skip
    const found = resolveExtensionless(base, index);
    if (found) resolved.push(found);
  }
  return resolved;
}

function extractJsTsImports(content) {
  const importPaths = [];
  const patterns = [
    /import\s+(?:[^'"]*?\sfrom\s+)?['"]([^'"]+)['"]/g,
    /require\(\s*['"]([^'"]+)['"]\s*\)/g,
    /export\s+(?:\*|\{[^}]*\})\s+from\s+['"]([^'"]+)['"]/g,
    /import\(\s*['"]([^'"]+)['"]\s*\)/g,
  ];
  for (const re of patterns) {
    let m;
    while ((m = re.exec(content)) !== null) {
      importPaths.push(m[1]);
    }
  }
  return importPaths;
}

function loadTsConfigAliases(root) {
  const content = safeReadFile(path.join(root, 'tsconfig.json'));
  if (!content) return [];
  let json;
  try {
    // tsconfig can contain comments; strip them crudely before parsing
    const stripped = content
      .replace(/\/\*[\s\S]*?\*\//g, '')
      .replace(/(^|[^:])\/\/.*$/gm, '$1');
    json = JSON.parse(stripped);
  } catch (e) {
    return [];
  }
  const co = json.compilerOptions || {};
  const baseUrl = co.baseUrl || '.';
  const aliases = [];
  if (co.paths) {
    for (const key of Object.keys(co.paths)) {
      const prefix = key.replace(/\/\*$/, '');
      const targets = co.paths[key];
      if (targets && targets[0]) {
        const target = toPosix(path.normalize(path.join(baseUrl, targets[0].replace(/\/\*$/, ''))));
        aliases.push({ prefix, target });
      }
    }
  }
  // common conventions even without explicit paths mapping
  if (!co.paths || !Object.keys(co.paths).some((k) => k.startsWith('@/'))) {
    aliases.push({ prefix: '@', target: toPosix(baseUrl) });
  }
  if (!co.paths || !Object.keys(co.paths).some((k) => k.startsWith('~/'))) {
    aliases.push({ prefix: '~', target: toPosix(baseUrl) });
  }
  return aliases;
}

function extractPythonImports(content) {
  const results = []; // { kind: 'absolute'|'relative', module, names }
  const lines = content.split(/\r?\n/);
  for (const line of lines) {
    let m;
    if ((m = line.match(/^\s*from\s+(\.+)(\S*)\s+import\s+(.+)$/))) {
      results.push({ kind: 'relative', dots: m[1].length, module: m[2], names: m[3].split(',').map((s) => s.trim().split(/\s+as\s+/)[0]) });
    } else if ((m = line.match(/^\s*from\s+([\w.]+)\s+import\s+(.+)$/))) {
      results.push({ kind: 'absolute', module: m[1], names: m[2].split(',').map((s) => s.trim().split(/\s+as\s+/)[0]) });
    } else if ((m = line.match(/^\s*import\s+([\w.]+(?:\s*,\s*[\w.]+)*)/))) {
      const mods = m[1].split(',').map((s) => s.trim());
      for (const mod of mods) results.push({ kind: 'absolute', module: mod, names: [] });
    }
  }
  return results;
}

function resolvePythonModulePath(modDotted, index) {
  if (!modDotted) return null;
  const asPath = modDotted.replace(/\./g, '/');
  const asModule = asPath + '.py';
  if (index.has(asModule)) return { path: asModule, isPackage: false };
  const asPackage = asPath + '/__init__.py';
  if (index.has(asPackage)) return { path: asPackage, isPackage: true };
  return null;
}

function extractGoImports(content) {
  const results = [];
  const blockMatch = content.match(/import\s*\(([\s\S]*?)\)/);
  if (blockMatch) {
    const lines = blockMatch[1].split(/\r?\n/);
    for (const line of lines) {
      const m = line.match(/"([^"]+)"/);
      if (m) results.push(m[1]);
    }
  }
  const singleMatches = content.matchAll(/^import\s+"([^"]+)"/gm);
  for (const m of singleMatches) results.push(m[1]);
  return results;
}

function extractJavaImports(content) {
  const results = [];
  const re = /^import\s+(?:static\s+)?([\w.]+)(\.\*)?\s*;/gm;
  let m;
  while ((m = re.exec(content)) !== null) {
    results.push({ dotted: m[1], wildcard: !!m[2] });
  }
  return results;
}

function extractKotlinImports(content) {
  const results = [];
  const re = /^import\s+([\w.]+)(\.\*)?\s*$/gm;
  let m;
  while ((m = re.exec(content)) !== null) {
    results.push({ dotted: m[1], wildcard: !!m[2] });
  }
  return results;
}

function extractRubyImports(content) {
  const relative = [];
  const loadPath = [];
  let m;
  const reRel = /require_relative\s+['"]([^'"]+)['"]/g;
  while ((m = reRel.exec(content)) !== null) relative.push(m[1]);
  const reReq = /(?<!_relative\s)require\s+['"]([^'"]+)['"]/g;
  while ((m = reReq.exec(content)) !== null) loadPath.push(m[1]);
  return { relative, loadPath };
}

function extractPhpImports(content) {
  const results = [];
  const re = /use\s+([A-Za-z0-9_\\]+)\s*;/g;
  let m;
  while ((m = re.exec(content)) !== null) results.push(m[1]);
  return results;
}

function extractCIncludes(content) {
  const results = [];
  const re = /#include\s*["<]([^">]+)[">]/g;
  let m;
  while ((m = re.exec(content)) !== null) results.push(m[1]);
  return results;
}

function loadComposerAutoload(root) {
  const content = safeReadFile(path.join(root, 'composer.json'));
  if (!content) return {};
  try {
    const json = JSON.parse(content);
    return (json.autoload && json.autoload['psr-4']) || {};
  } catch (e) {
    return {};
  }
}

function loadGoModuleName(root) {
  const content = safeReadFile(path.join(root, 'go.mod'));
  if (!content) return null;
  const m = content.match(/^module\s+(\S+)/m);
  return m ? m[1] : null;
}

function buildImportMap(root, files) {
  const index = buildFileIndex(files);
  const importMap = {};
  const tsAliases = loadTsConfigAliases(root);
  const composerAutoload = loadComposerAutoload(root);
  const goModuleName = loadGoModuleName(root);

  for (const relPath of files) importMap[relPath] = [];

  const codeFiles = files.filter((f) => detectFileCategory(f) === 'code');

  for (const relPath of codeFiles) {
    const absPath = path.join(root, relPath);
    const content = safeReadFile(absPath);
    if (content === null) continue;
    const language = detectLanguage(relPath);
    const fromDir = path.posix.dirname(relPath);
    const resolvedSet = new Set();

    try {
      if (language === 'typescript' || language === 'javascript' || language === 'vue' || language === 'svelte') {
        const imports = extractJsTsImports(content);
        for (const imp of imports) {
          const resolved = resolveJsTsImport(imp, fromDir, index, tsAliases);
          resolved.forEach((r) => resolvedSet.add(r));
        }
      } else if (language === 'python') {
        const imports = extractPythonImports(content);
        for (const imp of imports) {
          if (imp.kind === 'relative') {
            // resolve dots: 1 dot = current package dir, each extra dot = go up one level
            let baseDir = fromDir;
            for (let i = 1; i < imp.dots; i++) baseDir = path.posix.dirname(baseDir);
            const modPath = imp.module ? imp.module.replace(/\./g, '/') : '';
            const basePath = modPath ? toPosix(path.posix.join(baseDir, modPath)) : baseDir;
            const asModule = basePath + '.py';
            const asPackageInit = basePath + '/__init__.py';
            let matchedPkg = false;
            if (index.has(asModule)) resolvedSet.add(asModule);
            if (index.has(asPackageInit)) { resolvedSet.add(asPackageInit); matchedPkg = true; }
            if (!imp.module && index.has(baseDir + '/__init__.py')) { resolvedSet.add(baseDir + '/__init__.py'); matchedPkg = true; }
            if (matchedPkg) {
              for (const name of imp.names) {
                const sub = basePath ? `${basePath}/${name}` : `${baseDir}/${name}`;
                if (index.has(sub + '.py')) resolvedSet.add(sub + '.py');
                else if (index.has(sub + '/__init__.py')) resolvedSet.add(sub + '/__init__.py');
              }
            }
          } else {
            const match = resolvePythonModulePath(imp.module, index);
            if (match) {
              resolvedSet.add(match.path);
              if (match.isPackage) {
                const basePath = imp.module.replace(/\./g, '/');
                for (const name of imp.names) {
                  const sub = `${basePath}/${name}`;
                  if (index.has(sub + '.py')) resolvedSet.add(sub + '.py');
                  else if (index.has(sub + '/__init__.py')) resolvedSet.add(sub + '/__init__.py');
                }
              }
            }
          }
        }
      } else if (language === 'go') {
        const imports = extractGoImports(content);
        if (goModuleName) {
          for (const imp of imports) {
            if (imp.startsWith(goModuleName)) {
              const rel = imp.slice(goModuleName.length).replace(/^\//, '');
              // match any .go file inside that package directory
              const candidates = files.filter((f) => f.startsWith(rel ? rel + '/' : '') && f.endsWith('.go'));
              candidates.forEach((c) => resolvedSet.add(c));
            }
          }
        }
      } else if (language === 'rust') {
        const useMatches = content.matchAll(/use\s+(crate|super)::([\w:]+)/g);
        for (const m of useMatches) {
          const segments = m[2].split('::').filter(Boolean);
          const asPath = segments.join('/');
          const candidates = [
            `src/${asPath}.rs`,
            `src/${asPath}/mod.rs`,
          ];
          for (const c of candidates) if (index.has(c)) resolvedSet.add(c);
        }
        const modMatches = content.matchAll(/^\s*mod\s+(\w+)\s*;/gm);
        for (const m of modMatches) {
          const name = m[1];
          const candidates = [
            toPosix(path.posix.join(fromDir, `${name}.rs`)),
            toPosix(path.posix.join(fromDir, name, 'mod.rs')),
          ];
          for (const c of candidates) if (index.has(c)) resolvedSet.add(c);
        }
      } else if (language === 'java') {
        const imports = extractJavaImports(content);
        for (const imp of imports) {
          if (imp.wildcard) {
            const dirPath = imp.dotted.replace(/\./g, '/');
            const candidates = files.filter((f) => f.endsWith('.java') && path.posix.dirname(f) === dirPath);
            candidates.forEach((c) => resolvedSet.add(c));
          } else {
            const suffix = imp.dotted.replace(/\./g, '/') + '.java';
            const match = files.find((f) => f === suffix || f.endsWith('/' + suffix));
            if (match) resolvedSet.add(match);
          }
        }
      } else if (language === 'kotlin') {
        const imports = extractKotlinImports(content);
        for (const imp of imports) {
          if (imp.wildcard) {
            const dirPath = imp.dotted.replace(/\./g, '/');
            const candidates = files.filter((f) => f.endsWith('.kt') && path.posix.dirname(f) === dirPath);
            candidates.forEach((c) => resolvedSet.add(c));
          } else {
            const suffix = imp.dotted.replace(/\./g, '/') + '.kt';
            const match = files.find((f) => f === suffix || f.endsWith('/' + suffix));
            if (match) resolvedSet.add(match);
          }
        }
      } else if (language === 'ruby') {
        const { relative, loadPath } = extractRubyImports(content);
        for (const imp of relative) {
          const base = normalizeRelative(fromDir, imp);
          if (index.has(base + '.rb')) resolvedSet.add(base + '.rb');
          else if (index.has(base)) resolvedSet.add(base);
        }
        for (const imp of loadPath) {
          const probes = [`lib/${imp}.rb`, `app/${imp}.rb`, `${imp}.rb`];
          for (const p of probes) if (index.has(p)) resolvedSet.add(p);
        }
      } else if (language === 'php') {
        const imports = extractPhpImports(content);
        for (const imp of imports) {
          for (const nsPrefix of Object.keys(composerAutoload)) {
            const normalizedPrefix = nsPrefix.replace(/\\$/, '');
            if (imp.startsWith(normalizedPrefix)) {
              const rest = imp.slice(normalizedPrefix.length).replace(/^\\+/, '');
              const dir = composerAutoload[nsPrefix].replace(/\/$/, '');
              const filePath = toPosix(path.posix.join(dir, rest.replace(/\\/g, '/') + '.php'));
              if (index.has(filePath)) resolvedSet.add(filePath);
            }
          }
        }
      } else if (language === 'c' || language === 'cpp') {
        const imports = extractCIncludes(content);
        for (const imp of imports) {
          const probes = [
            normalizeRelative(fromDir, imp),
            `include/${imp}`,
            `src/${imp}`,
            imp,
          ];
          for (const p of probes) if (index.has(p)) { resolvedSet.add(p); break; }
        }
      }
    } catch (e) {
      // never let a single file's import extraction crash the whole scan
    }

    importMap[relPath] = Array.from(resolvedSet).sort();
  }

  return importMap;
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------
function main() {
  const projectRoot = process.argv[2];
  const outputPath = process.argv[3];

  if (!projectRoot || !outputPath) {
    console.error('Usage: node ua-project-scan.js <projectRoot> <outputPath>');
    process.exit(1);
  }

  let rootStat;
  try {
    rootStat = fs.statSync(projectRoot);
  } catch (e) {
    console.error('Fatal error: cannot access project root: ' + e.message);
    process.exit(1);
  }
  if (!rootStat.isDirectory()) {
    console.error('Fatal error: project root is not a directory: ' + projectRoot);
    process.exit(1);
  }

  try {
    const rawFiles = discoverFiles(projectRoot);
    const { kept: filteredFiles, filteredByIgnore } = applyExclusionFiltering(projectRoot, rawFiles);

    const fileRecords = filteredFiles.map((relPath) => {
      const absPath = path.join(projectRoot, relPath);
      return {
        path: relPath,
        language: detectLanguage(relPath),
        sizeLines: countLines(absPath),
        fileCategory: detectFileCategory(relPath),
      };
    }).sort((a, b) => (a.path < b.path ? -1 : a.path > b.path ? 1 : 0));

    const languages = Array.from(new Set(fileRecords.map((f) => f.language))).sort();

    const { frameworks, projectName, rawDescription } = detectFrameworks(projectRoot, filteredFiles);
    const finalName = resolveProjectName(projectRoot, projectName);

    const readmePath = path.join(projectRoot, 'README.md');
    const readmeContent = safeReadFile(readmePath);
    let readmeHead = '';
    if (readmeContent) {
      readmeHead = readmeContent.split(/\r?\n/).slice(0, 10).join('\n');
    }

    const totalFiles = fileRecords.length;
    const estimatedComplexity = estimateComplexity(totalFiles);
    const importMap = buildImportMap(projectRoot, filteredFiles);

    const result = {
      scriptCompleted: true,
      name: finalName,
      rawDescription: rawDescription || '',
      readmeHead,
      languages,
      frameworks,
      files: fileRecords,
      totalFiles,
      filteredByIgnore,
      estimatedComplexity,
      importMap,
    };

    fs.mkdirSync(path.dirname(outputPath), { recursive: true });
    fs.writeFileSync(outputPath, JSON.stringify(result, null, 2), 'utf8');
    process.exit(0);
  } catch (err) {
    console.error('Fatal error: ' + (err && err.stack ? err.stack : String(err)));
    process.exit(1);
  }
}

main();
