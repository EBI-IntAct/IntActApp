package uk.ac.ebi.intact.intactApp.internal.model;

public enum Databases {
    STRING("String", "string"),
    VIRUSES("Viruses", "viruses"),
    STITCH("Stitch", "stitch");

    String dbName;
    String apiName;

    Databases(String dbName, String apiName) {
        this.dbName = dbName;
        this.apiName = apiName;
    }

    public String toString() {
        return dbName;
    }

    public String getAPIName() {
        return apiName;
    }
}
