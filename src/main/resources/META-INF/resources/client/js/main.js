requirejs.config({
  //By default load any module IDs from js/lib
  baseUrl: 'js',

  paths: {
    lib: "lib"
  },

  shim: {

    'lib/bootstrap': {
      deps: ['lib/jquery']
    },

    'jquery-extensions': {
      deps: ['lib/jquery']
    },

    'lib/jquery.zclip': {
      deps: ['lib/jquery']
    },

    'lib/bootbox.min': {
      deps: ['lib/bootstrap']
    },

    'data': {
      deps: [
        'lib/jquery'
      ]},

 'client': {
      deps: [
        'oauth',
        'jquery-extensions',
        'lib/bootstrap',
        'lib/handlebars',
        'data',
        'resourceServerForm',
        'resourceServerGrid',
        'clientForm',
        'clientGrid',
        'accessTokenGrid',
        'statisticsGrid',
        'popoverBundle',
        'lib/jquery.zclip',
        'lib/bootbox.min'
      ]
    }
  }
});

require([
  "jquery-extensions",
  "lib/handlebars",
  "lib/bootstrap",
  "client"
]);