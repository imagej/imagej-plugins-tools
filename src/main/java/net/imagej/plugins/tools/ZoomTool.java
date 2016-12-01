/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.plugins.tools;

import net.imagej.display.ImageDisplay;

import org.scijava.display.Display;
import org.scijava.display.event.input.MsButtonEvent;
import org.scijava.display.event.input.MsMovedEvent;
import org.scijava.display.event.input.MsPressedEvent;
import org.scijava.display.event.input.MsReleasedEvent;
import org.scijava.display.event.input.MsWheelEvent;
import org.scijava.plugin.Plugin;
import org.scijava.tool.AbstractTool;
import org.scijava.tool.Tool;
import org.scijava.util.IntCoords;
import org.scijava.util.IntRect;

/**
 * Tool for zooming in and out of a display using the mouse.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(type = Tool.class, name = "Zoom",
	description = "Magnifying glass (or use \"+\" and \"-\" keys)",
	iconPath = "/icons/tools/zoom.png", priority = ZoomTool.PRIORITY)
public class ZoomTool extends AbstractTool {

	public static final double PRIORITY = -200;

	private static final int DRAG_THRESHOLD = 8;

	private final IntCoords mousePos = new IntCoords(0, 0);
	private final IntCoords mouseDown = new IntCoords(0, 0);
	private final IntCoords mouseUp = new IntCoords(0, 0);

	// -- Tool methods --

	// NB: We do not respond to onKeyDown() for plus and minus here, because
	// the ZoomHandler always-active tool deals with those shortcuts.
	// So they will work regardless of which tool is selected anyway.

	@Override
	public void onMouseDown(final MsPressedEvent evt) {
		mouseDown.x = evt.getX();
		mouseDown.y = evt.getY();
		evt.consume();
	}

	@Override
	public void onMouseUp(final MsReleasedEvent evt) {
		final Display<?> display = evt.getDisplay();
		if (!(display instanceof ImageDisplay)) return;
		final ImageDisplay imageDisplay = (ImageDisplay) display;

		mouseUp.x = evt.getX();
		mouseUp.y = evt.getY();
		final int xDist = Math.abs(mouseUp.x - mouseDown.x);
		final int yDist = Math.abs(mouseUp.y - mouseDown.y);

		// ensure mouse movement exceeds threshold
		if (xDist > DRAG_THRESHOLD || yDist > DRAG_THRESHOLD) {
			// over threshold: zoom to rectangle
			if (mouseUp.x < mouseDown.x) {
				// swap X coordinates
				final int x = mouseUp.x;
				mouseUp.x = mouseDown.x;
				mouseDown.x = x;
			}
			if (mouseUp.y < mouseDown.y) {
				// swap Y coordinates
				final int y = mouseUp.y;
				mouseUp.y = mouseDown.y;
				mouseDown.y = y;
			}
			final int width = mouseUp.x - mouseDown.x;
			final int height = mouseUp.y - mouseDown.y;
			final IntRect bounds =
				new IntRect(mouseDown.x, mouseDown.y, width, height);
			imageDisplay.getCanvas().zoomToFit(bounds);
		}
		else {
			// under threshold: just zoom
			final boolean zoomOut =
				evt.getButton() != MsButtonEvent.LEFT_BUTTON ||
					evt.getModifiers().isCtrlDown();
			if (zoomOut) {
				imageDisplay.getCanvas().zoomOut(mouseDown);
			}
			else {
				imageDisplay.getCanvas().zoomIn(mouseDown);
			}
		}
		evt.consume();
	}

	@Override
	public void onMouseMove(final MsMovedEvent evt) {
		mousePos.x = evt.getX();
		mousePos.y = evt.getY();
	}

	@Override
	public void onMouseWheel(final MsWheelEvent evt) {
		final Display<?> display = evt.getDisplay();
		if (!(display instanceof ImageDisplay)) return;
		final ImageDisplay imageDisplay = (ImageDisplay) display;

		final IntCoords center = new IntCoords(evt.getX(), evt.getY());
		if (evt.getWheelRotation() < 0) {
			imageDisplay.getCanvas().zoomIn(center);
		}
		else {
			imageDisplay.getCanvas().zoomOut(center);
		}
		evt.consume();
	}

}
