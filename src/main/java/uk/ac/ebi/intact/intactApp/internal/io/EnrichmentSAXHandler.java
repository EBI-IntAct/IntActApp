package uk.ac.ebi.intact.intactApp.internal.io;

import org.cytoscape.model.CyNetwork;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import uk.ac.ebi.intact.intactApp.internal.model.EnrichmentTerm;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class EnrichmentSAXHandler extends DefaultHandler {
    private final String tag_status = "status";
    private final String tag_code = "code";
    private final String tag_message = "message";
    private final String tag_warning = "warning";
    private final String tag_term = "term";
    private final String tag_name = "name";
    private final String tag_description = "description";
    private final String tag_numberOfGenes = "numberOfGenes";
    private final String tag_pvalue = "pvalue";
    private final String tag_bonferroni = "bonferroni";
    private final String tag_fdr = "fdr";
    private final String tag_genes = "genes";
    private final String tag_gene = "gene";
    private Hashtable<String, Integer> tags;
    private CyNetwork network;
    private Map<String, Long> stringNodesMap;
    // private double enrichmentCutoff;
    private String enrichmentCategory;
    private List<EnrichmentTerm> enrichmentTerms;
    private EnrichmentTerm currTerm;
    private List<String> currGeneList;
    private List<Long> currNodeList;
    private StringBuilder content;
    private String warning;
    private String message;
    private String status;
    private String status_code;
    private boolean in_status = false;
    private boolean in_code = false;
    private boolean in_warning = false;
    private boolean in_message = false;
    private boolean in_term = false;
    private boolean in_name = false;
    private boolean in_description = false;
    private boolean in_numberOfGenes = false;
    private boolean in_pvalue = false;
    private boolean in_bonferroni = false;
    private boolean in_fdr = false;
    private boolean in_genes = false;
    private boolean in_gene = false;

    // <term>
    // <name>GO:0008585</name>
    // <description>female gonad development</description>
    // <numberOfGenes>1</numberOfGenes>
    // <pvalue>1E0</pvalue>
    // <bonferroni>1E0</bonferroni>
    // <fdr>1E0</fdr>
    // <genes><gene>9606.ENSP00000269260</gene></genes>
    // </term>

    public EnrichmentSAXHandler(CyNetwork network, Map<String, Long> stringNodesMap,
                                String enrichmentCategory) {
        this.network = network;
        this.stringNodesMap = stringNodesMap;
        this.enrichmentCategory = enrichmentCategory;
        status = null;
        warning = null;
    }

    public void startDocument() throws SAXException {
        tags = new Hashtable<>();
        enrichmentTerms = new ArrayList<>();
        content = new StringBuilder();
    }

    public void endDocument() throws SAXException {
        // do something on endDocument?
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        content = new StringBuilder();
        Integer value = tags.get(localName);
        if (value == null) {
            tags.put(localName, 1);
        } else {
            int count = value;
            count++;
            tags.put(localName, count);
        }

        switch (localName) {
            case tag_status:
                in_status = true;
                break;
            case tag_code:
                in_code = true;
                break;
            case tag_warning:
                in_warning = true;
                break;
            case tag_message:
                in_message = true;
                break;
            case tag_term:
                in_term = true;
                currTerm = new EnrichmentTerm(enrichmentCategory);
                break;
            case tag_name:
                in_name = true;
                break;
            case tag_description:
                in_description = true;
                break;
            case tag_pvalue:
                in_pvalue = true;
                break;
            case tag_bonferroni:
                in_bonferroni = true;
                break;
            case tag_fdr:
                in_fdr = true;
                break;
            case tag_numberOfGenes:
                in_numberOfGenes = true;
                break;
            case tag_genes:
                in_genes = true;
                currGeneList = new ArrayList<>();
                currNodeList = new ArrayList<>();
                break;
            case tag_gene:
                in_gene = true;
                break;
        }
    }

    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        String key = localName;
        switch (key) {
            case tag_status:
                status = content.toString();
                in_status = false;
                break;
            case tag_code:
                status_code = content.toString();
                in_code = false;
                break;
            case tag_warning:
                warning = content.toString();
                in_warning = false;
                break;
            case tag_message:
                message = content.toString();
                in_message = false;
                break;
            case tag_term:
                in_term = false;
                // if (currTerm.getFDRPValue() <= enrichmentCutoff)
                enrichmentTerms.add(currTerm);
                break;
            case tag_name:
                in_name = false;
                if (in_term)
                    currTerm.setName(content.toString());
                break;
            case tag_description:
                in_description = false;
                currTerm.setDescription(content.toString());
                break;
            case tag_pvalue:
                in_pvalue = false;
                double pvalue = Double.parseDouble(content.toString());
                currTerm.setPValue(pvalue);
                break;
            case tag_bonferroni:
                in_bonferroni = false;
                double pvalueB = Double.parseDouble(content.toString());
                currTerm.setBonfPValue(pvalueB);
                break;
            case tag_fdr:
                in_fdr = false;
                double pvalueFDR = Double.parseDouble(content.toString());
                currTerm.setFDRPValue(pvalueFDR);
                break;
            case tag_numberOfGenes:
                in_numberOfGenes = false;
                break;
            case tag_genes:
                in_genes = false;
                currTerm.setGenes(currGeneList);
                currTerm.setNodesSUID(currNodeList);
                break;
            case tag_gene:
                in_gene = false;
                // ... add gene to list
                String enrGeneEnsemblID = content.toString();
                String enrGeneNodeName = enrGeneEnsemblID;
                if (stringNodesMap.containsKey(enrGeneEnsemblID)) {
                    final Long nodeSUID = stringNodesMap.get(enrGeneEnsemblID);
                    currNodeList.add(nodeSUID);
                    if (network.getDefaultNodeTable().getColumn(CyNetwork.NAME) != null) {
                        enrGeneNodeName = network.getDefaultNodeTable().getRow(nodeSUID)
                                .get(CyNetwork.NAME, String.class);
                    }
                }
                currGeneList.add(enrGeneNodeName);
                break;
        }

    }

    public void characters(char[] ch, int start, int length) {
        content.append(ch, start, length);
    }

    public boolean isStatusOK() {
        return status != null && status.equals("ok");
    }

    public String getStatusCode() {
        return status_code;
    }

    public String getMessage() {
        return message;
    }

    public String getWarning() {
        return warning;
    }

    public List<EnrichmentTerm> getParsedData() {
        return enrichmentTerms;
    }
}

