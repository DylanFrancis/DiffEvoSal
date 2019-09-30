package evo

class Solution : Comparable<Solution>{
    val weights: DoubleArray
    val error: Double

    constructor(weights: DoubleArray, error: Double) {
        this.weights = weights
        this.error = error
    }

    constructor(size : Int){
        weights = DoubleArray(size)
        error = 0.0
    }

    override fun compareTo(other: Solution) : Int {
        return (error - other.error).toInt()
    }

    override fun toString(): String {
        var s = ""
        s = "$s error: $error"
        weights.forEach { w ->  s = "$s $w" }
        return s
    }
}