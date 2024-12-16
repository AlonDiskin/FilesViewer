package com.alon.filesviewer.browser.ui.viewmodel

import com.alon.filesviewer.browser.domain.model.BrowseRequest
import java.util.Stack
import javax.inject.Inject

class BrowserRequestStack @Inject constructor() {

    private val stack = Stack<BrowseRequest>()

    fun setRoot(request: BrowseRequest) {
        stack.clear()
        stack.push(request)
    }

    fun setCurrent(request: BrowseRequest) {
        stack.push(request)
    }

    fun popToParent(): BrowseRequest {
        stack.pop()
        return stack.pop()
    }

    fun isCurrentRoot(): Boolean {
        return stack.size == 1
    }
}