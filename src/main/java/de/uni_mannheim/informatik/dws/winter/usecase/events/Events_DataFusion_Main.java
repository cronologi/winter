/** 
 *
 * Copyright (C) 2015 Data and Web Science Group, University of Mannheim, Germany (code@dwslab.de)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.uni_mannheim.informatik.dws.winter.usecase.events;

import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.usecase.events.datafusion.evaluation.EventCoordinatesEvaluationRule;
import de.uni_mannheim.informatik.dws.winter.usecase.events.datafusion.evaluation.EventLabelEvaluationRule;
import de.uni_mannheim.informatik.dws.winter.usecase.events.datafusion.evaluation.EventURIEvaluationRule;
import de.uni_mannheim.informatik.dws.winter.usecase.events.datafusion.fusers.EventDateFuserAll;
import de.uni_mannheim.informatik.dws.winter.usecase.events.datafusion.fusers.EventLabelFuserAll;
import de.uni_mannheim.informatik.dws.winter.usecase.events.datafusion.fusers.EventURIFuserAll;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.Event;
//import de.uni_mannheim.informatik.dws.winter.usecase.events.model.EventFactory;
import de.uni_mannheim.informatik.dws.winter.datafusion.CorrespondenceSet;
import de.uni_mannheim.informatik.dws.winter.datafusion.DataFusionEngine;
import de.uni_mannheim.informatik.dws.winter.datafusion.DataFusionEvaluator;
import de.uni_mannheim.informatik.dws.winter.datafusion.DataFusionStrategy;
import de.uni_mannheim.informatik.dws.winter.model.*;
import de.uni_mannheim.informatik.dws.winter.usecase.events.datafusion.evaluation.EventDateEvaluationRule;
import de.uni_mannheim.informatik.dws.winter.usecase.events.model.EventXMLReader;
import org.joda.time.DateTime;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Class containing the standard setup to perform a data fusion task, reading
 * input data from the movie usecase.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * * @author Daniel Ringler
 * 
 */
public class Events_DataFusion_Main {

	public static void main(String[] args) throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException,
			TransformerException {

		char separator = '+';
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
		boolean filterFrom = false;
		boolean filterTo = false;
		boolean applyKeywordSearch = false;
		LocalDate fromDate = LocalDate.MIN;
		LocalDate toDate = LocalDate.MAX;
		String keyword = "";

		// Load the Data into FusableDataSet
		FusibleDataSet<Event, Attribute> fusibleDataSetD = new FusibleHashedDataSet<>();
//		fusibleDataSetD.loadFromTSV(new File("WDI/usecase/event/input/dbpedia-1_s.tsv"),
//				new EventFactory(dateTimeFormatter, filterFrom, fromDate, filterTo, toDate, applyKeywordSearch, keyword), "events/event", separator, dateTimeFormatter, false, fromDate, false, toDate, true, keyword);


		FusibleDataSet<Event, Attribute> fusibleDataSetY = new FusibleHashedDataSet<>();
//		fusibleDataSetY.loadFromTSV(new File("WDI/usecase/event/input/yago-1_s.tsv"),
//				new EventFactory(dateTimeFormatter, filterFrom, fromDate, filterTo, toDate, applyKeywordSearch, keyword), "events/event", separator, dateTimeFormatter, false, fromDate, false, toDate, true, keyword);


		FusibleDataSet<Event, Attribute> fusedDataSet = runDataFusion(fusibleDataSetD,
				fusibleDataSetY,
				//null,
				separator, dateTimeFormatter, filterFrom, fromDate, filterTo, toDate, applyKeywordSearch, keyword);

	}

	public static FusibleDataSet<Event,Attribute> runDataFusion(FusibleDataSet<Event, Attribute> fusableDataSetD,
																		   FusibleDataSet<Event, Attribute> fusableDataSetY,
																		   //ResultSet<Correspondence<Event, Attribute>> correspondences,
																		   char separator,
																		   DateTimeFormatter dateTimeFormatter,
																		   boolean filterFrom, LocalDate fromDate,
																		   boolean filterTo, LocalDate toDate, boolean applyKeywordSearch, String keyword) throws IOException {

		//FusableDataSet<Event, DefaultSchemaElement> fusableDataSetD = (FusableDataSet<Event, DefaultSchemaElement>) dataSetD;
		System.out.println("DBpedia Data Set Density Report:");
		fusableDataSetD.printDataSetDensityReport();

		//FusableDataSet<Event, DefaultSchemaElement> fusableDataSetY = (FusableDataSet<Event, DefaultSchemaElement>) dataSetY;
		System.out.println("YAGO Data Set Density Report:");
		fusableDataSetY.printDataSetDensityReport();

		// Maintain Provenance
		// Scores (e.g. from rating)
		fusableDataSetD.setScore(1.0);
		fusableDataSetY.setScore(1.0);

		// Date (e.g. last update)
		fusableDataSetD.setDate(DateTime.parse("2016-04-01"));
		fusableDataSetY.setDate(DateTime.parse("2015-11-01"));

		CorrespondenceSet<Event, Attribute> correspondencesSet = new CorrespondenceSet<>();
			correspondencesSet
					.loadCorrespondences(
							//correspondences,
							new File("usecase/events/correspondences/dbpedia_2_yago.csv"),
							fusableDataSetD, fusableDataSetY);

		correspondencesSet.printGroupSizeDistribution();

		DataFusionStrategy<Event, Attribute> strategy = new DataFusionStrategy<>(new EventXMLReader());

//				new EventFactory(dateTimeFormatter, filterFrom, fromDate, filterTo, toDate, applyKeywordSearch, keyword));

		strategy.addAttributeFuser(new Attribute("Uri"), new EventURIFuserAll(),
				new EventURIEvaluationRule());
		strategy.addAttributeFuser(new Attribute("Label"), new EventLabelFuserAll(),
				new EventLabelEvaluationRule());
		strategy.addAttributeFuser(new Attribute("Date"), new EventDateFuserAll(),
				new EventDateEvaluationRule());
		//strategy.addAttributeFuser(new Attribute("Coordinates"), new EventCoordinatesFuserFirst(),
		//		new EventCoordinatesEvaluationRule());

		//... all attributes
		//...

		DataFusionEngine<Event, Attribute> engine = new DataFusionEngine<>(strategy);

		// calculate cluster consistency
		engine.printClusterConsistencyReport(correspondencesSet, null);

		// run the fusion
		FusibleDataSet<Event, Attribute> fusedDataSet = engine.run(correspondencesSet, null);

		// write the result
		//fusedDataSet.writeCSV(new File("WDI/usecase/event/output/fused.tsv"),new EventCSVFormatter());


		// load the gold standard
		DataSet<Event, Attribute> gs = new FusibleHashedDataSet<>();
//		gs.loadFromTSV(new File("../data/fused.tsv"),
//				new EventFactory(dateTimeFormatter, filterFrom, fromDate, filterTo, toDate, applyKeywordSearch, keyword), "/events/event", separator, dateTimeFormatter, false, fromDate, false, toDate, false, keyword);

		//gs.splitMultipleValues(separator);
		// evaluate
		//DataFusionEvaluator<Movie, DefaultSchemaElement> evaluator = new DataFusionEvaluator<>(
		//		strategy, new RecordGroupFactory<Movie, DefaultSchemaElement>());
		DataFusionEvaluator<Event, Attribute> evaluator = new DataFusionEvaluator<>(
				strategy, new RecordGroupFactory<Event, Attribute>());
		evaluator.setVerbose(true);
		double accuracy = evaluator.evaluate(fusedDataSet, gs, null);

		System.out.println(String.format("Accuracy: %.2f", accuracy));

		return fusedDataSet;
	}
}
