package jp.orto.monkeytiger;

import orto.ui.*;

public class MonkeyTiger implements ComponentListener {
	private Panel mainPanel, monkeyParent, tigerParent;
	private Button calcButton;

	public MonkeyTiger() {
		mainPanel = Browser.getMainWindow().getBody().getPanel("mainPanel");
		monkeyParent = mainPanel.getPanel("monkeyParent");
		tigerParent = mainPanel.getPanel("tigerParent");

		// Registers the event listener of calcButton.
		calcButton = mainPanel.getButton("calcButton");
		calcButton.addClickListener(this);
		calcButton.setEnabled(true);
	}

	public boolean componentAction(Component source, int type, int param) {
		if (source == calcButton) {
			// You have to create a thread becuase Panel.move() is used.
			new MovingThread().start();
		}
		return true;
	}
	
	class MovingThread extends Thread {
		public void run() {
			// Disables the calcButton.
			calcButton.setEnabled(false);

			// Gets "a" and "b" of a + b.
			int a = Integer.parseInt(mainPanel.getTextBox("a").getText());
			int b = Integer.parseInt(mainPanel.getTextBox("b").getText());

			// Creates "a" monkeys and "b" tigers.
			int sum = 0;
			for (int i = 0; i < a; i++) {
				Panel rabbit = monkeyParent.clone();
				rabbit.setVisible(true);
				rabbit.move(sum * 32, 100, 500);
				// Display the sum
				sum++;
				mainPanel.getTextBox("sum").setText(String.valueOf(sum));
			}
			for (int i = 0; i < b; i++) {
				Panel tiger = tigerParent.clone();
				tiger.setVisible(true);
				tiger.move(sum * 32, 100, 500);
				// Display the sum
				sum++;
				mainPanel.getTextBox("sum").setText(String.valueOf(sum));
			}
		}
	}
}
