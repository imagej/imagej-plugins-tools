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

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.display.ImageDisplay;

import org.scijava.display.Display;
import org.scijava.display.event.input.KyPressedEvent;
import org.scijava.display.event.input.MsWheelEvent;
import org.scijava.input.InputModifiers;
import org.scijava.input.KeyCode;
import org.scijava.plugin.Plugin;
import org.scijava.tool.AbstractTool;
import org.scijava.tool.Tool;

/**
 * Handles keyboard and mouse wheel operations that change the dimensional
 * position.
 * 
 * @author Grant Harris
 */
@Plugin(type = Tool.class, name = "Axis Position Shortcuts")
public class AxisPositionHandler extends AbstractTool {

	@Override
	public boolean isAlwaysActive() {
		return true;
	}

	@Override
	public boolean isActiveInAppFrame() {
		return true;
	}

	@Override
	public void onKeyDown(final KyPressedEvent evt) {
		final Display<?> display = evt.getDisplay();
		if (!(display instanceof ImageDisplay)) return;
		final ImageDisplay imageDisplay = (ImageDisplay) display;

		final KeyCode keyCode = evt.getCode();
		final char keyChar = evt.getCharacter();

		// determine direction to move based on key press
		final int delta;
		if (keyCode == KeyCode.PERIOD || keyCode == KeyCode.GREATER ||
			keyCode == KeyCode.KP_RIGHT || keyCode == KeyCode.RIGHT || keyChar == '>')
		{
			if (evt.getModifiers().isAltDown())
				delta = 10;
			else
				delta = 1;
		}
		else if (keyCode == KeyCode.COMMA || keyCode == KeyCode.LESS ||
			keyCode == KeyCode.KP_LEFT || keyCode == KeyCode.LEFT || keyChar == '<')
		{
			if (evt.getModifiers().isAltDown())
				delta = -10;
			else
				delta = -1;
		}
		else return; // inapplicable key

		final AxisType axis = getAxis(imageDisplay, evt.getModifiers());
		if (axis == null) return;

		final long pos = imageDisplay.getLongPosition(axis) + delta;
		imageDisplay.setPosition(pos, axis);
		evt.consume();
	}

	@Override
	public void onMouseWheel(final MsWheelEvent evt) {
		final Display<?> display = evt.getDisplay();
		if (!(display instanceof ImageDisplay)) return;
		final ImageDisplay imageDisplay = (ImageDisplay) display;

		final AxisType axis = getAxis(imageDisplay, evt.getModifiers());
		if (axis == null) return;

		final int rotation = evt.getWheelRotation();

		final long pos = imageDisplay.getLongPosition(axis) + rotation;
		imageDisplay.setPosition(pos, axis);
		evt.consume();
	}

	// -- Helper methods --

	/**
	 * Determines the axis to move based on the keyboard modifiers used.
	 * <ul>
	 * <li>No modifier moves channel axis</li>
	 * <li>Ctrl modifier moves Z axis</li>
	 * <li>Alt modifier moves Time axis</li>
	 * <li>If preferred axis does not exist, first avilable axis is used</li>
	 * </ul>
	 */
	private AxisType
		getAxis(final ImageDisplay display, final InputModifiers mods)
	{
		if (display.numDimensions() < 3) return null;

		// determine preferred axis based on keyboard modifier used
		final AxisType axis;
		if (mods.isAltDown() || mods.isAltGrDown()) axis = Axes.TIME;
		else if (mods.isCtrlDown() || mods.isMetaDown()) axis = Axes.Z;
		else axis = Axes.CHANNEL;

		if (display.dimensionIndex(axis) < 0) {
			// preferred axis does not exist; return first available axis instead
			return display.axis(2).type();
		}
		return axis;
	}

}
