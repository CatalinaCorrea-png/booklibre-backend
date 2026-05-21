rs.initiate({
    _id: "rs-config-server", configsvr: true, version: 1,
    members: [
        { _id: 0, host : 'configsvr01:27017' },
        { _id: 1, host : 'configsvr02:27017' },
        { _id: 2, host : 'configsvr03:27017' }
    ]
})

// 1. Config server (replica set)
// docker exec -it mongo-config-01 mongosh --port 27017 --file /scripts/init-configserver.js