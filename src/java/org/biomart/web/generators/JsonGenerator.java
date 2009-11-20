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

import org.biomart.objects.objects.*;

/**
 *
 *
 */
public class JsonGenerator {

    public static void main(String[] args) {
        try {

            String USERNAME = "anonymous";
            String PASSWORD = "";
            String FORMAT = "";

            MartApi martApi = new MartApi();

//            MartRegistry dummyMartRegistry = DummyPortal.createDummyMartRegistry();
//            MartApi martApi = new MartApi(dummyMartRegistry);

            List<Mart> marts = martApi.getRegistry(USERNAME, PASSWORD, FORMAT).getMartList();

            for (Mart mart : marts) {
                System.out.println("mart: " + mart.getName());

                List<Dataset> datasets = mart.getDatasetList();
                System.out.println("  datasets size: " + datasets.size());

                for (Dataset dataset : datasets) {
                    System.out.println("  dataset: " + dataset.getName());

                    List<Config> configs = dataset.getConfigList();
                    System.out.println("    configs size: " + configs.size());

                    for (Config config : configs) {
                        System.out.println("    config: " + config.getName());
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
