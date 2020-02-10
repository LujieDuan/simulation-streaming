function calculateNewValue() {
    return 0;
}

var config = require('./config');
var fs = require("fs");
var msg = 'Starting...';
console.log(msg);

const googleTrends = require('google-trends-api');
// googleTrends.interestOverTime({keyword: 'Flu', startTime: new Date('2019-05-13 00:00:00'), endTime: new Date('2019-05-14 00:00:00'), granularTimeResolution: true, geo: 'CA'})
// .then(function(results){
//   console.log('These results are awesome', results);
// })
// .catch(function(err){
//   console.error('Oh no there was an error', err);
// });


googleTrends.interestOverTime({keyword: config.keyword, startTime: config.start_time, endTime: config.end_time, granularTimeResolution: true, geo: 'CA'})
.then(function(results){
    r = JSON.parse(results);
    out_str = "";
    for (var i = 0; i < Object.keys(r.default.timelineData).length; i ++ ) {
        out_str += r.default.timelineData[i].value[0] + '\n';
    }
    fs.writeFile("output-values.json", out_str, (err) => {
        if (err) console.log(err);
      });
  //console.log('These results are awesome', JSON.stringify(JSON.parse(results), null, 4));
  fs.writeFile("output.json", JSON.stringify(JSON.parse(results), null, 4), (err) => {
    if (err) console.log(err);
    console.log("Successfully Written to File.");
  });

})
.catch(function(err){
  console.error('Oh no there was an error', err);
});

var redis = require("redis"),
client = redis.createClient("redis://redisdb:6379");

client.on("error", function (err) {
    console.log("Error " + err);
});

client.set("string key", "string val", redis.print);
client.hset("hash key", "hashtest 1", "some value", redis.print);
client.hset(["hash key", "hashtest 2", "some other value"], redis.print);
client.hkeys("hash key", function (err, replies) {
    console.log(replies.length + " replies:");
    replies.forEach(function (reply, i) {
        console.log("    " + i + ": " + reply);
    });
    client.quit();
});

// Send new value to the corresponding channel on Redis
value = calculateNewValue();
client.publish(config.channel, value);