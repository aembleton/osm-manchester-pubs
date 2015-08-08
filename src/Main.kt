import java.io.DataInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.InflaterInputStream

/**
 * Created by arthur on 07/08/15.
 */

fun main(args: Array<String>) {

    val fis = FileInputStream("greater-manchester-latest.osm.pbf")
    val dis = DataInputStream(fis)

    while (dis.available() > 0) {
        val len = dis.readInt()
        val blobHeader = ByteArray(len)
        dis.read(blobHeader)
        val h = Fileformat.BlobHeader.parseFrom(blobHeader)
        val blob = ByteArray(h.getDatasize())
        dis.read(blob)
        val b = Fileformat.Blob.parseFrom(blob)

        var blobData: InputStream
        if (b.hasZlibData()) {
            blobData = InflaterInputStream(b.getZlibData().newInput())
        } else {
            blobData = b.getRaw().newInput()
        }

        if (h.getType().equals("OSMHeader")) {
            val hb = Osmformat.HeaderBlock.parseFrom(blobData)
            println("hb: ${hb.getSource()}")
        } else if (h.getType().equals("OSMData")) {
            val pb = Osmformat.PrimitiveBlock.parseFrom(blobData)

            pb.getPrimitivegroupList().forEach {
                message ->
                message.getWaysList().forEach {
                    way ->
                    if (isPub(way, pb.getStringtable())) {
                        println("")
                        for (i in 0..way.getKeysCount() - 1) {
                            val key = pb.getStringtable().getS(way.getKeys(i)).toStringUtf8()
                            val value = pb.getStringtable().getS(way.getVals(i)).toStringUtf8()

                            println("$key = $value")
                        }
                    }
                }
            }
        }
    }

}

fun isPub(way: Osmformat.Way, stringTable: Osmformat.StringTable): Boolean {
    for (i in 0..way.getKeysCount() - 1) {
        val value = stringTable.getS(way.getVals(i)).toStringUtf8().trim().toLowerCase()
        if (value.equals("pub")) {
            return true
        }
    }

    return false
}