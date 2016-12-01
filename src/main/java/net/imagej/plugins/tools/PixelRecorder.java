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

import java.util.LinkedList;
import java.util.List;

import net.imagej.ChannelCollection;
import net.imagej.Dataset;
import net.imagej.Position;
import net.imagej.axis.Axes;
import net.imagej.display.DataView;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageCanvas;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imglib2.RandomAccess;
import net.imglib2.display.screenimage.awt.ARGBScreenImage;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

import org.scijava.Context;
import org.scijava.display.Display;
import org.scijava.display.event.input.MsEvent;
import org.scijava.plugin.Parameter;
import org.scijava.util.ColorRGB;
import org.scijava.util.Colors;
import org.scijava.util.IntCoords;
import org.scijava.util.RealCoords;

/**
 * Gathers pixel information (location, channel values) of pixel associated
 * with a given mouse event.
 * 
 * @author Barry DeZonia
 * @author Rick Lentz
 * @author Grant Harris
 * @author Curtis Rueden
 */
public class PixelRecorder {

	// -- instance variables --

	@Parameter
	private ImageDisplayService imageDisplayService;

	private long cx = 0;
	private long cy = 0;
	private Dataset dataset = null;
	private ChannelCollection channels = null;
	private ColorRGB color = Colors.BLACK;
	private boolean wasAltKeyDown = false;
	private boolean recordColor = false;
	
	// -- public interface --

	/** Constructor */
	public PixelRecorder(final Context context, final boolean recordColor) {
		context.inject(this);

		this.recordColor = recordColor;
		channels = new ChannelCollection();
	}

	/**
	 * This method takes a mouse event and records information internally
	 * about the location, color, and type of data referenced at the mouse
	 * position. After event is recorded users should utilize member query
	 * methods to get info about the event.
	 */
	public boolean record(final MsEvent evt) {
		final Display<?> display = evt.getDisplay();
		if (!(display instanceof ImageDisplay)) return false;
		final ImageDisplay imageDisplay = (ImageDisplay) display;

		final ImageCanvas canvas = imageDisplay.getCanvas();
		final IntCoords mousePos = new IntCoords(evt.getX(), evt.getY());
		if (!canvas.isInImage(mousePos)) return false;

		// mouse is over image

		wasAltKeyDown =
				evt.getModifiers().isAltDown() || evt.getModifiers().isAltGrDown();

		// TODO - update tool to probe more than just the active view
		final DataView activeView = imageDisplay.getActiveView();
		dataset = imageDisplayService.getActiveDataset(imageDisplay);

		final Img<? extends RealType<?>> image = dataset.getImgPlus();
		final RandomAccess<? extends RealType<?>> randomAccess =
			image.randomAccess();
		final int xAxis = dataset.dimensionIndex(Axes.X);
		final int yAxis = dataset.dimensionIndex(Axes.Y);
		final int chanAxis = dataset.dimensionIndex(Axes.CHANNEL);

		final RealCoords coords = canvas.panelToDataCoords(mousePos);
		cx = coords.getLongX();
		cy = coords.getLongY();

		Position planePos = activeView.getPlanePosition();
		long[] otherPositions;
		// channel axis not present?
		if (chanAxis == -1) {
			// record all positions
			otherPositions = new long[planePos.numDimensions()];
			for (int i = 0; i < planePos.numDimensions(); i++) {
				otherPositions[i] = planePos.getLongPosition(i);
			}
		}
		else { // channel axis is present
			// record all positions that are not a channel position
			otherPositions = new long[planePos.numDimensions()-1];
			int d = 0;
			for (int i = 0; i < planePos.numDimensions(); i++) {
				// TODO - this test of ch-2 will break when X & Y can exist outside
				//   first two axes.
				if (i != chanAxis-2) otherPositions[d++] = planePos.getLongPosition(i);
			}
		}
		
		// record color of displayed pixel
		if (recordColor) {
			final DatasetView view =
					imageDisplayService.getActiveDatasetView(imageDisplay);
			ARGBScreenImage screenImage = view.getScreenImage();
			int[] argbPixels = view.getScreenImage().getData();
			int pixelIndex = (int) (cy*screenImage.dimension(0) + cx);
			int argb = argbPixels[pixelIndex];
			int r = (argb >> 16) & 0xff;
			int g = (argb >>  8) & 0xff;
			int b = (argb >>  0) & 0xff;
			color = new ColorRGB(r,g,b);
		}

		// record channel values associated with the XY coord
		long numChannels;
		if (chanAxis == -1)
			numChannels = 1;
		else
			numChannels = dataset.dimension(chanAxis); 
		List<Double> values = new LinkedList<Double>();
		for (long chan = 0; chan < numChannels; chan++) {
			setPosition(randomAccess, cx, cy, chan, otherPositions, xAxis, yAxis, chanAxis);
			double value = randomAccess.get().getRealDouble();
			values.add(value);
		}
		
		channels = new ChannelCollection(values);
		
		return true;
	}

	/** Returns the Dataset associated with the processed mouse event. */
	public Dataset getDataset() {
		return dataset;
	}
	
	/** Returns the values of all the channels associated with the processed
	 * mouse event. */
	public ChannelCollection getValues() {
		return channels;
	}

	/**
	 * Returns the color of the pixel associated with the processed mouse event.
	 */
	public ColorRGB getColor() {
		return color;
	}

	/** Returns the X value of the mouse event in image coordinate space. */
	public long getCX() {
		return cx;
	}

	/** Returns the Y value of the mouse event in image coordinate space. */
	public long getCY() {
		return cy;
	}

	/** Returns true if the alt key was down during the mouse event that was
	 * recorded. */
	public boolean wasAltKeyDown() {
		return wasAltKeyDown;
	}

	public void releaseDataset() {
		dataset = null;
	}
	
	// -- private helpers --

	/** Sets the position of a randomAccess to (u,v,planePos). */
	private void setPosition(
		final RandomAccess<? extends RealType<?>> randomAccess, final long x,
		final long y, final long c, final long[] otherCoordValues, final int xAxis,
		final int yAxis, final int cAxis)
	{
		int i = 0;
		for (int d = 0; d < randomAccess.numDimensions(); d++) {
			if (d == xAxis) randomAccess.setPosition(x, xAxis);
			else if (d == yAxis) randomAccess.setPosition(y, yAxis);
			else if (d == cAxis) randomAccess.setPosition(c, cAxis);
			else randomAccess.setPosition(otherCoordValues[i++], d);
		}
	}

}
