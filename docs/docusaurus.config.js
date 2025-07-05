module.exports = {
  title: 'VisionCameraOld',
  tagline: '📸 The Camera library that sees the vision.',
  url: 'https://react-native-vision-camera-old.com',
  baseUrl: '/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'throw',
  favicon: '/favicon.ico',
  organizationName: 'mrousavy',
  projectName: 'react-native-vision-camera-old',
  themeConfig: {
    algolia: {
      appId: 'HBHXBT6T9E',
      apiKey: '64bc77eda92b7efcb7003b56815f1113',
      indexName: 'react-native-vision-camera-old',
    },
    prism: {
      additionalLanguages: ['swift', 'java', 'kotlin'],
    },
    navbar: {
      title: 'VisionCameraOld',
      logo: {
        alt: 'Logo',
        src: './android-chrome-192x192.png',
      },
      items: [
        {
          label: 'Guides',
          to: 'docs/guides',
          position: 'left',
        },
        {
          to: 'docs/api',
          label: 'API',
          position: 'left'
        },
        {
          href: 'https://github.com/mrousavy/react-native-vision-camera-old/tree/main/example',
          label: 'Example App',
          position: 'left'
        },
        {
          href: 'https://github.com/mrousavy/react-native-vision-camera-old',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Guides',
              to: 'docs/guides'
            },
            {
              label: 'API',
              to: 'docs/api',
            },
            {
              label: 'Example App',
              href: 'https://github.com/mrousavy/react-native-vision-camera-old/tree/main/example',
            },
          ],
        },
        {
          title: 'Community',
          items: [
            {
              label: 'GitHub discussions',
              href: 'https://github.com/mrousavy/react-native-vision-camera-old/discussions',
            },
            {
              label: 'Twitter (@mrousavy)',
              href: 'https://twitter.com/mrousavy',
            },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/mrousavy/react-native-vision-camera-old',
            },
            {
              label: 'Marc\'s Portfolio',
              href: 'https://mrousavy.com',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Marc Rousavy`,
    },
    image: 'img/11.png',
    metadata: [
      {
        name: 'keywords',
        content: 'documentation, coding, docs, guides, camera, react, native, react-native'
      },
      {
        name: 'og:title',
        content: 'VisionCameraOld Documentation'
      },
      {
        name: 'og:type',
        content: 'application'
      },
      {
        name: 'og:description',
        content: '📸 The Camera library that sees the vision.'
      },
      {
        name: 'og:image',
        content: '/img/11.png'
      },
    ],
    announcementBar: {
      id: 'support_us',
      content:
        '<b>I am currently working on <a target="_blank" href="https://github.com/mrousavy/react-native-vision-camera-old/issues/1376">VisionCameraOld V3 ✨</a> which brings a lot of new features and a full rewrite on Android. Please consider <a target="_blank" href="https://github.com/sponsors/mrousavy">sponsoring me on GitHub</a> so I can work on this.</b>',
      backgroundColor: '#ff5c5c',
      textColor: '#ffffff',
      isCloseable: false,
    },
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        sitemap: {
          changefreq: 'weekly',
          priority: 1.0,
          ignorePatterns: ['/tags/**'],
          filename: 'sitemap.xml',
        },
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          editUrl: 'https://github.com/mrousavy/react-native-vision-camera-old/edit/main/docs/',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
        gtag: {
          trackingID: 'G-FX9Q0YKV7S',
          anonymizeIP: false,
        },
      },
    ],
  ],
  plugins: [
    [
      'docusaurus-plugin-typedoc',
      {
        name: 'VisionCameraOld',
        entryPoints: ['../src'],
        exclude: "../src/index.ts",
        tsconfig: '../tsconfig.json',
        watch: process.env.TYPEDOC_WATCH,
        excludePrivate: true,
        excludeProtected: true,
        excludeExternals: true,
        excludeInternal: true,
        readme: "none",
        sidebar: {
          indexLabel: 'Overview'
        }
      },
    ],
  ],
};
