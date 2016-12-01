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
import org.scijava.display.event.input.MsClickedEvent;
import org.scijava.display.event.input.MsPressedEvent;
import org.scijava.display.event.input.MsReleasedEvent;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.tool.AbstractTool;
import org.scijava.tool.Tool;
import org.scijava.ui.UIService;

// TODO: Migrate to scijava-plugins-tools component.

/**
 * Handles display of general-purpose context menu (e.g., on right mouse click).
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Tool.class, name = "Context Menus",
	menuRoot = ImageDisplay.CONTEXT_MENU_ROOT)
public class ContextMenuHandler extends AbstractTool {

	@Parameter
	private UIService uiService;

	@Override
	public boolean isAlwaysActive() {
		return true;
	}

	@Override
	public void onMouseDown(final MsPressedEvent evt) {
		doPopupMenu(evt);
	}

	@Override
	public void onMouseUp(final MsReleasedEvent evt) {
		doPopupMenu(evt);
	}

	@Override
	public void onMouseClick(final MsClickedEvent evt) {
		doPopupMenu(evt);
	}

	// -- Helper methods --

	private void doPopupMenu(final MsButtonEvent evt) {
		if (!evt.isPopupTrigger()) return;

		final String menuRoot = getInfo().getMenuRoot();
		final Display<?> display = evt.getDisplay();
		uiService.showContextMenu(menuRoot, display, evt.getX(), evt.getY());

		// consume event, so that nothing else tries to handle it
		evt.consume();
	}

}
