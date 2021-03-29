---
title: Automation support
---

IntAct App provides different commands to automatise its different features.

Those commands are all placed under the namespace `intact`.

<!-- TOC -->
- [Internal commands](#internal-commands)    
  - [intact version](#intact-version)    
  - [intact query](#intact-query)    
  - [intact summary & intact evidence & intact mutation](#intact-summary--intact-evidence--intact-mutation)    
  - [intact reset-filters](#intact-reset-filters)
  - [intact extract](#intact-extract)
- [Rest API](#rest-api)<!-- /TOC -->

## Internal commands

### intact version

- Usage: Gives the current version of IntAct App
- Parameters: No parameters

```bash
intact version
→ Version: 0.9.6
```

### intact query

- Usage: Allow the creation of IntAct networks from a query
- Parameters:
    - **seedTerms** *(String)*
        - **Required**
        - Space separated terms to search among interactors ids and names to build the network around them.
        - Example: `"gtp lrr* cell Q5S007 EBI-2624319"`
    - exactQuery *(Boolean)*
        - `true` → Exact query
        - `false` **Default** → Fuzzy search
    - includeSeedPartners *(Boolean)*
        - `true` **Default** → resulting network will be made of given seed terms interactors and all interacting
          partner found for them.
        - `false` → resulting network will only be constituted of given terms interactors and interaction between them.
    - applyLayout *boolean*
        - `true` **Default** → Apply force directed layout algorithm on the new network
        - `false` → Do not apply any layout algorithm: All elements will be stacked on top of each others visually
    - maxInteractorsPerTerm *(Integer)*
        - `n ≼ 0` **Default** → All matching interactors will be used as seeds for network building.
        - `n > 0` → Only the top n interactors that matched terms will be used as seeds for network building
    - netName *(String)*
        - Name of the resulting network
        - **If not provided**, will be `IntAct Network - DD/MM/YYYY - HH:MM`
    - taxons *(String)*
        - Comma separated taxon ids of seed interactors around which the network will be built.
        - **If not provided**, all species will be accepted
        - Example: `"9606, 559292"`
    - types *(String)*
        - Comma separated allowed types of seeds interactor around which the network will be built.
        - **If not provided**, all types will be allowed for seeds
        - Example: `"protein, peptide, small molecule, gene, dna, ds dna, ss dna, rna, complex"`

```bash
intact query seedTerms="gtp lrrk2" taxons="9606, -2" types="protein, small molecule" netName="GTP-LRRK2" exactQuery="false" applyLayout="false"
→ Collecting interactors
→ Querying network from interactors
→ Querying IntAct servers
→ Parsing data
→ Create summary edges
→ Register network
→ Create and register network view + Initialize filters
```

### intact summary & intact evidence & intact mutation

- Usage: Allow the switching of view types for a specific IntAct Network View
- Parameters:
    - view *string*
        - IntAct Network view to manipulate
        - **If not provided**, current view will be used
        - Should be formatted as `"View of ${NETWORK_NAME}"`

```bash
intact evidence view="View of GTP-LRRK2"
```

### intact reset-filters

- Usage: Allow the reinitialisation of the different filters of an IntAct Network View
- Parameters:
    - view *string*
        - IntAct Network view to manipulate
        - **If not provided**, current view will be used
        - Should be formatted as `"View of ${NETWORK_NAME}"`

```bash
intact reset-filters view="View of GTP-LRRK2"
```

### intact extract

- Usage: Allow the extraction of an IntAct Network View to a standard Cytoscape view to allow its topological analysis
- Explanation: IntAct App uses summary edges in addition of its evidence edges to support its visualisation features. In
  order to perform topological analysis without any extra edges, you should therefore use this command on the desired
  view mode.
- Parameters:
    - view *string*
        - IntAct Network view to extract
        - **If not provided**, current view will be used
        - Should be formatted as `"View of ${NETWORK_NAME}"`
    - includeFiltered *boolean*
        - `true` → All elements of the selected view type will be exported, including the filtered ones
        - `false` **Default** → Only visible elements will be exported, thus excluding filtered elements
    - applyLayout *boolean*
        - `true` **Default** → Apply force directed layout algorithm after the extraction on the new network
        - `false` → Do not apply any layout algorithm: All elements of the extracted network will be stacked on top of
          each others visually

```bash
intact extract view="View of GTP-LRRK2" applyLayout="false" includeFiltered="false"
```

## Rest API

Once Cytoscape is open with IntAct App installed, you can access the available Rest API at
this [url](http://localhost:1234/v1/swaggerUI/swagger-ui/index.html?url=http%3A%2F%2Flocalhost%3A1234%2Fv1%2Fcommands%2Fswagger.json#/intact)
.

This API is generated with Swagger by Cytoscape, so you can directly try the different requests