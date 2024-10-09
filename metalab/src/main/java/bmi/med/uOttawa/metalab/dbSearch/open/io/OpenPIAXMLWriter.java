/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.dom4j.DocumentException;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.dbSearch.msFragger.MSFraggerPsm;
import bmi.med.uOttawa.metalab.dbSearch.msFragger.MSFraggerReader;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenMod;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenPSM;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindPSM;
import de.mpc.pia.intermediate.Accession;
import de.mpc.pia.intermediate.PIAInputFile;
import de.mpc.pia.intermediate.Peptide;
import de.mpc.pia.intermediate.PeptideSpectrumMatch;
import de.mpc.pia.intermediate.compiler.PIACompiler;
import de.mpc.pia.intermediate.compiler.PIASimpleCompiler;
import de.mpc.pia.modeller.score.ScoreModel;
import de.mpc.pia.modeller.score.ScoreModelEnum;
import de.mpc.pia.tools.MzIdentMLTools;
import de.mpc.pia.tools.OntologyConstants;
import uk.ac.ebi.jmzidml.model.mzidml.AbstractParam;
import uk.ac.ebi.jmzidml.model.mzidml.AnalysisSoftware;
import uk.ac.ebi.jmzidml.model.mzidml.CvParam;
import uk.ac.ebi.jmzidml.model.mzidml.Enzyme;
import uk.ac.ebi.jmzidml.model.mzidml.Enzymes;
import uk.ac.ebi.jmzidml.model.mzidml.FileFormat;
import uk.ac.ebi.jmzidml.model.mzidml.InputSpectra;
import uk.ac.ebi.jmzidml.model.mzidml.Param;
import uk.ac.ebi.jmzidml.model.mzidml.ParamList;
import uk.ac.ebi.jmzidml.model.mzidml.SearchDatabase;
import uk.ac.ebi.jmzidml.model.mzidml.SearchDatabaseRef;
import uk.ac.ebi.jmzidml.model.mzidml.SpectraData;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIDFormat;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentification;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationProtocol;
import uk.ac.ebi.jmzidml.model.mzidml.Tolerance;

/**
 * @author Kai Cheng
 *
 */
public class OpenPIAXMLWriter {

	private PIACompiler compiler;
	private PIAInputFile piaFile;
	private File out;
	private SpectrumIdentification spectrumID;

	public OpenPIAXMLWriter(String out) {
		this(new File(out));
	}

	public OpenPIAXMLWriter(File out) {
		this.compiler = new PIASimpleCompiler();
		this.piaFile = compiler.insertNewFile("msms", "", "tsv");
		this.out = out;
	}

	public void initialMSFragger(File database, String[] raws) {
		File[] rawfiles = new File[raws.length];
		for (int i = 0; i < rawfiles.length; i++) {
			rawfiles[i] = new File(raws[i]);
		}
		initialMSFragger(database, rawfiles);
	}

	public void initialMSFragger(File database, File[] raws) {

		AnalysisSoftware msfragger = new AnalysisSoftware();

		msfragger.setId("MSFragger");
		msfragger.setName("MSFragger");
		msfragger.setUri(
				"http://inventions.umich.edu/technologies/7143_msfragger-ultrafast-and-comprehensive-identification-of-peptides-from-tandem-mass-spectra");
		msfragger.setVersion("1.0");

		Param param = new Param();
		param.setParam(MzIdentMLTools.createPSICvParam(OntologyConstants.XTANDEM, null));
		msfragger.setSoftwareName(param);

		msfragger = compiler.putIntoSoftwareMap(msfragger);

		SearchDatabase searchDatabase = new SearchDatabase();
		searchDatabase.setId("MSFraggerDB");
		searchDatabase.setLocation(database.getAbsolutePath());
		searchDatabase.setName(database.getName());

		FileFormat fileFormat = new FileFormat();
		fileFormat.setCvParam(MzIdentMLTools.createPSICvParam(OntologyConstants.FASTA_FORMAT, null));
		searchDatabase.setFileFormat(fileFormat);

		param = new Param();
		param.setParam(MzIdentMLTools.createUserParam(database.getName(), null, "string"));
		searchDatabase.setDatabaseName(param);
		searchDatabase = compiler.putIntoSearchDatabasesMap(searchDatabase);

		SpectraData[] spectraDatas = new SpectraData[raws.length];
		for (int i = 0; i < raws.length; i++) {
			spectraDatas[i] = new SpectraData();
			fileFormat = new FileFormat();
			spectraDatas[i].setId("MSFraggerInput");
			spectraDatas[i].setLocation(raws[i].getAbsolutePath());

			fileFormat.setCvParam(MzIdentMLTools.createPSICvParam(OntologyConstants.THERMO_RAW_FORMAT, null));
			spectraDatas[i].setFileFormat(fileFormat);
			SpectrumIDFormat idFormat = new SpectrumIDFormat();
			idFormat.setCvParam(
					MzIdentMLTools.createPSICvParam(OntologyConstants.MULTIPLE_PEAK_LIST_NATIVEID_FORMAT, null));
			spectraDatas[i].setSpectrumIDFormat(idFormat);
			spectraDatas[i] = compiler.putIntoSpectraDataMap(spectraDatas[i]);
		}

		SpectrumIdentificationProtocol spectrumIDProtocol = new SpectrumIdentificationProtocol();
		spectrumIDProtocol.setId("MSFraggerAnalysis");
		spectrumIDProtocol.setAnalysisSoftware(msfragger);

		param = new Param();
		param.setParam(MzIdentMLTools.createPSICvParam(OntologyConstants.MS_MS_SEARCH, null));
		spectrumIDProtocol.setSearchType(param);

		ParamList paramList = new ParamList();
		paramList.getCvParam().add(MzIdentMLTools.createPSICvParam(OntologyConstants.FRAGMENT_MASS_TYPE_MONO, null));
		paramList.getCvParam().add(MzIdentMLTools.createPSICvParam(OntologyConstants.PARENT_MASS_TYPE_MONO, null));
		spectrumIDProtocol.setAdditionalSearchParams(paramList);

		Enzymes enzymes = new Enzymes();
		Enzyme enzyme = new Enzyme();

		enzyme.setId("enzyme");
		enzyme.setMissedCleavages(2);

		enzyme.setSiteRegexp("(?&lt;=[KR])(?!P)");
		enzymes.getEnzyme().add(enzyme);
		spectrumIDProtocol.setEnzymes(enzymes);

		Tolerance tolerance = new Tolerance();
		AbstractParam abstractParam = MzIdentMLTools.createPSICvParam(OntologyConstants.SEARCH_TOLERANCE_PLUS_VALUE,
				"10.0");
		MzIdentMLTools.setUnitParameterFromString("ppm", abstractParam);
		tolerance.getCvParam().add((CvParam) abstractParam);
		abstractParam = MzIdentMLTools.createPSICvParam(OntologyConstants.SEARCH_TOLERANCE_MINUS_VALUE, "10.0");
		MzIdentMLTools.setUnitParameterFromString("ppm", abstractParam);
		tolerance.getCvParam().add((CvParam) abstractParam);
		spectrumIDProtocol.setParentTolerance(tolerance);

		tolerance = new Tolerance();
		abstractParam = MzIdentMLTools.createPSICvParam(OntologyConstants.SEARCH_TOLERANCE_PLUS_VALUE, "20");
		MzIdentMLTools.setUnitParameterFromString("ppm", abstractParam);
		tolerance.getCvParam().add((CvParam) abstractParam);
		abstractParam = MzIdentMLTools.createPSICvParam(OntologyConstants.SEARCH_TOLERANCE_MINUS_VALUE, "20");
		MzIdentMLTools.setUnitParameterFromString("ppm", abstractParam);
		tolerance.getCvParam().add((CvParam) abstractParam);
		spectrumIDProtocol.setFragmentTolerance(tolerance);

		//PROTEIN_GROUP_LEVEL_GLOBAL_FDR
		paramList = new ParamList();
		paramList.getCvParam()
				.add(MzIdentMLTools.createPSICvParam(OntologyConstants.PROTEIN_LEVEL_LOCAL_FDR, "0.01"));
		spectrumIDProtocol.setThreshold(paramList);

		piaFile.addSpectrumIdentificationProtocol(spectrumIDProtocol);

		spectrumID = new SpectrumIdentification();
		spectrumID.setId("MSFraggerIdentification");
		spectrumID.setSpectrumIdentificationList(null);
		spectrumID.setSpectrumIdentificationProtocol(spectrumIDProtocol);

		for (int i = 0; i < spectraDatas.length; i++) {
			InputSpectra inputSpectra = new InputSpectra();
			inputSpectra.setSpectraData(spectraDatas[i]);
			spectrumID.getInputSpectra().add(inputSpectra);
		}

		SearchDatabaseRef searchDBRef = new SearchDatabaseRef();
		searchDBRef.setSearchDatabase(searchDatabase);
		spectrumID.getSearchDatabaseRef().add(searchDBRef);
		piaFile.addSpectrumIdentification(spectrumID);
	}

	public void initialPFind(File database, String[] raws) {
		File[] rawfiles = new File[raws.length];
		for (int i = 0; i < rawfiles.length; i++) {
			rawfiles[i] = new File(raws[i]);
		}
		initialPFind(database, rawfiles);
	}

	public void initialPFind(File database, File[] raws) {

		AnalysisSoftware pFind = new AnalysisSoftware();

		pFind.setId("pFind");
		pFind.setName("pFind");
		pFind.setUri("http://pfind.ict.ac.cn/software/pfind/");
		pFind.setVersion("3.1.6");

		Param param = new Param();
		param.setParam(MzIdentMLTools.createPSICvParam(OntologyConstants.XTANDEM, null));
		pFind.setSoftwareName(param);

		pFind = compiler.putIntoSoftwareMap(pFind);

		SearchDatabase searchDatabase = new SearchDatabase();
		searchDatabase.setId("pFindDB");
		searchDatabase.setLocation(database.getAbsolutePath());
		searchDatabase.setName(database.getName());

		FileFormat fileFormat = new FileFormat();
		fileFormat.setCvParam(MzIdentMLTools.createPSICvParam(OntologyConstants.FASTA_FORMAT, null));
		searchDatabase.setFileFormat(fileFormat);

		param = new Param();
		param.setParam(MzIdentMLTools.createUserParam(database.getName(), null, "string"));
		searchDatabase.setDatabaseName(param);
		searchDatabase = compiler.putIntoSearchDatabasesMap(searchDatabase);

		SpectraData[] spectraDatas = new SpectraData[raws.length];
		for (int i = 0; i < raws.length; i++) {
			spectraDatas[i] = new SpectraData();
			fileFormat = new FileFormat();
			spectraDatas[i].setId("pFindInput");
			spectraDatas[i].setLocation(raws[i].getAbsolutePath());

			fileFormat.setCvParam(MzIdentMLTools.createPSICvParam(OntologyConstants.THERMO_RAW_FORMAT, null));
			spectraDatas[i].setFileFormat(fileFormat);
			SpectrumIDFormat idFormat = new SpectrumIDFormat();
			idFormat.setCvParam(
					MzIdentMLTools.createPSICvParam(OntologyConstants.MULTIPLE_PEAK_LIST_NATIVEID_FORMAT, null));
			spectraDatas[i].setSpectrumIDFormat(idFormat);
			spectraDatas[i] = compiler.putIntoSpectraDataMap(spectraDatas[i]);
		}

		SpectrumIdentificationProtocol spectrumIDProtocol = new SpectrumIdentificationProtocol();
		spectrumIDProtocol.setId("pFindAnalysis");
		spectrumIDProtocol.setAnalysisSoftware(pFind);

		param = new Param();
		param.setParam(MzIdentMLTools.createPSICvParam(OntologyConstants.MS_MS_SEARCH, null));
		spectrumIDProtocol.setSearchType(param);

		ParamList paramList = new ParamList();
		paramList.getCvParam().add(MzIdentMLTools.createPSICvParam(OntologyConstants.FRAGMENT_MASS_TYPE_MONO, null));
		paramList.getCvParam().add(MzIdentMLTools.createPSICvParam(OntologyConstants.PARENT_MASS_TYPE_MONO, null));
		spectrumIDProtocol.setAdditionalSearchParams(paramList);

		Enzymes enzymes = new Enzymes();
		Enzyme enzyme = new Enzyme();

		enzyme.setId("enzyme");
		enzyme.setMissedCleavages(2);

		enzyme.setSiteRegexp("(?&lt;=[KR])(?!P)");
		enzymes.getEnzyme().add(enzyme);
		spectrumIDProtocol.setEnzymes(enzymes);

		Tolerance tolerance = new Tolerance();
		AbstractParam abstractParam = MzIdentMLTools.createPSICvParam(OntologyConstants.SEARCH_TOLERANCE_PLUS_VALUE,
				"10.0");
		MzIdentMLTools.setUnitParameterFromString("ppm", abstractParam);
		tolerance.getCvParam().add((CvParam) abstractParam);
		abstractParam = MzIdentMLTools.createPSICvParam(OntologyConstants.SEARCH_TOLERANCE_MINUS_VALUE, "10.0");
		MzIdentMLTools.setUnitParameterFromString("ppm", abstractParam);
		tolerance.getCvParam().add((CvParam) abstractParam);
		spectrumIDProtocol.setParentTolerance(tolerance);

		tolerance = new Tolerance();
		abstractParam = MzIdentMLTools.createPSICvParam(OntologyConstants.SEARCH_TOLERANCE_PLUS_VALUE, "20");
		MzIdentMLTools.setUnitParameterFromString("ppm", abstractParam);
		tolerance.getCvParam().add((CvParam) abstractParam);
		abstractParam = MzIdentMLTools.createPSICvParam(OntologyConstants.SEARCH_TOLERANCE_MINUS_VALUE, "20");
		MzIdentMLTools.setUnitParameterFromString("ppm", abstractParam);
		tolerance.getCvParam().add((CvParam) abstractParam);
		spectrumIDProtocol.setFragmentTolerance(tolerance);

		// PROTEIN_GROUP_LEVEL_GLOBAL_FDR
		paramList = new ParamList();
		paramList.getCvParam().add(MzIdentMLTools.createPSICvParam(OntologyConstants.PROTEIN_LEVEL_LOCAL_FDR, "0.01"));
		spectrumIDProtocol.setThreshold(paramList);

		piaFile.addSpectrumIdentificationProtocol(spectrumIDProtocol);

		spectrumID = new SpectrumIdentification();
		spectrumID.setId("pFindIdentification");
		spectrumID.setSpectrumIdentificationList(null);
		spectrumID.setSpectrumIdentificationProtocol(spectrumIDProtocol);

		for (int i = 0; i < spectraDatas.length; i++) {
			InputSpectra inputSpectra = new InputSpectra();
			inputSpectra.setSpectraData(spectraDatas[i]);
			spectrumID.getInputSpectra().add(inputSpectra);
		}

		SearchDatabaseRef searchDBRef = new SearchDatabaseRef();
		searchDBRef.setSearchDatabase(searchDatabase);
		spectrumID.getSearchDatabaseRef().add(searchDBRef);
		piaFile.addSpectrumIdentification(spectrumID);
	}
	
	public void add(OpenPSM openpsm) {

		int charge = openpsm.getCharge();
		double precursorMr = openpsm.getPrecursorMr();
		double mass2charge = precursorMr / (double) charge + AminoAcidProperty.PROTON_W;
		PeptideSpectrumMatch psm = compiler.createNewPeptideSpectrumMatch(charge, mass2charge, openpsm.getMassDiff(),
				openpsm.getRt(), openpsm.getSequence(), openpsm.getMiss(),
				openpsm.getFileName() + "_" + openpsm.getScan(), openpsm.getFileName(), piaFile, spectrumID);
		psm.setIsDecoy(!openpsm.isTarget());

		Peptide peptide = compiler.getPeptide(openpsm.getSequence());
		if (peptide == null) {
			peptide = compiler.insertNewPeptide(openpsm.getSequence());
		}
		peptide.addSpectrum(psm);

		ScoreModel score = new ScoreModel(openpsm.getHyperscore(), ScoreModelEnum.XTANDEM_HYPERSCORE);
		psm.addScore(score);

		score = new ScoreModel(openpsm.getExpect(), ScoreModelEnum.XTANDEM_EXPECT);
		psm.addScore(score);

		// PERCOLATOR_SCORE
		score = new ScoreModel(openpsm.getClassScore(), ScoreModelEnum.PSM_LEVEL_COMBINED_FDR_SCORE);
		psm.addScore(score);

		score = new ScoreModel(openpsm.getQValue(), ScoreModelEnum.PERCOLATOR_Q_VALUE);
		psm.addScore(score);

		Accession acc = compiler.getAccession(openpsm.getProtein());
		if (acc == null) {
			acc = compiler.insertNewAccession(openpsm.getProtein(), null);
		}
		acc.addFile(piaFile.getID());

		compiler.addAccessionPeptideConnection(acc, peptide);
		compiler.insertCompletePeptideSpectrumMatch(psm);
	}

	public void add(MSFraggerPsm msfraggerPSM) {

		int charge = msfraggerPSM.getCharge();
		double precursorMr = msfraggerPSM.getPrecursorMr();
		double mass2charge = precursorMr / (double) charge + AminoAcidProperty.PROTON_W;
		PeptideSpectrumMatch psm = compiler.createNewPeptideSpectrumMatch(charge, mass2charge,
				msfraggerPSM.getMassDiff(), msfraggerPSM.getRt(), msfraggerPSM.getSequence(), msfraggerPSM.getMiss(),
				msfraggerPSM.getFileName() + "_" + msfraggerPSM.getScan(), msfraggerPSM.getFileName(), piaFile,
				spectrumID);
		psm.setIsDecoy(!msfraggerPSM.isTarget());

		Peptide peptide = compiler.getPeptide(msfraggerPSM.getSequence());
		if (peptide == null) {
			peptide = compiler.insertNewPeptide(msfraggerPSM.getSequence());
		}
		peptide.addSpectrum(psm);

		ScoreModel score = new ScoreModel(msfraggerPSM.getHyperscore(), ScoreModelEnum.XTANDEM_HYPERSCORE);
		psm.addScore(score);

		score = new ScoreModel(msfraggerPSM.getExpect(), ScoreModelEnum.XTANDEM_EXPECT);
		psm.addScore(score);

		Accession acc = compiler.getAccession(msfraggerPSM.getProtein());
		if (acc == null) {
			acc = compiler.insertNewAccession(msfraggerPSM.getProtein(), null);
		}
		acc.addFile(piaFile.getID());

		compiler.addAccessionPeptideConnection(acc, peptide);
		compiler.insertCompletePeptideSpectrumMatch(psm);
	}
	
	public void add(PFindPSM pfindPsm) {
		int charge = pfindPsm.getCharge();
		double precursorMr = pfindPsm.getExpMH() - AminoAcidProperty.PROTON_W;
		double mass2charge = precursorMr / (double) charge + AminoAcidProperty.PROTON_W;
		PeptideSpectrumMatch psm = compiler.createNewPeptideSpectrumMatch(charge, mass2charge, pfindPsm.getMassShift(),
				pfindPsm.getRt(), pfindPsm.getSequence(), pfindPsm.getMiss(), pfindPsm.getFileName(),
				pfindPsm.getFileName(), piaFile, spectrumID);

		psm.setIsDecoy(!pfindPsm.isTarget());

		Peptide peptide = compiler.getPeptide(pfindPsm.getSequence());
		if (peptide == null) {
			peptide = compiler.insertNewPeptide(pfindPsm.getSequence());
		}
		peptide.addSpectrum(psm);

		ScoreModel score = new ScoreModel(pfindPsm.getRawScore(), ScoreModelEnum.XTANDEM_HYPERSCORE);
		psm.addScore(score);

		score = new ScoreModel(pfindPsm.getFinalScore(), ScoreModelEnum.XTANDEM_EXPECT);
		psm.addScore(score);

		String[] pros = pfindPsm.getProteins();
		for (int i = 0; i < pros.length; i++) {
			Accession acc = compiler.getAccession(pros[i]);
			if (acc == null) {
				acc = compiler.insertNewAccession(pros[i], null);
			}

			acc.addFile(piaFile.getID());

			compiler.addAccessionPeptideConnection(acc, peptide);
		}

		compiler.insertCompletePeptideSpectrumMatch(psm);
	}
	
	public void addSpecies(PFindPSM pfindPsm) {
		int charge = pfindPsm.getCharge();
		double precursorMr = pfindPsm.getExpMH() - AminoAcidProperty.PROTON_W;
		double mass2charge = precursorMr / (double) charge + AminoAcidProperty.PROTON_W;
		PeptideSpectrumMatch psm = compiler.createNewPeptideSpectrumMatch(charge, mass2charge, pfindPsm.getMassShift(),
				pfindPsm.getRt(), pfindPsm.getSequence(), pfindPsm.getMiss(), pfindPsm.getFileName(),
				pfindPsm.getFileName(), piaFile, spectrumID);
		psm.setIsDecoy(!pfindPsm.isTarget());

		Peptide peptide = compiler.getPeptide(pfindPsm.getSequence());
		if (peptide == null) {
			peptide = compiler.insertNewPeptide(pfindPsm.getSequence());
		}
		peptide.addSpectrum(psm);

		ScoreModel score = new ScoreModel(pfindPsm.getRawScore(), ScoreModelEnum.XTANDEM_HYPERSCORE);
		psm.addScore(score);

		score = new ScoreModel(pfindPsm.getFinalScore(), ScoreModelEnum.XTANDEM_EXPECT);
		psm.addScore(score);

		String[] pros = pfindPsm.getProtein().split(";");
		HashSet<String> spset = new HashSet<String>();
		for (int i = 0; i < pros.length; i++) {
			if (pros[i].contains("HUMAN")) {
				return;
			}

			String species = pros[i].substring(0, pros[i].lastIndexOf("_"));
			if (!spset.contains(species)) {
				spset.add(species);
				Accession acc = compiler.getAccession(species);
				if (acc == null) {
					acc = compiler.insertNewAccession(species, null);
				}

				acc.addFile(piaFile.getID());

				compiler.addAccessionPeptideConnection(acc, peptide);
			}

		}

		compiler.insertCompletePeptideSpectrumMatch(psm);
	}

	public void close() throws IOException {

		compiler.buildClusterList();
		compiler.buildIntermediateStructure();

		// piac.setName(name);
		compiler.writeOutXML(out);
	}

	public static void createTTestFilterTask(String in, String mod, String rawdir, String db, String out)
			throws IOException, DocumentException {

		File raw = new File(rawdir);
		File[] rawfiles = raw.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				if (name.endsWith("raw") || name.endsWith("RAW"))
					return true;
				return false;
			}
		});
		String[] raws = new String[rawfiles.length];
		for (int i = 0; i < raws.length; i++) {
			raws[i] = rawfiles[i].getAbsolutePath();
		}

		OpenPsmTsvReader reader = new OpenPsmTsvReader(in);
		OpenPSM[] validatedPSMs = reader.getAllOpenPSMs();
		Arrays.sort(validatedPSMs, new OpenPSM.InverseClassScoreComparator());

		OpenModXmlReader modreader = new OpenModXmlReader(mod);
		OpenMod[] openMods = modreader.getOpenMods();

		int t = 0;
		int d = 0;
		HashMap<Integer, ArrayList<Double>[]> modmap = new HashMap<Integer, ArrayList<Double>[]>();
		HashMap<Integer, ArrayList<Integer>> psmmap = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < validatedPSMs.length; i++) {
			int modid = validatedPSMs[i].getOpenModId();

			boolean isTarget = validatedPSMs[i].isTarget();
			if (isTarget) {
				t++;
			} else {
				d++;
			}
			double fdr = (double) d / (double) t;
			validatedPSMs[i].setQValue(fdr);
			double classScore = validatedPSMs[i].getQValue();

			if (isTarget) {
				t++;
				if (modmap.containsKey(modid)) {
					modmap.get(modid)[0].add(classScore);
				} else {
					ArrayList<Double> tlist = new ArrayList<Double>();
					ArrayList<Double> dlist = new ArrayList<Double>();
					tlist.add(classScore);
					modmap.put(modid, new ArrayList[] { tlist, dlist });
				}
			} else {
				d++;
				if (modmap.containsKey(modid)) {
					modmap.get(modid)[1].add(classScore);
				} else {
					ArrayList<Double> tlist = new ArrayList<Double>();
					ArrayList<Double> dlist = new ArrayList<Double>();
					dlist.add(classScore);
					modmap.put(modid, new ArrayList[] { tlist, dlist });
				}
			}
			if (psmmap.containsKey(modid)) {
				psmmap.get(modid).add(i);
			} else {
				ArrayList<Integer> list = new ArrayList<Integer>();
				list.add(i);
				psmmap.put(modid, list);
			}

		}
		System.out.println(modmap.size() + "\t" + openMods.length + "\t" + validatedPSMs.length);

		ArrayList<OpenPSM> filteredList = new ArrayList<OpenPSM>();
		int totaltarget = 0;
		int totaldecoy = 0;

		int[] useModIds = new int[modmap.size()];
		Integer[] modkey = modmap.keySet().toArray(new Integer[modmap.size()]);
		Arrays.sort(modkey);

		ArrayList<OpenMod> modlist = new ArrayList<OpenMod>();
		for (int i = 0; i < modkey.length; i++) {

			ArrayList<Double> tlist = modmap.get(modkey[i])[0];
			ArrayList<Double> dlist = modmap.get(modkey[i])[1];
			double[] targetScore = new double[tlist.size()];
			SummaryStatistics sstarget = new SummaryStatistics();
			for (int j = 0; j < targetScore.length; j++) {
				targetScore[j] = tlist.get(j);
				sstarget.addValue(tlist.get(j));
			}

			SummaryStatistics ssdecoy = new SummaryStatistics();
			double[] decoyScore = new double[dlist.size()];
			for (int j = 0; j < decoyScore.length; j++) {
				decoyScore[j] = dlist.get(j);
				ssdecoy.addValue(dlist.get(j));
			}

			boolean pass = false;
			useModIds[modkey[i]] = -1;
			if (targetScore.length > 1) {
				if (decoyScore.length > 1) {
					if (TestUtils.tTest(sstarget, ssdecoy, 0.01)) {
						pass = true;
						openMods[modkey[i]].setId(modlist.size());
						modlist.add(openMods[modkey[i]]);
						useModIds[modkey[i]] = openMods[modkey[i]].getId();
					}
				} else {
					pass = true;
					openMods[modkey[i]].setId(modlist.size());
					modlist.add(openMods[modkey[i]]);
					useModIds[modkey[i]] = openMods[modkey[i]].getId();
				}
			}

			if (pass) {

				ArrayList<Integer> psmIdList = psmmap.get(modkey[i]);
				OpenPSM[] psms = new OpenPSM[psmIdList.size()];
				for (int j = 0; j < psms.length; j++) {
					psms[j] = validatedPSMs[psmIdList.get(j)];
					filteredList.add(psms[j]);
				}

				/*
				 * Arrays.sort(psms, new OpenPSM.InverseClassScoreComparator()); int target = 0;
				 * int decoy = 0;
				 * 
				 * for (int j = 0; j < psms.length; j++) { if (psms[j].isTarget()) { target++; }
				 * else { decoy++; } double locfdr = (double) decoy / (double) target; //
				 * psms[j].setLocalFDR(locfdr);
				 * 
				 * if (target != 0 && locfdr < 0.01) { filteredList.add(psms[j]); if
				 * (psms[j].isTarget()) { totaltarget++; } else { totaldecoy++; } } else {
				 * break; } }
				 * 
				 * System.out.println("mod\t" + modlist.size() + "\t" + target + "\t" + decoy);
				 */
			}
		}

		OpenPSM[] psms = filteredList.toArray(new OpenPSM[filteredList.size()]);
		Arrays.sort(psms, new OpenPSM.InverseClassScoreComparator());

		int t1 = 0;
		int d1 = 0;
		for (int i = 0; i < psms.length; i++) {
			boolean isTarget = psms[i].isTarget();
			if (isTarget) {
				t1++;
			} else {
				d1++;
			}
			double fdr = (double) d1 / (double) t1;
			psms[i].setQValue(fdr);
		}
		System.out.println("psms\t" + t1 + "\t" + d1);

		OpenPIAXMLWriter writer = new OpenPIAXMLWriter(out);
		writer.initialMSFragger(new File(db), raws);
		for (OpenPSM psm : filteredList) {
			writer.add(psm);
		}
		writer.close();
	}

	private static void generate(String rawdir, String fasta, String resultdir, String output) throws IOException {
		File[] raws = (new File(rawdir)).listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if (pathname.getName().endsWith("raw"))
					return true;
				return false;
			}

		});

		OpenPIAXMLWriter writer = new OpenPIAXMLWriter(output);
		writer.initialMSFragger(new File(fasta), raws);

		File[] pepxmls = (new File(resultdir)).listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if (pathname.getName().endsWith("pepXML.temp"))
					return true;
				return false;
			}

		});

		for (int i = 0; i < pepxmls.length; i++) {
			MSFraggerReader reader = new MSFraggerReader(pepxmls[i]);
			MSFraggerPsm[] psms = reader.getPSMs();
			for (int j = 0; j < psms.length; j++) {
				writer.add(psms[j]);
			}
		}
		writer.close();
	}

	private static void generateMill(String rawdir, String fasta, String resultdir, String output) throws IOException {
		File[] raws = (new File(rawdir)).listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if (pathname.getName().endsWith("raw"))
					return true;
				return false;
			}

		});

		OpenPIAXMLWriter writer = new OpenPIAXMLWriter(output);
		writer.initialMSFragger(new File(fasta), raws);

		File[] pepxmls = (new File(resultdir)).listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if (pathname.getName().endsWith("pepXML.temp"))
					return true;
				return false;
			}

		});

		for (int i = 0; i < pepxmls.length; i++) {
			MSFraggerReader reader = new MSFraggerReader(pepxmls[i]);
			MSFraggerPsm[] psms = reader.getPSMs();
			for (int j = 0; j < psms.length; j++) {
				writer.add(psms[j]);
			}
		}
		writer.close();
	}
}
