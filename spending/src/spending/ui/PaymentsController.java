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

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

import spending.core.Payment;
import spending.core.SpendingDB;

/**
 * @author Dmitry Stadnik (dtrace@seznam.cz)
 */
public class PaymentsController {

	private Table table;
	private ToolItem removeItem;
	private Text infoText;
	private boolean infoChanged;
	private boolean showForToday;

	public void createView(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		ToolBar toolBar = new ToolBar(parent, SWT.HORIZONTAL);
		toolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		ToolItem addItem = new ToolItem(toolBar, SWT.PUSH);
		addItem.setText("Add");
		addItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				addPayment();
			}
		});
		removeItem = new ToolItem(toolBar, SWT.PUSH);
		removeItem.setText("Remove");
		removeItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelectedPayment();
			}
		});
		removeItem.setEnabled(false);

		Button todayItem = new Button(parent, SWT.CHECK);
		todayItem.setLayoutData(new GridData());
		todayItem.setText("Today");
		todayItem.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				showForToday = ((Button) e.widget).getSelection();
				refresh();
			}
		});

		table = new Table(parent, SWT.BORDER | SWT.SINGLE);
		GridData tableData = new GridData(GridData.FILL_BOTH);
		tableData.horizontalSpan = 2;
		table.setLayoutData(tableData);
		TableColumn dateCol = new TableColumn(table, SWT.LEFT);
		dateCol.setText("Date");
		TableColumn amountCol = new TableColumn(table, SWT.RIGHT);
		amountCol.setText("Amount");
		TableColumn currencyCol = new TableColumn(table, SWT.LEFT);
		currencyCol.setText("Currency");
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				removeItem.setEnabled(table.getSelectionCount() > 0);
				refreshInfo();
			}
		});

		Label infoLabel = new Label(parent, SWT.NONE);
		GridData infoLabelData = new GridData();
		infoLabelData.horizontalSpan = 2;
		infoLabel.setLayoutData(infoLabelData);
		infoLabel.setText("Info:");

		infoText = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		GridData infoData = new GridData(GridData.FILL_HORIZONTAL);
		int fontHeight = parent.getFont().getFontData()[0].getHeight();
		infoData.heightHint = fontHeight * 5;
		infoData.horizontalSpan = 2;
		infoText.setLayoutData(infoData);
		infoText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent event) {
				infoChanged = true;
			}
		});
		infoText.addFocusListener(new FocusListener() {

			public void focusLost(FocusEvent event) {
				if (infoChanged) {
					if (table.getSelectionCount() > 0) {
						updateSelectedPayment();
					}
					infoChanged = false;
				}
			}

			public void focusGained(FocusEvent event) {
			}
		});

		refresh();
	}

	public void refresh() {
		table.removeAll();
		try {
			ISqlJetCursor cursor = showForToday ? SpendingDB
					.getPayments(new Date()) : SpendingDB.getAllPayments();
			try {
				DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
				while (!cursor.eof()) {
					Payment p = new Payment();
					p.read(cursor);
					TableItem item = new TableItem(table, 0);
					item.setText(new String[] { df.format(p.date),
							String.valueOf(p.amount),
							p.currency == null ? "" : p.currency });
					item.setData("rowid", cursor.getRowId());
					item.setData("payment", p);
					cursor.next();
				}
			} finally {
				cursor.close();
			}
		} catch (SqlJetException e) {
			e.printStackTrace();
		}
		for (TableColumn col : table.getColumns()) {
			col.pack();
		}
	}

	private long getSelectedRowId() {
		TableItem item = table.getSelection()[0];
		return (Long) item.getData("rowid");
	}

	private void refreshInfo() {
		if (table.getSelectionCount() == 0) {
			infoText.setText("");
			return;
		}
		try {
			Payment p = SpendingDB.getPayment(getSelectedRowId());
			infoText.setText(p == null || p.info == null ? "" : p.info);
		} catch (SqlJetException e) {
			e.printStackTrace();
			infoText.setText("");
		}
	}

	private void addPayment() {
		AddPaymentDialog d = new AddPaymentDialog(Display.getCurrent()
				.getActiveShell());
		d.setText("Add Payment");
		Payment p = d.open();
		try {
			long rowid = SpendingDB.addPayment(p);
			refresh();
			for (TableItem item : table.getItems()) {
				if (rowid == (Long) item.getData("rowid")) {
					table.setSelection(item);
					break;
				}
			}
		} catch (SqlJetException e) {
			e.printStackTrace();
		}
	}

	private void updateSelectedPayment() {
		try {
			TableItem item = table.getSelection()[0];
			Payment p = (Payment) item.getData("payment");
			p.info = infoText.getText();
			SpendingDB.updatePayment(getSelectedRowId(), p);
		} catch (SqlJetException e) {
			e.printStackTrace();
		}
		refresh();
	}

	private void removeSelectedPayment() {
		try {
			SpendingDB.removePayment(getSelectedRowId());
		} catch (SqlJetException e) {
			e.printStackTrace();
		}
		refresh();
	}
}
