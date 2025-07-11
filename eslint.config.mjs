// @ts-check
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import js from '@eslint/js';
import { FlatCompat } from '@eslint/eslintrc';
import { fixupConfigRules } from '@eslint/compat';
import globals from 'globals';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const compat = new FlatCompat({
  baseDirectory: __dirname,
  recommendedConfig: js.configs.recommended,
  allConfig: js.configs.all,
});

export default [
  // Global ignores
  {
    ignores: [
      'scripts/**',
      'lib/**',
      'docs/**',
      'example/**',
      'app.plugin.js',
      'node_modules/**',
      'build/**',
      'dist/**',
      '.git/**',
    ],
  },

  // Base configurations
  js.configs.recommended,

  // React Native configuration with compatibility fixes
  ...fixupConfigRules([
    ...compat.extends('@react-native'),
  ]),

  // Custom rules and overrides
  {
    files: ['**/*.{js,jsx,ts,tsx}'],
    languageOptions: {
      ecmaVersion: 2018,
      sourceType: 'module',
      globals: {
        ...globals.node,
        _log: 'readonly',
      },
    },
    rules: {
      // ESLint rules
      semi: 'off',
      curly: ['warn', 'multi-or-nest', 'consistent'],
      'no-mixed-spaces-and-tabs': ['warn', 'smart-tabs'],
      'no-async-promise-executor': 'warn',
      'require-await': 'warn',
      'no-return-await': 'warn',
      'no-await-in-loop': 'warn',
      'comma-dangle': 'off', // prettier already detects this
      'no-restricted-syntax': [
        'error',
        {
          selector: 'TSEnumDeclaration',
          message: "Enums have various disadvantages, use TypeScript's union types instead.",
        },
      ],

      // React plugin rules
      'react/no-unescaped-entities': 'off',

      // React Native plugin rules
      'react-native/no-unused-styles': 'warn',
      'react-native/split-platform-components': 'off',
      'react-native/no-inline-styles': 'warn',
      'react-native/no-color-literals': 'off',
      'react-native/no-raw-text': 'off',
      'react-native/no-single-element-style-arrays': 'warn',

      // React hooks rules
      'react-hooks/exhaustive-deps': [
        'error',
        {
          additionalHooks: '(useDerivedValue|useAnimatedStyle|useAnimatedProps|useWorkletCallback|useFrameProcessor)',
        },
      ],
    },
  },
];
