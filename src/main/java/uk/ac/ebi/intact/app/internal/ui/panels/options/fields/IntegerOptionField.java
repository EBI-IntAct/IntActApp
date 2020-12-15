package uk.ac.ebi.intact.app.internal.ui.panels.options.fields;

import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.OptionManager;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class IntegerOptionField extends OptionField<OptionManager.NumericOption<Integer>> implements ChangeListener {
    private final JSlider slider = new JSlider(SwingConstants.HORIZONTAL);
    private final JTextField textField;
    private boolean ignore = false;

    public IntegerOptionField(OptionManager.NumericOption<Integer> option, JPanel container, EasyGBC layoutHelper) {
        super(option, container, layoutHelper);
        Integer value = option.getValue();
        textField = new JTextField(option.max.toString().length());
        textField.setHorizontalAlignment(SwingConstants.RIGHT);
        textField.setText(value.toString());
        textField.addActionListener((e) -> textFieldValueChanged());
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                textFieldValueChanged();
            }
        });
        container.add(textField, layoutHelper.noExpand());

        slider.setMinimum(option.min);
        slider.setMaximum(option.max);
        int range = option.max - option.min;
        slider.setMajorTickSpacing(range);
        slider.setMinorTickSpacing(range / 8);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setPaintTrack(true);
        slider.setValue(value);
        slider.addChangeListener(this);

        slider.setToolTipText(option.description);

        container.add(slider, layoutHelper.right().expandHoriz());
    }

    @Override
    public void addListener(Runnable listener) {
        slider.addChangeListener(e -> listener.run());
    }


    @Override
    public void stateChanged(ChangeEvent e) {
        int value = slider.getValue();
        option.setValue(value);
        textField.setText(String.valueOf(value));
    }

    private void textFieldValueChanged() {
        if (ignore) return;
        ignore = true;
        String text = textField.getText();
        try {
            int n = Integer.parseInt(text);
            if (n >= option.min && n <= option.max) {
                slider.setValue(n);
                ignore = false;
                return;
            }
        } catch (NumberFormatException ignored) {
        }
        inputError();
        ignore = false;
    }

    private Integer inputError() {
        textField.setBackground(Color.RED);
        JOptionPane.showMessageDialog(null,
                String.format("Please enter a valid integer between %d and %d", option.min, option.max),
                "Alert", JOptionPane.ERROR_MESSAGE);
        textField.setBackground(UIManager.getColor("TextField.background"));

        // Reset the value to correspond to the current slider setting
        Integer val = slider.getValue();
        textField.setText(val.toString());
        return val;
    }
}
