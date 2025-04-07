package uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Range {
    int start, end;

    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }
}