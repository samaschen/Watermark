// you do not need to understand how it works, ignore it
class Pirate (name : String) {
    var name: String = name
        private set    
}
// Do not touch the lines above

fun main() {
    // fix the declaration below
    val captain = Pirate("Hector Barbossa")
    println(captain.name)
    val cur_cap = Pirate("Jack Sparrow")
    println(cur_cap.name)
    // put your code here


}
