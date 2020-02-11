var config = require('./config');
var fs = require("fs");
const googleTrends = require('google-trends-api');
console.log('INFO: Google Trends Collector Starting...');

const sleep = (milliseconds) => {
    return new Promise(resolve => setTimeout(resolve, milliseconds))
}

const collect = async () => {
    
    googleTrends.interestOverTime({keyword: config.keyword, startTime: config.start_time, endTime: config.end_time, granularTimeResolution: true, geo: 'CA'})
    .then(function(results){
        r = JSON.parse(results);
        out_str = "";
        final_count = ""
        for (var i = 0; i < Object.keys(r.default.timelineData).length; i ++ ) {
            out_str += r.default.timelineData[i].value[0] + '\n';
            final_count = r.default.timelineData[i].value[0] + '\n';
        }
        fs.writeFile("output-values.json", out_str, (err) => {
            if (err) console.log(err);
        });
        fs.writeFile("output.json", JSON.stringify(JSON.parse(results), null, 4), (err) => {
            if (err) console.log(err);
            console.log("INFO: Successfully Written to File.");
        });

        var redis = require("redis"),
        client = redis.createClient("redis://redisdb:6379");

        client.on("error", function (err) {
            console.log("ERROR: " + err);
        });

        // Send new value to the corresponding channel on Redis
        client.publish(config.channel, final_count);
    })
    .catch(function(err){
    console.error('ERROR: ', err);
    });
    sleep(1000 * 60 * 60 * 24).then(() => {
        collect();
    });
}


sleep(1000 * 2).then(() => {
    collect();
});
