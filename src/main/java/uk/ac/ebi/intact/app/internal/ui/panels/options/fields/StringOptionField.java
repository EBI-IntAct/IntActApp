package uk.ac.ebi.intact.app.internal.ui.panels.options.fields;

import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.OptionManager;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class StringOptionField extends OptionField<OptionManager.Option<String>> {
    private final JTextField textField;

    public StringOptionField(OptionManager.Option<String> option, JPanel container, EasyGBC layoutHelper) {
        super(option, container, layoutHelper);
        textField = new JTextField(option.defaultValue);
        textField.setText(option.getValue());
        textField.addActionListener((e) -> option.setValue(textField.getText()));
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                option.setValue(textField.getText());
            }
        });

        textField.setToolTipText(option.description);

        container.add(textField, layoutHelper);
    }

    @Override
    public void addListener(Runnable listener) {
        textField.addActionListener(e -> listener.run());
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                listener.run();
            }
        });
    }

}
