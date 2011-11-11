package jp.orto.popupnote;

import orto.ui.*;

import java.util.*;

public class PopUpNote implements ComponentListener {
	private Hashtable buttonTableMap = new Hashtable();

	public PopUpNote() {
		Body body = Browser.getMainWindow().getBody();
		for(int i=1; i<=4; i++) {
			Table table = body.getTable("table" + i);
			table.setDragable(true);

			AnchorButton closeButton = table.getAnchorButton("closeButton");
			closeButton.addClickListener(this);
			buttonTableMap.put(closeButton, table);
		}
	}

	public boolean componentAction(Component source, int type, int param) {
		((Table)buttonTableMap.get(source)).setVisible(false);
		return true;
	}
}
