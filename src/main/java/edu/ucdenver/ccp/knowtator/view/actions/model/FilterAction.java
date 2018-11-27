/*
 *  MIT License
 *
 *  Copyright (c) 2018 Harrison Pielke-Lombardo
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package edu.ucdenver.ccp.knowtator.view.actions.model;

import edu.ucdenver.ccp.knowtator.model.FilterType;
import edu.ucdenver.ccp.knowtator.view.KnowtatorView;
import edu.ucdenver.ccp.knowtator.view.actions.AbstractKnowtatorAction;
import edu.ucdenver.ccp.knowtator.view.actions.KnowtatorEdit;

import javax.swing.undo.UndoableEdit;

public class FilterAction extends AbstractKnowtatorAction {

	private final boolean isFilter;
	private final boolean previousIsFilter;
	private final FilterType filterType;

	public FilterAction(FilterType filterType, boolean isFilter) {
		super("Change filterType");
		this.filterType = filterType;
		this.isFilter = isFilter;
		this.previousIsFilter = KnowtatorView.MODEL.isFilter(filterType);

	}

	@Override
	public void execute() {
		KnowtatorView.MODEL.setFilter(filterType, isFilter);
	}

	@Override
	public UndoableEdit getEdit() {
		return new KnowtatorEdit("Change filterType") {
			@Override
			public void undo() {
				KnowtatorView.MODEL.setFilter(filterType, previousIsFilter);
			}

			@Override
			public void redo() {
				KnowtatorView.MODEL.setFilter(filterType, isFilter);
			}
		};
	}
}
