rs.initiate({_id: "rs-shard-02", version: 1,
    members: [
        { _id: 0, host : "shard02-a:27017" },
        { _id: 1, host : "shard02-b:27017" },
        { _id: 2, host : "shard02-c:27017" },
    ] })

// 3. Shard 02 (replica set)
// docker exec -it shard-02-node-a mongosh --port 27017 --file /scripts/init-shard02.js