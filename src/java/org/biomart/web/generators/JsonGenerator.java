// give more sensible empty value other than null to getDescription etc

package org.biomart.web.generators;

import java.io.*;
import java.util.*;

import com.aliasi.tokenizer.*;

import org.jboss.dna.common.text.Inflector;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.martRemote.MartApi;
import org.biomart.objects.lite.*;

/**
 * Generate the static json files required by Martview
 *
 * <dl>
 *   <dt>datasets.json</dt>
 *   <dd>contains the metadata of all datasets, with the exception of filters/attributes.</dd>
 *   <dt>dataset.json</dt>
 *   <dd>contains the metadata of a speficic dataset, including filters/attributes.</dd>
 * </dl>
 */
public class JsonGenerator {

    public static void main(String[] args) {
        try {
            final String USERNAME = "anonymous";
            final String PASSWORD = "";
            final String FORMAT = "";
            final String PARTITION_FILTER = "main_partition_filter.\"hsapiens_gene_ensembl,mmusculus_gene_ensembl,celegans_gene_ensembl\"";

            MartApi api = new MartApi();

            JSONObject jsonDatasets = new JSONObject();
            JSONObject jsonDataset;
            JSONObject jsonFilter;
            JSONObject jsonAttribute;

            List<LiteMart> marts = api.getRegistry(USERNAME, PASSWORD, FORMAT).getLiteMartList();

            for (LiteMart mart : marts) {
                List<LiteDataset> datasets = api.getDatasets(USERNAME, PASSWORD, FORMAT, mart.getName(), mart.getVersion()).getLiteDatasetList();

                // FIXME: remove all hard-coded values
                for (LiteDataset dataset : datasets) {

                    if (mart.getVisible() && mart.getName().equals("ensembl_mart_55") && dataset.getVisible()) {
                        jsonDataset = jsonifyDataset(mart, dataset);
                        jsonDatasets.accumulate("rows", jsonDataset);

                        LiteContainer root = api.getRootContainer(USERNAME, PASSWORD, FORMAT, "gene_ensembl", PARTITION_FILTER).getLiteContainer();

                        JSONObject tree = jsonifyContainer(root, null);

//                        // gene_ensembl should be dataset.getName() instead, but it returns the full name (config name?)
//                        List<LiteFilter> filters = api.getFilters(USERNAME, PASSWORD, FORMAT, "gene_ensembl", PARTITION_FILTER).getLiteFilterList();
//                        for (LiteFilter filter : filters) {
//                            if (filter.getVisible()) {
//                                jsonFilter = jsonifyFilter(filter);
//                                jsonDataset.accumulate("filters", jsonFilter);
//                            }
//                        }
//                        // gene_ensembl should be dataset.getName() instead, but it returns the full name (config name?)
//                        List<LiteAttribute> attributes = api.getAttributes(USERNAME, PASSWORD, FORMAT, "gene_ensembl", PARTITION_FILTER).getLiteAttributeList();
//
//
//                        for (LiteAttribute attribute : attributes) {
//                            if (attribute.getVisible()) {
//                                jsonAttribute = jsonifyAttribute(attribute);
//                                jsonDataset.accumulate("attributes", jsonAttribute);
//                            }
//                        }

                        jsonDataset.put("tree", tree);
                        write(jsonDataset, dataset.getName() + ".json");
                    }
                }
            }
            write(jsonDatasets, "datasets.json");
        } catch (FunctionalException e) {
            e.printStackTrace();
        } catch (TechnicalException e) {
            e.printStackTrace();
        }
    }

    /**
     * Transform a container into its json representation
     * <p>
     * Note that the transformation is recursive along the entire depth of the container tree
     *
     * @param container the container to be jsonified
     * @return          the jsonified container
     */
    private static JSONObject jsonifyContainer(LiteContainer container, String parentStems) {
        List<LiteAttribute> attributes = container.getLiteAttributeList();
        List<LiteFilter> filters = container.getLiteFilterList();
        List<LiteContainer> children = container.getLiteContainerList();

        JSONObject js = new JSONObject();

        // add common props
        js.put("id", container.getName());
        js.put("leaf", false);
        js.put("text", container.getDisplayName());
        js.put("qtip", container.getDescription());

        // add stems prop
        ArrayList<String> stems = new ArrayList<String>();
        stems.add(container.getDisplayName());
        stems.add(container.getDescription());
        stems.add(parentStems);
        js.put("stems", normalize(join(stems, "")));

        for (LiteFilter filter : filters) {
//            if (filter.getVisible()) {
            JSONObject jsonFilter = jsonifyFilter(filter, js.getString("stems"));
            js.accumulate("filters", jsonFilter);
//            }
        }

        for (LiteAttribute attribute : attributes) {
//            if (attribute.getVisible()) {
            JSONObject jsonAttribute = jsonifyAttribute(attribute, js.getString("stems"));
            js.accumulate("attributes", jsonAttribute);
//            }
        }

        for (LiteContainer child : children) {
            js.accumulate("children", jsonifyContainer(child, js.getString("stems")));
        }

        return js;
    }

    /**
     * Transform a dataset into its json representation
     *
     * @param mart    the mart that contains the dataset to be jsonified
     * @param dataset the dataset to be jsonified
     * @return        the jsonified dataset
     */
    private static JSONObject jsonifyDataset(LiteMart mart, LiteDataset dataset) {
        JSONObject js = new JSONObject();

        // add common props
        js.put("id", dataset.getName());
//        js.put("title", dataset.getDisplayName());
//        js.put("description", dataset.getDescription());

        // add stems prop
        ArrayList<String> stems = new ArrayList<String>();
        stems.add(mart.getDisplayName());
//        stems.add(mart.getDescription());
        stems.add(mart.getVersion().toString());
//        stems.add(dataset.getDisplayName());
//        stems.add(dataset.getDescription());
        js.put("stems", normalize(join(stems, " ")));

        return js;
    }

    /**
     * Transform a filter into its json representation
     *
     * @param filter the filter to be jsonified
     * @return       the jsonified filter
     */
    private static JSONObject jsonifyFilter(LiteFilter filter, String parentStems) {
        JSONObject js = new JSONObject();

        // add common props
        js.put("id", filter.getName());
        js.put("leaf", true);
        js.put("text", filter.getDisplayName());
        js.put("qtip", filter.getDescription());

        // add other props
        js.put("default", filter.getSelectedByDefault());
        js.put("type", filter.getDisplayType());
        js.put("multiValue", filter.getMultiValue());
        js.put("upload", filter.getUpload());
        js.put("qualifier", filter.getQualifier());
        js.put("buttonURL", filter.getButtonURL());

        // add stems prop
        ArrayList<String> stems = new ArrayList<String>();
        stems.add(filter.getDisplayName());
        stems.add(filter.getDescription());
        stems.add(parentStems);
        js.put("stems", normalize(join(stems, "")));

        return js;
    }

    /**
     * Transform an attribute into its json representation
     *
     * @param attribute the attribute to be jsonified
     * @return          the jsonified attribute
     */
    private static JSONObject jsonifyAttribute(LiteAttribute attribute, String parentStems) {
        JSONObject js = new JSONObject();

        // add common props
        js.put("id", attribute.getName());
        js.put("leaf", true);
        js.put("text", attribute.getDisplayName());
        js.put("qtip", attribute.getDescription());

        // add other props
        js.put("default", attribute.getSelectedByDefault());
        js.put("linkURL", attribute.getLinkURL());
        js.put("maxLength", attribute.getMaxLength());

        // add stems prop
        ArrayList<String> stems = new ArrayList<String>();
        stems.add(attribute.getDisplayName());
        stems.add(attribute.getDescription());
        stems.add(parentStems);
        js.put("stems", normalize(join(stems, "")));

        return js;
    }

    /**
     * Persist a JSON object by writing a file on disk, using the specified file name
     *
     * @param json     the JSON object to be persisted
     * @param filename the name of the file to be written on disk
     */
    private static void write(JSONObject json, String filename) {
        try {
            FileWriter filewriter = new FileWriter(filename);
            json.write(filewriter);
            filewriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Normalize a string by removing multiple occurrences of a word, stop words and punctuation, lowercasing, and stemming
     * @param s the string to be normalized
     * @return  the normalized string
     */
    private static String normalize(String s) {
        TokenizerFactory factory;
        Tokenizer tokenizer;

        IndoEuropeanTokenCategorizer categorizer = IndoEuropeanTokenCategorizer.CATEGORIZER;

        Inflector inflector = new Inflector();
        String singular;
        String plural;

        // reduce string to a list of words (lowercase, no punctuation, no stop words)
        List<String> words = new ArrayList<String>();
        factory = new EnglishStopTokenizerFactory(new LowerCaseTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE));
        tokenizer = factory.tokenizer(s.toCharArray(), 0, s.length());
        for (String token : tokenizer) {
            if (!(categorizer.categorize(token).equals("PUNC-") || categorizer.categorize(token).equals("OTHER"))) {
                if (!(categorizer.categorize(token).equals("1-DIG") || categorizer.categorize(token).equals("2-DIG") || categorizer.categorize(token).equals("3-DIG") || categorizer.categorize(token).equals("4-DIG") || categorizer.categorize(token).equals("5+-DIG"))) {
                    singular = inflector.singularize(token);
                    plural = inflector.pluralize(token);
                    if (!(words.contains(singular))) {
                        words.add(singular);
                    }
                    if (!(words.contains(plural))) {
                        words.add(plural);
                    }
                } else {
                    if (!(words.contains(token))) {
                        words.add(token);
                    }
                }
            }
        }
        s = join(words, " ");

        // reduce string to a list of stems
        List<String> stems = new ArrayList<String>();
        factory = new PorterStemmerTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE);
        tokenizer = factory.tokenizer(s.toCharArray(), 0, s.length());
        for (String token : tokenizer) {
            if (!(stems.contains(token))) {
                stems.add(token);
            }
        }
        return join(stems, " ");
    }

    /**
     * Join a list of strings into a single string using the specified separator
     *
     * @param list the list of strings to be joined
     * @param sep  the separator used to concatenate the list of strings
     * @return     the concatenated string
     */
    private static String join(List<String> list, String sep) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            String string = list.get(i);
            if (string != null) {
                if (i != 0) {
                    sb.append(sep);
                }
                sb.append(string);
            }
        }
        return sb.toString();
    }
}
