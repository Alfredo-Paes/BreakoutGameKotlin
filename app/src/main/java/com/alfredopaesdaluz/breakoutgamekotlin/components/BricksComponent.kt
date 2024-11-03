package com.alfredopaesdaluz.breakoutgamekotlin.components

class BricksComponent (
    var row: Int,
    var column: Int,
    var width: Int,
    var height: Int
) {
    var isVisible: Boolean = true
        private set

    fun setInvisible() {
        isVisible = false
    }

    fun getVisibility(): Boolean {
        return isVisible
    }

    fun setVisibility(isVisible: Boolean) {
        this.isVisible = isVisible
    }
}