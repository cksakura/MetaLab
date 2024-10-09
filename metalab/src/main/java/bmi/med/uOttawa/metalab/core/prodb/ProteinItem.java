/**
 * 
 */
package bmi.med.uOttawa.metalab.core.prodb;

/**
 * @author Kai Cheng
 *
 */
public class ProteinItem {

	private String ref;
	private String sequence;
	private String genome;

	public ProteinItem(String ref, String sequence) {
		this.ref = ref;
		this.sequence = sequence;
	}

	public ProteinItem(String ref, String sequence, String genome) {
		this.ref = ref;
		this.sequence = sequence;
		this.genome = genome;
	}

	public String getRef() {
		return ref;
	}

	public String getSequence() {
		return sequence;
	}

	public char getAminoacid(int loc) {
		return sequence.charAt(loc);
	}

	public String getPeptide(int begin, int end) {
		if (begin < 0) {
			throw new IndexOutOfBoundsException();
		}
		if (end > this.sequence.length()) {
			throw new IndexOutOfBoundsException();
		}

		return this.sequence.substring(begin, end);
	}

	public String getGenome() {
		return genome;
	}

	public int length() {
		return sequence.length();
	}

	/**
	 * 
	 * @param pep
	 * @return the start position of the peptide, from 0
	 */
	public int indexOf(String pep) {
		char[] proChar = sequence.toCharArray();
		char[] pepChar = pep.toCharArray();
		for (int i = 0; i <= proChar.length - pepChar.length; i++) {

			int j;
			int haveX = 0;
			for (j = 0; j < pepChar.length; j++) {

				if (proChar[i + j] == pepChar[j]) {
					continue;

				} else {

					if (proChar[i + j] == 'X' || proChar[i + j] == '*') {
						haveX++;
						continue;

					} else if (proChar[i + j] == 'B') {
						if (pepChar[j] == 'D' || pepChar[j] == 'N') {
							continue;
						} else {
							break;
						}
					} else if (proChar[i + j] == 'Z') {
						if (pepChar[j] == 'Q' || pepChar[j] == 'E') {
							continue;
						} else {
							break;
						}
					} else if (proChar[i + j] == 'I' || proChar[i + j] == 'L') {
						if (pepChar[j] == 'I' || pepChar[j] == 'L') {
							continue;
						} else {
							break;
						}
					} else {
						break;
					}
				}
			}
			if (j == pepChar.length) {
				if (haveX <= 3)
					return i;
			}
		}
		return -1;
	}

}
