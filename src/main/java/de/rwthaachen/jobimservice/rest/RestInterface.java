package de.rwthaachen.jobimservice.rest;

import de.rwthaachen.jobimservice.jbt.JoBimSqlConnector;
import de.rwthaachen.jobimservice.status.StatusMonitor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * REST interface for StatusMonitor and JBT
 */
@Path("/jbt")
public class RestInterface {
    // To include the experimental status monitor uncomment this line, the first line in the constructor
    // as well as the section under listBackendsJson()
    //private StatusMonitor statusMonitor;

    private Map<String, JoBimSqlConnector> joBimBackendMap = new HashMap<>();

    /**
     * Constructor that creates StatusMonitor and JBT backends
     */
    public RestInterface() {
        //statusMonitor = new StatusMonitor();

        String config = "conf_mysql_wikipedia_stanford.xml";
        JoBimSqlConnector joBimSqlConnector = new JoBimSqlConnector();
        joBimSqlConnector.init(config);
        joBimBackendMap.put("mysql_wikipedia_stanford", joBimSqlConnector);

        config = "conf_web_wikipedia_trigram.xml";
        joBimSqlConnector = new JoBimSqlConnector();
        joBimSqlConnector.initWeb(config);
        joBimBackendMap.put("web_wikipedia_trigram", joBimSqlConnector);
    }

    /**
     * Prints list of JBT backends
     * @return List of JBT backends
     */
    @GET
    @Path("listBackends") //http://127.0.0.1:8080/jbt/listBackends
    @Produces(MediaType.TEXT_PLAIN)
    public String listBackends() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : joBimBackendMap.keySet()) {
            stringBuilder.append(key);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * Prints list of JBT backends
     * @return List of JBT backends
     */
    @GET
    @Path("listBackendsJson") //http://127.0.0.1:8080/jbt/listBackendsJson
    @Produces(MediaType.APPLICATION_JSON)
    public String listBackendsJson() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        String delimiter = "";
        for (String key : joBimBackendMap.keySet()) {
            stringBuilder.append(delimiter).append("\"").append(key).append("\"");
            delimiter = ",";
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

//    //<editor-fold desc=".:: Status Reports ::.">
//    /**
//     * Returns status report in plain text
//     * @return Status report in plain text
//     */
//    @GET
//    @Path("statusText") //http://127.0.0.1:8080/jbt/statusText
//    @Produces(MediaType.TEXT_PLAIN)
//    public String test() {
//        return statusMonitor.reportPlaintext();
//    }
//
//    /**
//     * Returns status report in HTML
//     * @return Status report in HTML
//     */
//    @GET
//    @Path("status") //http://127.0.0.1:8080/jbt/status
//    @Produces(MediaType.TEXT_HTML)
//    public String status() {
//        return statusMonitor.reportHtml();
//    }
//
//    /**
//     * Returns status report in JSON (via marshalling)
//     * @return Status report in JSON
//     */
//    @GET
//    @Path("statusJson") //http://127.0.0.1:8080/jbt/statusJson
//    @Produces(MediaType.APPLICATION_JSON)
//    public StatusMonitor.HealthReport statusJson() {
//        return statusMonitor.reportObject();
//    }
//    //</editor-fold>

    //<editor-fold desc=".:: Distributional Semantics ::.">
    //BEWARE If you use a Stanford model you need to look up tagged terms, e.g. exceptionally#RB
    //       To be able to use the REST interface they need to be URL encoded, e.g. exceptionally%23RB
    //       Full example: http://127.0.0.1:8080/jbt/similarTerms?term=exceptionally%23RB&backend=mysql_wikipedia_stanford

    //<editor-fold desc="1) Counts">
    /**
     * Returns count for a term
     * @param term Term to count
     * @return Term count
     */
    @GET
    @Path("countTerm")
    @Produces(MediaType.TEXT_PLAIN)
    public String countTerm(@QueryParam("term") String term, @QueryParam("backend") String backend){
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);
        if(joBimSqlConnector != null) {
            return joBimSqlConnector.getCountTerm(term);
        }
        return "";
    }

    /**
     * Returns count for a term
     * @param term Term to count
     * @return Term count as JSON value
     */
    @GET
    @Path("countTermJson")
    @Produces(MediaType.APPLICATION_JSON)
    public String countTermJson(@QueryParam("term") String term, @QueryParam("backend") String backend){
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);
        if(joBimSqlConnector != null) {
            return "{\"count\":" + joBimSqlConnector.getCountTerm(term) + "}";
        }
        return "{}";
    }

    /**
     * Returns context count for a term
     * @param term Term whose contexts to count
     * @return Context count
     */
    @GET
    @Path("countContext")
    @Produces(MediaType.TEXT_PLAIN)
    public String countContext(@QueryParam("term") String term, @QueryParam("backend") String backend){
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);
        if(joBimSqlConnector != null) {
            return joBimSqlConnector.getCountContext(term);
        }
        return "";
    }

    /**
     * Returns context count for a term
     * @param term Term whose contexts to count
     * @return Context Count as JSON value
     */
    @GET
    @Path("countContextJson")
    @Produces(MediaType.APPLICATION_JSON)
    public String countContextJson(@QueryParam("term") String term, @QueryParam("backend") String backend){
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);
        if(joBimSqlConnector != null) {
            return "{\"count\":" + joBimSqlConnector.getCountContext(term) + "}";
        }
        return "{}";
    }
    //</editor-fold>

    //<editor-fold desc="2) Similar Terms">
    /**
     * Determines similar terms for term
     * @param term Term to find similar terms for
     * @return All similar terms
     */
    @GET
    @Path("similarTerms")
    @Produces(MediaType.TEXT_PLAIN)
    public String similarTerms(@QueryParam("term") String term, @QueryParam("backend") String backend){
        // Feel free to use getOrDefault here (but then we need to settle on a default)
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);
        if(joBimSqlConnector != null) {
            return joBimSqlConnector.getSimilarTerms(term);
        }
        return "";
    }

    /**
     * Converts a list returned by JBT into valid JSON according to RFC 7159
     * cmp. rfc-editor.org/rfc/rfc7159.txt
     * This method simply surrounds each String with quotes
     *
     * @param jbtList List returned from JBT
     * @return Same list in JSON
     */
    public String convertJbtListToJson(String jbtList) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] tempArray = jbtList.replaceAll("[\\s\\[\\]]","").split(",");

        stringBuilder.append("[");
        String delimiter = "";
        for (String i : tempArray) {
            stringBuilder.append(delimiter).append("\"").append(i).append("\"");
            delimiter = ",";
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    /**
     * Determines similar terms for term
     * @param term Term to find similar terms for
     * @return All similar terms as JSON array
     */
    @GET
    @Path("similarTermsJson")
    @Produces(MediaType.APPLICATION_JSON)
    public String similarTermsJson(@QueryParam("term") String term, @QueryParam("backend") String backend){
        // Feel free to use getOrDefault here (but then we need to settle on a default)
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);

        if(joBimSqlConnector != null) {
            return convertJbtListToJson(joBimSqlConnector.getSimilarTerms(term));
        }
        return "[]";

    }

    /**
     * Determines n highest ranked similar terms for term
     * @param term Term to find similar terms for
     * @param n How many highest ranked similar terms are returned
     * @return Filtered similar terms
     */
    @GET
    @Path("similarTermsTopN")
    @Produces(MediaType.TEXT_PLAIN)
    public String similarTermsTopN(@QueryParam("term") String term, @QueryParam("n") int n,
                                   @QueryParam("backend") String backend){
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);
        if(joBimSqlConnector != null) {
            return joBimSqlConnector.getSimilarTerms(term, n);
        }
        return "";
    }

    /**
     * Determines n highest ranked similar terms for term
     * @param term Term to find similar terms for
     * @param n How many highest ranked similar terms are returned
     * @return Filtered similar terms as JSON array
     */
    @GET
    @Path("similarTermsTopNJson")
    @Produces(MediaType.APPLICATION_JSON)
    public String similarTermsTopNJson(@QueryParam("term") String term, @QueryParam("n") int n,
                                   @QueryParam("backend") String backend){
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);
        if(joBimSqlConnector != null) {
            return convertJbtListToJson(joBimSqlConnector.getSimilarTerms(term, n));
        }
        return "[]";
    }

    /**
     * Finds similar terms with thresholding (for score above 20 use 81.0)
     * @param term Term to find similar terms for
     * @param d Threshold
     * @return Filtered similar terms
     */
    @GET
    @Path("similarTermsThresholded")
    @Produces(MediaType.TEXT_PLAIN)
    public String similarTermsThresholded(@QueryParam("term") String term, @QueryParam("d") double d,
                                          @QueryParam("backend") String backend){
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);
        if(joBimSqlConnector != null) {
            return joBimSqlConnector.getSimilarTerms(term, d);
        }
        return "";
    }

    /**
     * Finds similar terms with thresholding (for score above 20 use 81.0)
     * @param term Term to find similar terms for
     * @param d Threshold
     * @return Filtered similar terms as JSON array
     */
    @GET
    @Path("similarTermsThresholdedJson")
    @Produces(MediaType.APPLICATION_JSON)
    public String similarTermsThresholdedJson(@QueryParam("term") String term, @QueryParam("d") double d,
                                          @QueryParam("backend") String backend){
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);
        if(joBimSqlConnector != null) {
            return convertJbtListToJson(joBimSqlConnector.getSimilarTerms(term, d));
        }
        return "[]";
    }

    /**
     * Performs similar term search for an entire sentence (words separated by spaces)
     * Note: will return empty lists for Stanford backend, because tokens are not tagged
     * @param sentence The sentence to analyze
     * @return Similar terms for each word
     */
    @GET
    @Path("similarTermsFromSentence")
    @Produces(MediaType.TEXT_PLAIN)
    public String similarTermsFromSentence(@QueryParam("sentence") String sentence, @QueryParam("backend") String backend){
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);
        if(joBimSqlConnector != null) {
            return joBimSqlConnector.getSimilarTermsFromSentence(sentence);
        }
        return "";
    }
    //</editor-fold>

    //<editor-fold desc="3) Contexts">
    /**
     * Determines contexts for a term
     * @param term Term to find contexts for
     * @return All contexts found
     */
    @GET
    @Path("contexts")
    @Produces(MediaType.TEXT_PLAIN)
    public String contexts(@QueryParam("term") String term, @QueryParam("backend") String backend){
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);
        if(joBimSqlConnector != null) {
            return joBimSqlConnector.getContextsForTerm(term);
        }
        return "";
    }

    /**
     * Determines contexts for a term
     * @param term Term to find contexts for
     * @return All contexts found as JSON array
     */
    @GET
    @Path("contextsJson")
    @Produces(MediaType.APPLICATION_JSON)
    public String contextsJson(@QueryParam("term") String term, @QueryParam("backend") String backend){
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);
        if(joBimSqlConnector != null) {
            return convertJbtListToJson(joBimSqlConnector.getContextsForTerm(term));
        }
        return "[]";
    }

    /**
     * Determines contexts for a term
     * @param term Term to find contexts for
     * @param n How many highest ranked contexts are returned
     * @return Filtered contexts
     */
    @GET
    @Path("contextsTopN")
    @Produces(MediaType.TEXT_PLAIN)
    public String contextsTopN(@QueryParam("term") String term, @QueryParam("n") int n,
                                   @QueryParam("backend") String backend){
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);
        if(joBimSqlConnector != null) {
            return joBimSqlConnector.getContextsForTerm(term, n);
        }
        return "";
    }

    /**
     * Determines contexts for a term
     * @param term Term to find contexts for
     * @param n How many highest ranked contexts are returned
     * @return Filtered contexts as JSON array
     */
    @GET
    @Path("contextsTopNJson")
    @Produces(MediaType.APPLICATION_JSON)
    public String contextsTopNJson(@QueryParam("term") String term, @QueryParam("n") int n,
                               @QueryParam("backend") String backend){
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);
        if(joBimSqlConnector != null) {
            return convertJbtListToJson(joBimSqlConnector.getContextsForTerm(term, n));
        }
        return "[]";
    }

    /**
     * Determines contexts for a term with thresholding
     * @param term Term to find contexts for
     * @param d Threshold
     * @return Filtered contexts
     */
    @GET
    @Path("contextsThresholded")
    @Produces(MediaType.TEXT_PLAIN)
    public String contextsThresholded(@QueryParam("term") String term, @QueryParam("d") double d,
                                          @QueryParam("backend") String backend){
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);
        if(joBimSqlConnector != null) {
            return joBimSqlConnector.getContextsForTerm(term, d);
        }
        return "";
    }

    /**
     * Determines contexts for a term with thresholding
     * @param term Term to find contexts for
     * @param d Threshold
     * @return Filtered contexts as JSON array
     */
    @GET
    @Path("contextsThresholdedJson")
    @Produces(MediaType.APPLICATION_JSON)
    public String contextsThresholdedJson(@QueryParam("term") String term, @QueryParam("d") double d,
                                      @QueryParam("backend") String backend){
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);
        if(joBimSqlConnector != null) {
            return convertJbtListToJson(joBimSqlConnector.getContextsForTerm(term, d));
        }
        return "[]";
    }
    //</editor-fold">

    //<editor-fold desc="4) Sense Clusters">
    /**
     * Determines senses for a term
     * @param term Term to find senses for
     * @return Found senses
     */
    @GET
    @Path("getSenses")
    @Produces(MediaType.TEXT_PLAIN)
    public String getSenses(@QueryParam("term") String term, @QueryParam("backend") String backend){
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);
        if(joBimSqlConnector != null) {
            return joBimSqlConnector.getSensesVerbose(term);
        }
        return "";
    }

    /**
     * Determines senses for a term
     * @param term Term to find senses for
     * @return Found senses
     */
    @GET
    @Path("getSensesJson")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSensesJson(@QueryParam("term") String term, @QueryParam("backend") String backend){
        JoBimSqlConnector joBimSqlConnector = joBimBackendMap.get(backend);
        if(joBimSqlConnector != null) {
            String[] tempArray = joBimSqlConnector.getSenses(term).split(";");
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[");
            String delimiter = "";
            for (String i : tempArray) {
                stringBuilder.append(delimiter).append(convertJbtListToJson(i));
                delimiter = ",";
            }
            stringBuilder.append("]");

            return stringBuilder.toString();
        }
        return "[]";
    }
    //</editor-fold>

    //</editor-fold>
}
