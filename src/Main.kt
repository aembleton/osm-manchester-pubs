import java.io.DataInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.util.*
import java.util.zip.InflaterInputStream

/**
 * Created by arthur on 07/08/15.
 */


data class Pub(val name:String, val id:Long)
data class PubLocation(val name:String, val lat:Double, val lon:Double)

fun main(args: Array<String>) {

    val pubs = getPubs()
    val pubLocations = HashSet<PubLocation>()
println(pubs.size())
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
            val lat_offset = pb.getLatOffset()
            val lon_offset = pb.getLonOffset()
            val granularity = pb.getGranularity()
            pb.getPrimitivegroupList().forEach {
                message ->

                    if (message.getDense().getIdCount()>0) {

                        var id:Long = 0
                        var lat:Long = 0
                        var lon:Long = 0

                        for (i in 0..message.getDense().getIdCount()-1) {

                            id += message.getDense().getId(i)
                            lat += message.getDense().getLat(i)
                            lon += message.getDense().getLon(i)

                            val pub = pubs.firstOrNull { p -> p.id == id }

                            if (pub != null) {
                                val latitude = .000000001 * (lat_offset + (granularity * lat))
                                val longitude = .000000001 * (lon_offset + (granularity * lon))
                                pubLocations.add(PubLocation(pub.name, latitude, longitude))
                            }

                            val keyValNum = message.getDense().getKeysVals(i)
                            if (keyValNum != 0) {
                                val keyVal = pb.getStringtable().getS(keyValNum).toStringUtf8()
                                //println("$id=$keyVal")
                            }
                        }
                }
            }
        }
    }

//    pubLocations.forEach {
//        pub ->
//        println(pub.name)
//        println("https://www.google.co.uk/maps/place/${pub.lat},${pub.lon}")
//        println()
//    }

    println("${pubLocations.size()} pubs found")
}

fun getPubs():Set<Pub> {
    val pubs = HashSet<Pub>()

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

                        for (i in 0..way.getKeysCount() - 1) {
                            val key = pb.getStringtable().getS(way.getKeys(i)).toStringUtf8()
                            val value = pb.getStringtable().getS(way.getVals(i)).toStringUtf8()

                            if (key == "name") {
                                pubs.add(Pub(value, way.getRefs(0)))
                            }
                        }
                    }
                }
                message.getNodesList().forEach {
                    node ->
                    println("got one")
                    if (isPub(node, pb.getStringtable())) {
                        for (i in 0..node.getKeysCount() - 1) {
                            val key = pb.getStringtable().getS(node.getKeys(i)).toStringUtf8()
                            val value = pb.getStringtable().getS(node.getVals(i)).toStringUtf8()

                            if (key == "name") {
                                pubs.add(Pub(value, node.getId()))
                            }
                        }
                    }
                }

                if (message.getDense().getIdCount()>0) {
                    for (i in 0..message.getDense().getIdCount()-1) {
                        val keyValNum = message.getDense().getKeysVals(i)
                        if (keyValNum != 0) {
                            val keyVal = pb.getStringtable().getS(keyValNum).toStringUtf8()
                            if (keyVal == "pub"){
                                pubs.add(Pub(keyVal, message.getDense().getId(i)))
                            }
                        }
                    }
                }

            }
        }
    }

    dis.close()
    fis.close()

    return pubs
}

fun isPub(node: Osmformat.Node, stringTable: Osmformat.StringTable): Boolean {
    for (i in 0..node.getKeysCount() - 1) {
        val value = stringTable.getS(node.getVals(i)).toStringUtf8().trim().toLowerCase()
        if (value.equals("pub")) {
            return true
        }
    }

    return false
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