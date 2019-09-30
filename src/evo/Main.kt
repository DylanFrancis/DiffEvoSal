package evo

import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Math.pow
import java.util.concurrent.ExecutorService
import kotlin.random.Random
import kotlin.streams.toList

fun main(){
    Thread(Runnable { val p = SalPrediction(10000, 0.01, 0.4)
        p.start() }).start()

    Thread(Runnable { val p = SalPrediction(10000, 0.02, 0.4)
        p.start() }).start()

    Thread(Runnable { val p = SalPrediction(10000, 0.01, 0.8)
        p.start() }).start()

    Thread(Runnable { val p = SalPrediction(10000, 0.02, 0.8)
        p.start() }).start()
}

class SalPrediction(val population_size : Int, val f : Double, val c : Double){
    var trainingSet = listOf<Sample>()
    var testSet = listOf<Sample>()
    var currentPopulation = arrayListOf<Solution>()
    val newPopulation = arrayListOf<Solution>()
    val time = System.currentTimeMillis()

    init {
        readData()
        currentPopulation = initialisePopulation()
    }

    fun start(){
        for (i in 0..15000) {
            mutate()
            evaluate()
            save(i)
        }
        printGen()
        printTest()
    }

    val toBePrinted = mutableListOf<String>()

    fun save(generation : Int){
        toBePrinted.add("$generation" +
                ";${currentPopulation.stream().mapToDouble { t -> t.error }.average().asDouble}" +
                ";${currentPopulation.min().toString()}\n")
    }

    fun printTest(){
        FileOutputStream("./output_${population_size}_${f}_${c}_$time.csv", true).bufferedWriter().use { out ->
            val best = testPop(currentPopulation.min()!!, testSet)
            println(best)
            out.write("test;${best.toString()}\n")
            out.flush()
        }
    }

    fun printGen (){
        FileOutputStream("./output_${population_size}_${f}_${c}_$time.csv", true).bufferedWriter().use { out ->
            toBePrinted.forEach({s -> out.write(s)})
            out.flush()
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
            newPopulation.add(testPop(u, trainingSet))
        }
    }

    fun pick(cur : Solution) : Solution{
        var r : Solution
        do r = currentPopulation.random()
        while (cur == r)
        return r
    }

    fun testPop(pop : Solution, set : List<Sample>) : Solution {
        var error = 0.0
        for (sample in set){
            var prediction = 0.0
            for (p in 0..6){
                prediction += sample.parems[p + 1] * pop.weights[p]
            }
            error += pow(sample.parems[0] - prediction, 2.0)
        }
        return Solution(pop.weights, error)
    }

    /**
     * Returns a List of DoubleArrays with random Doubles for initial population initialisation
     */
    fun initialisePopulation() : ArrayList<Solution>{
        fun getWeights() : DoubleArray{
            val arr = DoubleArray(7)
            for (i in 0..6) arr[i] = Random.nextDouble(0.0, 5000.0)
            return arr
        }

        val arr = arrayListOf<Solution>()
        for (i in 0..population_size){
            arr.add(testPop(Solution(getWeights(), 0.0), trainingSet))
        }
        return arr
    }

    /**
     * Reads data to be trained upon
     */
    fun readData(){
        var dataSet = listOf<Sample>()
        FileInputStream("./resources/SalData.csv").bufferedReader().use { input ->
            dataSet = input.lines().map { i -> Sample(i.split(",")) }.toList()
        }
        trainingSet = dataSet.subList(0, (dataSet.size * 0.7).toInt())
        testSet     = dataSet.subList((dataSet.size * 0.7).toInt(), dataSet.size)
    }
}

