var files = require('./lib/files');
var types = require('./lib/types');
var time = require('./lib/time');
var logger = require('./lib/logger');
var string = require('./lib/string');
var object = require('./lib/object');
var stream = require('./lib/stream');

exports.log = logger.log;
exports.time = time.time;
exports.diff = time.diff;
exports.getTime = time.getTime;
