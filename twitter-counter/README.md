# Twitter Counter


```shell script
rsync --progress --files-from=<(find ~/Downloads/AllSKTweets/ -mtime -3 -type f -exec basename {} \;) ~/Downloads/AllSKTweets/ ~/programming/simulation-streaming/twitter-counter/data/

```
