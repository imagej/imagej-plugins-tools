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

import net.imagej.ChannelCollection;
import net.imagej.axis.Axes;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;

import org.scijava.app.StatusService;
import org.scijava.display.event.input.MsMovedEvent;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.tool.AbstractTool;
import org.scijava.tool.Tool;

/**
 * Displays pixel values under the cursor.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Tool.class, name = "Probe")
public class PixelProbe extends AbstractTool {

	@Parameter
	private StatusService statusService;

	@Parameter
	private ImageDisplayService imageDisplayService;

	private PixelRecorder recorder;

	// -- Tool methods --

	@Override
	public boolean isAlwaysActive() {
		return true;
	}

	// NB - this tool does not consume the events by design
	
	@Override
	public void onMouseMove(final MsMovedEvent evt) {
		final ImageDisplay disp = imageDisplayService.getActiveImageDisplay();
		if (disp == null || !recorder().record(evt)) {
			statusService.clearStatus();
			return;
		}
		final int xAxis = disp.dimensionIndex(Axes.X);
		final int yAxis = disp.dimensionIndex(Axes.Y);
		final int channelIndex = disp.dimensionIndex(Axes.CHANNEL);
		final long cx = recorder().getCX();
		final long cy = recorder().getCY();
		ChannelCollection values = recorder().getValues();
		StringBuilder builder = new StringBuilder();
		builder.append("x=");
		final double xValue = disp.axis(xAxis).calibratedValue(cx);
		if (!Double.isNaN(xValue) && xValue != cx) {
			String calibratedVal = String.format("%.2f", xValue);
			builder.append(calibratedVal);
		}
		else
			builder.append(cx);
		if (disp.axis(0).unit() != null) {
			builder.append(" ");
			builder.append(disp.axis(0).unit());
		}
		builder.append(", y=");
		final double yValue = disp.axis(yAxis).calibratedValue(cy);
		if (!Double.isNaN(yValue) && yValue != cy) {
			String calibratedVal = String.format("%.2f", yValue);
			builder.append(calibratedVal);
		}
		else
			builder.append(cy);
		if (disp.axis(1).unit() != null) {
			builder.append(" ");
			builder.append(disp.axis(1).unit());
		}
		builder.append(", value=");
		// single channel image
		if (channelIndex == -1 ||
				recorder().getDataset().dimension(channelIndex) == 1)
		{
			String valueStr = valueString(values.getChannelValue(0));
			builder.append(valueStr);
		}
		else { // has multiple channels
			int currChannel = disp.getIntPosition(channelIndex);
			String valueStr = valueString(values.getChannelValue(currChannel));
			builder.append(valueStr);
			builder.append(" from (");
			for (int i = 0; i < values.getChannelCount(); i++) {
				valueStr = valueString(values.getChannelValue(i));
				if (i > 0) builder.append(",");
				builder.append(valueStr);
			}
			builder.append(")");
		}
		recorder().releaseDataset();
		statusService.showStatus(builder.toString());
	}
	
	// -- helpers --
	
	private String valueString(double value) {
		if (recorder().getDataset().isInteger())
			return String.format("%d",(long)value);
		return String.format("%f", value);
	}

	private PixelRecorder recorder() {
		if (recorder == null) recorder = new PixelRecorder(getContext(), false);
		return recorder;
	}

}
