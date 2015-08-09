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

        if (h.getType().equals("OSMData")) {
            val pb = Osmformat.PrimitiveBlock.parseFrom(blobData)
            pb.getPrimitivegroupList().forEach {
                message ->
                message.getWaysList().forEach {
                    way ->
                    if (isPub(way, pb.getStringtable())) {
                        //println("")

                        for (i in 0..way.getKeysCount() - 1) {
                            val key = pb.getStringtable().getS(way.getKeys(i)).toStringUtf8()
                            val value = pb.getStringtable().getS(way.getVals(i)).toStringUtf8()

                            //println("$key = $value")
                        }

                        //println("refs: ${way.getRefsList()}")
                        //println("id=${way.getId()} guid=${way.getInfoOrBuilder().getUid()}")
                    }
                }
                val nodes = message.getNodesList().filter { node -> node.getId()==3671342415 }
                if (nodes.isNotEmpty()) {
                    println("FOUND ${nodes.get(0).getLat()}")

                }

                if (message.getDense().getIdCount()>0) {
                    //if (message.getDense().getIdList().contains(3671342415)) {
                    var id = message.getDense().getId(0);
                    var lat = message.getDense().getLat(0);
                    var lon = message.getDense().getLon(0);

                    if (id==3671342415) {
                        println(getLatLonString(pb,lat,lon))
                    }

                    if (message.getDense().getIdCount() > 1) {
                        for (i in 1..message.getDense().getIdCount() - 1) {

                            id += message.getDense().getId(i)
                            lat += message.getDense().getLat(i)
                            lon += message.getDense().getLon(i)

                            if (id==3671342415) {
                                println(getLatLonString(pb,lat,lon))
                            }
                        }
                    }
                }
            }
        }
    }

}

fun getLatLonString(pb:Osmformat.PrimitiveBlock, lat:Long, lon:Long):String {
    val lat_offset=pb.getLatOffset()
    val lon_offset=pb.getLonOffset()
    val granularity=pb.getGranularity()
    val latitude=.000000001 * (lat_offset + (granularity * lat))
    val longitude=.000000001 * (lon_offset + (granularity * lon))

    return "latlon: $latitude,$longitude"
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