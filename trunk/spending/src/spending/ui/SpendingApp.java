/**
 * Copyright (C) 2009 TMate Software Ltd
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package spending.ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.tmatesoft.sqljet.core.SqlJetException;

import spending.core.SpendingDB;

/**
 * @author Dmitry Stadnik (dtrace@seznam.cz)
 */
public class SpendingApp {

	public static void main(String[] args) {
		try {
			SpendingDB.open();
		} catch (SqlJetException e) {
			e.printStackTrace();
			return;
		}
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Spending");
		PaymentsController pc = new PaymentsController();
		pc.createView(shell);
		shell.setSize(400, 400);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
		try {
			SpendingDB.close();
		} catch (SqlJetException e) {
			e.printStackTrace();
		}
	}
}
