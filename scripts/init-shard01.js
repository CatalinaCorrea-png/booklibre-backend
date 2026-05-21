rs.initiate({_id: "rs-shard-01", version: 1,
    members: [
        { _id: 0, host : "shard01-a:27017" },
        { _id: 1, host : "shard01-b:27017" },
        { _id: 2, host : "shard01-c:27017" },
    ] })

// 2. Shard 01 (replica set)
// docker exec -it shard-01-node-a mongosh --port 27017 --file /scripts/init-shard01.js