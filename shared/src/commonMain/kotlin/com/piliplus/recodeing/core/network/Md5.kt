package com.piliplus.recodeing.core.network

private val Md5Shift = intArrayOf(
    7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
    5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20,
    4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
    6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21,
)

private val Md5Constants = IntArray(64) { index ->
    (kotlin.math.abs(kotlin.math.sin(index + 1.0)) * 4294967296.0).toLong().toInt()
}

internal fun md5Hex(value: String): String {
    val input = value.encodeToByteArray()
    val bitLength = input.size.toLong() * 8L
    val paddedSize = ((input.size + 9 + 63) / 64) * 64
    val message = ByteArray(paddedSize)
    input.copyInto(message)
    message[input.size] = 0x80.toByte()
    repeat(8) { index -> message[paddedSize - 8 + index] = (bitLength ushr (index * 8)).toByte() }

    var a0 = 0x67452301
    var b0 = 0xEFCDAB89.toInt()
    var c0 = 0x98BADCFE.toInt()
    var d0 = 0x10325476

    for (blockOffset in message.indices step 64) {
        val words = IntArray(16) { index ->
            val offset = blockOffset + index * 4
            (message[offset].toInt() and 0xFF) or
                ((message[offset + 1].toInt() and 0xFF) shl 8) or
                ((message[offset + 2].toInt() and 0xFF) shl 16) or
                ((message[offset + 3].toInt() and 0xFF) shl 24)
        }
        var a = a0
        var b = b0
        var c = c0
        var d = d0

        repeat(64) { index ->
            val (f, wordIndex) = when (index) {
                in 0..15 -> ((b and c) or (b.inv() and d)) to index
                in 16..31 -> ((d and b) or (d.inv() and c)) to ((5 * index + 1) % 16)
                in 32..47 -> (b xor c xor d) to ((3 * index + 5) % 16)
                else -> (c xor (b or d.inv())) to ((7 * index) % 16)
            }
            val nextD = d
            d = c
            c = b
            b += (a + f + Md5Constants[index] + words[wordIndex]).rotateLeft(Md5Shift[index])
            a = nextD
        }
        a0 += a
        b0 += b
        c0 += c
        d0 += d
    }

    return intArrayOf(a0, b0, c0, d0).joinToString("") { word ->
        buildString(8) {
            repeat(4) { byteIndex -> append(((word ushr (byteIndex * 8)) and 0xFF).toString(16).padStart(2, '0')) }
        }
    }
}
