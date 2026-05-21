sh.addShard("rs-shard-01/shard01-a:27017")
sh.addShard("rs-shard-01/shard01-b:27017")
sh.addShard("rs-shard-01/shard01-c:27017")
sh.addShard("rs-shard-02/shard02-a:27017")
sh.addShard("rs-shard-02/shard02-b:27017")
sh.addShard("rs-shard-02/shard02-c:27017")

// 4. Registrar shards en el router (correrlo cuando los replica sets ya tengan PRIMARY elegido)
// docker exec -it router-01 mongosh --port 27017 --file /scripts/init-router.js