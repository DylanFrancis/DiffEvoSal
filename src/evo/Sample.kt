package evo

class Sample(data: List<String>) {
    val parems = data.stream().mapToInt{s -> s.toInt()}.toArray()
        get() = field
}