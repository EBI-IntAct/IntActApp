package uk.ac.ebi.intact.app.internal.ui.components.labels.center;

import javax.swing.text.BoxView;
import javax.swing.text.Element;

class CenteredBoxView extends BoxView {
    public CenteredBoxView(Element elem, int axis) {
        super(elem, axis);
    }

    protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
        super.layoutMajorAxis(targetSpan, axis, offsets, spans);
        int textBlockHeight = 0;
        int offset = 0;

        for (int i = 0; i < spans.length; i++) {

            textBlockHeight = spans[i];
        }
        offset = (targetSpan - textBlockHeight) / 2;
        for (int i = 0; i < offsets.length; i++) {
            offsets[i] += offset;
        }
    }
}
