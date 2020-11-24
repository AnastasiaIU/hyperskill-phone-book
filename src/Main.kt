package phonebook

import java.io.File
import kotlin.math.floor
import kotlin.math.sqrt

fun main() {
    sortAndSearch("linear search", ::linearSearch)
    sortAndSearch("bubble sort + jump search", ::jumpSearch, ::bubbleSort)
    sortAndSearch("quick sort + binary search", ::binarySearch, ::quickSort)
    sortAndSearch("hash table", hashTable = true)
}

object Data {
    val phoneNumbers = File("files\\directory.txt").readLines()
    val toFind = File("files\\find.txt").readLines()
    var stopped = ""
    var startBbS = 0L // BbS - Bubble Sort
    var timeLS = 0L // LS - Linear Search
}

fun sortAndSearch(
        searchName: String,
        searchType: ((List<String>, String) -> Boolean)? = null,
        sortType: ((List<String>) -> List<String>)? = null,
        hashTable: Boolean = false
) {
    var found = 0
    var startSort = 0L
    var endSort = 0L
    var data = Data.phoneNumbers
    val hashMap = HashMap<String, String>()

    println("Start searching ($searchName)...")

    if (sortType != null) {
        startSort = System.currentTimeMillis()

        if (sortType == ::bubbleSort) Data.startBbS = startSort

        data = sortType(data)

        endSort = System.currentTimeMillis()
    }

    if (hashTable) {
        startSort = System.currentTimeMillis()

        data.forEach { hashMap[it.substringAfter(" ").toHashCode()] = it }

        endSort = System.currentTimeMillis()
    }

    val startSearch = System.currentTimeMillis()

    when {
        hashTable -> Data.toFind.forEach { if (hashMap.containsKey(it.toHashCode())) found++ }
        Data.stopped.isNotEmpty() -> Data.toFind.forEach { if (linearSearch(data, it)) found++ }
        searchType != null -> Data.toFind.forEach { if (searchType(data, it)) found++ }
    }

    val endSearch = System.currentTimeMillis()
    val timeSort = String.format("%1\$tM min. %1\$tS sec. %1\$tL ms.", endSort - startSort)
    val timeSearch = String.format("%1\$tM min. %1\$tS sec. %1\$tL ms.", endSearch - startSearch)
    val timeTotal = String.format("%1\$tM min. %1\$tS sec. %1\$tL ms.", endSort - startSort + (endSearch - startSearch))
    var textSort = ""

    if (searchType == ::linearSearch) Data.timeLS = endSearch - startSearch

    if (sortType != null) textSort = "Sorting time: $timeSort${Data.stopped}\nSearching time: $timeSearch\n"
    if (hashTable) textSort = "Creating time: $timeSort${Data.stopped}\nSearching time: $timeSearch\n"

    println("Found $found / ${Data.toFind.size} entries. Time taken: $timeTotal\n" + textSort)

    Data.stopped = ""
}

fun bubbleSort(data: List<String>): List<String> {
    val sortedData = data.toMutableList()

    loop@for (i in data.lastIndex - 1 downTo 0) {
        for (j in 0..i) {
            if (sortedData[j].substringAfter(" ") > sortedData[j + 1].substringAfter(" ")) {
                val tempLine = sortedData[j]
                sortedData[j] = sortedData[j + 1]
                sortedData[j + 1] = tempLine
            }

            if (System.currentTimeMillis() - Data.startBbS > Data.timeLS * 10) {
                Data.stopped = " - STOPPED, moved to linear search"
                break@loop
            }
        }
    }

    return sortedData.toList()
}

fun quickSort(data: List<String>): List<String> {
    return if (data.size < 2) data else {
        val pivot = data.random()
        val (prePivot, afterPivot) = data.partition { it.substringAfter(" ") <= pivot.substringAfter(" ") }

        quickSort(prePivot) + quickSort(afterPivot)
    }
}

fun String.toHashCode(): String {
    var firstName = this.substringBefore(" ")
    var lastName = this.substringAfter(" ")

    while (firstName.length < 5) firstName += "0"
    while (lastName.length < 5) lastName += "0"

    return firstName.substring(0, 5) + lastName.substring(0, 5)
}

fun linearSearch(data: List<String>, toFind: String): Boolean {
    for (line in data) if (line.contains(toFind)) return true
    return false
}

fun jumpSearch(data: List<String>, toFind: String): Boolean {
    val sqrt = floor(sqrt(data.size.toDouble())).toInt()
    var index1 = if (data.size - sqrt * sqrt == 0) data.lastIndex else data.size - (data.size - sqrt * sqrt) + 1
    var index2 = data.size

    for (i in data.indices step sqrt) {
        if (data[i].substringAfter(" ") >= toFind) {
            index1 = i - sqrt + 1
            index2 = i + 1
            break
        }
    }

    data.subList(index1, index2).reversed().forEach { if (it.contains(toFind)) return true }

    return false
}

fun binarySearch(data: List<String>, toFind: String): Boolean {
    return if (data.size == 1) data[0].contains(toFind) else {
        val middle = data.lastIndex / 2

        if (data[middle].contains(toFind)) true else {
            val subList = if (data[middle].substringAfter(" ") > toFind) data.subList(0, middle) else {
                data.subList(middle + 1, data.size)
            }

            binarySearch(subList, toFind)
        }
    }
}
