
module.exports = {
    /**
     * Unpacks a buffer into a type and data
     * @param {*} method first byte to indicate endian, > or <, then second for type.
     * @param {*} data the raw byte list
     * @returns 
     */
    unpack(type="", data=Buffer.from([])) {
        if (type.length != 2) {
            return null;
        }

        const bigEndian = type[0] == ">";
        const dataType = type[1];

        let unpacked = null;

        switch (dataType) {
            case "h": // short / int16
                unpacked = bigEndian ? data.readUInt16BE() : data.readUInt16LE();
                break;
            case "i": // int32 / int
                unpacked = bigEndian ? data.readUInt32BE() : data.readUInt32LE();
                break;
            case "q": // long / int64
                unpacked = bigEndian ? data.readBigUInt64BE() : data.readBigUInt64LE();
                break;
            case "f": // float / float32
                unpacked = bigEndian ? data.readFloatBE() : data.readFloatLE();
                break;
            case "d": // double / float64
                unpacked = bigEndian ? data.readDoubleBE() : data.readDoubleLE();
                break;
            case "?": // bool / boolean
                unpacked = data.readUInt8() == 1;
                break;
            case "I": // uint32 / uint
                unpacked = bigEndian ? data.readUInt32BE() : data.readUInt32LE();
                break;
                
            default:
                unpacked = data.toString();
                break;
        }

        return unpacked;
    },

    /**
     * Packs a value into a buffer
     * @param {*} type the type of the value
     * @param {*} value the value to pack
     * @returns 
     */
    pack(type, value) {
        const bigEndian = type[0] == ">";

        let packed = null;

        switch (type[1]) {
            case "h": // short / int16
                packed = Buffer.alloc(2);
                bigEndian ? packed.writeUInt16BE(value) : packed.writeUInt16LE(value);
                break;
            case "i": // int32 / int
                packed = Buffer.alloc(4);
                bigEndian ? packed.writeUInt32BE(value) : packed.writeUInt32LE(value);
                break;
            case "q": // long / int64
                packed = Buffer.alloc(8);
                bigEndian ? packed.writeBigUInt64BE(value) : packed.writeBigUInt64LE(value);
                break;
            case "f": // float / float32
                packed = Buffer.alloc(4);
                bigEndian ? packed.writeFloatBE(value) : packed.writeFloatLE(value);
                break;
            case "d": // double / float64
                packed = Buffer.alloc(8);
                bigEndian ? packed.writeDoubleBE(value) : packed.writeDoubleLE(value);
                break;
            case "?": // bool / boolean
                packed = Buffer.alloc(1);
                packed.writeUInt8(value ? 1 : 0);
                break;
            case "I": // uint32 / uint
                packed = Buffer.alloc(4);
                bigEndian ? packed.writeUInt32BE(value) : packed.writeUInt32LE(value);
                break;
            default:
                packed = Buffer.from(value);
                break;
        }

        return packed;
    }
}