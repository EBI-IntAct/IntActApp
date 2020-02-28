package uk.ac.ebi.intact.intactApp.internal.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Map;

class SelectAllTermAction extends AbstractAction {
    final String term;
    final Map<String, ResolveTableModel> tableModelMap;


    public SelectAllTermAction(final String term, final Map<String, ResolveTableModel> tableModelMap) {
        super("Select Everything for " + term);
        this.tableModelMap = tableModelMap;
        this.term = term;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ResolveTableModel rtm = tableModelMap.get(term);
        for (int i = 0; i < rtm.getRowCount(); i++) {
            rtm.setValueAt(true, i, 0);
        }
    }
}
