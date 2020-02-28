package uk.ac.ebi.intact.intactApp.internal.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Map;

class ClearEverythingAction extends AbstractAction {
    final Map<String, ResolveTableModel> tableModelMap;


    public ClearEverythingAction(final Map<String, ResolveTableModel> tableModelMap) {
        super("Clear Everything");
        this.tableModelMap = tableModelMap;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (String term : tableModelMap.keySet()) {
            ResolveTableModel rtm = tableModelMap.get(term);
            for (int i = 0; i < rtm.getRowCount(); i++) {
                rtm.setValueAt(false, i, 0);
            }
        }
    }
}
