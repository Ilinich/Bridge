// The image CDN sends no Access-Control-Allow-Origin, so a browser refuses the pictures the
// native builds load without a thought. Same-origin "/media" is proxied to it here, and a
// deployed build needs the same one-line rewrite on whatever serves it. See ImageProxy.kt.
config.devServer = config.devServer || {};
config.devServer.proxy = [
    {
        context: ["/media"],
        target: "https://r2.thesportsdb.com",
        changeOrigin: true,
        pathRewrite: { "^/media": "/images/media" },
    },
];
