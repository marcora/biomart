/*
 *
 *
 */
package org.biomart.web.generators;

import java.io.*;

import java.util.List;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;

import org.biomart.martRemote.MartApi;

// import org.biomart.test.DummyPortal;

import org.biomart.objects.lite.*;

/**
 *
 *
 */
public class JsonGenerator {

    public static void main(String[] args) {
        try {

            final String USERNAME = "anonymous";
            final String PASSWORD = "";
            final String FORMAT = "";
            final String PARTITION_FILTER = "";

            MartApi api = new MartApi();

            List<LiteMart> marts = api.getRegistry(USERNAME, PASSWORD, FORMAT).getLiteMartList();

            for (LiteMart mart : marts) {

                System.out.println("mart: " + mart.getName());

                List<LiteDataset> datasets = api.getDatasets(USERNAME, PASSWORD, FORMAT, mart.getName(), mart.getVersion()).getLiteDatasetList();

                for (LiteDataset dataset : datasets) {

                    System.out.println("  dataset: " + dataset.getName());

                    List<LiteFilter> filters = api.getFilters(USERNAME, PASSWORD, FORMAT, dataset.getName(), PARTITION_FILTER).getLiteFilterList();

                    for (LiteFilter filter : filters) {
                        System.out.println("    filter: " + filter.getName());
                    }

                    List<LiteAttribute> attributes = api.getAttributes(USERNAME, PASSWORD, FORMAT, dataset.getName(), PARTITION_FILTER).getLiteAttributeList();

                    for (LiteAttribute attribute : attributes) {
                        System.out.println("    attribute: " + attribute.getName());
                    }
                }
            }
        } catch (FunctionalException e) {
            e.printStackTrace();
        } catch (TechnicalException e) {
            e.printStackTrace();
        }
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
