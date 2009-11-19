/*
 *
 *
 */
package org.biomart.web.generators;

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
            String PARTITIONS = "main_partition_filter.\"hsapiens_gene_ensembl,mmusculus_gene_ensembl,celegans_gene_ensembl\"";

            JSONObject registry = martApi.getRegistry(USERNAME, PASSWORD, FORMAT).getJsonObject();
            System.out.println(registry);

            JSONObject datasets = martApi.getDatasets(USERNAME, PASSWORD, FORMAT, MART, VERSION).getJsonObject();
            System.out.println(datasets);

            JSONObject root = martApi.getRootContainer(USERNAME, PASSWORD, FORMAT, DATASET, PARTITIONS).getJsonObject();
            System.out.println(root);

            JSONObject filters = martApi.getFilters(USERNAME, PASSWORD, FORMAT, DATASET, PARTITIONS).getJsonObject();
            System.out.println(filters);

            JSONObject attributes = martApi.getAttributes(USERNAME, PASSWORD, FORMAT, DATASET, PARTITIONS).getJsonObject();
            System.out.println(attributes);

        } catch (FunctionalException e) {
            e.printStackTrace();
        } catch (TechnicalException e) {
            e.printStackTrace();
        }
    }
}
