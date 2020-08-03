package uk.ac.ebi.intact.app.internal.ui.components.combo.box.models;

import org.apache.commons.lang3.NotImplementedException;

import javax.swing.*;
import java.util.*;

public class SortedComboBoxModel<E extends Comparable<E>> extends AbstractListModel<E> implements MutableComboBoxModel<E> {

    private final SortedSet<E> elements = new TreeSet<>();
    private Vector<E> elementsVector;
    private Object selectedObject;

    public SortedComboBoxModel(Vector<E> elementsVector) {
        elementsVector.forEach(this::addElement);
        setSelectedItem(elements.first());
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if ((selectedObject != null && !selectedObject.equals(anItem)) || selectedObject == null && anItem != null) {
            selectedObject = anItem;
            fireContentsChanged(this, -1, -1);
        }
    }

    @Override
    public Object getSelectedItem() {
        return selectedObject;
    }

    @Override
    public int getSize() {
        return elements.size();
    }

    @Override
    public E getElementAt(int index) {
        return elementsVector.get(index);
    }

    @Override
    public void addElement(E element) {
        elements.add(element);
        elementsVector = new Vector<>(elements);

        int indexOfElement = elementsVector.indexOf(element);
        fireIntervalAdded(this, indexOfElement, indexOfElement);
        if (elements.size() == 1 && selectedObject == null && element != null) {
            setSelectedItem(element);
        }
    }

    @Override
    public void removeElement(Object obj) {
        if (elements.remove(obj)) {
            int removedIndex = elementsVector.indexOf(obj);
            elementsVector.remove(removedIndex);
            fireIntervalRemoved(this, removedIndex, removedIndex);
        }
    }


    @Override
    public void insertElementAt(E item, int index) {
        throw new NotImplementedException("List is sorted so you should not be able to select the index of added items");
    }


    @Override
    public void removeElementAt(int index) {
        try {
            E removed = elementsVector.remove(index);
            elements.remove(removed);
            fireIntervalRemoved(this, index, index);
        } catch (IndexOutOfBoundsException | UnsupportedOperationException e) {
            e.printStackTrace();
        }
    }
}
