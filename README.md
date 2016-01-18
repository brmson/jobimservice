JoBim Text REST Service
=======================

## Introduction and Setup
This REST backend provides JoBim Text functionality to YodaQA in order to integrate distributional semantics with the QA
pipeline. Please note that it has no ties with TU Darmstadt or IBM, but only provides access to the JoBim Text framework.

Dr. Riedl from Darmstadt University kindly uploaded their Wikipedia Stanford model to [Sourceforge](http://sourceforge.net/projects/jobimtext/files/data/models/wikipedia_stanford/)
Just download the 6.6GB, extract all files into a directory called "wikipedia_stanford" in the jobimtext_pipeline_0.1.2
folder and copy the wikipedia_stanford.sql file also into jobimtext_pipeline_0.1.2. Now remove suffix "_filtered_g1"
from two files, copy "wikipedia_stanford_LMI_s0.0_w2_f2_wf0_wpfmax1000_wpfmin2_p1000_simsortlimit200" into a file called
wikipedia_stanford_dt and compute sense clusters via `java -cp lib/org.jobimtext-0.1.2.jar:lib/* org.jobimtext.sense.ComputeSenseClusters -N 200 -n 100 -mc 3 -ms 5 -mr 100 -i wikipedia_stanford_dt -o wikipedia_stanford_sense_cluster`.
You should now be set to import the files into a database.

Install MariaDB, open a shell, go to jobimtext_pipeline_0.1.2 folder, open SQL CLI with `mysql -u root -p` (leave
password empty - just hit Enter), then execute `source /<full_path_to_jobimtext_pipeline_0.1.2>/wikipedia_stanford.sql` (takes a couple of minutes).
Remember: To see all databases run `show databases;`, to see all tables in the DB we created run `show tables from DT_wikipedia_stanford;`

If you change the MariaDB user and/or password make sure to change them in the XML descriptors in this project as well.
Otherwise, the default is root without a password. The descriptors are adapted from the [official JBT SVN repository](http://sourceforge.net/p/jobimtext/code/HEAD/tree/trunk/org.jobimtext.examples.api/).

## Running and API Specification
You can run the program with the Gradle task **runRestBackend** - it should then be available under 127.0.0.1:<port>/jbt/
To see the actual REST interface, please refer to *RestInterface.java*.

The following methods exist (and can simply be appended to http://127.0.0.1:8080/jbt/):
 - `listBackends`
 - `countTerm`
 - `countContext`
 - `similarTerms`
 - `similarTermsTopN`
 - `similarTermsThresholded`
 - `contexts`
 - `contextsTopN`
 - `contextsThresholded`
 - `getSenses`

The exact functionality and return types can be found in the JavaDocs.
Only `listBackends` requires no arguments, all others at least need a term
and a backend, some also an additional value. Furthermore, all functions are
available in JSON versions - just append "Json" to the method name.

Please note that the Stanford backend expects tagged input terms which need to be URL encoded.
For instance, `http://localhost:8080/jbt/countTerm?term=exceptionally%23RB&backend=mysql_wikipedia_stanford`
is a valid API call.

## Building models yourself
So, how can one build such a model? TU Darmstadt offers a [tutorial](http://maggie.lt.informatik.tu-darmstadt.de/jobimtext/wordpress/wp-content/uploads/2014/04/JoBimText-Tutorial-Practice-Commands.txt) that covers all relevant steps as well as a [Hadoop VM](https://sourceforge.net/projects/jobimtextgpl.jobimtext.p/files/hadoop-VM/) to follow along.

But: It does have some holes. I will briefly go over the main ideas and fill in the blanks.

IMPORTANT: Give the VM a ton of RAM or later steps will break. I tried VirtualBox defaults on a 8GB RAM box and it
broke, so I assigned 16GB.

Then they tap into the VM via SSH, download an example corpus called "mouse_corpus" that they upload to HDFS
(`hadoop fs -mkdir <corpus_name>` and `hadoop fs -put <corpus_name> <corpus_name>/corpus.txt`) and then the major step is computing the distributional
thesaurus with Hadoop. For this you generate a shell script via generateHadoopScript.py, e.g. `python 
generateHadoopScript.py <corpus_name> -hl matetools_small_lemmatized -f 0 -w 0 -wpfmax 50 -p 100 -l 50 -nb` and run the
result, e.g. `sh <corpus_name>_matetools_small_lemmatized_s0.0_f0_w0_wf0_wpfmax50_wpfmin2_p100_sc_one_LMI_simsort_ms_2_l50.sh`.
This takes a couple of minutes.

Afterwards, you can copy the results from HDFS to local files, in the case of the tutorial `hadoop fs -text
<corpus_name>_matetools_small_lemmatized__FreqSigLMI__PruneContext_s_0.0_w_0_f_0_wf_0_wpfmax_50_wpfmin_2_p_100__AggrPerFt__SimCount_sc_one_ac_False__SimSortlimit_50_minsim_2/p* > <corpus_name>_dt` (the most important file).

IMPORTANT: They forgot underscores in the tutorial - it should be "mouse_corpus_word_count" and "mouse_corpus_feature_count"
The tutorial also skips creating a SQL file: `python createTables.py <corpus_name> 100 LMI 50 . > import_<corpus_name>.sql`

With these files, we can do some cool stuff. For instance, cluster senses via `java -cp lib/org.jobimtext-0.1.2.jar:lib/*
org.jobimtext.sense.ComputeSenseClusters -N 50 -n 50 -i <corpus_name>_dt -o <corpus_name>_senses`

IMPORTANT: The cp command they refer to should be `cp ../mouse_corpus corpus/mouse_corpus.txt`
Then just run `java -Xmx3g -cp "lib/*" org.jobimtext.util.RunJoBimIngestionLocal descriptors/PattamaikaUIMAOperations.xml`
to find patterns (they don't mention it there, but this actually finds Hearst patterns via UIMA Ruta).

You can now label your sense clusters with this via `java -cp lib/org.jobimtext.pattamaika-0.1.2.jar:lib/*
org.jobimtext.pattamaika.SenseLabeller -mf 1 -ms 2 -mm 1 -sep '#' -tsep ', ' -p pattern_out/pattern_out_0.txt -s <corpus_name>_senses -o
<corpus_name>_senses_isa`

## Example
Let us assume we look up the term "exceptionally". First of all: If we use a Stanford backend, we have to look up the
tagged term or we will receive an empty result, e.g. "exceptionally#RB".

You immediately see the power of JoBim Text: It recognizes that "exceptionally#RB" is similar to terms like "extremely#RB",
"extraordinarily#RB", "incredibly#RB", "exceedingly#RB", "remarkably#RB" etc. It can provide accurate counts for these
terms and it can provide context to e.g. distinguish "cold", the disease from "cold", the sensation. And finally we can
group these interpretations. When you look into the trigram output it actually distinguishes the meaning of the term in
"[extremely, unusually, incredibly, extraordinarily, ..." from the meaning in "[attractive, intelligent, elegant, ...",
which is quite clever.

## Code Structure
The Main class simply creates a RestServer (based on Jetty and Jersey) in the rest package, which in turn runs the
RestInterface as a servlet. The RestInterface provides access to two backends in a RESTful way:
* **StatusMonitor (status package)**  Prints statūs of different components and provides health reports
* **JoBimSqlConnector (jbt package)** Provides access to JoBim Text framework

## Todo
- [X] General REST Interface to JoBim Text
- [X] Documentation
- [ ] Finish status monitor
- [ ] Use this backend in any YodaQA component
- [ ] Unit and integration Tests

## License
This code is dual licensed under [GPLv3](http://www.gnu.org/licenses/gpl.html) and [ASLv2](http://www.apache.org/licenses/LICENSE-2.0).

### GPLv3
    JoBim Text REST Service
    Copyright 2016  RWTH Aachen

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

### ASLv2
    JoBim Text REST Service
    Copyright 2016 RWTH Aachen

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

## References
If you want to follow the integration effort, a good place to start is the [Distributional Semantics issue on Github](https://github.com/brmson/yodaqa/issues/27).

For more information about the framework itself visit the [Language Technology Group at TU Darmstadt](http://maggie.lt.informatik.tu-darmstadt.de/jobimtext/)

Good books about the subject are "Structure Discovery in Natural Language" by Prof. Dr. Chris Biemann and "Semantic
Domains in Computational Linguistics" by Dr. Alfio Gliozzo (btw: they are the authors of the framework, which is
actually named after them: "Jo" stands for Alfio Gliozzo and "Bim" for Prof. Biemann.