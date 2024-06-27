
function Assert(obj, message) {
    if (!obj) {
        throw new Error(message);
    }
}

function AssertHas(obj, prop) {
    if (typeof prop == "string") {
        if (!obj[prop]) {
            throw new Error(`Object does not have property ${prop}`);
        }
    } else if (typeof prop == "object" && prop.length) {
        for (let p of prop) {
            if (!obj[p]) {
                throw new Error(`Object does not have property ${p}`);
            }
        }
    }
}

module.exports = {
    Assert, AssertHas
}