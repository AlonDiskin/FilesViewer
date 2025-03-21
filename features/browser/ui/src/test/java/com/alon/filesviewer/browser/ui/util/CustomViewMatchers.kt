package com.alon.filesviewer.browser.ui.util

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.ChipGroup
import org.hamcrest.Description
import org.hamcrest.Matcher

fun withCheckedChip(id: Int): Matcher<View> {
    return object : BoundedMatcher<View, ChipGroup>(ChipGroup::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with checked chip id:${id}")
        }

        override fun matchesSafely(item: ChipGroup): Boolean {
            return item.checkedChipId == id
        }

    }
}

fun withRecyclerViewSize(size: Int): Matcher<View> {
    return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with items size:${size}")
        }

        override fun matchesSafely(item: RecyclerView): Boolean {
            return item.adapter!!.itemCount == size
        }
    }
}

fun withBottomNavSelected(id: Int): Matcher<View> {
    return object : BoundedMatcher<View, BottomNavigationView>(BottomNavigationView::class.java) {

        override fun describeTo(description: Description) {
            description.appendText("with item selected:${id}")
        }

        override fun matchesSafely(item: BottomNavigationView): Boolean {
            return item.selectedItemId == id
        }
    }
}