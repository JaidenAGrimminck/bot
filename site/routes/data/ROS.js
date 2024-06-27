const { AssertHas, Assert } = require("../../tests/Assert");
//const rosnodejs = require('rosnodejs');

//const nh = rosnodejs.nh;

const TAGS = {
    SUBANDFORWARD: 0x01,
    PUBLISH: 0x02
}

const TYPES = {
    STRING: 'std_msgs/String',
    INTEGER: "std_msgs/Int32",
    FLOAT: "std_msgs/Float32",
    BOOL: "std_msgs/Bool"
}

let subscribers = {};

function subscribe(tag, type, callback) {
    Assert(Object.keys(TYPES).includes(type), "Invalid type!");
    Assert(typeof tag == "string", "Tag must be a string!");

    if (subscribers[tag] == undefined) {
        subscribers[tag] = [];
    }

    subscribers[tag].push(callback);

    // todo: implement on raspi
    // nh.subscribe(tag, type, (msg) => {
    //     callback(msg.data);
    // })
}

function publish(tag, type, data) {
    Assert(Object.keys(TYPES).includes(type), "Invalid type!");
    Assert(typeof tag == "string", "Tag must be a string!");

    /* implement on raspi
    let pub = nh.advertise(tag, type);
    pub.publish({data});
    */
}

async function handleROSRequest(tag, data) {
    if (tag == TAGS.SUBANDFORWARD) {
        AssertHas(data, ["type", "name"]);

        subscribe(data.tag, (data) => {
            io.emit('ros', {data, tag: TAGS.PUBLISH});
        });
    } else if (tag == TAGS.PUBLISH) {
        //todo: implement
        AssertHas(data, ["type", "name"])

        publish(data.tag, data.type, data.data);
    }
}

module.exports = { handleROSRequest, TAGS, subscribe };