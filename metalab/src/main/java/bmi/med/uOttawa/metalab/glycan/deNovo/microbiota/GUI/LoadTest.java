package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.GUI;

import java.awt.EventQueue;
import java.io.IOException;

public final class LoadTest {
	
	private static void test(String in) throws IOException {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DenovoGSMViewFrame frame = new DenovoGSMViewFrame(in);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		LoadTest.test("D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3.txt");
	}

}
