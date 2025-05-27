# MetaLab: Automated Pipeline for Metaproteomic Data Analysis

MetaLab is an integrated, user-friendly software platform for fast and automated metaproteomic data analysis. It provides a complete pipeline for microbial protein identification, quantification, and taxonomic profiling directly from mass spectrometry raw data. MetaLab is designed to simplify and accelerate metaproteomics research for microbiome studies.

## Features
- **Automated Workflow:** From raw MS data to protein identification, quantification, and taxonomic profiling.
- **Sample-Specific Database Generation:** Efficiently handles large and complex protein databases for metaproteomics.
- **Spectral Clustering:** Dramatically improves the speed and sensitivity of peptide identification.
- **Quantitative Analysis:** Supports label-free and labeled quantification methods.
- **Taxonomic Profiling:** Estimates the relative abundance of taxa at all phylogenetic ranks.
- **User-Friendly GUI:** Designed for ease of use by researchers in microbiome and proteomics fields.
- **Compatibility:** Taxonomy result files are fully compatible with widely used metagenomics tools.

## Publications
If you use MetaLab in your research, please cite the following publications:

1. **MetaLab: an automated pipeline for metaproteomic data analysis**  
   Kai Cheng, Zhibin Ning, Xu Zhang, Leyuan Li, Bo Liao, Janice Mayne, Alain Stintzi, Daniel Figeys  
   *Microbiome* **5**, 157 (2017).  
   [https://doi.org/10.1186/s40168-017-0375-2](https://doi.org/10.1186/s40168-017-0375-2)

2. **(Add your ACS J. Proteome Res. 2022 citation here)**

## Installation
MetaLab is a Java-based application. To build from source:

1. Ensure you have [Java 8+](https://adoptopenjdk.net/) and [Maven](https://maven.apache.org/) installed.
2. Clone this repository:
   ```sh
   git clone https://github.com/cksakura/MetaLab.git
   cd MetaLab
   ```
3. Build the project:
   ```sh
   mvn clean package
   ```
4. Run the application (example):
   ```sh
   java -jar target/metalab-*.jar
   ```

## Usage
- Launch the GUI and follow the workflow to import raw data, set parameters, and run analysis.
- For detailed instructions, see the user manual or the original publications.

## License
MetaLab is distributed under an open source license. See the LICENSE file or refer to the original publication for details.

## Contact
For questions, bug reports, or contributions, please open an issue on GitHub or contact the authors as listed in the publications.

---

**References:**
- [MetaLab: an automated pipeline for metaproteomic data analysis (Microbiome, 2017)](https://doi.org/10.1186/s40168-017-0375-2)
- [MetaLab 2.0 and related updates (add ACS J. Proteome Res. 2022 link here)] 