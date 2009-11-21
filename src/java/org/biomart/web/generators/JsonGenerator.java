package org.biomart.web.generators;

import java.io.*;

import java.util.*;

import net.sf.json.JSONObject;

import com.aliasi.tokenizer.*;
import org.jboss.dna.common.text.Inflector;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;

import org.biomart.martRemote.MartApi;

// import org.biomart.test.DummyPortal;

import org.biomart.objects.lite.*;

/**
 * Generate the static json files required by Martview
 *
 * <dl>
 * <dt>datasets.json</dt>
 * <dd>contains the metadata of all datasets, with the exception of filters/attributes.</dd>
 * <dt>dataset.json</dt>
 * <dd>contains the metadata of a speficic dataset, including filters/attributes.</dd>
 * </dl>
 */
public class JsonGenerator {

    public static void main(String[] args) {
        try {

            final String USERNAME = "anonymous";
            final String PASSWORD = "";
            final String FORMAT = "";
            final String PARTITION_FILTER = "";


            System.out.println(normalize("If your computer or network is protected by a firewall or proxy, make sure that Firefox is permitted to access the Web."));


            MartApi api = new MartApi();

            List<LiteMart> marts = api.getRegistry(USERNAME, PASSWORD, FORMAT).getLiteMartList();

            for (LiteMart mart : marts) {

                System.out.println("mart: " + mart.getName());

                List<LiteDataset> datasets = api.getDatasets(USERNAME, PASSWORD, FORMAT, mart.getName(), mart.getVersion()).getLiteDatasetList();

                for (LiteDataset dataset : datasets) {

                    System.out.println("  dataset: " + dataset.getName());

//                    List<LiteFilter> filters = api.getFilters(USERNAME, PASSWORD, FORMAT, dataset.getName(), PARTITION_FILTER).getLiteFilterList();
//
//                    for (LiteFilter filter : filters) {
//                        System.out.println("    filter: " + filter.getName());
//                    }
//
//                    List<LiteAttribute> attributes = api.getAttributes(USERNAME, PASSWORD, FORMAT, dataset.getName(), PARTITION_FILTER).getLiteAttributeList();
//
//                    for (LiteAttribute attribute : attributes) {
//                        System.out.println("    attribute: " + attribute.getName());
//                    }
                }
            }
        } catch (FunctionalException e) {
            e.printStackTrace();
        } catch (TechnicalException e) {
            e.printStackTrace();
        }
    }

    /**
     * Persist a JSON object by writing a file on disk, using the specified file name
     *
     * @param jsonobject the JSON object to be persisted
     * @param filename   the name of the file to be written on disk
     */
    private static void write(JSONObject jsonobject, String filename) {
        try {
            FileWriter out = new FileWriter(filename);
            jsonobject.write(out);
            out.close();
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
        List<String> words;
        TokenizerFactory factory;
        Tokenizer tokenizer;
        IndoEuropeanTokenCategorizer categorizer = IndoEuropeanTokenCategorizer.CATEGORIZER;
        Inflector inflector = new Inflector();
        String singular;
        String plural;
        StringBuffer sb;

        // reduces input text to a list of tokens (lowercase, no punctuation, no stop words)
        words = new ArrayList<String>();
        factory = new EnglishStopTokenizerFactory(new LowerCaseTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE));
        tokenizer = factory.tokenizer(s.toCharArray(), 0, s.length());
        for (String token : tokenizer) {
            if (!(categorizer.categorize(token).equals("PUNC-"))) {
                singular = inflector.singularize(token);
                plural = inflector.pluralize(token);
                if (!(words.contains(singular))) {
                    words.add(singular);
                }
                if (!(words.contains(plural))) {
                    words.add(plural);
                }
            }
        }

        // reduces input text to a list of tokens (stemmed using Porter)
        s = join(words, " ");
        words = new ArrayList<String>();
        factory = new PorterStemmerTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE);
        tokenizer = factory.tokenizer(s.toCharArray(), 0, s.length());
        for (String token : tokenizer) {
            if (!(words.contains(token))) {
                words.add(token);
            }
        }

        return join(words, " ");
    }

    /**
     * Join a list of strings into a single string using the specified separator
     *
     * @param strings the list of strings to be joined
     * @param sep     the separator used to concatenate the list of strings
     * @return        the concatenated string
     */
    private static String join(List<String> strings, String sep) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strings.size(); i++) {
            if (i != 0) {
                sb.append(sep);
            }
            sb.append(strings.get(i));
        }
        return sb.toString();
    }
}
