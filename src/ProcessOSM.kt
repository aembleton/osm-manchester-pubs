/**
 * Created by arthur on 19/08/15.
 */

package osmformat

import java.io.File
import java.math.BigDecimal
import java.util.*

data class Pub(val name:String, val lat:BigDecimal, val lon:BigDecimal)
enum class State{DEFAULT, NODE, IS_A_PUB}

fun main(args: Array<String>) {
    val osm = File("greater-manchester-latest.osm")
    val pubs = HashSet<Pub>()
    var state = State.DEFAULT
    var name = ""
    var lat = BigDecimal.ZERO
    var lon = BigDecimal.ZERO

    osm.forEachLine{
        when {
            it.trim().startsWith("<node id=") -> {

                val extractedLat = """lat="(.*?)"""".toRegex().match(it)?.value?.substringAfter(""""""")?.substringBefore(""""""")
                val extractedLon = """lon="(.*?)"""".toRegex().match(it)?.value?.substringAfter(""""""")?.substringBefore(""""""")

                if (extractedLat != null && extractedLon!=null) {
                    lat = BigDecimal(extractedLat)
                    lon = BigDecimal(extractedLon)
                    //println("$lat, $lon")
                }

                state = State.NODE
            }
            it.trim().equals("""<tag k="amenity" v="pub"/>""") -> state = State.IS_A_PUB
            it.trim().startsWith("""<tag k="name" v="""") -> {
                val extractedName = """v="(.*?)"""".toRegex().match(it)?.value?.substringAfter(""""""")?.substringBefore(""""""")
                if (extractedName != null){
                    name = extractedName
                }
            }
            it.trim().equals("</node>") -> {
                if (state == State.IS_A_PUB && name.isNotBlank()){
                    pubs.add(Pub(name, lat, lon))
                }
                name = ""
                state = State.DEFAULT
            }
        }
    }

    pubs.forEach { pub ->
        println("${pub.name} ->  https://www.google.co.uk/maps?q=${pub.lat},${pub.lon}")
    }
}
