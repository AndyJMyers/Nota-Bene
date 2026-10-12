package com.notabene.app

import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayOrderTest {
    @Test fun untouchedOrderStaysUnchanged() {
        assertEquals(listOf(3L, 2L, 1L), displayOrder(listOf(3L, 2L, 1L), "") { it })
    }
    @Test fun deletedDuplicateAndMalformedIdsAreHarmless() {
        assertEquals(listOf(2L, 1L, 3L), displayOrder(listOf(3L, 2L, 1L), "99,2,2,bad,1") { it })
    }
    @Test fun newItemsFollowExplicitlyOrderedItems() {
        assertEquals(listOf(1L, 2L, 4L, 3L), displayOrder(listOf(4L, 3L, 2L, 1L), "1,2") { it })
    }
    @Test fun canMoveToEitherEnd() {
        assertEquals("3,1,2", movedOrder(listOf(1L, 2L, 3L), 3, 0))
        assertEquals("2,3,1", movedOrder(listOf(1L, 2L, 3L), 1, 2))
    }
    @Test fun stalePositionsAreClampedAndMissingItemsNotRecreated() {
        assertEquals("2,1", movedOrder(listOf(1L, 2L), 1, 99))
        assertEquals("1,2", movedOrder(listOf(1L, 2L), 8, 0))
    }
    @Test fun completedGroupRemainsSeparate() {
        val items = listOf(1L to true, 2L to false, 3L to false)
        assertEquals(listOf(3L to false, 2L to false, 1L to true),
            displayOrder(items, "1,3,2") { it.first }.sortedBy { it.second })
    }
}
