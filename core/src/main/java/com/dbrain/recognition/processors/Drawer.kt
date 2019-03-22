package com.dbrain.recognition.processors

import android.graphics.Canvas

/**
 *   Basic class for drawing custom overlays over camera image.
 **/
abstract class Drawer {
    /**
     * This method is called when any event in processor happened - postEvent() method in processor called.
     * Called on main thread.
     */
    abstract fun receiveEvent(data: DataBundle)

    /**
     *  This method is called when any event from processor received and on every onDraw() method of the overlay view.
     *
     *  @param cropRegionWidth calculated width of the crop region defined by crop parameters of the builder.
     *  @param cropRegionHeight calculated height the crop region defined by crop parameters of the builder.
     *  Use them for displaying custom drawings on canvas. For example, boundaries of the crop region.
     *  The center of the crop region is always in the center of the screen.
     **/
    abstract fun draw(cropRegionWidth: Int, cropRegionHeight: Int, canvas: Canvas?)

}