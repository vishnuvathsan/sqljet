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

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import spending.core.Payment;

/**
 * @author Dmitry Stadnik (dtrace@seznam.cz)
 */
public class AddPaymentDialog extends Dialog {

	private Payment payment;
	private Button okButton;

	public AddPaymentDialog(Shell parent) {
		super(parent);
		payment = new Payment();
		payment.date = new Date();
	}

	public Payment open() {
		Shell parent = getParent();
		Shell shell = new Shell(parent, SWT.TITLE | SWT.BORDER
				| SWT.APPLICATION_MODAL);
		shell.setText(getText());
		createControls(shell);
		shell.pack();
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return payment;
	}

	private void createControls(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		GC gc = new GC(parent);
		int charWidth = gc.getFontMetrics().getAverageCharWidth();
		gc.dispose();

		Label amountLabel = new Label(parent, SWT.NONE);
		amountLabel.setLayoutData(new GridData());
		amountLabel.setText("Amount:");

		Text amountText = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
		GridData amountData = new GridData(GridData.FILL_HORIZONTAL);
		amountData.widthHint = charWidth * 16;
		amountText.setLayoutData(amountData);
		amountText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent event) {
				Text text = (Text) event.widget;
				payment.amount = 0;
				try {
					payment.amount = Long.parseLong(text.getText());
					text.setForeground(Display.getCurrent().getSystemColor(
							SWT.COLOR_WIDGET_FOREGROUND));
				} catch (NumberFormatException nfe) {
					text.setForeground(Display.getCurrent().getSystemColor(
							SWT.COLOR_RED));
				}
			}
		});

		Label currencyLabel = new Label(parent, SWT.NONE);
		currencyLabel.setLayoutData(new GridData());
		currencyLabel.setText("Currency:");

		Text currencyText = new Text(parent, SWT.BORDER | SWT.SINGLE);
		GridData currencyData = new GridData(GridData.FILL_HORIZONTAL);
		currencyData.widthHint = charWidth * 3;
		currencyText.setLayoutData(currencyData);
		currencyText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent event) {
				Text text = (Text) event.widget;
				if (text.getText().length() == 3) {
					payment.currency = text.getText();
					text.setForeground(Display.getCurrent().getSystemColor(
							SWT.COLOR_WIDGET_FOREGROUND));
				} else {
					payment.currency = null;
					text.setForeground(Display.getCurrent().getSystemColor(
							SWT.COLOR_RED));
				}
			}
		});

		okButton = new Button(parent, SWT.PUSH);
		GridData okData = new GridData();
		okData.horizontalSpan = 2;
		okData.horizontalAlignment = SWT.RIGHT;
		okButton.setLayoutData(okData);
		okButton.setText("Add");
		okButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				okButton.getShell().close();
			}
		});
	}
}
