package evo

import java.io.FileInputStream
import kotlin.math.abs
import kotlin.random.Random
import kotlin.streams.toList

fun main(){
    val p = SalPrediction(10000)
    p.start()
}

class SalPrediction(val population_size : Int){
    val data = readData()
    var currentPopulation = initialisePopulation()
    val newPopulation = arrayListOf<Solution>()

    var f = 0.000001    // scale factor
    var c = 0.5         // crossover probability

    fun start(){
        for (i in 0..10000000) {
            mutate()
            evaluate()
            println(currentPopulation.min())
        }
    }

    fun evaluate(){
        val nextPopulation = arrayListOf<Solution>()
        for (x in 0 until currentPopulation.size){
            val curPop = currentPopulation[x]
            val newPop = newPopulation[x]
            if (newPop.error < curPop.error) nextPopulation.add(newPop)
            else nextPopulation.add(curPop)
        }
        currentPopulation = nextPopulation
        newPopulation.clear()
    }

    fun mutate(){
        for (r1 in currentPopulation) {
            val r2 = pick(r1)
            val r3 = pick(r2)

            val newVectors = DoubleArray(r1.weights.size)
            val u = Solution(r1.weights.size)

            for (r in 0 until r1.weights.size) newVectors[r] = r1.weights[r] + f * (r2.weights[r] - r3.weights[r])

            for (r in 0 until r1.weights.size){
                if (Random.nextDouble(0.0, 1.1) < c) u.weights[r] = newVectors[r]
                else u.weights[r] = r1.weights[r]
            }
            newPopulation.add(testPop(u))
        }
    }

    fun pick(cur : Solution) : Solution{
        var r : Solution
        do r = currentPopulation.random()
        while (cur == r)
        return r
    }

    fun testPop(pop : Solution) : Solution {
        var error = 0.0
        for (sample in data){
            var prediction = 0.0
            for (p in 0..6){
                prediction += sample.parems[p + 1] * pop.weights[p]
            }
            error += abs(prediction - sample.parems[0])
        }
        return Solution(pop.weights, error)
    }

    /**
     * Returns a List of DoubleArrays with random Doubles for initial population initialisation
     */
    fun initialisePopulation() : ArrayList<Solution>{
        fun getWeights() : DoubleArray{
            val arr = DoubleArray(7)
            for (i in 0..6) arr[i] = Random.nextDouble(0.0, 100000.0)
            return arr
        }

        val arr = arrayListOf<Solution>()
        for (i in 0..population_size){
            arr.add(testPop(Solution(getWeights(), 0.0)))
        }
        return arr
    }

    /**
     * Reads data to be trained upon
     */
    fun readData() : List<Sample>{
        FileInputStream("./resources/SalData.csv").bufferedReader().use { input ->
            return input.lines().map { i -> Sample(i.split(",")) }.toList()
        }
    }
}

