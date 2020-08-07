package uk.ac.ebi.intact.app.internal.model.managers.sub.managers.color.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.awt.*;
import java.util.Objects;

public class ColorSetting {
    String taxId;
    String taxonName;
    @JsonIgnore
    Color color;
    int colorHexa;

    public ColorSetting() {
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getTaxonName() {
        return taxonName;
    }

    public void setTaxonName(String taxonName) {
        this.taxonName = taxonName;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        this.colorHexa = color.getRGB();
    }

    public int getColorHexa() {
        return colorHexa;
    }

    @JsonSetter()
    public void setColorHexa(int colorHexa) {
        this.colorHexa = colorHexa;
        this.color = new Color(colorHexa, true);
    }

    public ColorSetting(String taxId, String taxonName, Color color) {
        this.taxId = taxId;
        this.taxonName = taxonName;
        this.color = color;
        this.colorHexa = color.getRGB();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorSetting that = (ColorSetting) o;
        return taxId.equals(that.taxId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taxId);
    }

    @Override
    public String toString() {
        return taxonName + " --> #" + Integer.toHexString(colorHexa);
    }
}
