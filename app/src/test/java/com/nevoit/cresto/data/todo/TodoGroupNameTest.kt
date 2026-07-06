package com.nevoit.cresto.data.todo

import org.junit.Assert.assertEquals
import org.junit.Test

class TodoGroupNameTest {

    @Test
    fun resolveTodoGroupName_keepsNameWhenUnused() {
        assertEquals(
            "Work",
            resolveTodoGroupName(
                baseName = "Work",
                usedNames = setOf("Life")
            )
        )
    }

    @Test
    fun resolveTodoGroupName_addsNextSuffixWhenNameExists() {
        assertEquals(
            "Work (2)",
            resolveTodoGroupName(
                baseName = "Work",
                usedNames = setOf("Work", "Work (1)")
            )
        )
    }

    @Test
    fun resolveTodoGroupName_suffixesAlreadySuffixedNamesFromInputName() {
        assertEquals(
            "Work (1) (1)",
            resolveTodoGroupName(
                baseName = "Work (1)",
                usedNames = setOf("Work", "Work (1)")
            )
        )
    }
}
