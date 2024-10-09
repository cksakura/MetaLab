/**
 * 
 */
package bmi.med.uOttawa.metalab.core.tools;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

/**
 * @author Kai Cheng
 *
 */
public class Drawer {
	
	public static void drawMatrix(int[][] data, String output) throws IOException {

		int[] maxlist = new int[data.length];
		for (int i = 0; i < maxlist.length; i++) {
			int[] tempi = new int[data[i].length];
			System.arraycopy(data[i], 0, tempi, 0, tempi.length);
			Arrays.sort(tempi);
			maxlist[i] = tempi[tempi.length - 1];
		}

		Arrays.sort(maxlist);
		double half = (double) maxlist[maxlist.length - 1] / 2.0;

		int verticalCount = data.length;
		int horizonCount = data[0].length;

		int width = 1400 / horizonCount;
		int height = 700 / verticalCount;

		// System.out.println(width+"\t"+height);

		BufferedImage image = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_RGB);

		Graphics graphics = image.getGraphics();
		Graphics2D g2 = (Graphics2D) graphics;
		g2.setBackground(Color.white);
		g2.clearRect(0, 0, 1600, 900);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Font legandTitle = new Font("Arial", Font.PLAIN, 24);
		g2.setColor(Color.BLACK);
		g2.setFont(legandTitle);

		g2.drawString("m/z", 550, 40);
		for (int i = 0; i < data[0].length; i++) {
			if (i % 100 == 0 && i > 0) {
				g2.drawString(String.valueOf(i), 80 + i * width, 70);
				g2.drawLine(100 + i * width, 80, 100 + i * width, 90);
			}
		}
		g2.drawLine(100, 90, 1100, 90);

		int[] count = new int[data[0].length];
		
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				if (data[i][j] > 0) {
					count[j]++;
					
					int colorId = (int) (1.0 - (double) data[i][j] / half);
					if (colorId < 0)
						colorId = 0;
					g2.setColor(new Color(colorId, colorId, colorId, 100));

					Ellipse2D ellipse = new Ellipse2D.Double(100 + j * width, 100 + i * height, width, height);
					// g2.draw(ellipse);
					g2.fill(ellipse);
				} else {
					g2.setColor(Color.WHITE);
					Ellipse2D ellipse2 = new Ellipse2D.Double(100 + j * width, 100 + i * height, width, height);
					// g2.draw(ellipse2);
					g2.fill(ellipse2);
				}
			}
		}

		ImageIO.write(image, "PNG", new File(output));
		
		for(int i=0;i<count.length;i++){
			if(count[i]>data.length*0.5){
				System.out.println("oxo\t"+i);
			}
		}
	}

}
