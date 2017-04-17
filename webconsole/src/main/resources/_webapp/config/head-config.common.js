/**
 * Configuration for head elements added during the creation of index.html.
 */
module.exports = {
  link: [
    /** <link> favicon for the website **/
    { rel: 'icon', type: 'image/png', href: '/snamp/assets/icon/favicon.ico' },

    /** <link> tags for a Web App Manifest **/
    { rel: 'manifest', href: '/snamp/assets/manifest.json' }
  ],
  meta: [
    { name: 'msapplication-TileColor', content: '#00bcd4' },
    { name: 'theme-color', content: '#00bcd4' }
  ]
};
