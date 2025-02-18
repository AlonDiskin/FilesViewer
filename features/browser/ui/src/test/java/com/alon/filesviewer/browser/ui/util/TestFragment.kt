package com.alon.filesviewer.browser.ui.util

import androidx.fragment.app.Fragment
import com.alon.filesviewer.browser.ui.R
class TestFragment : Fragment(R.layout.fragment_test) {

    companion object {
        const val KEY_TYPE = "type"
    }

    fun getType(): String {
        return arguments?.getString(KEY_TYPE)!!
    }
}