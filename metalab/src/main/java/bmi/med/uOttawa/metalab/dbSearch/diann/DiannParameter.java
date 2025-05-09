package bmi.med.uOttawa.metalab.dbSearch.diann;

import java.util.HashMap;

import bmi.med.uOttawa.metalab.core.enzyme.Enzyme;
import bmi.med.uOttawa.metalab.dbSearch.deepDetect.DeepDetectParameter;
import bmi.med.uOttawa.metalab.task.dia.DiaLibSearchPar;

public class DiannParameter {

	private int threads = 4;
	private int verbose = 1;
	private double qvalue = 0.01;
	private double looseQValue = 0.05;
	private boolean matrices = true;
	private boolean predictor = true;
	private int min_fr_mz = 200;
	private int max_fr_mz = 1800;
	private String enzyme = "Trypsin/P";
	private int missed_cleavages = 1;
	private int min_pep_len = 7;
	private int max_pep_len = 30;
	private int min_pr_mz = 350;
	private int max_pr_mz = 1200;
	private int min_pr_charge = 2;
	private int max_pr_charge = 3;
	private int maxMod = 1;
	private boolean m_excision = true;
	private boolean carbami_C = true;
	private boolean Oxidation_M = false;
	private boolean Acety_proN = false;
	private boolean phospho_STY = false;
	private boolean k_GG = false;
	private boolean reanalyse = false;
	private boolean smart_profiling = true;
	private boolean relaxed_prot_inf = false;
	private boolean noProInfer = false;
	private HashMap<String, String> cutMap;
	private boolean exportDecoy = false;

	private int massAccu = 15;
	private int ms1Accu = 15;
	private int scanWin = 15;
	private boolean mbr = true;
	private int quanStrategyId = 0;

	private static String[] quanStrategies = { "", "--no-ifs-removal", "--peak-center",
			"--peak-center --no-ifs-removal" };

	public DiannParameter() {
		this.cutMap = new HashMap<String, String>();
		this.cutMap.put("Trypsin/P", "K*,R*");
		this.cutMap.put("Trypsin", "K*,R*,!*P");
		this.cutMap.put("Lys-C", "K*");
		this.cutMap.put("ChymoTrypsin", "F*,Y*,W*,M*,L*,!*P");
		this.cutMap.put("AspN", "D*");
		this.cutMap.put("GluC", "E*");
	}

	public Enzyme getMetaLabEnzyme() {
		if (enzyme.equalsIgnoreCase("Trypsin/P")) {
			return Enzyme.TrypsinP;
		} else if (enzyme.equalsIgnoreCase("Trypsin")) {
			return Enzyme.Trypsin;
		} else if (enzyme.equalsIgnoreCase("Lys-C")) {
			return Enzyme.LysCP;
		} else if (enzyme.equalsIgnoreCase("ChymoTrypsin")) {
			return Enzyme.ChymoTrypsinPlus;
		} else if (enzyme.equalsIgnoreCase("AspN")) {
			return Enzyme.AspN;
		} else if (enzyme.equalsIgnoreCase("GluC")) {
			return Enzyme.GluC;
		} else {
			return Enzyme.TrypsinP;
		}
	}

	public String getCut() {
		String cut = "";
		if (this.cutMap.containsKey(enzyme)) {
			cut = this.cutMap.get(enzyme);
		}
		return cut;
	}

	public boolean isExportDecoy() {
		return exportDecoy;
	}

	public void setExportDecoy(boolean exportDecoy) {
		this.exportDecoy = exportDecoy;
	}

	public String generateConvert(String[] files) {
		StringBuilder sb = new StringBuilder();
		sb.append("--convert ");
		for (int i = 0; i < files.length; i++) {
			sb.append("--f ");
			sb.append("\"").append(files[i]).append("\" ");
		}

		sb.append("--threads ").append(threads).append(" ");
		sb.append("--verbose ").append(verbose);
		return sb.toString();
	}

	public String generateConvert() {
		StringBuilder sb = new StringBuilder();
		sb.append("--convert ");
		sb.append("--threads ").append(threads).append(" ");
		sb.append("--verbose ").append(verbose);
		return sb.toString();
	}

	public String generateLib2Tsv(String lib, String tsv) {
		StringBuilder sb = new StringBuilder();
		sb.append("--threads ").append(threads).append(" ");
		sb.append("--lib ").append("\"").append(lib).append("\" ");
		sb.append("--out-lib ").append("\"").append(tsv).append("\" ");
		sb.append("--gen-spec-lib");

		return sb.toString();
	}

	public String generateTsv2Lib(String tsv) {
		StringBuilder sb = new StringBuilder();
		sb.append("--threads ").append(threads).append(" ");
		sb.append("--lib ").append("\"").append(tsv).append("\" ");
		sb.append("--gen-spec-lib");

		return sb.toString();
	}

	public String generateTsv2Lib(String tsv, String[] fasta) {
		StringBuilder sb = new StringBuilder();
		sb.append("--threads ").append(threads).append(" ");
		sb.append("--lib ").append("\"").append(tsv).append("\" ");
		for (int i = 0; i < fasta.length; i++) {
			sb.append("--fasta ").append("\"").append(fasta[i]).append("\" ");
		}
		sb.append("--gen-spec-lib");

		return sb.toString();
	}

	public String generateLibSearch(DiaLibSearchPar libSearchPar, String output, String... libs) {
		StringBuilder sb = new StringBuilder();
		sb.append("--threads ").append(libSearchPar.getThreadCount()).append(" ");
		sb.append("--verbose ").append(verbose).append(" ");
		sb.append("--out ").append("\"").append(output).append("\" ");
		sb.append("--qvalue ").append(libSearchPar.getqValue()).append(" ");

		for (String lib : libs) {
			sb.append("--lib ").append("\"").append(lib).append("\" ");
		}

		sb.append("--window ").append(libSearchPar.getScanWin()).append(" ");
		sb.append("--mass-acc ").append(libSearchPar.getMassAccu()).append(" ");
		sb.append("--mass-acc-ms1 ").append(libSearchPar.getMs1Accu()).append(" ");

		if (this.matrices) {
			sb.append("--matrices ");
			sb.append("--matrix-qvalue ").append(libSearchPar.getqValue()).append(" ");
			sb.append("--matrix-spec-q ").append(libSearchPar.getqValue()).append(" ");
		}

		if (this.relaxed_prot_inf) {
			sb.append("--relaxed-prot-inf ");
		}

		if (this.smart_profiling) {
			sb.append("--smart-profiling ");
		}

		if (this.reanalyse) {
			sb.append("--reanalyse ");
		}

		if (this.exportDecoy) {
			sb.append("--report-decoys ");
		}

		if (libSearchPar.getQuanStrategyId() > 0 && libSearchPar.getQuanStrategyId() < quanStrategies.length) {
			sb.append(quanStrategies[libSearchPar.getQuanStrategyId()]);
		}

		return sb.toString();
	}

	public String generateLibSearch(DiaLibSearchPar libSearchPar, String output, boolean fast, boolean genLib,
			boolean qvalueCutoff, String... libs) {
		StringBuilder sb = new StringBuilder();
		sb.append("--threads ").append(libSearchPar.getThreadCount()).append(" ");
		sb.append("--verbose ").append(verbose).append(" ");
		sb.append("--out ").append("\"").append(output).append("\" ");
		if (qvalueCutoff) {
			sb.append("--qvalue ").append(libSearchPar.getqValue()).append(" ");
		} else {
			sb.append("--qvalue ").append(looseQValue).append(" ");
		}

		for (String lib : libs) {
			sb.append("--lib ").append("\"").append(lib).append("\" ");
		}

		sb.append("--window ").append(libSearchPar.getScanWin()).append(" ");
		sb.append("--mass-acc ").append(libSearchPar.getMassAccu()).append(" ");
		sb.append("--mass-acc-ms1 ").append(libSearchPar.getMs1Accu()).append(" ");

		if (fast) {
			sb.append("--no-maxlfq ");
			sb.append("--no-quant-files ");
			sb.append("--no-calibration ");
			sb.append("--no-norm ");
			sb.append("--no-prot-inf ");
		} else {
			sb.append("--matrices ");
			sb.append("--relaxed-prot-inf ");
			if (libSearchPar.getQuanStrategyId() > 0 && libSearchPar.getQuanStrategyId() < quanStrategies.length) {
				sb.append(quanStrategies[libSearchPar.getQuanStrategyId()]).append(" ");
			}
			if (qvalueCutoff) {
				sb.append("--matrix-qvalue ").append(libSearchPar.getqValue()).append(" ");
				sb.append("--matrix-spec-q ").append(libSearchPar.getqValue()).append(" ");
			} else {
				sb.append("--matrix-qvalue ").append(looseQValue).append(" ");
				sb.append("--matrix-spec-q ").append(looseQValue).append(" ");
			}
		}

		if (genLib) {
			sb.append("--out-lib ").append("\"").append(output + ".lib").append("\" ");
			sb.append("--gen-spec-lib ");
		}

		sb.append("--smart-profiling ");
		sb.append("--reanalyse ");

		if (this.exportDecoy) {
			sb.append("--report-decoys ");
		}

		return sb.toString();
	}

	public String generateLibSearchFast(DiaLibSearchPar libSearchPar, String output, String... libs) {
		StringBuilder sb = new StringBuilder();
		sb.append("--threads ").append(libSearchPar.getThreadCount()).append(" ");
		sb.append("--verbose ").append(verbose).append(" ");
		sb.append("--out ").append("\"").append(output).append("\" ");
		sb.append("--qvalue ").append(libSearchPar.getqValue()).append(" ");
		sb.append("--no-prot-inf ");

		for (String lib : libs) {
			sb.append("--lib ").append("\"").append(lib).append("\" ");
		}

		sb.append("--window ").append(libSearchPar.getScanWin()).append(" ");
		sb.append("--mass-acc ").append(libSearchPar.getMassAccu()).append(" ");
		sb.append("--mass-acc-ms1 ").append(libSearchPar.getMs1Accu()).append(" ");

		sb.append("--smart-profiling ");
		sb.append("--no-maxlfq ");
		sb.append("--no-quant-files ");
		sb.append("--no-calibration ");
		sb.append("--no-norm ");
		if (libSearchPar.getQuanStrategyId() > 0 && libSearchPar.getQuanStrategyId() < quanStrategies.length) {
			sb.append(quanStrategies[libSearchPar.getQuanStrategyId()]);
		}

		return sb.toString();
	}

	public String generateLibSearchFastLib(DiaLibSearchPar libSearchPar, String output, String... libs) {
		StringBuilder sb = new StringBuilder();
		sb.append("--threads ").append(libSearchPar.getThreadCount()).append(" ");
		sb.append("--verbose ").append(verbose).append(" ");
		sb.append("--out ").append("\"").append(output).append("\" ");
		sb.append("--qvalue ").append(libSearchPar.getqValue()).append(" ");
		sb.append("--no-prot-inf ");

		for (String lib : libs) {
			sb.append("--lib ").append("\"").append(lib).append("\" ");
		}

		sb.append("--window ").append(libSearchPar.getScanWin()).append(" ");
		sb.append("--mass-acc ").append(libSearchPar.getMassAccu()).append(" ");
		sb.append("--mass-acc-ms1 ").append(libSearchPar.getMs1Accu()).append(" ");

		sb.append("--smart-profiling ");
		sb.append("--no-maxlfq ");
		sb.append("--no-quant-files ");
		sb.append("--no-calibration ");
		sb.append("--no-norm ");
		sb.append("--out-lib ").append("\"").append(output + ".lib").append("\" ");
		sb.append("--gen-spec-lib ");

		if (libSearchPar.getQuanStrategyId() > 0 && libSearchPar.getQuanStrategyId() < quanStrategies.length) {
			sb.append(quanStrategies[libSearchPar.getQuanStrategyId()]);
		}

		return sb.toString();
	}

	public String generateLibSearch(String[] files, String out, String... libs) {
		StringBuilder sb = new StringBuilder();
		sb.append("--threads ").append(threads).append(" ");
		sb.append("--verbose ").append(verbose).append(" ");
		sb.append("--out ").append("\"").append(out).append("\" ");
		sb.append("--qvalue ").append(looseQValue).append(" ");

		sb.append("--window ").append(scanWin).append(" ");
		sb.append("--mass-acc ").append(massAccu).append(" ");
		sb.append("--mass-acc-ms1 ").append(ms1Accu).append(" ");

		if (matrices) {
			sb.append("--matrices ");
			sb.append("--matrix-qvalue ").append(looseQValue).append(" ");
			sb.append("--matrix-spec-q ").append(looseQValue).append(" ");
		}
		if (this.noProInfer) {
			sb.append("--no-prot-inf ");
		}

		for (String lib : libs) {
			sb.append("--lib ").append("\"").append(lib).append("\" ");
		}

		if (relaxed_prot_inf) {
			sb.append("--relaxed-prot-inf ");
		}

		if (smart_profiling) {
			sb.append("--smart-profiling ");
		}

		if (reanalyse) {
			sb.append("--reanalyse");
		}

		if (this.exportDecoy) {
			sb.append("--report-decoys ");
		}

		return sb.toString();
	}

	public String generateLibSearch(String[] files, String out, String lib, boolean genSpecLib) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < files.length; i++) {
			sb.append("--f ");
			sb.append("\"").append(files[i]).append("\" ");
		}

		sb.append("--threads ").append(threads).append(" ");
		sb.append("--verbose ").append(verbose).append(" ");
		sb.append("--out ").append("\"").append(out).append("\" ");
		sb.append("--qvalue ").append(qvalue).append(" ");
		if (matrices) {
			sb.append("--matrices ");
		}
		if (this.noProInfer) {
			sb.append("--no-prot-inf ");
		}

		sb.append("--lib ").append("\"").append(lib).append("\" ");

		if (relaxed_prot_inf) {
			sb.append("--relaxed-prot-inf ");
		}

		if (genSpecLib) {

			sb.append("--out-lib ").append("\"").append(out + ".lib").append("\" ");
			sb.append("--gen-spec-lib ");
			if (reanalyse) {
				sb.append("--reanalyse ");
			}

			if (smart_profiling) {
				sb.append("--smart-profiling ");
			}
		}

		if (this.exportDecoy) {
			sb.append("--report-decoys ");
		}

		return sb.toString();
	}

	public String generateLibSearch(String[] files, String out, String lib, boolean genSpecLib, boolean autoInfer) {
		StringBuilder sb = new StringBuilder();
		sb.append("--threads ").append(threads).append(" ");
		sb.append("--verbose ").append(verbose).append(" ");
		sb.append("--out ").append("\"").append(out).append("\" ");
		sb.append("--qvalue ").append(qvalue).append(" ");

		if (matrices) {
			sb.append("--matrices ");
		} else {
			sb.append("--no-maxlfq ");
			sb.append("--no-quant-files ");
			sb.append("--no-calibration ");
			sb.append("--no-norm ");
		}

		if (noProInfer) {
			sb.append("--no-prot-inf ");
		}

		sb.append("--lib ").append("\"").append(lib).append("\" ");

		if (relaxed_prot_inf) {
			sb.append("--relaxed-prot-inf ");
		}

		if (!autoInfer) {
			sb.append("--window ").append(this.scanWin).append(" ");
			sb.append("--mass-acc ").append(this.massAccu).append(" ");
			sb.append("--mass-acc-ms1 ").append(this.ms1Accu).append(" ");
		}

		if (smart_profiling) {
			sb.append("--smart-profiling ");
		}

		if (reanalyse) {
			sb.append("--reanalyse ");
		}

		if (genSpecLib) {
			sb.append("--out-lib ").append("\"").append(out + ".lib").append("\" ");
			sb.append("--gen-spec-lib ");
		}

		if (this.exportDecoy) {
			sb.append("--report-decoys ");
		}

		return sb.toString();
	}

	public String generateLibSearchFast(String[] files, String out, String... libs) {
		StringBuilder sb = new StringBuilder();
		sb.append("--threads ").append(threads).append(" ");
		sb.append("--verbose ").append(verbose).append(" ");
		sb.append("--out ").append("\"").append(out).append("\" ");
		sb.append("--qvalue ").append(qvalue).append(" ");
		sb.append("--no-prot-inf ");

		for (String lib : libs) {
			sb.append("--lib ").append("\"").append(lib).append("\" ");
		}

		sb.append("--window ").append(this.scanWin).append(" ");
		sb.append("--mass-acc ").append(this.massAccu).append(" ");
		sb.append("--mass-acc-ms1 ").append(this.ms1Accu).append(" ");

		sb.append("--smart-profiling ");
		sb.append("--no-maxlfq ");
		sb.append("--no-quant-files ");
		sb.append("--no-calibration ");
		sb.append("--no-norm");

		return sb.toString();
	}

	public String generateFastaSearch(String[] files, String out, String fasta, String out_lib, boolean fast) {
		StringBuilder sb = new StringBuilder();
		sb.append("--lib \"\" ");
		sb.append("--threads ").append(threads).append(" ");
		sb.append("--verbose ").append(verbose).append(" ");
		sb.append("--out ").append("\"").append(out).append("\" ");
		sb.append("--qvalue ").append(qvalue).append(" ");
		if (matrices) {
			sb.append("--matrices ");
		}

		sb.append("--out-lib ").append("\"").append(out_lib).append("\" ");

		sb.append("--gen-spec-lib ");
		if (predictor) {
			sb.append("--predictor ");
		}

		sb.append("--fasta ").append("\"").append(fasta).append("\" ");
		sb.append("--fasta-search ");

		sb.append("--min-fr-mz ").append(min_fr_mz).append(" ");
		sb.append("--max-fr-mz ").append(max_fr_mz).append(" ");

		if (m_excision) {
			sb.append("--met-excision ");
		}

		if (carbami_C) {
			sb.append("--unimod4 ");
		}

		String cut = this.cutMap.get(enzyme);
		sb.append("--cut ").append(cut).append(" ");
		sb.append("--missed-cleavages ").append(missed_cleavages).append(" ");
		sb.append("--min-pep-len ").append(min_pep_len).append(" ");
		sb.append("--max-pep-len ").append(max_pep_len).append(" ");
		sb.append("--min-pr-mz ").append(min_pr_mz).append(" ");
		sb.append("--max-pr-mz ").append(max_pr_mz).append(" ");
		sb.append("--min-pr-charge ").append(min_pr_charge).append(" ");
		sb.append("--max-pr-charge ").append(max_pr_charge).append(" ");
		sb.append("--var-mods ").append(maxMod).append(" ");

		if (Oxidation_M) {
			sb.append("--var-mod ").append("UniMod:35,15.994915,M").append(" ");
		}
		if (Acety_proN) {
			sb.append("--var-mod ").append("UniMod:1,42.010565,*n").append(" ");
			sb.append("--monitor-mod UniMod:1 ");
		}
		if (phospho_STY) {
			sb.append("--var-mod ").append("UniMod:21,79.966331,STY").append(" ");
			sb.append("--monitor-mod UniMod:21 ");
		}
		if (k_GG) {
			sb.append("--var-mod ").append("UniMod:121,114.042927,K").append(" ");
			sb.append("--monitor-mod UniMod:121 ");
			sb.append("--no-cut-after-mod ").append("UniMod:121").append(" ");
		}

		if (reanalyse) {
			sb.append("--reanalyse ");
		}

		if (smart_profiling) {
			sb.append("--smart-profiling ");
		}

		if (relaxed_prot_inf) {
			sb.append("--relaxed-prot-inf ");
		}

		if (fast) {
			sb.append("--min-corr 2.0 --corr-diff 1.0 --time-corr-only --extracted-ms1 ");
		}

		return sb.toString();
	}

	public String generateFasta(String fasta, String out_lib, boolean fast) {
		StringBuilder sb = new StringBuilder();
		sb.append("--threads ").append(threads).append(" ");
		sb.append("--verbose ").append(verbose).append(" ");
		sb.append("--qvalue ").append(qvalue).append(" ");

		sb.append("--out-lib ").append("\"").append(out_lib).append("\" ");
		sb.append("--gen-spec-lib ");
		if (predictor) {
			sb.append("--predictor ");
		}

		sb.append("--fasta ").append("\"").append(fasta).append("\" ");
		sb.append("--fasta-search ");

		sb.append("--min-fr-mz ").append(min_fr_mz).append(" ");
		sb.append("--max-fr-mz ").append(max_fr_mz).append(" ");

		if (m_excision) {
			sb.append("--met-excision ");
		}

		if (carbami_C) {
			sb.append("--unimod4 ");
		}

		String cut = this.cutMap.get(enzyme);
		sb.append("--cut ").append(cut).append(" ");
		sb.append("--missed-cleavages ").append(missed_cleavages).append(" ");
		sb.append("--min-pep-len ").append(min_pep_len).append(" ");
		sb.append("--max-pep-len ").append(max_pep_len).append(" ");
		sb.append("--min-pr-mz ").append(min_pr_mz).append(" ");
		sb.append("--max-pr-mz ").append(max_pr_mz).append(" ");
		sb.append("--min-pr-charge ").append(min_pr_charge).append(" ");
		sb.append("--max-pr-charge ").append(max_pr_charge).append(" ");
		sb.append("--var-mods ").append(maxMod).append(" ");

		if (Oxidation_M) {
			sb.append("--var-mod ").append("UniMod:35,15.994915,M").append(" ");
		}
		if (Acety_proN) {
			sb.append("--var-mod ").append("UniMod:1,42.010565,*n").append(" ");
			sb.append("--monitor-mod UniMod:1 ");
		}
		if (phospho_STY) {
			sb.append("--var-mod ").append("UniMod:21,79.966331,STY").append(" ");
			sb.append("--monitor-mod UniMod:21 ");
		}
		if (k_GG) {
			sb.append("--var-mod ").append("UniMod:121,114.042927,K").append(" ");
			sb.append("--monitor-mod UniMod:121 ");
			sb.append("--no-cut-after-mod ").append("UniMod:121").append(" ");
		}

		if (reanalyse) {
			sb.append("--reanalyse ");
		}

		if (smart_profiling) {
			sb.append("--smart-profiling ");
		}

		if (relaxed_prot_inf) {
			sb.append("--relaxed-prot-inf ");
		}

		if (fast) {
			sb.append("--min-corr 2.0 --corr-diff 1.0 --time-corr-only --extracted-ms1 ");
		}

		sb.append("--duplicate-proteins");

		return sb.toString();
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public void setVerbose(int verbose) {
		this.verbose = verbose;
	}

	public void setQvalue(double qvalue) {
		this.qvalue = qvalue;
	}

	public void setMatrices(boolean matrices) {
		this.matrices = matrices;
	}

	public void setPredictor(boolean predictor) {
		this.predictor = predictor;
	}

	public void setMin_fr_mz(int min_fr_mz) {
		this.min_fr_mz = min_fr_mz;
	}

	public void setMax_fr_mz(int max_fr_mz) {
		this.max_fr_mz = max_fr_mz;
	}

	public void setMet_excision(boolean m_excision) {
		this.m_excision = m_excision;
	}

	public void setEnzyme(String enzyme) {
		this.enzyme = enzyme;
	}

	public String getEnzyme() {
		return enzyme;
	}

	public void setMissed_cleavages(int missed_cleavages) {
		this.missed_cleavages = missed_cleavages;
	}

	public void setMin_pep_len(int min_pep_len) {
		this.min_pep_len = min_pep_len;
	}

	public void setMax_pep_len(int max_pep_len) {
		this.max_pep_len = max_pep_len;
	}

	public void setMin_pr_mz(int min_pr_mz) {
		this.min_pr_mz = min_pr_mz;
	}

	public void setMax_pr_mz(int max_pr_mz) {
		this.max_pr_mz = max_pr_mz;
	}

	public void setMin_pr_charge(int min_pr_charge) {
		this.min_pr_charge = min_pr_charge;
	}

	public void setMax_pr_charge(int max_pr_charge) {
		this.max_pr_charge = max_pr_charge;
	}

	public void setMaxMod(int maxMod) {
		this.maxMod = maxMod;
	}

	public void setReanalyse(boolean reanalyse) {
		this.reanalyse = reanalyse;
	}

	public void setSmart_profiling(boolean smart_profiling) {
		this.smart_profiling = smart_profiling;
	}

	public void setRelaxed_prot_inf(boolean relaxed_prot_inf) {
		this.relaxed_prot_inf = relaxed_prot_inf;
	}

	public int getThreads() {
		return threads;
	}

	public int getVerbose() {
		return verbose;
	}

	public double getQvalue() {
		return qvalue;
	}

	public boolean isMatrices() {
		return matrices;
	}

	public boolean isNoProInfer() {
		return noProInfer;
	}

	public void setNoProInfer(boolean noProInfer) {
		this.noProInfer = noProInfer;
	}

	public boolean isPredictor() {
		return predictor;
	}

	public int getMin_fr_mz() {
		return min_fr_mz;
	}

	public int getMax_fr_mz() {
		return max_fr_mz;
	}

	public int getMissed_cleavages() {
		return missed_cleavages;
	}

	public int getMin_pep_len() {
		return min_pep_len;
	}

	public int getMax_pep_len() {
		return max_pep_len;
	}

	public int getMin_pr_mz() {
		return min_pr_mz;
	}

	public int getMax_pr_mz() {
		return max_pr_mz;
	}

	public int getMin_pr_charge() {
		return min_pr_charge;
	}

	public int getMax_pr_charge() {
		return max_pr_charge;
	}

	public int getMaxMod() {
		return maxMod;
	}

	public boolean isReanalyse() {
		return reanalyse;
	}

	public boolean isSmart_profiling() {
		return smart_profiling;
	}

	public boolean isRelaxed_prot_inf() {
		return relaxed_prot_inf;
	}

	public boolean isM_excision() {
		return m_excision;
	}

	public void setM_excision(boolean m_excision) {
		this.m_excision = m_excision;
	}

	public boolean isCarbami_C() {
		return carbami_C;
	}

	public void setCarbami_C(boolean carbami_C) {
		this.carbami_C = carbami_C;
	}

	public boolean isOxidation_M() {
		return Oxidation_M;
	}

	public void setOxidation_M(boolean oxidation_M) {
		Oxidation_M = oxidation_M;
	}

	public boolean isAcety_proN() {
		return Acety_proN;
	}

	public void setAcety_proN(boolean acety_proN) {
		Acety_proN = acety_proN;
	}

	public boolean isPhospho_STY() {
		return phospho_STY;
	}

	public void setPhospho_STY(boolean phospho_STY) {
		this.phospho_STY = phospho_STY;
	}

	public boolean isK_GG() {
		return k_GG;
	}

	public void setK_GG(boolean k_GG) {
		this.k_GG = k_GG;
	}

	public int getMassAccu() {
		return massAccu;
	}

	public void setMassAccu(int massAccu) {
		this.massAccu = massAccu;
	}

	public int getMs1Accu() {
		return ms1Accu;
	}

	public void setMs1Accu(int ms1Accu) {
		this.ms1Accu = ms1Accu;
	}

	public boolean isMbr() {
		return mbr;
	}

	public void setMbr(boolean mbr) {
		this.mbr = mbr;
	}

	public int getScanWin() {
		return scanWin;
	}

	public void setScanWin(int scanWin) {
		this.scanWin = scanWin;
	}

	public int getQuanStrategyId() {
		return quanStrategyId;
	}

	public void setQuanStrategyId(int quanStrategyId) {
		this.quanStrategyId = quanStrategyId;
	}

	public String getQuanStrategy() {
		return quanStrategies[this.quanStrategyId];
	}

	public DeepDetectParameter getDeepPredictPar() {
		DeepDetectParameter par = new DeepDetectParameter();
		par.setMissCleavage(missed_cleavages);
		par.setMinLength(min_pep_len);
		par.setMaxLength(max_pep_len);
		Enzyme enzyme = this.getMetaLabEnzyme();

		String name = "";
		switch (enzyme) {
		case TrypsinP:
			name = "Trypsin";
			break;
		case LysCP:
			name = "LysC";
			break;
		case ChymoTrypsin:
			name = "Chymotrypsin";
			break;
		case ChymoTrypsinPlus:
			name = "Chymotrypsin";
			break;
		default:
			name = enzyme.name();
			break;
		}

		par.setProtease(name);
		return par;
	}
}
