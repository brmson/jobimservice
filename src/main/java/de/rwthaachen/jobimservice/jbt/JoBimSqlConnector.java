package de.rwthaachen.jobimservice.jbt;

import org.jobimtext.api.struct.*;

import java.util.Arrays;
import java.util.List;

public class JoBimSqlConnector {

    IThesaurusDatastructure<String, String> dt;

    /**
     * Initializes DatabaseThesaurusDatastructure given by XML descriptor (for local databases)
     * @param config Name of XML descriptor in root project directory
     */
    public void init(String config) {
        dt = new DatabaseThesaurusDatastructure(config);
        dt.connect();
    }

    /**
     * Initializes WebThesaurusDatastructure given by XML descriptor (for web services)
     * @param config Name of XML descriptor in root project directory
     */
    public void initWeb(String config) {
        dt = new WebThesaurusDatastructure(config);
        dt.connect();
    }

    /**
     * Destroys object, to be called on close
     */
    public void destroy() {
        dt.destroy();
    }

    /**
     * Returns count for a term
     * @param term Term to count
     */
    public String getCountTerm(String term) {
        return dt.getTermCount(term).toString();
    }

    /**
     * Returns context count for a term
     * @param term Term whose contexts to count
     */
    public String getCountContext(String term) {
        String context = dt.getTermContextsScores(term, 1).get(0).key;
        return dt.getContextsCount(context).toString();
    }

    /**
     * Determines senses for a term, provides additional information
     * @param term Term to find senses for
     * @return Found senses
     */
    public String getSensesVerbose(String term) {
        StringBuilder stringBuilder = new StringBuilder();

        String[] sensesTypes = dt.getSensesTypes();
        stringBuilder.append(Arrays.toString(sensesTypes));
        stringBuilder.append("\n");

        for (String sense : sensesTypes) {

            stringBuilder.append("Output for sense [sense is, sense words, ISA labels]: ");
            stringBuilder.append(sense);
            if(sense.equals(dt.getStandardSense())) {
                stringBuilder.append(" [standard sense] ");
            }

            for (Sense s : dt.getSenses(term, sense)) {
                stringBuilder.append(s);
            }
        }

        return stringBuilder.toString();
    }

    /**
     * Determines senses for a term without additional information
     * @param term Term to find senses for
     * @return Found senses
     */
    public String getSenses(String term) {
        StringBuilder stringBuilder = new StringBuilder();

        String[] sensesTypes = dt.getSensesTypes();

        for (String sense : sensesTypes) {

            for (Sense s : dt.getSenses(term, sense)) {
                stringBuilder.append(s);
            }
        }

        return stringBuilder.toString().replaceAll("\\[\\]","").replaceAll("[0-9]+:","").replaceAll("\\]","\\];").replaceAll("[\\s+]","").replaceAll(";$","");
    }

    /**
     * Determines similar terms for term
     * @param term Term to find similar terms for
     * @return All similar terms
     */
    public String getSimilarTerms(String term) {
        return dt.getSimilarTerms(term).toString();
    }

    /**
     * Determines n highest ranked similar terms for term
     * @param term Term to find similar terms for
     * @param n How many highest ranked similar terms are returned
     * @return Filtered similar terms
     */
    public String getSimilarTerms(String term, int n) {
        return dt.getSimilarTerms(term, n).toString();
    }

    /**
     * Finds similar terms with thresholding (for score above 20 use 81.0)
     * @param term Term to find similar terms for
     * @param d Threshold
     * @return Filtered similar terms
     */
    public String getSimilarTerms(String term, double d) {
        return dt.getSimilarTerms(term, d).toString();
    }

    /**
     * Determines contexts for a term
     * @param term Term to find contexts for
     * @return All contexts found
     */
    public String getContextsForTerm(String term) {
        return dt.getTermContextsScores(term).toString();
    }

    /**
     * Determines contexts for a term
     * @param term Term to find contexts for
     * @param n How many highest ranked contexts are returned
     * @return Filtered contexts
     */
    public String getContextsForTerm(String term, int n) {
        return dt.getTermContextsScores(term, n).toString();
    }

    /**
     * Determines contexts for a term with thresholding
     * @param term Term to find contexts for
     * @param d Threshold
     * @return Filtered contexts
     */
    public String getContextsForTerm(String term, double d) {
        return dt.getTermContextsScores(term, d).toString();
    }

    /**
     * Performs similar term search for an entire sentence (words separated by spaces)
     * Note: will return empty lists for Stanford backend, because tokens are not tagged
     * @param sentence The sentence to analyze
     * @return Similar terms for each word
     */
    public String getSimilarTermsFromSentence(String sentence) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String token : sentence.split(" ")) {
            List<Order2> similarities = dt.getSimilarContexts(token, 10);
            System.out.println(token + ": ");
            stringBuilder.append(token);
            stringBuilder.append(":\t");

            for (Order2 o2 : similarities) {
                stringBuilder.append(o2.key + ":" + o2.score + "\n");
            }
        }

        return stringBuilder.toString();
    }

}
