package bmi.med.uOttawa.metalab.dbSearch.skyline;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xml.sax.helpers.DefaultHandler;

import bmi.med.uOttawa.metalab.dbSearch.skyline.SkylinePrecursor.PrecursorPeak;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;
import uk.ac.ebi.jmzml.xml.io.MzMLObjectIterator;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;

public class SkylineResultReader {
	
	private List<SkylineProtein> proteins;
	
	public SkylineResultReader(String in) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			// Create a SAXParser
			SAXParser saxParser = factory.newSAXParser();
			// Create an instance of the handler
			SkylineSAXHandler handler = new SkylineSAXHandler();
			// Parse the .sky file
			saxParser.parse(in, handler);
			
			this.proteins = handler.getProteins();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<SkylineProtein> getProteins() {
		return proteins;
	}

	class SkylineSAXHandler extends DefaultHandler {
		private List<SkylineProtein> proteins;
		private SkylineProtein currentProtein;
		private SkylinePeptide currentPeptide;
		private SkylinePrecursor currentPrecursor;

		public SkylineSAXHandler() {
			proteins = new ArrayList<>();
		}

		public List<SkylineProtein> getProteins() {
			return proteins;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			if (qName.equalsIgnoreCase("protein")) {
				String name = attributes.getValue("name");
				String description = attributes.getValue("description");
				currentProtein = new SkylineProtein(name, description);
			} else if (qName.equalsIgnoreCase("peptide")) {
				currentPeptide = new SkylinePeptide(attributes.getValue("sequence"),
						attributes.getValue("modified_sequence"),
						Short.parseShort(attributes.getValue("num_missed_cleavages")));
			} else if (qName.equalsIgnoreCase("precursor")) {
				currentPrecursor = new SkylinePrecursor(Short.parseShort(attributes.getValue("charge")),
						Float.parseFloat(attributes.getValue("calc_neutral_mass")),
						Float.parseFloat(attributes.getValue("precursor_mz")));
			} else if (qName.equalsIgnoreCase("precursor_peak")) {
				if (currentPrecursor != null) {
					float rt = attributes.getValue("retention_time") == null ? 0.0f
							: Float.parseFloat(attributes.getValue("retention_time"));
					float startTime = attributes.getValue("start_time") == null ? 0.0f
							: Float.parseFloat(attributes.getValue("start_time"));
					float endTime = attributes.getValue("end_time") == null ? 0.0f
							: Float.parseFloat(attributes.getValue("end_time"));
					float area = attributes.getValue("area") == null ? 0.0f
							: Float.parseFloat(attributes.getValue("area"));
					float massErrorPPM = attributes.getValue("mass_error_ppm") == null ? 0.0f
							: Float.parseFloat(attributes.getValue("mass_error_ppm"));
					float qvalue = attributes.getValue("qvalue") == null ? 0.0f
							: Float.parseFloat(attributes.getValue("qvalue"));
					currentPrecursor.addPrecursorPeak(attributes.getValue("replicate"), rt, startTime, endTime, area,
							massErrorPPM, qvalue);
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (qName.equalsIgnoreCase("precursor")) {
				if (currentPeptide != null) {
					currentPeptide.addPrecursor(currentPrecursor);
				}
			} else if (qName.equalsIgnoreCase("peptide")) {
				if (currentProtein != null) {
					currentProtein.addPeptide(currentPeptide);
				}
			} else if (qName.equalsIgnoreCase("protein")) {
				proteins.add(currentProtein);
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			// Handle character data if needed
		}
	}
	
	private static void writePP(String in, String out, String rep, String mzml) {

		MzMLUnmarshaller unmarshaller = new MzMLUnmarshaller(new File(mzml));
		MzMLObjectIterator<Spectrum> spectrumIterator = unmarshaller
				.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);

		float[][] data = new float[1000000][1000];
		List<Double> rtList = new ArrayList<Double>();
		int scanId = 0;

		try (PrintWriter writer = new PrintWriter(new File(out, rep + "_peaks.tsv"))) {
			while (spectrumIterator.hasNext()) {
				Spectrum spectrum = spectrumIterator.next();

				if (spectrum.getPrecursorList() == null) {
					StringBuilder sb = new StringBuilder();
					for (CVParam cvParam : spectrum.getScanList().getScan().get(0).getCvParam()) {
						if ("MS:1000016".equals(cvParam.getAccession())) {
							rtList.add(Double.parseDouble(cvParam.getValue()));
							sb.append("RT\t").append(Double.parseDouble(cvParam.getValue())).append("\n");
						}
					}

					List<BinaryDataArray> bdlist = spectrum.getBinaryDataArrayList().getBinaryDataArray();
					Number[] mzArray = bdlist.get(0).getBinaryDataAsNumberArray();
					Number[] intensityArray = bdlist.get(1).getBinaryDataAsNumberArray();
					if (mzArray.length == intensityArray.length) {
						for (int i = 0; i < mzArray.length; i++) {
							int mzId = (int) (mzArray[i].floatValue() * 1000.0);
							data[mzId][scanId] += intensityArray[i].floatValue();
							sb.append(mzArray[i].floatValue()).append("\t").append(intensityArray[i].floatValue()).append("\n");
						}
					}
					writer.print(sb);

					scanId++;
				}
			}
		} catch (IOException e) {

		}

		double[] rtArray = rtList.stream().mapToDouble(Double::doubleValue).toArray();

		SkylineResultReader reader = new SkylineResultReader(in);
		List<SkylineProtein> proteins = reader.getProteins();
		List<PrecursorPeak> allPPList = new ArrayList<PrecursorPeak>();
		try (PrintWriter writer = new PrintWriter(new File(out, rep + "_precursors.tsv"))) {
			writer.println("Protein\tPeptide\tCharge\tMW\tMZ\tErrorPPM\tRT\tRT_start\tRT_end\tArea");
			for (int i = 0; i < proteins.size(); i++) {
				SkylineProtein sProtein = proteins.get(i);
				List<SkylinePeptide> peptides = sProtein.getPeptides();
				for (int j = 0; j < peptides.size(); j++) {
					SkylinePeptide sPeptide = peptides.get(j);
					List<SkylinePrecursor> precursors = sPeptide.getPrecursors();
					for (int k = 0; k < precursors.size(); k++) {
						SkylinePrecursor sPrecursor = precursors.get(k);
						List<PrecursorPeak> pps = sPrecursor.getPpList();
						allPPList.addAll(pps);

						for (int l = 0; l < pps.size(); l++) {
							PrecursorPeak pp = pps.get(l);
							if (pp.getReplicate().equals(rep)) {
								StringBuilder sb = new StringBuilder();
								sb.append(sProtein.getName()).append("\t");
								sb.append(sPeptide.getSequence()).append("\t");
								sb.append(sPrecursor.getCharge()).append("\t");
								sb.append(sPrecursor.getMw()).append("\t");
								sb.append(sPrecursor.getMz()).append("\t");
								sb.append(pp.getMassErrorPPM()).append("\t");
								sb.append(pp.getRt()).append("\t");
								sb.append(pp.getRtStart()).append("\t");
								sb.append(pp.getRtEnd()).append("\t");
								sb.append(pp.getArea());

								float mz = sPrecursor.getMz();
								int mzId = (int) (mz * 1000.0);
								int startId = mzId - 10;
								int endId = mzId + 10 >= data.length ? data.length : mzId + 10;

								int startRtId = Arrays.binarySearch(rtArray, pp.getRtStart());
								if (startRtId < 0) {
									startRtId = -startRtId;
								}
								int endRtId = Arrays.binarySearch(rtArray, pp.getRtEnd());
								if (endRtId < 0) {
									endRtId = -endRtId;
								}

								if (startRtId >= 0 && endRtId >= 0 && endRtId - startRtId > 1) {
									float[] intensities = new float[endRtId - startRtId];
									for (int m = startRtId; m < endRtId; m++) {
										for (int n = startId; n <= endId; n++) {
											intensities[m - startRtId] += data[n][m];
										}
									}

									for (int m = 0; m < intensities.length; m++) {
										sb.append("\t").append(intensities[m]);
									}
								}

								writer.println(sb);
							}
						}
					}
				}
			}
			writer.close();
		} catch (IOException e) {

		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		SkylineResultReader.writePP("Z:\\Kai\\Raw_files\\DIA_validate\\"
				+ "Astral_2mz_yeast_EncyclopeDIA_carafe_2024-10-11_14-35-53.sky\\Astral_2mz_yeast_EncyclopeDIA_carafe.sky",
				"Z:\\Kai\\Raw_files\\DIA_validate\\" + "Astral_2mz_yeast_EncyclopeDIA_carafe_2024-10-11_14-35-53.sky",
				"Ast_20240130_Bo_AI_31_2mz_Yeast01",
				"Z:\\Kai\\Raw_files\\DIA_validate\\yeast\\Ast_20240130_Bo_AI_31_2mz_Yeast01.mzML");
	}

}
