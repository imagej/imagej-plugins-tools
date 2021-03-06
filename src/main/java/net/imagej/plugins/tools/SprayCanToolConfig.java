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

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Implements the configuration code for {@link SprayCanTool}.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, label = "Spray Can Tool", initializer = "initAll")
public class SprayCanToolConfig implements Command {

	@Parameter(type = ItemIO.BOTH)
	private SprayCanTool tool;

	// TODO - it would be nice to persist these values but the associated
	// tools cannot persist values. thus you get in a situation that the
	// these values do not equal the tool's initial values which is
	// confusing. Tools need to be able to persist some values to get around this.

  @Parameter(label = "Spray Width (pixels):", min = "1", persist = false)
  private int width;

  @Parameter(label = "Dot Size (pixels):", min = "1", persist = false)
  private int dotSize;
  
  @Parameter(label = "Flow Rate (1-10):", min = "1", max = "10", persist=false)
  private int rate;
  
	@Override
	public void run() {
		tool.setWidth(width);
		tool.setRate(rate);
		tool.setDotSize(dotSize);
	}

	protected void initAll() {
		width = tool.getWidth();
		rate = tool.getRate();
		dotSize = tool.getDotSize();
	}

}
