/*
 *
 *
 */
package org.biomart.web.generators;

import java.io.*;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.martRemote.MartApi;

/**
 *
 *
 */
public class JsonGenerator {

    public static void main(String[] args) {
        try {

            MartApi martApi = new MartApi();

            String USERNAME = "anonymous";
            String PASSWORD = "";
            String FORMAT = "";
            String MART = "ensembl_mart_55";
            Integer VERSION = 55;
            String DATASET = "gene_ensembl";
            String PARTITION_FILTER = "main_partition_filter.\"hsapiens_gene_ensembl,mmusculus_gene_ensembl,celegans_gene_ensembl\"";

            JSONObject registry = martApi.getRegistry(USERNAME, PASSWORD, FORMAT).getJsonObject();
            write(registry, "registry.json");
            System.out.println(registry);

            JSONObject datasets = martApi.getDatasets(USERNAME, PASSWORD, FORMAT, MART, VERSION).getJsonObject();
            write(datasets, "datasets.json");
            System.out.println(datasets);

            JSONObject root = martApi.getRootContainer(USERNAME, PASSWORD, FORMAT, DATASET, PARTITION_FILTER).getJsonObject();
            write(root, "root.json");
            System.out.println(root);

            JSONObject filters = martApi.getFilters(USERNAME, PASSWORD, FORMAT, DATASET, PARTITION_FILTER).getJsonObject();
            write(filters, "filters.json");
            System.out.println(filters);

            JSONObject attributes = martApi.getAttributes(USERNAME, PASSWORD, FORMAT, DATASET, PARTITION_FILTER).getJsonObject();
            write(attributes, "attributes.json");
            System.out.println(attributes);

        } catch (FunctionalException e) {

            e.printStackTrace();
        } catch (TechnicalException e) {

            e.printStackTrace();
        }
    }

    private static JSONObject decorateMart(JSONObject mart) {
        return mart;
    }

    private static JSONObject decorateDataset(JSONObject dataset) {
        return dataset;
    }

    private static JSONObject decorateContainer(JSONObject container) {
        return container;
    }

    private static JSONObject decorateFilter(JSONObject filter) {
        return filter;
    }

    private static JSONObject decorateAttribute(JSONObject attribute) {
        return attribute;
    }

    private static void write(JSONObject jsonobject, String filename) {
        try {
            FileWriter out = new FileWriter(filename);
            jsonobject.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }
}
