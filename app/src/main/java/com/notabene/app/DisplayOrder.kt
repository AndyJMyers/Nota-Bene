package com.notabene.app

/** Unknown/deleted IDs are ignored; new records retain their normal order at the end. */
internal fun <T> displayOrder(items: List<T>, stored: String, id: (T) -> Long): List<T> {
    val ranks = stored.split(',').mapNotNull { it.toLongOrNull() }.distinct()
        .withIndex().associate { it.value to it.index }
    return items.sortedBy { ranks[id(it)] ?: Int.MAX_VALUE }
}

internal fun movedOrder(ids: List<Long>, id: Long, position: Int): String {
    if (id !in ids) return ids.joinToString(",")
    val result = ids.distinct().toMutableList()
    result.remove(id)
    result.add(position.coerceIn(0, result.size), id)
    return result.joinToString(",")
}
