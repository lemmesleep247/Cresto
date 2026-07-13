package com.nevoit.cresto.ui.components.glasense

import org.junit.Assert.assertEquals
import org.junit.Test

class GlasenseSwipeableTest {
    @Test
    fun `offset moves in either configured swipe direction`() {
        assertEquals(
            -80f,
            rubberBandSwipeOffset(
                currentOffset = 0f,
                delta = -80f,
                leftRevealOffsetPx = 100f,
                rightRevealOffsetPx = 120f,
                viewportWidthPx = 500f
            )
        )
        assertEquals(
            80f,
            rubberBandSwipeOffset(
                currentOffset = 0f,
                delta = 80f,
                leftRevealOffsetPx = 100f,
                rightRevealOffsetPx = 120f,
                viewportWidthPx = 500f
            )
        )
    }

    @Test
    fun `offset does not move toward a direction without actions`() {
        assertEquals(
            0f,
            rubberBandSwipeOffset(
                currentOffset = 0f,
                delta = -80f,
                leftRevealOffsetPx = 0f,
                rightRevealOffsetPx = 120f,
                viewportWidthPx = 500f
            )
        )
        assertEquals(
            0f,
            rubberBandSwipeOffset(
                currentOffset = 0f,
                delta = 80f,
                leftRevealOffsetPx = 100f,
                rightRevealOffsetPx = 0f,
                viewportWidthPx = 500f
            )
        )
    }

    @Test
    fun `rubber band resistance is symmetric`() {
        val leftOffset = rubberBandSwipeOffset(
            currentOffset = -100f,
            delta = -80f,
            leftRevealOffsetPx = 100f,
            rightRevealOffsetPx = 120f,
            viewportWidthPx = 500f
        )
        val rightOffset = rubberBandSwipeOffset(
            currentOffset = 100f,
            delta = 80f,
            leftRevealOffsetPx = 120f,
            rightRevealOffsetPx = 100f,
            viewportWidthPx = 500f
        )

        assertEquals(-leftOffset, rightOffset)
    }
}
