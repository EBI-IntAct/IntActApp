package uk.ac.ebi.intact.intactApp.internal.ui;

import uk.ac.ebi.intact.intactApp.internal.model.Species;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Makes the species combo box searchable.
 */
public class JComboBoxDecorator {

    public static List<Species> previousEntries = new ArrayList<>();

    public static void decorate(final JComboBox<Species> jcb, boolean editable, boolean species) {
        List<Species> entries = new ArrayList<>();
        for (int i = 0; i < jcb.getItemCount(); i++) {
            if (species) {
                entries.add(jcb.getItemAt(i));
            }
        }
        decorate(jcb, editable, entries);
    }

    public static void decorate(final JComboBox<Species> jcb, boolean editable,
                                final List<Species> entries) {

        Species selectedSpecies = (Species) jcb.getSelectedItem();
        // System.out.println("JComboBoxDecorator: selectedItem = "+selectedSpecies);
        jcb.setEditable(editable);
        jcb.setModel(new DefaultComboBoxModel(entries.toArray()));

        final JTextField textField = (JTextField) jcb.getEditor().getEditorComponent();
        // textField.setText(selectedSpecies.getName());
        jcb.setSelectedItem(selectedSpecies);

        textField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        int currentCaretPosition = textField.getCaretPosition();
                        comboFilter(textField.getText(), jcb, entries);
                        textField.setCaretPosition(currentCaretPosition);
                    }
                });
            }
        });
    }

    /**
     * Create a list of entries that match the user's entered text.
     */
    private static void comboFilter(String enteredText, JComboBox<Species> jcb,
                                    List<Species> entries) {
        List<Species> entriesFiltered = new ArrayList<>();
        boolean changed = true;
        DefaultComboBoxModel<Species> jcbModel = (DefaultComboBoxModel<Species>) jcb.getModel();

        if (enteredText == null) {
            return;
        }

        for (Species entry : entries) {
            if (entry.getName().toLowerCase().contains(enteredText.toLowerCase())) {
                entriesFiltered.add(entry);
                // System.out.println(jcbModel.getIndexOf(entry));
            }
        }

        if (previousEntries.size() == entriesFiltered.size()
                && previousEntries.containsAll(entriesFiltered)) {
            changed = false;
        }

        if (changed && entriesFiltered.size() > 1) {
            previousEntries = entriesFiltered;
            jcb.setModel(new DefaultComboBoxModel( entriesFiltered.toArray()));
            jcb.setSelectedItem(enteredText);
            jcb.showPopup();
        } else if (entriesFiltered.size() == 1) {
            if (entriesFiltered.get(0).toString().equalsIgnoreCase(enteredText)) {
                previousEntries = new ArrayList<>();
                jcb.setSelectedItem(entriesFiltered.get(0));
                jcb.hidePopup();
            } else {
                previousEntries = entriesFiltered;
                jcb.setModel(new DefaultComboBoxModel(entriesFiltered.toArray()));
                jcb.setSelectedItem(enteredText);
                jcb.showPopup();
            }
        } else if (entriesFiltered.size() == 0) {
            previousEntries = new ArrayList<>();
            jcb.hidePopup();
        }
    }

}
