package org.calf.reader.novel.widget.recycler.scroller;

public interface FastScrollStateChangeListener {

    /**
     * Called when fast scrolling begins
     */
    void onFastScrollStart(FastScroller fastScroller);

    /**
     * Called when fast scrolling ends
     */
    void onFastScrollStop(FastScroller fastScroller);
}